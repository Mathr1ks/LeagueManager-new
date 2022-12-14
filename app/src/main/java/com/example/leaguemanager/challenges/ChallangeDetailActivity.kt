package com.example.leaguemanager.challenges

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.*
import com.example.leaguemanager.teams.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class ChallangeDetailActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle

    var auth= Firebase.auth
    val storage = Firebase.storage
    val storageRef = storage.reference

    lateinit var matchName : TextView
    lateinit var matchPassword: TextView
    lateinit var iconTeam1: ImageView
    lateinit var iconTeam2: ImageView
    lateinit var nameTeam1: TextView
    lateinit var nameTeam2: TextView
    lateinit var rankTeam2: TextView
    lateinit var rankTeam1: TextView
    lateinit var resultButton: Button
    lateinit var team1Layout : ConstraintLayout
    lateinit var team2Layout : ConstraintLayout
    lateinit var team1Players : RecyclerView
    lateinit var team2Players : RecyclerView
    lateinit var dataTeam1: MutableList<DataChallenges>
    lateinit var dataTeam2: MutableList<DataChallenges>
    lateinit var team1Adapter: DataChallengesAdapter
    lateinit var team2Adapter: DataChallengesAdapter
    var team1 = ""
    var team2 = ""
    var leader = false

    var database : DatabaseReference = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challange_detail)
        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayoutC1)
        val navView : NavigationView = findViewById(R.id.nav_viewC1)
        matchName = findViewById<TextView>(R.id.matchName)
        matchPassword = findViewById<TextView>(R.id.matchPassword)
        iconTeam1 = findViewById<ImageView>(R.id.team1Icon)
        iconTeam2 = findViewById<ImageView>(R.id.team2Icon)
        nameTeam1 = findViewById<TextView>(R.id.team1Name)
        nameTeam2 = findViewById<TextView>(R.id.team2Name)
        rankTeam1 = findViewById<TextView>(R.id.team1Rank)
        rankTeam2 = findViewById<TextView>(R.id.team2Rank)
        resultButton = findViewById<Button>(R.id.addResultButton)
        team1Layout = findViewById<ConstraintLayout>(R.id.team1)
        team2Layout = findViewById<ConstraintLayout>(R.id.team2)
        team1Players = findViewById<RecyclerView>(R.id.team1Players)
        team2Players = findViewById<RecyclerView>(R.id.team2Players)
        var matchId = intent.getStringExtra("matchId").toString()

        dataTeam1 = mutableListOf()
        dataTeam2 = mutableListOf()

        team1Adapter = DataChallengesAdapter(dataTeam1)
        team1Players.adapter = team1Adapter
        team1Players.layoutManager = LinearLayoutManager(this)

        team2Adapter = DataChallengesAdapter(dataTeam2)
        team2Players.adapter = team2Adapter
        team2Players.layoutManager = LinearLayoutManager(this)


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

        matchName.text = matchName.text.toString() + matchId
        database.child("matches").child(matchId).child("password").get().addOnSuccessListener{
            matchPassword.text = matchPassword.text.toString() + it.value.toString()
        }
        database.child("matches").child(matchId).child("team1").get().addOnSuccessListener {
            team1=it.value.toString()
            database.child("teams").child(team1.toString()).child("logoId").get()
                .addOnSuccessListener {
                    storageRef.child("/TeamIcons/${it.value.toString()}.png").downloadUrl.addOnSuccessListener { temp ->
                        Picasso.get().load(temp.toString())
                            .into(iconTeam1)
                    }
                }
            val pendings2 = database.child("teams").child(team1.toString()).child("rank").get()
                .addOnSuccessListener {
                    var temp = "Rank: "+it.value.toString()
                    rankTeam1.text = temp
                }
            nameTeam1.text = team1.toString()
            val refMembers =
                database.child("teams").child(team1).child("members")

            refMembers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot!!.children
                    var tp = 1

                    children.forEach {
                        Log.d("no gracze", it.value.toString())
                        dataTeam1.add(
                            DataChallenges.MemberList(
                                it.value.toString(),
                                team1
                            )
                        )
                        team1Adapter.notifyItemInserted(dataTeam1.size - 1)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        database.child("matches").child(matchId).child("team2").get().addOnSuccessListener{
            team2=it.value.toString()
            database.child("teams").child(team2.toString()).child("logoId").get()
                .addOnSuccessListener {
                    storageRef.child("/TeamIcons/${it.value.toString()}.png").downloadUrl.addOnSuccessListener { temp ->
                        Picasso.get().load(temp.toString())
                            .into(iconTeam2)
                    }
                }
            val pendings3 = database.child("teams").child(team2.toString()).child("rank").get()
                .addOnSuccessListener {
                    var temp = "Rank: "+it.value.toString()
                    rankTeam2.text = temp
                }
            nameTeam2.text = team2.toString()
            val refMembers =
                database.child("teams").child(team2).child("members")

            refMembers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot!!.children
                    var tp = 1

                    children.forEach {
                        Log.d("no gracze", it.value.toString())
                        dataTeam2.add(
                            DataChallenges.MemberList(
                                it.value.toString(),
                                team2
                            )
                        )
                        team2Adapter.notifyItemInserted(dataTeam2.size - 1)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }





        team1Layout.setOnClickListener{
            if(team1Players.isGone){
               team1Players.visibility = View.VISIBLE
            }
            else{
                team1Players.visibility = View.GONE
            }
        }
        team2Layout.setOnClickListener{
            if(team2Players.isGone){
                team2Players.visibility = View.VISIBLE
            }
            else{
                team2Players.visibility = View.GONE
            }
        }
        com.example.leaguemanager.database.child("users").child(com.example.leaguemanager.auth.uid.toString()).child("leader").get().addOnSuccessListener {
            leader = it.value.toString().toBoolean()
            if(!leader){
                resultButton.visibility = View.GONE
            }
        }
        resultButton.setOnClickListener{
            val intent = Intent(this, AddChallangeResultActivity::class.java)
            intent.putExtra("matchId", matchId.toString())
            startActivity(intent)
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
}