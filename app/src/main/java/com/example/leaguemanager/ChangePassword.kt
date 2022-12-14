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

private lateinit var changePasswordButton: Button
private lateinit var oldpassword: TextView
private lateinit var password: TextView
private lateinit var confirm: TextView

class ChangePassword : AppCompatActivity() {
    lateinit var toggle : ActionBarDrawerToggle
    var auth=Firebase.auth
    var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)


        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout3)
        val navView : NavigationView = findViewById(R.id.nav_view2)
        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        //navView.bringToFront()

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




        oldpassword = findViewById<TextView>(R.id.oldpassword)
        password = findViewById<TextView>(R.id.password)
        confirm = findViewById<TextView>(R.id.confirm)

        changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        changePasswordButton.setOnClickListener {
            if(TextUtils.isEmpty(password.text.toString())){
                password.setError("Hasło nie może być puste")
                password.requestFocus()
                return@setOnClickListener
            }
            if(password.text.toString().length<8){
                password.setError("Hasło nie może być krótsze niż 8 znaków")
                password.requestFocus()
                return@setOnClickListener
            }
            if(!password.text.toString().equals(confirm.text.toString())){
                password.setError("Hasła się różnią")
                password.requestFocus()
                return@setOnClickListener
            }

            val user = Firebase.auth.currentUser!!
            val credential = EmailAuthProvider
                .getCredential(user.email.toString(),oldpassword.text.toString())

            user.reauthenticate(credential)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful) {
                        user!!.updatePassword(password.text.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(baseContext, "Password Changed.",
                                        Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    Toast.makeText(baseContext, "Password has not been changed.",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                    else{
                        Toast.makeText(baseContext, "Old password does not match",
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }
}