package com.example.leaguemanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.leaguemanager.teams.TeamActivity
import com.example.leaguemanager.teams.UserWithoutTeamActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SettingsActivity : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle
    var auth=Firebase.auth

    var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference


    private lateinit var changeSummonerButton: Button
    private lateinit var changePassowrdButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var logOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout4)
        val navView : NavigationView = findViewById(R.id.nav_view4)

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
        changeSummonerButton = findViewById<Button>(R.id.changeSummonerButton)
        changeSummonerButton.setOnClickListener {
            val intent = Intent(this, ChangeSummoner::class.java)
            startActivity(intent)
        }
        changePassowrdButton = findViewById<Button>(R.id.changePassowrdButton)
        changePassowrdButton.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }
        deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)
        deleteAccountButton.setOnClickListener {
            val intent = Intent(this, DeleteUser::class.java)
            startActivity(intent)
        }




        logOutButton = findViewById<Button>(R.id.logOutButton)
        logOutButton.setOnClickListener {
            Firebase.auth.signOut()
            Global.user.userReset()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



    }
}