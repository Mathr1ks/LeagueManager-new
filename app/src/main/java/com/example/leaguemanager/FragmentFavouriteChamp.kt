package com.example.leaguemanager

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class FragmentFavouriteChamp() : Fragment(R.layout.fragment_favourite_champ) {
    constructor(masteryIndex: Int, liveData: MutableLiveData<String>, platform: String) : this(){
        this.masteryIndex = masteryIndex
        this.liveData = liveData
        this.platform = platform
    }
    var masteryIndex: Int = 0
    var liveData: MutableLiveData<String> = MutableLiveData()
    var platform: String = ""
    var playerId: String =""
    lateinit var champName: TextView
    lateinit var champId: String
    lateinit var champImg:ImageView
    lateinit var champLvl:TextView
    lateinit var champPoints:TextView
    lateinit var bestScore:TextView

    var championId: Int = 0
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        champName = view.findViewById<TextView>(R.id.fav_champ_name)
        champImg = view.findViewById<ImageView>(R.id.fav_champ)
        champLvl = view.findViewById<TextView>(R.id.fav_champ_level)
        champPoints = view.findViewById<TextView>(R.id.fav_champ_points)
        bestScore = view.findViewById<TextView>(R.id.last_time_played)
        champName.text= playerId.toString()
        liveData.observe(viewLifecycleOwner, Observer {
            playerId = liveData.value.toString()
            apiMasteryTask().execute()
        })
        super.onViewCreated(view, savedInstanceState)
    }



    inner class apiMasteryTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            response = URL("https://$platform.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/$playerId?api_key=${Home_Page.api_key}").readText(Charsets.UTF_8)
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val jsonArr: JSONArray
            jsonArr = JSONArray(result)
            if (masteryIndex < jsonArr.length()) {
                val championMastery = jsonArr.getJSONObject(masteryIndex)
                championId = championMastery.getInt("championId")
                val champLvlString = "Level " + championMastery.getInt("championLevel").toString()
                champLvl.text = champLvlString
                val champPointsString = championMastery.getInt("championPoints").toString()
                champPoints.text = champPointsString
                val timeUnix = championMastery.getLong("lastPlayTime")
                val formatter = SimpleDateFormat("dd/MM/yyyy")
                val dateString = formatter.format(Date(timeUnix));
                bestScore.text = dateString

                val storage = Firebase.storage
                val storageRef = storage.reference
                storageRef.child("champions/$championId.png").downloadUrl.addOnSuccessListener { temp ->
                    Picasso.get().load(temp.toString()).into(champImg)

                }.addOnFailureListener {
                    // Handle any errors
                }
                apiChampions().execute()
            }
        }

        }
    inner class apiChampions() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            response = URL(Global.dragonLink+"/data/en_US/champion.json").readText(Charsets.UTF_8)
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val jsonObj: JSONObject
            jsonObj = JSONObject(result)
            val allChampions = jsonObj.getJSONObject("data")
            val allChampionsNames: Iterator<String> =allChampions.keys()
            while (allChampionsNames.hasNext()) {
                val championName = allChampionsNames.next()

                val championDetails = allChampions.getJSONObject(championName)
                val id = championDetails.getInt("key")
                if (id == championId){
                    champName.text=championDetails.getString("name")
                    break
                }

            }
        }

    }


}