package com.example.leaguemanager

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.teams.TeamActivity
import com.example.leaguemanager.teams.UserWithoutTeamActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

lateinit var texts: EditText
lateinit var confirmButton: Button


lateinit var dataAdapter: MessageAdapter
lateinit var recycleView: RecyclerView
lateinit var lista: MutableList<Message>
var auth=Firebase.auth
var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference


class TeamChat : AppCompatActivity() {

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var registration:ListenerRegistration
    override fun onResume() {
        super.onResume()
        val db = Firebase.firestore
        registration = db.collection(Global.user.teamName.toString()).orderBy("timestamp")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                val source = if (value != null && value.metadata.hasPendingWrites())
                    "Local"
                else
                    "Server"
                for (doc in value!!.documentChanges) {
                    Log.e("tentego",source)
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            var message:String=""
                            doc.document.getString("text")?.let {
                                message=it
                            }
                            var from:String=""
                            doc.document.getString("user")?.let {
                                from= it
                            }
                            if(Global.user.summonername.equals(from)){
                                lista.add(Message.MyMessage(message))
                            }
                            else{
                                var nickname:String=""
                                doc.document.getString("user")?.let {
                                    nickname= it
                                }
                                var icon:String=""
                                doc.document.getString("icon")?.let {
                                    icon= it
                                }
                                lista.add(Message.FriendMessage(message,icon,nickname ))
                            }
                            dataAdapter.notifyItemInserted(lista.size-1)
                            texts.setText("")
                            hideKeyboard()
                            recycleView.scrollToPosition(lista.size-1)
                            Log.e("dodanie",message)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            var string:String=""
                            doc.document.getString("text")?.let {
                                string=it
                            }
                            string+=" napisane przez: "
                            doc.document.getString("user")?.let {
                                string+= it
                            }
                            string+=" o: "
                            doc.document.getTimestamp("timestamp")?.let {
                                string+=it.toString()
                            }

                            Log.e("modyfikacja",string)
                        }
                        DocumentChange.Type.REMOVED -> Log.d(TAG, "Removed city: ${doc.document.data}")
                    }
                }


            }

    }

    override fun onPause() {
        super.onPause()
        registration.remove()
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_chat)

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
        dataAdapter= MessageAdapter(lista)
        recycleView.adapter= dataAdapter
        recycleView.layoutManager=LinearLayoutManager(this)


        Log.e("xD",Global.user.toString())
        val db = Firebase.firestore
        texts = findViewById<EditText>(R.id.message)
        confirmButton = findViewById<Button>(R.id.confirmButton)
        confirmButton.setOnClickListener {
            if(TextUtils.isEmpty(texts.text.toString())){
                return@setOnClickListener
            }
            var docData = hashMapOf(
                "text" to texts.text.toString(),
                "user" to Global.user.summonername,
                "icon" to Global.user.iconId,
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection(Global.user.teamName.toString()).document()
                .set(docData)
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }






    }
}