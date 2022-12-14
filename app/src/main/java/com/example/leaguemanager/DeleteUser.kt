package com.example.leaguemanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.leaguemanager.teams.TeamActivity
import com.example.leaguemanager.teams.UserWithoutTeamActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DeleteUser : AppCompatActivity() {

    private lateinit var changePasswordButton: Button
    private lateinit var password: TextView
    lateinit var toggle : ActionBarDrawerToggle
    var auth=Firebase.auth
    var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_user)

        password = findViewById<TextView>(R.id.password)
        changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        changePasswordButton.setOnClickListener {
            if (TextUtils.isEmpty(password.text.toString())) {
                password.setError("Hasło nie może być puste")
                password.requestFocus()
                return@setOnClickListener
            }
            if (password.text.toString().length < 8) {
                password.setError("Hasło nie może być krótsze niż 8 znaków")
                password.requestFocus()
                return@setOnClickListener
            }
            val user = Firebase.auth.currentUser!!
            val credential = EmailAuthProvider
                .getCredential(user.email.toString(),password.text.toString())
            var database =
                Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference

            user.reauthenticate(credential)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        database.child("users").child(Firebase.auth.uid.toString()).removeValue().addOnCompleteListener{
                            user!!.delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Global.user.userReset()
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                    else{
                                        Toast.makeText(baseContext, "Can't Delete Account, try later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }






                    }
                    else{
                        Toast.makeText(baseContext, "Password does not match",
                            Toast.LENGTH_SHORT).show()
                    }
                }




        }












        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout3)
        val navView : NavigationView = findViewById(R.id.nav_view2)
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


    }
}