package com.example.leaguemanager

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL

class LiveGame: AppCompatActivity()  {
    var platform = "eun1"
    var region = "europe"
    val apiKey = "RGAPI-8f8ec7ca-7744-4413-bf6b-c76ebccd4ccc"
    //val apiKey = "RGAPI-0b42cee0-023c-468d-a9da-c2f0862c3e8d"
    var idSummoner = "xxJEMHK8GvoCAEhIx0lQBc3wfVlM7WR3RQ_w4Q8SJtrf-0A"
    var liveData: MutableLiveData<String> = MutableLiveData()
    var liveData2: MutableLiveData<String> = MutableLiveData()
    lateinit var summonerId: String
    lateinit var liveGameDataList: MutableList<LiveGameData>

    private val liveGameDataAdapter: LiveGameDataAdapter by lazy { LiveGameDataAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.live_game)
        idSummoner = intent.getStringExtra("id").toString()
        liveData2.value = "1"
        liveData2.observe(this, Observer{
            apiLiveGameTask().execute()
        })
        liveData.observe(this, Observer {
            liveGameDataList = mutableListOf<LiveGameData>()

            var parse: JSONObject
            var liveDataVal = liveData.value
            if(liveDataVal!="0") {
                parse = JSONObject(liveData.value)
                val jsonArray: JSONArray = parse.getJSONArray("participants")

                //val gameType = parse.getJSONObject("info").get("gameQueueConfigId")
                val gameType = parse.get("gameQueueConfigId")

                var queueName = "RANKED_SOLO_5x5"
                if (gameType as Int == 440)
                    queueName = "RANKED_FLEX_SR"
                val gameTypeConverted =
                    if (gameType as Int == 400) "DRAFT" else if (gameType as Int == 420) "SOLO/DUO" else if (gameType as Int == 430) "BLIND" else if (gameType as Int == 440) "FLEX" else if (gameType as Int == 450) "ARAM" else "Jakis inny tryb"
                val gameDuration = parse.get("gameLength")
                Log.d("GAME", gameDuration.toString())
                val gameDurationMinutes = gameDuration as Int / 60
                val gameDurationSeconds = gameDuration % 60


                liveGameDataList.add(
                    LiveGameData.HeaderHistory(
                        gameTypeConverted,
                        gameDurationMinutes.toString() + ":" + gameDurationSeconds
                    )
                )

                for (i in 0..9) {
                    var champInfo = ArrayList<String>()
                    var jo: JSONObject
                    jo = jsonArray.get(i) as JSONObject
                    var runes: JSONObject = jo.getJSONObject("perks")
                    summonerId = jo.getString("summonerId")

                    champInfo = arrayListOf(
                        jo.getString("summonerName"),
                        jo.getString("summonerId"),
                        jo.getInt("championId").toString(),
                        jo.getInt("spell1Id").toString(),
                        jo.getInt("spell2Id").toString(),
                        runes.getInt("perkStyle").toString(),
                        runes.getInt("perkSubStyle").toString(),
                    )

                    apiRankTask(
                        summonerId,
                        champInfo,
                        i,
                        queueName
                    ).execute()
                }
            }
            else{
                Toast.makeText(applicationContext, "User is not playing right now!", LENGTH_SHORT).show()
                finish()
            }
        })
    }




    inner class apiLiveGameTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                response =
                    URL("https://$platform.api.riotgames.com/lol/spectator/v4/active-games/by-summoner/$idSummoner?api_key=$apiKey").readText(
                        Charsets.UTF_8
                    )
            }
            catch(ex: FileNotFoundException){
                response ="0"
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            liveData.value = result
        }
    }

    inner class apiRankTask() : AsyncTask<String, Void, String>() {
        constructor(
                summonerId: String,
                champList: ArrayList<String>,
                id: Int,
                queueName: String
        ) : this() {
            this.summonerId = summonerId
            this.champList = champList
            this.id = id
            this.queueName = queueName
        }

        var summonerId: String = ""
        lateinit var champList: ArrayList<String>
        var id: Int = 0
        var queueName: String = "RANKED_SOLO_5x5"
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            response =
                    URL(" https://$platform.api.riotgames.com/lol/league/v4/entries/by-summoner/$summonerId?api_key=$apiKey").readText(
                            Charsets.UTF_8
                    )
            return response
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val jsonArr: JSONArray
            jsonArr = JSONArray(result)
            var rank1: String = "u"
            var rank2: String = "u"
            var test: Int = jsonArr.length()
            var i = 0
            while(i<test)
            {
                val rankObj = jsonArr.getJSONObject(i)
                if (rankObj.getString("queueType") == queueName) {
                    rank2 = rankObj.getString("tier")
                    rank1 = rankObj.getString("rank")
                }
                i+=1
            }

            liveGameDataList.add(
                    LiveGameData.ChampInfo(
                            champList[0],
                            champList[1],
                            champList[2].toInt(),
                            champList[3].toInt(),
                            champList[4].toInt(),
                            champList[5].toInt(),
                            champList[6].toInt(),
                            rank1,
                            rank2
                    )
            )
            if (id == 9) {
                liveGameDataList.add(
                        1,
                        LiveGameData.InformationAboutTeam1("TEAM1")
                )
                liveGameDataList.add(
                        7,
                        LiveGameData.InformationAboutTeam2("TEAM2")
                )
            }

            getLiveGameData()
            liveGameDataAdapter.setData(getLiveGameData())
            val recycledView2 = findViewById<RecyclerView>(R.id.recycledView2).apply {
                layoutManager = LinearLayoutManager(this@LiveGame)
                hasFixedSize()
                this.adapter = liveGameDataAdapter
            }
        }
    }

    private fun getLiveGameData(): MutableList<LiveGameData> = liveGameDataList
}