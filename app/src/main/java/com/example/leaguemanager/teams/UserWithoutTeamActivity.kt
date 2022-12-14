package com.example.leaguemanager.teams

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.*
import com.example.leaguemanager.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import kotlin.math.log

class UserWithoutTeamActivity : AppCompatActivity() {
    lateinit var createTeamButton: Button
    lateinit var teamName: TextView
    lateinit var errorText: TextView
    lateinit var pendingInvites: RecyclerView
    lateinit var adapter: PendingInvitesAdapter
    lateinit var pendingList: MutableList<PendingInvitesData>
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var username: String
    var auth=Firebase.auth

    var database : DatabaseReference = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_without_team)
        createTeamButton = findViewById<Button>(R.id.createButton)
        teamName = findViewById<TextView>(R.id.teamName)
        errorText = findViewById<TextView>(R.id.errorText)
        pendingInvites = findViewById<RecyclerView>(R.id.invitesList)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout5)
        val navView : NavigationView = findViewById(R.id.nav_view5)
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
                        lateinit var intent:Intent
                        if(teamMember=="null")
                            intent = Intent(this,UserWithoutTeamActivity::class.java)

                        else
                            intent = Intent(this,TeamActivity::class.java)
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
        pendingList = mutableListOf()
        adapter = PendingInvitesAdapter(pendingList)
        pendingInvites.adapter = adapter
        pendingInvites.layoutManager = LinearLayoutManager(this)
        database.child("users").child(auth.uid.toString()).child("summonername").get().addOnSuccessListener {
            username=it.value.toString()
        }
        val pendings = database.child("users").child(auth.uid.toString()).child("pendingInvites")
        pendings.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot!!.children
                children.forEach{
                    val invite = it.value.toString()
                    val ref = database.child("teams")
                    var rank = "0"
                    var icon = "0"
                    ref.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot!!.children
                            children.forEach {
                                val team = it.key.toString()
                                Log.d("team",team)
                                if(team == invite){
                                    rank = it.child("rank").getValue().toString()
                                    icon =it.child("logoId").getValue().toString()
                                    Log.d("CHILD", it.child("rank").getValue().toString())

                                    val item = PendingInvitesData(
                                        invite,
                                        icon.toInt(),
                                        rank.toInt()
                                    )
                                    pendingList.add(item)
                                    adapter.notifyItemInserted(pendingList.size-1)
                                }

                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })



        createTeamButton.setOnClickListener{
            val ref = database.child("teams")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot!!.children
                    var tp = 1
                    children.forEach {
                        val team = it.key.toString()
                        //Log.d("team",team)
                        //Log.d("myTeam",teamName.text.toString())
                        if(team == teamName.text.toString()){
                            errorText.text="Team already exist!"
                            tp = 0
                        }

                    }
                    if(tp ==1){
                        errorText.text=""
                        database.child("users").child(auth.uid.toString()).child("leader").setValue(true)
                        writeNewTeam(teamName.text.toString(),username,1,1, arrayListOf(username))
                        database.child("users").child(auth.uid.toString()).child("iconId").get().addOnSuccessListener {
                            Global.user.iconId = it.value.toString()
                            Global.user.teamName=teamName.text.toString()
                            intent = Intent(this@UserWithoutTeamActivity,TeamActivity::class.java)
                            startActivity(intent)
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        }
    }
    fun writeNewTeam(teamName: String, leader: String, logoID:Int, rank: Int, members: ArrayList<String> ){
        var team = TeamData(leader,logoID,rank,members)
        database.child("teams").child(teamName).setValue(team)
        database.child("teams").child(teamName).child("pendingid").setValue("0")
        database.child("teams").child(teamName).child("memberId").setValue("1")
        database.child("teams").child(teamName).child("leader").setValue(username)
        database.child("teams").child(teamName).child("members").child("0").setValue(leader)
        database.child("users").child(auth.uid.toString()).child("teamName").setValue(teamName)
        database.child("teams").child(teamName).child("RP").setValue(0)
        database.child("teams").child(teamName).child("rank").setValue("x")

    }
}