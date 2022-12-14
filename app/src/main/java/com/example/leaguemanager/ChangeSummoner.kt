package com.example.leaguemanager

import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import androidx.lifecycle.Observer
import com.example.leaguemanager.teams.TeamActivity
import com.example.leaguemanager.teams.UserWithoutTeamActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

private lateinit var summonername: TextView
private lateinit var changeSummoner: Button
lateinit var platformSpinner: Spinner
lateinit var errorText: TextView
val api_key = "RGAPI-8f8ec7ca-7744-4413-bf6b-c76ebccd4ccc"
val liveData: MutableLiveData<Int> = MutableLiveData()
var platform: String = "BR1"
var region: String = "AMERICA"
var rank: MyUsers? =null
class ChangeSummoner : AppCompatActivity() {
    var auth=Firebase.auth
    var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    lateinit var toggle : ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_summoner)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                   rank = dataSnapshot.getValue<MyUsers>()!!

                }
                database
                    .removeEventListener(this)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                val intent = Intent(getApplicationContext(), MainActivity::class.java)
                startActivity(intent)
                Log.w(ContentValues.TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        database
            .addValueEventListener(postListener)





        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout3)
        val navView : NavigationView = findViewById(R.id.nav_view3)
        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //navView.bringToFront()
        navView.setNavigationItemSelectedListener {
            when(it.itemId){

                R.id.nav_home ->{
                    val intent = Intent(this,Home_Page::class.java)
                    startActivity(intent)
                }
                R.id.nav_search ->{
                    val intent = Intent(this,FindSummoner::class.java)
                    startActivity(intent)
                }
                R.id.nav_teams ->{
                    database.child("users").child(auth.uid.toString()).child("teamName").get().addOnSuccessListener {
                        var teamMember = it.value.toString()
                        Log.d("TEST: ",teamMember)
                        lateinit var intent:Intent
                        if(teamMember=="null")
                            intent = Intent(this, UserWithoutTeamActivity::class.java)

                        else
                            intent = Intent(this, TeamActivity::class.java)
                        startActivity(intent)
                    }
                }
                R.id.nav_settings ->{
                    val intent = Intent(this,SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout ->{
                    Firebase.auth.signOut()
                    Global.user.userReset()
                    val intent = Intent(this,Home_Page::class.java)
                    startActivity(intent)
                }
            }

            true
        }







        var database =
            Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
        var auth = Firebase.auth
        val currentUser = Firebase.auth.currentUser
        summonername = findViewById<TextView>(R.id.username)
        errorText = findViewById<TextView>(R.id.errorText)
        changeSummoner = findViewById<Button>(R.id.confirmButton)
        changeSummoner.setOnClickListener {

            rank?.users?.forEach {
                if(it.value.get("summonername").toString().equals(summonername.text.toString())) {
                    errorText.setText("User Already Exists!!!")
                    return@setOnClickListener
                }
            }







            apiClientTask().execute()
            liveData.observe(this, Observer {
                val temp = liveData.value
                if (temp == 0) {
                    errorText.text = "This summoner cannot be found"
                } else {
                    var oldUsername=""
                    database.child("users").child(auth.uid.toString()).child("summonername").get().addOnSuccessListener {
                        oldUsername=it.value.toString()
                    }
                    database.child("users").child(auth.uid.toString()).child("summonername").setValue(
                        summonername.text.toString())
                        .addOnSuccessListener {
                            Global.user.summonername= summonername.text.toString()

                        }.addOnFailureListener {
                            Toast.makeText(baseContext, "Summonername has not been changed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    database.child("users").child(auth.uid.toString()).child("platform").setValue(
                        platform)
                        .addOnSuccessListener {
                            Global.user.platform= platform

                        }.addOnFailureListener {
                            Toast.makeText(baseContext, "Summonername has not been changed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    database.child("users").child(auth.uid.toString()).child("region").setValue(
                        region)
                        .addOnSuccessListener {
                            Global.user.region= region

                            val intent = Intent(this, Home_Page::class.java)
                            startActivity(intent)
                        }.addOnFailureListener {
                            Toast.makeText(baseContext, "Summonername has not been changed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    database.child("users").child(auth.uid.toString()).child("teamName").get().addOnSuccessListener {
                        var teamName = it.value.toString()
                        val pendingsTeam = database.child("teams").child(teamName).child("members")
                        pendingsTeam.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val children = snapshot!!.children

                                children.forEach{
                                    if(it.value.toString() == oldUsername){
                                        Log.d("ELO: ",it.key.toString())
                                        pendingsTeam.child(it.key.toString()).setValue(Global.user.summonername)
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }.addOnFailureListener{

                    }

                }
            })


        }
        platformSpinner = findViewById<Spinner>(R.id.platformSpinner)
        platformSpinner.adapter = ArrayAdapter<CharSequence>(
            this,
            R.layout.platforms_spinner,
            resources.getStringArray(R.array.platform)
        )
        platformSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                (p1 as TextView).setTextColor(resources.getColor(R.color.lightgray))
                platform = platformSpinner.selectedItem.toString()
                if (platform == "EUN1" || platform == "EUW1" || platform == "TR1" || platform == "RU") {
                    region = "EUROPE"
                } else if (platform == "BR1" || platform == "LA1" || platform == "LA2" || platform == "NA1" || platform == "OC1") {
                    region = "AMERICAS"
                } else
                    region = "ASIA"

            }
        }


    }


    inner class apiClientTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                response =
                    URL("https://$platform.api.riotgames.com/lol/summoner/v4/summoners/by-name/${summonername.text.toString()}?api_key=${api_key}").readText(
                        Charsets.UTF_8
                    )
            } catch (ex: FileNotFoundException) {
                response = "0"
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result == "0")
                liveData.value = 0
            else {
                val jsonObj: JSONObject
                jsonObj = JSONObject(result)
                try {
                    jsonObj.getString("name")
                } catch (ex1: JSONException) {
                    Log.d("intencja", "123")
                    liveData.value = 0
                    return
                }
                liveData.value = 1
            }
        }

    }
}