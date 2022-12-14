package com.example.leaguemanager.challenges

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

class IncomingChallengesActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle

    lateinit var dataChallengesList: MutableList<DataChallenges>
    lateinit var dataChallengesAdapter: DataChallengesAdapter
    private lateinit var recycledView1: RecyclerView
    lateinit var teamName:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_challenges)

        teamName = intent.getStringExtra("teamName").toString()

        recycledView1 = findViewById<RecyclerView>(R.id.incomingChallengesList)
        dataChallengesList = mutableListOf()
        dataChallengesAdapter = DataChallengesAdapter(dataChallengesList)
        recycledView1.adapter = dataChallengesAdapter
        recycledView1.layoutManager = LinearLayoutManager(this)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayoutC4)
        val navView : NavigationView = findViewById(R.id.nav_viewC4)
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
        val ref = database.child("teams").child(teamName).child("incomingChallenges")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot!!.children
                var tp = 1
                children.forEach {

                    dataChallengesList.add(
                        DataChallenges.IncomingChallengesList(
                            teamName,
                            it.value.toString()

                        )
                    )
                    dataChallengesAdapter.notifyItemInserted(dataChallengesList.size - 1)


                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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
}