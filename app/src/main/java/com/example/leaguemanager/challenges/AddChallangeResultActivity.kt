package com.example.leaguemanager.challenges

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.leaguemanager.*
import com.example.leaguemanager.teams.TeamActivity
import com.example.leaguemanager.teams.UserWithoutTeamActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class AddChallangeResultActivity : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle

    lateinit var matchId: EditText
    lateinit var addResultButton: Button
    lateinit var errorText: TextView
    val storage = Firebase.storage
    val storageRef = storage.reference
    var region = "EUROPE"
    var platform = "EUN1"
    lateinit var matchname: String
    var auth = Firebase.auth
    var database: DatabaseReference =
        Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_challange_result)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayoutC2)
        matchId = findViewById<EditText>(R.id.matchId)
        addResultButton = findViewById<Button>(R.id.addResultButton)
        errorText = findViewById<TextView>(R.id.errorText)
        val navView: NavigationView = findViewById(R.id.nav_viewC2)
        matchname = intent.getStringExtra("matchId").toString()
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {
                    val intent = Intent(this, Home_Page::class.java)
                    startActivity(intent)
                }
                R.id.nav_search -> {
                    val intent = Intent(this, FindSummoner::class.java)
                    startActivity(intent)
                }
                R.id.nav_teams -> {
                    database.child("users").child(auth.uid.toString()).child("teamName").get()
                        .addOnSuccessListener {
                            var teamMember = it.value.toString()
                            lateinit var intent: Intent
                            if (teamMember == "null")
                                intent = Intent(this, UserWithoutTeamActivity::class.java)
                            else
                                intent = Intent(this, TeamActivity::class.java)
                            startActivity(intent)
                        }
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    Firebase.auth.signOut()
                    Global.user.userReset()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }

            true
        }
        database.child("users").child(auth.uid.toString()).child("region").get()
            .addOnSuccessListener {
                region = it.value.toString()
            }
        database.child("users").child(auth.uid.toString()).child("platform").get()
            .addOnSuccessListener {
                platform = it.value.toString()
            }
        addResultButton.setOnClickListener {
            var resultId = matchId.text.toString()
            apiSignleMatchTask(resultId).execute()

        }
    }

    override fun onBackPressed() {
        database.child("users").child(auth.uid.toString()).child("teamName").get().addOnSuccessListener {
            var teamName = it.value
            var intent = Intent(this, ChallengesActivity::class.java)
            intent.putExtra("teamName", teamName.toString())
            startActivity(intent)
            finish()
        }
    }

    inner class apiSignleMatchTask() : AsyncTask<String, Void, String>() {
        constructor(matchId: String) : this() {
            this.matchId = matchId
        }

        var matchId: String = ""
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            var match = platform + "_" + matchId
            try {
                response =
                    URL("https://$region.api.riotgames.com/lol/match/v5/matches/$match?api_key=${Home_Page.api_key}").readText(
                        Charsets.UTF_8
                    )
            } catch (ex: FileNotFoundException) {
                response = "err"
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            if (result == "err") {
                errorText.text = "Match not found!"
            } else {
                var jsonObj = JSONObject(result)
                val infoObj = jsonObj.getJSONObject("info")
                val participants = infoObj.getJSONArray("participants")
                var win: Boolean = true
                for(i in 0 until participants.length()){
                    Log.d("username", Global.user.summonername.toString())
                    val item = participants.getJSONObject(i)
                    val summName = item.getString("summonerName")
                    if(summName.lowercase() == Global.user.summonername.toString().lowercase()){
                        win = item.getBoolean("win")
                        break
                    }
                }


                database.child("matches").child(matchname).child("team1").get().addOnSuccessListener {
                    var team1 = it.value.toString()
                    var team2 = ""
                    database.child("matches").child(matchname).child("team2").get()
                        .addOnSuccessListener {
                            team2 = it.value.toString()
                            database.child("users").child(auth.uid.toString()).child("teamName").get().addOnSuccessListener {
                                var myTeam = it.value.toString()
                                if(myTeam==team1){
                                    database.child("teams").child(myTeam.toString()).child("RP").get().addOnSuccessListener{
                                        var points = it.value.toString().toInt()
                                        if(win)
                                            points +=15
                                        else
                                            points -=11
                                        database.child("teams").child(myTeam.toString()).child("RP").setValue(points.toString())
                                    }
                                    database.child("teams").child(team2.toString()).child("RP").get().addOnSuccessListener{
                                        var points = it.value.toString().toInt()
                                        if(win)
                                            points -=11
                                        else
                                            points +=15
                                        database.child("teams").child(team2.toString()).child("RP").setValue(points.toString())
                                    }
                                }
                                else{
                                    database.child("teams").child(myTeam.toString()).child("RP").get().addOnSuccessListener{
                                        var points = it.value.toString().toInt()
                                        if(win)
                                            points +=15
                                        else
                                            points -=11
                                        database.child("teams").child(myTeam.toString()).child("RP").setValue(points.toString())
                                    }
                                    database.child("teams").child(team1.toString()).child("RP").get().addOnSuccessListener{
                                        var points = it.value.toString().toInt()
                                        if(win)
                                            points -=11
                                        else
                                            points +=15
                                        database.child("teams").child(team1.toString()).child("RP").setValue(points.toString())
                                    }
                                }
                            }

                            var ref = database.child("teams").child(team1).child("activeChallenges")
                            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val children = snapshot!!.children
                                    var tp = 1

                                    children.forEach {
                                        if (it.value == matchname) {
                                            ref.child(it.key.toString()).removeValue()
                                        }
                                    }
                                    ref = database.child("teams").child(team2).child("activeChallenges")
                                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val children = snapshot!!.children
                                            var tp = 1

                                            children.forEach {
                                                if (it.value == matchname) {
                                                    ref.child(it.key.toString()).removeValue()
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })

                            val pendingIdTeam1Ref =
                                database.child("teams").child(team1).child("pendingid")

                            pendingIdTeam1Ref.get().addOnSuccessListener {
                                val pendingId = it.value.toString().toInt() + 1
                                pendingIdTeam1Ref.setValue(pendingId.toString())
                                database.child("teams").child(team1).child("historyChallenges")
                                    .child(pendingId.toString()).child("matchName")
                                    .setValue(matchname)
                                database.child("teams").child(team1).child("historyChallenges")
                                    .child(pendingId.toString()).child("team1").setValue(team1)
                                database.child("teams").child(team1).child("historyChallenges")
                                    .child(pendingId.toString()).child("team2").setValue(team2)
                                database.child("teams").child(team1).child("historyChallenges")
                                    .child(pendingId.toString()).child("matchId").setValue(matchId)

                            }
                            val pendingIdTeam2Ref =
                                database.child("teams").child(team2).child("pendingid")
                            pendingIdTeam2Ref.get().addOnSuccessListener {
                                val pendingId = it.value.toString().toInt() + 1
                                pendingIdTeam2Ref.setValue(pendingId.toString())
                                database.child("teams").child(team2).child("historyChallenges")
                                    .child(pendingId.toString()).child("matchName")
                                    .setValue(matchname)
                                database.child("teams").child(team2).child("historyChallenges")
                                    .child(pendingId.toString()).child("team1").setValue(team1)
                                database.child("teams").child(team2).child("historyChallenges")
                                    .child(pendingId.toString()).child("team2").setValue(team2)
                                database.child("teams").child(team2).child("historyChallenges")
                                    .child(pendingId.toString()).child("matchId").setValue(matchId)

                            }
                            database.child("matches").child(matchname).removeValue()
                            val intent = Intent(this@AddChallangeResultActivity, TeamActivity::class.java)
                            startActivity(intent)
                        }

                }

            }

        }

    }
}