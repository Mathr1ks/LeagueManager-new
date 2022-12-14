package com.example.leaguemanager

import java.util.HashMap

data class Ranks(
    val teams: MutableMap<String, MutableMap <String,Any>> = HashMap()
)
data class MyUsers(
    val users: MutableMap<String, MutableMap <String,Any>> = HashMap()
)

data class Wpis(
    val team: String? =null,
    val logoid: String? = null,
    val rank: String?= null,
    val RP: Int?= null

)
