package com.example.leaguemanager

import android.widget.TextView
import java.util.jar.Pack200

sealed class DataHistory {


    data class ChampHistory(
        val summonerName: String,
        val championID: Int,
        val summonerSpell1: Int,
        val summonerSpell2: Int,
        val rune1: Int,
        val rune2: Int,
        val kill: Int,
        val assist: Int,
        val death: Int,
        val minionScore: Int,
        val gold: Int,
        val damage: Int,
        val items: List<Int>,
        val rank1: String,
        val rank2: String
    ) : DataHistory()

    data class HeaderHistory(
        val type: String,
        val playTime: String,
        val date: String,
    ) : DataHistory()

    data class InformationAboutTeam(
        val result: String,
        val teamStats: String,
        val barons: String,
        val dragons: String,
        val towers: String,
    ) : DataHistory()
}