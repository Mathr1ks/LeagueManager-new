package com.example.leaguemanager

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.teams.TeamActivity
import com.example.leaguemanager.teams.UserWithoutTeamActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.HashMap

class Ranking : AppCompatActivity() {

    lateinit var dataAdapter: RankingAdapter
    lateinit var recycleView: RecyclerView
    lateinit var lista: MutableList<RankingWpis>
    var auth=Firebase.auth
    var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)
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
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                }
            }

            true
        }


        recycleView=findViewById(R.id.messageList)
        lista= mutableListOf()
        dataAdapter= RankingAdapter(lista)
        recycleView.adapter= dataAdapter
        recycleView.layoutManager= LinearLayoutManager(this)


        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    var rank = dataSnapshot.getValue<Ranks>()!!
                    var temprow = mutableListOf<Wpis>()
                    rank.teams.forEach {
                        var wpis:Wpis= Wpis(it.key,it.value.get("logoId").toString(),it.value.get("rank").toString(),it.value.get("RP").toString().toInt())
                        temprow.add(wpis)
                    }
                    var sortedByName =  temprow.sortedByDescending { myObject -> myObject.RP }
                    var i =1
                sortedByName.forEach{
                    database.child("teams").child(it.team.toString()).child("rank").setValue(i.toString())
                    lista.add(RankingWpis.MyWpis(it.team, it.logoid, i.toString(),it.RP.toString()))
                    dataAdapter.notifyItemInserted(lista.size-1)
                    i += 1

                }
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


    }
}