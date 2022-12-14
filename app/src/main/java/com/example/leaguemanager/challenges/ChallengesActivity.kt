package com.example.leaguemanager.challenges

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.*
import com.example.leaguemanager.teams.DataChallengesAdapter
import com.example.leaguemanager.teams.TeamActivity
import com.example.leaguemanager.teams.UserWithoutTeamActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ChallengesActivity : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var sendChallengesButton: Button
    lateinit var incomingChallengesButton: Button
    lateinit var teamName: String
    var leader = false
    lateinit var dataChallengesList: MutableList<DataChallenges>
    lateinit var dataChallengesAdapter: DataChallengesAdapter
    private lateinit var recycledView1: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenges)
        teamName = intent.getStringExtra("teamName").toString()

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayoutC3)
        val navView : NavigationView = findViewById(R.id.nav_viewC3)

        sendChallengesButton= findViewById(R.id.sendChallengeButtonActivity)
        incomingChallengesButton= findViewById(R.id.incomingChallengesButtonActivity)
        recycledView1 = findViewById<RecyclerView>(R.id.activeChallengesList)
        dataChallengesList = mutableListOf()
        dataChallengesAdapter = DataChallengesAdapter(dataChallengesList)
        recycledView1.adapter = dataChallengesAdapter
        recycledView1.layoutManager = LinearLayoutManager(this)


        database.child("users").child(auth.uid.toString()).child("leader").get().addOnSuccessListener {
            leader = it.value.toString().toBoolean()
            if(!leader){
                sendChallengesButton.visibility = View.GONE
                incomingChallengesButton.visibility = View.GONE
            }
        }

        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.nav_home ->{
                    val intent = Intent(this, Home_Page::class.java)
                    startActivity(intent)
                }
                R.id.nav_search ->{
                    val intent = Intent(this, FindSummoner::class.java)
                    startActivity(intent)
                }
                R.id.nav_teams ->{
                    database.child("users").child(auth.uid.toString()).child("teamName").get().addOnSuccessListener {
                        var teamMember = it.value.toString()
                        Log.d("TEST: ",teamMember)
                        lateinit var intent: Intent
                        if(teamMember=="null")
                            intent = Intent(this, UserWithoutTeamActivity::class.java)

                        else
                            intent = Intent(this, TeamActivity::class.java)
                        startActivity(intent)
                    }
                }
                R.id.nav_settings ->{
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout ->{
                    Firebase.auth.signOut()
                    Global.user.userReset()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }

            true
        }
        val ref = database.child("teams").child(teamName).child("activeChallenges")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot!!.children
                var tp = 1
                children.forEach {

                    var matchId = it.value.toString()
                    val ref = database.child("matches").child(matchId).child("team1").get().addOnSuccessListener {
                        if(it.value.toString() == teamName){
                            database.child("matches").child(matchId).child("team2").get().addOnSuccessListener{
                                dataChallengesList.add(
                                    DataChallenges.ActiveChallengesList(
                                        teamName,
                                        it.value.toString(),
                                        matchId
                                        )
                                )
                                dataChallengesAdapter.notifyItemInserted(dataChallengesList.size - 1)
                            }
                        }
                        else{
                            dataChallengesList.add(
                                DataChallenges.ActiveChallengesList(
                                    teamName,
                                    it.value.toString(),
                                    matchId
                                )
                            )
                            dataChallengesAdapter.notifyItemInserted(dataChallengesList.size - 1)
                        }
                    }


                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        sendChallengesButton.setOnClickListener{
            val intent = Intent(this@ChallengesActivity, SendChallengeActivity::class.java)
            intent.putExtra("teamName", teamName)
            startActivity(intent)
        }


        incomingChallengesButton.setOnClickListener{
            val intent = Intent(this@ChallengesActivity, IncomingChallengesActivity::class.java)
            intent.putExtra("teamName", teamName)
            startActivity(intent)
        }


    }
    override fun onBackPressed() {
        startActivity(Intent(this, TeamActivity::class.java))
        finish()
    }
}