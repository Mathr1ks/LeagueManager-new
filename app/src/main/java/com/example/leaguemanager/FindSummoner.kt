package com.example.leaguemanager

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
import com.example.leaguemanager.teams.TeamActivity
import com.example.leaguemanager.teams.UserWithoutTeamActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.FileNotFoundException
import java.net.URL

class FindSummoner : AppCompatActivity() {
    lateinit var platformSpinner: Spinner
    var platform: String = "BR1"
    var region: String = "AMERICA"
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var errorText: TextView
    lateinit var usernameText: EditText
    var auth=Firebase.auth
    var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    lateinit var confirmButton: Button
    val liveData: MutableLiveData<Int> = MutableLiveData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_summoner)
        platformSpinner = findViewById<Spinner>(R.id.platformSpinner)
        errorText = findViewById<TextView>(R.id.errorText)
        usernameText = findViewById<EditText>(R.id.username)
        confirmButton = findViewById<Button>(R.id.confirmButton)
        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout3)
        val navView : NavigationView = findViewById(R.id.nav_view3)
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
        confirmButton.setOnClickListener{
            apiClientTask().execute()
            liveData.observe(this, Observer {
                val temp = liveData.value
                Log.d("intencja",temp.toString())
                if(temp==0){
                    errorText.text = "This summoner cannot be found"
                }
                else{
                    val intent = Intent(this@FindSummoner,FIndSummonerPage::class.java)
                    val username = usernameText.text.toString()
                    intent.putExtra("Username",username)
                    intent.putExtra("Platform",platform)
                    intent.putExtra("Region",region)
                    startActivity(intent)
                }
            })
        }
        platformSpinner.adapter = ArrayAdapter<CharSequence>(this,R.layout.platforms_spinner, resources.getStringArray(R.array.platform))
        platformSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                (p1 as TextView).setTextColor(resources.getColor(R.color.lightgray))
                platform = platformSpinner.selectedItem.toString()
                if (platform == "EUN1" || platform == "EUW1"||platform=="TR1"||platform == "RU"){
                    region = "EUROPE"
                }
                else if (platform == "BR1" || platform =="LA1"||platform=="LA2"||platform=="NA1"||platform=="OC1"){
                    region ="AMERICAS"
                }
                else
                    region = "ASIA"

            }
        }

    }
    inner class apiClientTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://$platform.api.riotgames.com/lol/summoner/v4/summoners/by-name/${usernameText.text}?api_key=${Home_Page.api_key}").readText(Charsets.UTF_8)
            } catch (ex: FileNotFoundException){
                response = "0"
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result=="0")
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