package com.example.leaguemanager

import android.app.Application
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.content.Intent
import android.widget.EditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.lang.Thread.sleep


class Global : Application() {
    companion object {
        @JvmField
        var user: User = User()
        var dragonLink: String = "http://ddragon.leagueoflegends.com/cdn/11.24.1"
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var reminder: TextView
    private lateinit var loginbutton: Button
    private lateinit var signbutton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        var database =
            Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
        //Firebase.auth.signOut()
         if (Firebase.auth.currentUser != null) {

            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_loading_screen)

            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Global.user = dataSnapshot.getValue<User>()!!
                    }
                    database.child("users").child(Firebase.auth.uid.toString())
                        .removeEventListener(this)
                    if (Global.user.summonername != null) {
                        /// Home_Page
                        val intent = Intent(getApplicationContext(), Home_Page::class.java)
                        startActivity(intent)
                    } else {
                        //SummonerName
                        val intent = Intent(getApplicationContext(), SummonerName::class.java)
                        startActivity(intent)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    val intent = Intent(getApplicationContext(), MainActivity::class.java)
                    startActivity(intent)
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                }
            }
            database.child("users").child(Firebase.auth.uid.toString())
                .addValueEventListener(postListener)

        } else {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            loginbutton = findViewById<Button>(R.id.login_button)
            loginbutton.setOnClickListener {
                username = findViewById<EditText>(R.id.username)
                password = findViewById<EditText>(R.id.password)
                val tmpusername = username.text.toString()
                if (!Patterns.EMAIL_ADDRESS.matcher(tmpusername).matches()) {
                    username.setError("Podaj Prawidłowy Email")
                    username.requestFocus()
                    return@setOnClickListener
                }
                val tmppassword = password.text.toString()
                if (tmppassword.length < 8) {
                    password.setError("Hasło nie może być krótsze niż 8 znaków")
                    password.requestFocus()
                    return@setOnClickListener
                }
                Firebase.auth.signInWithEmailAndPassword(tmpusername, tmppassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {


                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Global.user = dataSnapshot.getValue<User>()!!
                                    }

                                    database.child("users").child(Firebase.auth.uid.toString())
                                        .removeEventListener(this)

                                    if (Global.user.summonername != null) {
                                        //Home_Page
                                        val intent =
                                            Intent(getApplicationContext(), Home_Page::class.java)
                                        startActivity(intent)
                                    } else {
                                        ///SummonerName
                                        val intent = Intent(
                                            getApplicationContext(),
                                            SummonerName::class.java
                                        )
                                        startActivity(intent)
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    database.child("users").child(Firebase.auth.uid.toString())
                                        .removeEventListener(this)
                                    val intent =
                                        Intent(getApplicationContext(), MainActivity::class.java)
                                    startActivity(intent)
                                }
                            }

                                Log.e("imie4",
                                    database.child("users").child(Firebase.auth.uid.toString()).toString()
                                )

                            database.child("users").child(Firebase.auth.uid.toString())
                                .addValueEventListener(postListener)

                        } else {
                            Log.w(TAG, "loginWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, "Unable to log in",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }

            signbutton = findViewById<Button>(R.id.sign_button)
            signbutton.setOnClickListener {
                val intent = Intent(this, Register_Page::class.java)
                startActivity(intent)
            }

            reminder = findViewById<TextView>(R.id.reminder)
            reminder.setOnClickListener {
                val intent = Intent(this, ResetPassword_Page::class.java)
                startActivity(intent)
            }

        }


    }


}