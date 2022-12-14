package com.example.leaguemanager.teams

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.app.Activity

import android.content.Intent
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.leaguemanager.*
import com.google.android.material.navigation.NavigationView


class InviteTeamMemberActivity : AppCompatActivity() {

    lateinit var addMemberName: TextView
    lateinit var teamName: String
    lateinit var dataTeamsList: MutableList<DataTeams>
    lateinit var addMemberButton: Button
    lateinit var dataTeamsAdapter: DataTeamsAdapter
    lateinit var errorText :TextView
    private lateinit var recycledView1 :RecyclerView
    lateinit var toggle : ActionBarDrawerToggle
    var auth=Firebase.auth
    var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.team_add_member)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayoutT2)
        val navView : NavigationView = findViewById(R.id.nav_viewT2)
        errorText = findViewById<TextView>(R.id.errorText)

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
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                }
            }

            true
        }

        recycledView1 = findViewById<RecyclerView>(R.id.invitesMemberList)
        teamName = intent.getStringExtra("teamName").toString()

        dataTeamsList = mutableListOf()
        dataTeamsAdapter = DataTeamsAdapter(dataTeamsList)
        recycledView1.adapter=dataTeamsAdapter
        recycledView1.layoutManager = LinearLayoutManager(this)
        addMemberButton = findViewById(R.id.addMemberButton)
        addMemberName = findViewById(R.id.addMemberName)
        val database =
            Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
        val ref = database.child("teams").child(teamName).child("pendingInvites")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot!!.children
                var tp = 1
                children.forEach {
                    dataTeamsList.add(
                        DataTeams.InvitedMemberList(
                            it.value.toString(),
                            4,
                            teamName
                        )
                    )
                    dataTeamsAdapter.notifyItemInserted(dataTeamsList.size-1)


                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        addMemberButton.setOnClickListener {
            errorText.text=""
            hideSoftKeyboard(this)
            var flag=0;
            val  ref2 = database.child("users")
            var tp =0
            ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot!!.children
                    children.forEach{
                        if (it.child("summonername").value.toString() == addMemberName.text.toString()){
                            tp=1
                            val ref5 = database.child("teams").child(teamName).child("members")
                            val ref3 = ref2.child(it.key.toString()).child("pendingInvites")
                            val ref4 = ref2.child(it.key.toString()).child("pendingid")
                            ref3.addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val ch = snapshot!!.children
                                    ch.forEach{
                                        if(it.value.toString()==teamName) {
                                            errorText.text = "User already invited!"
                                            flag = 1;
                                        }



                                    }
                                    if(flag==0){
                                        var isInTeam = 0
                                        ref5.addListenerForSingleValueEvent(object: ValueEventListener{
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val ch = snapshot!!.children
                                                ch.forEach{
                                                    if(it.value.toString()==addMemberName.text.toString()) {
                                                        isInTeam =1
                                                        errorText.text = "User already in your team!"
                                                    }
                                                }
                                                if(isInTeam==0){
                                                    ref4.get().addOnSuccessListener {
                                                        val pendingId = it.value.toString().toInt()+1
                                                        ref3.child(pendingId.toString()).setValue(teamName)
                                                        ref4.setValue(pendingId.toString())

                                                        val ref5 = database.child("teams").child(teamName).child("pendingInvites")
                                                        ref5.addListenerForSingleValueEvent(object : ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                val children = snapshot!!.children
                                                                var tp = 1
                                                                val ref2 = database.child("teams").child(teamName).child("pendingid").get().addOnSuccessListener {
                                                                    val pendingId = it.value.toString().toInt()+1
                                                                    ref.child(it.value.toString()).setValue(addMemberName.text.toString())
                                                                    database.child("teams").child(teamName).child("pendingid").setValue(pendingId.toString())
                                                                }
                                                                dataTeamsList.add(
                                                                    DataTeams.InvitedMemberList(
                                                                        addMemberName.text.toString(),
                                                                        4,
                                                                        teamName
                                                                    )
                                                                )
                                                                dataTeamsAdapter.notifyItemInserted(dataTeamsList.size-1)



                                                            }

                                                            override fun onCancelled(error: DatabaseError) {
                                                                TODO("Not yet implemented")
                                                            }

                                                        })
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

                        }

                    }
                    if(tp==0){
                        errorText.text="User not found!"
                    }



                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }








    }

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(
            INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken,
                0
            )
        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this, TeamActivity::class.java))
        finish()
    }


    private fun getTeamsData(): MutableList<DataTeams> = dataTeamsList


}