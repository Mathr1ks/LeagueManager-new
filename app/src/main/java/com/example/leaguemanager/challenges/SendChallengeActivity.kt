package com.example.leaguemanager.challenges

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SendChallengeActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle

    lateinit var teamName: String
    lateinit var sendChallengeButton: Button
    lateinit var teamNameSendChallengeEditText: EditText
    lateinit var dataChallengesList: MutableList<DataChallenges>
    lateinit var dataChallengesAdapter: DataChallengesAdapter
    lateinit var errorText: TextView
    private lateinit var recycledView1: RecyclerView
    var auth = Firebase.auth
    var database =
        Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_challenge)

        teamName = intent.getStringExtra("teamName").toString()

        teamNameSendChallengeEditText = findViewById(R.id.teamnameSendChallenge)

        recycledView1 = findViewById<RecyclerView>(R.id.sentChallengesList)
        errorText = findViewById<TextView>(R.id.errorText)
        dataChallengesList = mutableListOf()
        dataChallengesAdapter = DataChallengesAdapter(dataChallengesList)
        recycledView1.adapter = dataChallengesAdapter
        recycledView1.layoutManager = LinearLayoutManager(this)
        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayoutC5)
        val navView : NavigationView = findViewById(R.id.nav_viewC5)
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

        val ref = database.child("teams").child(teamName).child("sentChallenges")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot!!.children
                var tp = 1
                children.forEach {

                    dataChallengesList.add(
                        DataChallenges.SentChallengesList(
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

        sendChallengeButton = findViewById(R.id.sendChallengeButton)

        sendChallengeButton.setOnClickListener {

            hideSoftKeyboard(this)
            errorText.text = ""
            var flagaa=0
            val teamExistRef = database.child("teams")
            var tp =0
            teamExistRef.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ch = snapshot!!.children
                    ch.forEach{
                        if (it.key.toString()==teamNameSendChallengeEditText.text.toString()){
                            tp=1
                            var flag=0;
                            val sentChallengeTeam0Ref = database.child("teams").child(teamName).child("sentChallenges")
                            val pendingIdTeam0Ref = database.child("teams").child(teamName).child("pendingid")
                            sentChallengeTeam0Ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val children = snapshot!!.children
                                    children.forEach {
                                        if(it.value.toString()==teamNameSendChallengeEditText.text.toString()) {
                                            flag = 1;
                                            errorText.text = "Challenge already exist!"
                                        }
                                    }

                                    if (flag==0){
                                        pendingIdTeam0Ref.get().addOnSuccessListener {
                                            val reff = database.child("teams").child(teamName).child("incomingChallenges")
                                            reff.addListenerForSingleValueEvent(object :ValueEventListener{
                                                override fun onDataChange(snapshot1: DataSnapshot) {
                                                    val children1 = snapshot1!!.children
                                                    children1.forEach{
                                                        if(it.value.toString()==teamNameSendChallengeEditText.text.toString()){
                                                            flagaa = 1
                                                            errorText.text = "Team already challenged!"
                                                        }

                                                    }
                                                    if (flagaa==0 && teamName!=teamNameSendChallengeEditText.text.toString()){
                                                        val pendingId = it.value.toString().toInt()+1
                                                        sentChallengeTeam0Ref.child(pendingId.toString()).setValue(teamNameSendChallengeEditText.text.toString())
                                                        pendingIdTeam0Ref.setValue(pendingId.toString())

                                                        dataChallengesList.add(
                                                            DataChallenges.SentChallengesList(
                                                                teamName,
                                                                teamNameSendChallengeEditText.text.toString(),

                                                                )
                                                        )
                                                        dataChallengesAdapter.notifyItemInserted(dataChallengesList.size - 1)

                                                        var flag1=0;
                                                        val sentChallengeTeam1Ref = database.child("teams").child(teamNameSendChallengeEditText.text.toString()).child("incomingChallenges")
                                                        val pendingIdTeam1Ref = database.child("teams").child(teamNameSendChallengeEditText.text.toString()).child("pendingid")
                                                        sentChallengeTeam1Ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                val children = snapshot!!.children
                                                                children.forEach {
                                                                    if(it.value.toString()==teamName) {
                                                                        flag1 = 1;
                                                                    }
                                                                }

                                                                if (flag1==0){
                                                                    pendingIdTeam1Ref.get().addOnSuccessListener {
                                                                        val pendingId = it.value.toString().toInt()+1
                                                                        sentChallengeTeam1Ref.child(pendingId.toString()).setValue(teamName)
                                                                        pendingIdTeam1Ref.setValue(pendingId.toString())

                                                                    }
                                                                }
                                                                teamNameSendChallengeEditText.text.delete(0,teamNameSendChallengeEditText.text.length)

                                                            }

                                                            override fun onCancelled(error: DatabaseError) {
                                                                TODO("Not yet implemented")
                                                            }


                                                        })
                                                    }
                                                    if(teamName==teamNameSendChallengeEditText.text.toString()){
                                                        errorText.text = "Can't challenge yourself!"
                                                    }
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
                    if(tp==0){
                        errorText.text = "Team doesn't exist!"
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
        database.child("users").child(auth.uid.toString()).child("teamName").get().addOnSuccessListener {
            var teamName = it.value
            var intent = Intent(this, ChallengesActivity::class.java)
            intent.putExtra("teamName", teamName.toString())
            startActivity(intent)
            finish()
        }
    }
}