package com.example.leaguemanager

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ResetPassword_Page : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var resetbutton: Button
    private lateinit var email: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpassword_page)
        val currentUser = Firebase.auth.currentUser
        if(currentUser!=null){
            val intent = Intent(this, SummonerName::class.java)
            startActivity(intent)
        }
        auth = Firebase.auth

        email = findViewById<EditText>(R.id.email)
        resetbutton = findViewById<Button>(R.id.reset_button)
        resetbutton.setOnClickListener {
            val tmp_email = email.text.toString()
            if(!Patterns.EMAIL_ADDRESS.matcher(tmp_email).matches()){
                email.setError("Podaj Prawidłowy Email")
                email.requestFocus()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(tmp_email).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Wiadomość e-mail resetująca hasło została wysłana na pocztę.",
                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Email sent.")
                }
                else{
                    Toast.makeText(baseContext, "Nie udało się wysłać wiadomości resetującej hasło.",
                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Email couldn't be send.")
                }
            }

        }

    }
}