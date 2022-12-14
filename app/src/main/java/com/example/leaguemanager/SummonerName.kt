package com.example.leaguemanager

import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL


class SummonerName : AppCompatActivity() {
    private lateinit var accept_button: Button
    private lateinit var username: EditText
    private lateinit var auth: FirebaseAuth
    lateinit var platformSpinner: Spinner
    lateinit var errorText: TextView
    val api_key = "RGAPI-8f8ec7ca-7744-4413-bf6b-c76ebccd4ccc"
    val liveData: MutableLiveData<Int> = MutableLiveData()
    var platform: String = "BR1"
    var region: String = "AMERICA"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summonername)

        auth = Firebase.auth
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            if (Global.user.summonername != null) {
                val intent = Intent(this, Home_Page::class.java)
                startActivity(intent)
            }
        }

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    rank = dataSnapshot.getValue<MyUsers>()!!

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

        platformSpinner = findViewById<Spinner>(R.id.platformSpinner)
        errorText = findViewById<TextView>(R.id.errorText)
        username = findViewById<EditText>(R.id.username)
        platformSpinner.adapter = ArrayAdapter<CharSequence>(
            this,
            R.layout.platforms_spinner,
            resources.getStringArray(R.array.platform)
        )
        platformSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                (p1 as TextView).setTextColor(resources.getColor(R.color.lightgray))
                platform = platformSpinner.selectedItem.toString()
                if (platform == "EUN1" || platform == "EUW1" || platform == "TR1" || platform == "RU") {
                    region = "EUROPE"
                } else if (platform == "BR1" || platform == "LA1" || platform == "LA2" || platform == "NA1" || platform == "OC1") {
                    region = "AMERICAS"
                } else
                    region = "ASIA"

            }
        }
        var database =
            Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference

        accept_button = findViewById<Button>(R.id.accept_button)
        accept_button.setOnClickListener {
            rank?.users?.forEach {
                if(it.value.get("summonername").toString().equals(username.text.toString())) {
                    errorText.setText("User Already Exists!!!")
                    return@setOnClickListener
                }
            }
            apiClientTask().execute()
            liveData.observe(this, Observer {
                val temp = liveData.value
                if (temp == 0) {
                    errorText.text = "This summoner cannot be found"
                } else {
                    val user = User(
                        null,true,username.text.toString(),null,true,platform,region,null,null,"0"
                    )
                    database.child("users").child(auth.uid.toString()).setValue(user)
                        .addOnSuccessListener {
                            Global.user=user
                            val intent = Intent(this, Home_Page::class.java)
                            startActivity(intent)
                        }.addOnFailureListener {
                            username.setError("Can't connect to database \ntry later")
                            username.requestFocus()
                        }
                }
            })

        }


    }

    inner class apiClientTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                response =
                    URL("https://$platform.api.riotgames.com/lol/summoner/v4/summoners/by-name/${username.text}?api_key=${api_key}").readText(
                        Charsets.UTF_8
                    )
            } catch (ex: FileNotFoundException) {
                response = "0"
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result == "0")
                liveData.value = 0
            else {
                val jsonObj: JSONObject
                jsonObj = JSONObject(result)
                try {
                    jsonObj.getString("name")
                } catch (ex1: JSONException) {
                    Log.d("intencja", "123")
                    liveData.value = 0
                    return
                }
                liveData.value = 1
            }
        }

    }
}