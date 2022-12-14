package com.example.leaguemanager

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Register_Page : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var register_button: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirm: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        val currentUser = Firebase.auth.currentUser
        if(currentUser!=null){
            val intent = Intent(this, SummonerName::class.java)
            startActivity(intent)
        }


        email = findViewById<EditText>(R.id.email)
        password = findViewById<EditText>(R.id.password)
        confirm = findViewById<EditText>(R.id.confirm)


        register_button = findViewById<Button>(R.id.register_button)
        register_button.setOnClickListener {
            if(!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()){
                email.setError("Podaj Prawidłowy Email")
                email.requestFocus()
                return@setOnClickListener
            }
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
            Firebase.auth.createUserWithEmailAndPassword(email.text.toString(),password.text.toString()).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User created.")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                }
                else {
                    Toast.makeText(baseContext, "Nie udało się utworzyć konta.",
                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "User not created.")
                }
            }

        }

    }
}