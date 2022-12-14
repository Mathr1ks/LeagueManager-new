package com.example.leaguemanager.teams

data class TeamData(
    var leader: String? = null,
    var logoId: Int?=null,
    var rank: Int?=null,
    var members: ArrayList<String>? =null
)