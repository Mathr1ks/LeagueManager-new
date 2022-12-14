package com.example.leaguemanager
sealed class RankingWpis {
    data class MyWpis(
        val team: String? =null,
        val logoid: String? = null,
        val rank: String?= null,
        val RP: String?= null
    ): RankingWpis()
}