package com.example.leaguemanager.teams

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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.*
import com.example.leaguemanager.challenges.ChallengesActivity
import com.example.leaguemanager.challenges.SendChallengeActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class TeamActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var dataTeamsAdapter: DataTeamsAdapter
    lateinit var AddMemberButton: Button
    lateinit var rankButton: Button
    lateinit var challengesActivityButton: Button

    lateinit var customSpinner: Spinner
    lateinit var dataTeamsList: MutableList<DataTeams>
    lateinit var iconView: ImageView
    lateinit var iconId: String
    var auth=Firebase.auth
    var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    lateinit var teamName: TextView
    lateinit var teamRank: TextView
    lateinit var recycledView1: RecyclerView
    lateinit var teamHistoryList: MutableList<DataTeams>
    lateinit var teamHistoryView: RecyclerView
    lateinit var teamHistoryAdapter:DataTeamsAdapter
    lateinit var username:String

    lateinit var chatButton:ImageButton
    var leader = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team)
        recycledView1 = findViewById<RecyclerView>(R.id.membersList)

        teamHistoryView = findViewById<RecyclerView>(R.id.teamHistoryList)
        teamHistoryList = mutableListOf()
        teamHistoryAdapter = DataTeamsAdapter(teamHistoryList)
        teamHistoryView.adapter = teamHistoryAdapter
        teamHistoryView.layoutManager = LinearLayoutManager(this)

        customSpinner = findViewById<Spinner>(R.id.customSpinner)
        customSpinner.visibility = View.INVISIBLE
        iconView = findViewById<ImageView>(R.id.imageViewTeamIcon)
        chatButton = findViewById<ImageButton>(R.id.chatButton)
        dataTeamsList = mutableListOf()
        dataTeamsAdapter = DataTeamsAdapter(dataTeamsList)
        teamName = findViewById<TextView>(R.id.addMemberName)
        teamRank = findViewById<TextView>(R.id.teamRank)
        recycledView1.adapter = dataTeamsAdapter
        recycledView1.layoutManager = LinearLayoutManager(this)


        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayoutT1)
        val navView : NavigationView = findViewById(R.id.nav_viewT1)

        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.nav_home ->{
                    val intent = Intent(this,Home_Page::class.java)
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

        var database =
            Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
        val storage = Firebase.storage
        val storageRef = storage.reference


        var auth = Firebase.auth

        database.child("users").child(auth.uid.toString()).child("leader").get().addOnSuccessListener {
            leader = it.value.toString().toBoolean()
            if(!leader){
                AddMemberButton.visibility = View.GONE
            }
        }
        iconView.setOnClickListener() {
            if (leader){
                customSpinner.visibility = View.VISIBLE
            }
        }
        setupCustomSpinner()


        customSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                iconId = findViewById<TextView>(R.id.testSpinerText).text.toString()
                val pendings3 =
                    database.child("teams").child(teamName.text.toString()).child("logoId").get()
                        .addOnSuccessListener {
                            Log.d("logoid", iconId)
                            Log.d("logoid", it.value.toString())
                            val ref = database.child("teams").child(teamName.text.toString())
                                .child("logoId")
                            if (iconId != it.value.toString() && iconId != "0") {
                                ref.setValue(iconId)
                            }

                        }
            }
        }





        database.child("users").child(auth.uid.toString()).child("teamName").get()
            .addOnSuccessListener {
                teamName.text = it.value.toString()
                Log.d("tean name : ", teamName.text.toString())

                database.child("teams").child(teamName.text.toString()).child("logoId").get()
                    .addOnSuccessListener {
                        Log.d("logoidasdfasf333333333", it.value.toString())
                        storageRef.child("/TeamIcons/${it.value.toString()}.png").downloadUrl.addOnSuccessListener { temp ->
                            Picasso.get().load(temp.toString())
                                .into(iconView)
                        }
                    }

                val pendings2 = database.child("teams").child(teamName.text.toString()).child("rank").get()
                    .addOnSuccessListener {
                        teamRank.text = it.value.toString()
                    }


                val refMembers =
                    database.child("teams").child(teamName.text.toString()).child("members")

                refMembers.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        var tp = 1

                        children.forEach {
                            Log.d("no gracze", it.value.toString())
                            dataTeamsList.add(
                                DataTeams.MemberList(
                                    it.value.toString(),
                                    teamName.text.toString()
                                )
                            )
                            dataTeamsAdapter.notifyItemInserted(dataTeamsList.size - 1)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                var matchId = ""

                val refHistory = database.child("teams").child(teamName.text.toString()).child("historyChallenges")
                refHistory.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        children.forEach{
                            val key = it.key.toString()
                            Log.d("key",key)
                            val refDetail = database.child("teams").child(teamName.text.toString()).child("historyChallenges")
                                .child(key).child("matchId").get().addOnSuccessListener {
                                    matchId = it.value.toString()
                                    Log.d("matchid",matchId)
                                    var i=0
                                    apiSignleMatchTask(matchId,key).execute()

                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })


            }




        AddMemberButton = findViewById(R.id.AddMemberActivityButton)

        AddMemberButton.setOnClickListener {
            val intent = Intent(this@TeamActivity, InviteTeamMemberActivity::class.java)
            intent.putExtra("teamName", teamName.text.toString())
            startActivity(intent)
        }
        chatButton.setOnClickListener{
            val intent = Intent(this@TeamActivity, TeamChat::class.java)
            startActivity(intent)
        }

        challengesActivityButton= findViewById(R.id.challengesActivityButton)
        challengesActivityButton.setOnClickListener{
            val intent = Intent(this@TeamActivity, ChallengesActivity::class.java)
            intent.putExtra("teamName", teamName.text.toString())
            startActivity(intent)
        }

        rankButton = findViewById(R.id.button)

        rankButton.setOnClickListener{
            val intent = Intent(this@TeamActivity, Ranking::class.java)
            startActivity(intent)
        }


    }

    private fun setupCustomSpinner() {
        val adapter = SpinnerAdapter(this, SpinerImages.list!!)
        customSpinner.adapter = adapter
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Home_Page::class.java))
        finish()
    }
    inner class apiSignleMatchTask() : AsyncTask<String, Void, String>() {
        constructor(matchId: String, key:String) : this() {
            this.matchId = matchId
            this.key = key

        }

        var matchId: String = ""
        var key: String = ""

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            Log.d("here","siem")
            var match = Global.user.platform + "_" + matchId

            response = URL("https://${Global.user.region}.api.riotgames.com/lol/match/v5/matches/$match?api_key=${Home_Page.api_key}").readText(Charsets.UTF_8)
            return response
        }

        override fun onPostExecute(result: String?) {
                var teamOponentName=""

                var jsonObj = JSONObject(result)
                val infoObj = jsonObj.getJSONObject("info")
                var gameDuration = infoObj.getInt("gameDuration").toString()
                val time = Integer.parseInt(gameDuration)/60
                val rest = (gameDuration.toDouble()/60)-time
                val seconds = (rest*60).roundToInt()
                var tp = 0
                var matchDate:Long =0
                try {
                    matchDate = infoObj.getLong("gameEndTimestamp")
                }
                catch (exception: JSONException){
                    tp=1
                }
                var myTeamLeader = ""
                database.child("teams").child(teamName.text.toString()).child("leader").get().addOnSuccessListener {
                    myTeamLeader = it.value.toString()
                    val formatter = SimpleDateFormat("dd/MM/yyyy")
                    val dateString = formatter.format(Date(matchDate));
                    val participants = infoObj.getJSONArray("participants")
                    var win: Boolean = false
                    for(i in 0 until participants.length()){
                        val item = participants.getJSONObject(i)
                        val summName = item.getString("summonerName")
                        if(summName.lowercase() == myTeamLeader.lowercase()){
                            Log.d("asdasdasd","asdas")
                            win = item.getBoolean("win")
                            break
                        }
                    }


                    val refDetail1 =
                        database.child("teams").child(teamName.text.toString())
                            .child("historyChallenges")
                            .child(key).child("team1").get()
                            .addOnSuccessListener {
                                Log.d("teamName", it.value.toString())
                                if (it.value.toString() == teamName.text.toString()) {
                                    val refDetail2 = database.child("teams")
                                        .child(teamName.text.toString())
                                        .child("historyChallenges")
                                        .child(key).child("team2").get()
                                        .addOnSuccessListener {
                                            teamOponentName =
                                                it.value.toString()
                                            Log.d("as",it.value.toString())
                                            teamHistoryList.add(
                                                DataTeams.ChallengesHistoryList(
                                                    matchId,
                                                    teamOponentName,
                                                    time.toString() + "m " + seconds.toString() + "s",
                                                    dateString,
                                                    win
                                                )
                                            )
                                            teamHistoryAdapter.notifyItemInserted(
                                                teamHistoryList.size - 1
                                            )
                                        }
                                } else {
                                    teamOponentName = it.value.toString()
                                    Log.d("as2",it.value.toString())
                                    teamHistoryList.add(
                                        DataTeams.ChallengesHistoryList(
                                            matchId,
                                            teamOponentName,
                                            time.toString() + "m " + seconds.toString() + "s",
                                            dateString,
                                            win
                                        )
                                    )
                                    teamHistoryAdapter.notifyItemInserted(
                                        teamHistoryList.size - 1
                                    )
                                }
                            }
                }


        }
    }

}