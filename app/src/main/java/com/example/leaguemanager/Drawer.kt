package com.example.leaguemanager

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.content.Intent
class Drawer : AppCompatActivity(){

    lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawer)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

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
                    val intent = Intent(this,Home_Page::class.java)
                    startActivity(intent)
                }
                R.id.nav_settings ->{
                    val intent = Intent(this,Home_Page::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout ->{
                    val intent = Intent(this,Home_Page::class.java)
                    startActivity(intent)
                }
            }

            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}