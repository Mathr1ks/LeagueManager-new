package com.example.leaguemanager

import android.os.AsyncTask
import android.os.AsyncTask.SERIAL_EXECUTOR
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.security.acl.Owner
import java.sql.Timestamp

class History : AppCompatActivity() {


    var idMatch = ""
    val apiKey = "RGAPI-8f8ec7ca-7744-4413-bf6b-c76ebccd4ccc"
    var liveData: MutableLiveData<String> = MutableLiveData()
    var liveData2: MutableLiveData<String> = MutableLiveData()
    lateinit var dataHistoryList: MutableList<DataHistory>
    lateinit var rank: TextView
    var platform = ""
    var region = ""
    lateinit var summonerId: String
    lateinit var puuid: String

    private val dataHistoryAdapter: DataHistoryAdapter by lazy {
        DataHistoryAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        platform = intent.getStringExtra("platform").toString()
        region = intent.getStringExtra("region").toString()
        idMatch = intent.getStringExtra("matchId").toString()
        liveData2.value = "1"
        liveData2.observe(this,Observer{
            apiClientTask().execute()
        })
        liveData.observe(this, Observer {
            dataHistoryList = mutableListOf<DataHistory>()

            var parse: JSONObject
            parse = JSONObject(liveData.value)
            val jsonArray: JSONArray = parse.getJSONObject("info").getJSONArray("participants")

            val gameType = parse.getJSONObject("info").get("queueId")
            var queueName = "RANKED_SOLO_5x5"
            if (gameType == 440)
                queueName = "RANKED_FLEX_SR"
            val gameTypeConverted =
                if (gameType == 400) "DRAFT" else if (gameType == 420) "SOLO/DUO" else if (gameType == 430) "BLIND" else if (gameType == 440) "FLEX" else if (gameType == 450) "ARAM" else "GAME"
            val gameDuration = parse.getJSONObject("info").get("gameDuration")
            val gameDurationMinutes = gameDuration as Int / 60
            val gameDurationSeconds = gameDuration % 60

            val gameStartedDate = parse.getJSONObject("info").get("gameStartTimestamp")
            val gameStartedDateParsed: String =
                Timestamp(gameStartedDate as Long).toString().subSequence(0, 16) as String

            var team1Result = ""
            var team2Result = ""
            var team1Stats: IntArray = intArrayOf(0, 0, 0, 0, 0, 0)
            var team2Stats: IntArray = intArrayOf(0, 0, 0, 0, 0, 0)
            val jsonArrayStats: JSONArray = parse.getJSONObject("info").getJSONArray("teams")
            for (i in 0..1) {
                val jsonTeam: JSONObject =
                    (jsonArrayStats.get(i) as JSONObject).getJSONObject("objectives")
                if (i == 0) {
                    team1Stats[3] = (jsonTeam.get("baron") as JSONObject).getInt("kills")
                    team1Stats[4] = (jsonTeam.get("dragon") as JSONObject).getInt("kills")
                    team1Stats[5] = (jsonTeam.get("tower") as JSONObject).getInt("kills")
                } else {
                    team2Stats[3] = (jsonTeam.get("baron") as JSONObject).getInt("kills")
                    team2Stats[4] = (jsonTeam.get("dragon") as JSONObject).getInt("kills")
                    team2Stats[5] = (jsonTeam.get("tower") as JSONObject).getInt("kills")
                }

            }

            dataHistoryList.add(
                DataHistory.HeaderHistory(
                    gameTypeConverted,
                    gameDurationMinutes.toString() + ":" + gameDurationSeconds,
                    gameStartedDateParsed
                )
            )

            for (i in 0..9) {
                var champInfo = ArrayList<String>()
                var jo: JSONObject
                jo = jsonArray.get(i) as JSONObject
                var runes: JSONArray
                runes = jo.getJSONObject("perks").getJSONArray("styles")
                var runes2: JSONObject
                runes2 = runes.get(0) as JSONObject
                var runes3: JSONArray
                runes3 = runes2.getJSONArray("selections")
                var runes4: JSONObject
                runes4 = runes3.get(0) as JSONObject
                var runes5: JSONObject
                runes5 = runes.get(1) as JSONObject

                summonerId = jo.getString("summonerId")
                puuid = jo.getString("puuid")
                Log.d(puuid,"PUUID3")
                    champInfo = arrayListOf(
                        jo.getString("summonerName"),
                        jo.getInt("championId").toString(),
                        jo.getString("summoner1Id"),
                        jo.getString("summoner2Id"),
                        runes4.getInt("perk").toString(),
                        runes5.getInt("style").toString(),
                        jo.getInt("kills").toString(),
                        jo.getInt("assists").toString(),
                        jo.getInt("deaths").toString(),
                        jo.getInt("totalMinionsKilled").toString(),
                        jo.getInt("goldEarned").toString(),
                        jo.getInt("totalDamageDealtToChampions").toString(),

                        jo.getInt("item0").toString(),
                        jo.getInt("item1").toString(),
                        jo.getInt("item2").toString(),
                        jo.getInt("item3").toString(),
                        jo.getInt("item4").toString(),
                        jo.getInt("item5").toString()
                    )

                    if (i < 5) {
                        team1Stats[0] += jo.getInt("kills")
                        team1Stats[1] += jo.getInt("deaths")
                        team1Stats[2] += jo.getInt("assists")
                    } else {
                        team2Stats[0] += jo.getInt("kills")
                        team2Stats[1] += jo.getInt("deaths")
                        team2Stats[2] += jo.getInt("assists")
                    }

                    if (i == 0) {
                        if (jo.getBoolean("win")) {
                            team1Result = "WIN"
                            team2Result = "LOST"
                        } else {
                            team1Result = "LOST"
                            team2Result = "WIN"
                        }
                    }
                    apiRankTask(
                        summonerId,
                        champInfo,
                        i,
                        team1Result,
                        team1Stats,
                        team2Result,
                        team2Stats,
                        queueName,
                        puuid
                    ).execute()
                }

        })


    }

    inner class apiClientTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            response =
                URL("https://$region.api.riotgames.com/lol/match/v5/matches/$idMatch?api_key=$apiKey").readText(
                    Charsets.UTF_8
                )
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            liveData.value = result
        }
    }


    inner class apiRankTask() : AsyncTask<String, Void, String>() {
        constructor(
            playerId: String,
            champList: ArrayList<String>,
            id: Int,
            team1Result: String,
            team1Stats: IntArray,
            team2Result: String,
            team2Stats: IntArray,
            queueName: String,
            puuid : String
        ) : this() {
            this.playerId = playerId
            this.champList = champList
            this.id = id
            this.team1Result = team1Result
            this.team1Stats = team1Stats
            this.team2Result = team2Result
            this.team2Stats = team2Stats
            this.queueName = queueName
            this.puuid = puuid
        }

        var playerId: String = ""
        lateinit var champList: ArrayList<String>
        var id: Int = 0
        var team1Result: String = ""
        var team2Result: String = ""
        var team1Stats: IntArray = intArrayOf(0, 0, 0, 0, 0, 0)
        var team2Stats: IntArray = intArrayOf(0, 0, 0, 0, 0, 0)
        var queueName: String = "RANKED_SOLO_5x5"
        var puuid : String = ""
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            Log.d("PUUID",puuid)
            if(puuid != "BOT")
            {
                response =
                    URL("https://$platform.api.riotgames.com/lol/league/v4/entries/by-summoner/$playerId?api_key=$apiKey").readText(
                        Charsets.UTF_8
                    )
            }
            else
                response = ""

            return response
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            var rank1: String = "u"
            var rank2: String = "u"
            Log.d("PUUID2",puuid)
            if(puuid!="BOT")
            {
                val jsonArr: JSONArray
                jsonArr = JSONArray(result)
                var test: Int = jsonArr.length()
                var i =0
                while(i<test)
                {
                    val rankObj = jsonArr.getJSONObject(i)
                    if (rankObj.getString("queueType") == queueName) {
                        rank2 = rankObj.getString("tier")
                        rank1 = rankObj.getString("rank")
                    }
                    i+=1
                }
            }

            dataHistoryList.add(
                DataHistory.ChampHistory(
                    champList[0],
                    champList[1].toInt(),
                    champList[2].toInt(),
                    champList[3].toInt(),
                    champList[4].toInt(),
                    champList[5].toInt(),
                    champList[6].toInt(),
                    champList[7].toInt(),
                    champList[8].toInt(),
                    champList[9].toInt(),
                    champList[10].toInt(),
                    champList[11].toInt(),
                    listOf(
                        champList[12].toInt(),
                        champList[13].toInt(),
                        champList[14].toInt(),
                        champList[15].toInt(),
                        champList[16].toInt(),
                        champList[17].toInt(),
                    ),
                    rank1,
                    rank2
                )
            )
            if (id == 9) {
                dataHistoryList.add(
                    1,
                    DataHistory.InformationAboutTeam(
                        team1Result,
                        team1Stats[0].toString() + "/" + team1Stats[1].toString() + "/" + team1Stats[2],
                        team1Stats[3].toString(),
                        team1Stats[4].toString(),
                        team1Stats[5].toString()
                    )
                )
                dataHistoryList.add(
                    7,
                    DataHistory.InformationAboutTeam(
                        team2Result,
                        team2Stats[0].toString() + "/" + team2Stats[1].toString() + "/" + team2Stats[2],
                        team2Stats[3].toString(),
                        team2Stats[4].toString(),
                        team2Stats[5].toString()
                    )
                )
            }

            getHistoryData()
            dataHistoryAdapter.setData(getHistoryData())
            val recycledView1 = findViewById<RecyclerView>(R.id.recycledView1).apply {
                layoutManager = LinearLayoutManager(this@History)
                hasFixedSize()
                this.adapter = dataHistoryAdapter
            }
        }
    }


    private fun getHistoryData(): MutableList<DataHistory> = dataHistoryList

}