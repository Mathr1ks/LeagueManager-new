package com.example.leaguemanager

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.teams.UserWithoutTeamActivity
import com.example.leaguemanager.teams.TeamActivity
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.net.URL
import com.squareup.picasso.Picasso
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONException


class Home_Page : AppCompatActivity() {
    companion object{
        val api_key = "RGAPI-8f8ec7ca-7744-4413-bf6b-c76ebccd4ccc"
    }
    var platform : String = ""
    var region: String = ""
    var player_name = ""
    var playerId: String = ""
    var puuId: String = ""
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var arrMatches: JSONArray
    lateinit var rankSoloIcon: TextView
    lateinit var rankFlexIcon: TextView
    lateinit var playerLevel: TextView
    lateinit var historyRecycleView: RecyclerView
    lateinit var favouriteChampFragment1: FragmentFavouriteChamp
    lateinit var favouriteChampFragment2: FragmentFavouriteChamp
    lateinit var favouriteChampFragment3: FragmentFavouriteChamp
    lateinit var renewButton: Button
    lateinit var historyList: MutableList<ShortHistoryData>
    lateinit var adapter:HistoryShortAdapter
    lateinit var liveGameButton: Button
    val liveData: MutableLiveData<String> = MutableLiveData()
    val liveData2: MutableLiveData<String> = MutableLiveData()
    var auth=Firebase.auth
    var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    var iconId = 1
    var summonerLevel = 0

    lateinit var summonerName: TextView
    lateinit var summonerIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        player_name = ""
        region = "EUROPE"
        platform = "EUN1"
        Log.d("name", player_name)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout2)
        val navView : NavigationView = findViewById(R.id.nav_view2)
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
                            intent = Intent(this,UserWithoutTeamActivity::class.java)

                        else
                            intent = Intent(this,TeamActivity::class.java)
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

        renewButton = findViewById<Button>(R.id.buttonRenew)
        summonerName = findViewById<TextView>(R.id.player_name)
        summonerIcon = findViewById<ImageView>(R.id.player_icon)
        rankSoloIcon=findViewById<TextView>(R.id.rank_solo_icon)
        rankFlexIcon=findViewById<TextView>(R.id.rank_flex_icon)
        playerLevel=findViewById<TextView>(R.id.player_level)
        historyRecycleView = findViewById<RecyclerView>(R.id.historyList)
        historyList = mutableListOf()
        adapter = HistoryShortAdapter(historyList)
        historyRecycleView.adapter=adapter
        historyRecycleView.layoutManager = LinearLayoutManager(this)
        liveGameButton = findViewById<Button>(R.id.buttonLiveGame)
        liveGameButton.setOnClickListener{
            val intent = Intent(this,LiveGame::class.java)
            intent.putExtra("id",playerId)
            startActivity(intent)
        }
        renewButton.setOnClickListener{
            apiClientTask().execute()
        }

        favouriteChampFragment1 = FragmentFavouriteChamp(0,liveData,platform)
        favouriteChampFragment2 = FragmentFavouriteChamp(1,liveData,platform)
        favouriteChampFragment3 = FragmentFavouriteChamp(2,liveData,platform)

        supportFragmentManager.beginTransaction().addToBackStack("0").replace(
            R.id.fragment_favourite_1,
            favouriteChampFragment1,
            "1"
        ).addToBackStack("1").commit()

        supportFragmentManager.beginTransaction().addToBackStack("1").replace(
            R.id.fragment_favourite_2,
            favouriteChampFragment2,
            "2"
        ).addToBackStack("2").commit()

        supportFragmentManager.beginTransaction().addToBackStack("2").replace(
            R.id.fragment_favourite_3,
            favouriteChampFragment3,
            "3"
        ).addToBackStack("3").commit()

        database.child("users").child(auth.uid.toString()).child("region").get().addOnSuccessListener {
            region=it.value.toString()
        }
        database.child("users").child(auth.uid.toString()).child("platform").get().addOnSuccessListener {
            platform=it.value.toString()
        }
        database.child("users").child(auth.uid.toString()).child("summonername").get().addOnSuccessListener {
            player_name=it.value.toString()
            liveData2.value="1"
            liveData2.observe(this,Observer{
                apiClientTask().execute()
            })
            //apiClientTask().execute()
        }
        Log.d("region",region)
        Log.d("Platform",platform)
        Log.d("playername",player_name)



    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        while (true){
            if( supportFragmentManager.backStackEntryCount >0)
                supportFragmentManager.popBackStackImmediate()
            else break
        }
        super.onBackPressed()
    }
    inner class apiClientTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            response = URL("https://$platform.api.riotgames.com/lol/summoner/v4/summoners/by-name/$player_name?api_key=$api_key").readText(Charsets.UTF_8)
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val jsonObj:JSONObject
            jsonObj = JSONObject(result)
            summonerName.text = jsonObj.getString("name")
            playerId = jsonObj.getString("id")
            liveData.value = playerId
            puuId = jsonObj.getString("puuid")
            iconId = jsonObj.getInt("profileIconId")
            Log.d("idIcon",iconId.toString())
            database.child("users").child(auth.uid.toString()).child("iconId").setValue(iconId.toString())
            summonerLevel = jsonObj.getInt("summonerLevel")
            var temp = summonerLevel.toString()+" lvl"
            playerLevel.text = temp
            Picasso.get().load(Global.dragonLink+"/img/profileicon/$iconId.png").into(summonerIcon)

            apiRankTask().execute()
            apiMatchTask().execute()

        }

    }
    inner class apiRankTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            response = URL(" https://$platform.api.riotgames.com/lol/league/v4/entries/by-summoner/$playerId?api_key=$api_key").readText(Charsets.UTF_8)
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val jsonArr:JSONArray
            jsonArr = JSONArray(result)
            if (jsonArr.length()==2) {
                val rankFirst = jsonArr.getJSONObject(1)
                val tierFirst = rankFirst.getString("tier")
                val valFirst = rankFirst.getString("rank")
                val typeFirst = rankFirst.getString("queueType")
                if(typeFirst=="RANKED_SOLO_5x5"){
                    setRankImage(rankSoloIcon, valFirst, tierFirst)
                    database.child("users").child(auth.uid.toString()).child("rankSolo").setValue(parseRank(valFirst,tierFirst))

                }
                else{
                    setRankImage(rankFlexIcon, valFirst, tierFirst)
                    database.child("users").child(auth.uid.toString()).child("rankFlex").setValue(parseRank(valFirst,tierFirst))
                }
                val rankSecond = jsonArr.getJSONObject(0)
                val tierSecond = rankSecond.getString("tier")
                val valSecond = rankSecond.getString("rank")
                val typeSecond = rankSecond.getString("queueType")
                if(typeSecond=="RANKED_FLEX_SR"){
                    setRankImage(rankFlexIcon, valSecond, tierSecond)
                    database.child("users").child(auth.uid.toString()).child("rankFlex").setValue(parseRank(valSecond,tierSecond))
                }
                else{
                    setRankImage(rankSoloIcon, valSecond, tierSecond)
                    database.child("users").child(auth.uid.toString()).child("rankSolo").setValue(parseRank(valSecond,tierSecond))
                }
            }
            if(jsonArr.length()==1){
                val temp = jsonArr.getJSONObject(0)
                val queueType = temp.getString("queueType")
                if (queueType=="RANKED_SOLO_5x5"){
                    val soloTier = temp.getString("tier")
                    val soloRank = temp.getString("rank")
                    setRankImage(rankSoloIcon, soloRank, soloTier)
                    database.child("users").child(auth.uid.toString()).child("rankSolo").setValue(parseRank(soloRank,soloTier))
                    database.child("users").child(auth.uid.toString()).child("rankFlex").setValue("U")

                }
                else{
                    val flexTier = temp.getString("tier")
                    val flexRank = temp.getString("rank")
                    setRankImage(rankFlexIcon, flexRank, flexTier)
                    database.child("users").child(auth.uid.toString()).child("rankFlex").setValue(parseRank(flexRank,flexTier))
                    database.child("users").child(auth.uid.toString()).child("rankSolo").setValue("U")
                }
            }
            if(jsonArr.length()==0){
                database.child("users").child(auth.uid.toString()).child("rankSolo").setValue("U")
                database.child("users").child(auth.uid.toString()).child("rankFlex").setValue("U")
            }
        }


    }
    inner class apiMatchTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            response = URL("https://$region.api.riotgames.com/lol/match/v5/matches/by-puuid/$puuId/ids?start=0&count=10&api_key=$api_key").readText(Charsets.UTF_8)
            return response
        }

        override fun onPostExecute(result: String?) {
            historyList.clear()
            arrMatches = JSONArray(result)
            historyRecycleView.removeAllViews()
            val id = arrMatches.length()
            for (i in 0..id-1){
                val matchId = arrMatches[i].toString()
                apiSignleMatchTask(matchId).execute()

            }
            super.onPostExecute(result)

        }


    }
    inner class apiSignleMatchTask() : AsyncTask<String, Void, String>() {
        constructor(matchId: String) : this(){
            this.matchId=matchId
        }
        var matchId:String = ""
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            response = URL("https://$region.api.riotgames.com/lol/match/v5/matches/$matchId?api_key=$api_key").readText(Charsets.UTF_8)
            return response
        }

        override fun onPostExecute(result: String?) {
            val jsonObj = JSONObject(result)
            val infoObj = jsonObj.getJSONObject("info")
            var gameDuration = infoObj.getInt("gameDuration").toString()
            val time = Integer.parseInt(gameDuration)/60
            val rest = (gameDuration.toDouble()/60)-time
            val seconds = (rest*60).roundToInt()
            val matchType = infoObj.getString("gameMode")
            var tp = 0
            var matchDate:Long =0
            try {
                matchDate = infoObj.getLong("gameEndTimestamp")
            }
            catch (exception: JSONException){
                tp=1
            }
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            val dateString = formatter.format(Date(matchDate));
            val participants = infoObj.getJSONArray("participants")
            var win: Boolean = true
            var championId: Int = 1
            var perksPrimaryStyle: Int = -1
            var perksSubStyle: Int = -1
            var summonerSpell1: Int = -1
            var summonerSpell2: Int = -1
            var kills: Int = -1
            var assists: Int = -1
            var deaths: Int = -1
            var item0: Int = -1
            var item1: Int = -1
            var item2: Int = -1
            var item3: Int = -1
            var item4: Int = -1
            var item5: Int = -1
            var itemWard: Int = -1

            for(i in 0 until participants.length()){
                val item = participants.getJSONObject(i)
                val summName = item.getString("summonerName")
                Log.d("tutaj",summName)
                Log.d("NAZWA ",player_name)
                val tpPuuid = item.getString("puuid")
                if (puuId.toString() ==tpPuuid.toString()){
                    Log.d("tutaj2",summName)
                    win = item.getBoolean("win")
                    championId = item.getInt("championId")
                    Log.d("championID1",championId.toString())
                    val perks = item.getJSONObject("perks")
                    val perksStyle = perks.getJSONArray("styles")
                    for (i in 0 until perksStyle.length()){
                        val item = perksStyle.getJSONObject(i)
                        if( i==0){
                            perksPrimaryStyle=item.getInt("style")
                        }
                        if( i==1){
                            perksSubStyle=item.getInt("style")
                        }
                    }
                    summonerSpell1 = item.getInt("summoner1Id")
                    summonerSpell2 = item.getInt("summoner2Id")
                    kills = item.getInt("kills")
                    assists = item.getInt("assists")
                    deaths = item.getInt("deaths")
                    item0 = item.getInt("item0")
                    item1 = item.getInt("item1")
                    item2 = item.getInt("item2")
                    item3 = item.getInt("item3")
                    item4 = item.getInt("item4")
                    item5 = item.getInt("item5")
                    itemWard = item.getInt("item6")

                    break
                }
            }
            val item = ShortHistoryData(
                matchId,
                region,
                platform,
                time.toString() + "m " + seconds.toString() + "s",
                matchType,
                dateString,
                win,
                championId,
                perksPrimaryStyle,
                perksSubStyle,
                summonerSpell1,
                summonerSpell2,
                kills,
                assists,
                deaths,
                item0,
                item1,
                item2,
                item3,
                item4,
                item5,
                itemWard
            )
            historyList.add(item)
            adapter.notifyItemInserted(historyList.size-1)
            super.onPostExecute(result)

        }


    }

    fun setRankImage(image: TextView, rank:String, tier:String){
        var shortTier = ""
        when (tier){
            "IRON" -> {
                image.setBackgroundColor(resources.getColor(R.color.iron))
                shortTier = "I"
            }
            "SILVER" -> {
                image.setBackgroundColor(resources.getColor(R.color.silver))
                shortTier = "S"
            }
            "BRONZE"->{
                image.setBackgroundColor(resources.getColor(R.color.bronze))
                shortTier = "B"
            }
            "GOLD" -> {
                image.setBackgroundColor(resources.getColor(R.color.gold))
                shortTier = "G"
            }
            "PLATINUM" -> {
                image.setBackgroundColor(resources.getColor(R.color.platinum))
                shortTier = "P"
            }
            "DIAMOND" -> {
                image.setBackgroundColor(resources.getColor(R.color.diamond))
                shortTier = "D"
            }
            "MASTER" -> {
                image.setBackgroundColor(resources.getColor(R.color.master))
                shortTier = "M"
            }
            "GRANDMASTER" -> {
                image.setBackgroundColor(resources.getColor(R.color.grandmaster))
                shortTier = "GM"
            }
            "CHALLENGER" -> {
                image.setBackgroundColor(resources.getColor(R.color.challenger))
                shortTier = "C"
            }

        }
        var shortRank=""
        when (rank){
            "I" -> shortRank="1"
            "II" -> shortRank="2"
            "III" -> shortRank="3"
            "IV" -> shortRank="4"
        }
        var result = ""
        if(shortTier=="GM"||shortTier=="C")
            result = shortTier
        else
            result = shortTier+shortRank
        image.text = result
    }
    fun parseRank(rank:String, tier:String):String{
        var shortTier = ""
        when (tier){
            "IRON" -> {
                shortTier = "I"
            }
            "SILVER" -> {
                shortTier = "S"
            }
            "BRONZE"->{
                shortTier = "B"
            }
            "GOLD" -> {
                shortTier = "G"
            }
            "PLATINUM" -> {
                shortTier = "P"
            }
            "DIAMOND" -> {
                shortTier = "D"
            }
            "MASTER" -> {
                shortTier = "M"
            }
            "GRANDMASTER" -> {
                shortTier = "GM"
            }
            "CHALLENGER" -> {
                shortTier = "C"
            }

        }
        var shortRank=""
        when (rank){
            "I" -> shortRank="1"
            "II" -> shortRank="2"
            "III" -> shortRank="3"
            "IV" -> shortRank="4"
        }
        var result = ""
        if(shortTier=="GM"||shortTier=="C")
            result = shortTier
        else
            result = shortTier+shortRank

        return result
    }

}