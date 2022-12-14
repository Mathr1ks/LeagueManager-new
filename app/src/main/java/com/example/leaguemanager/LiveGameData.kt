package com.example.leaguemanager

import android.widget.TextView
import java.util.jar.Pack200

sealed class LiveGameData {

    data class HeaderHistory(
            val type: String,
            val playTime: String
    ) : LiveGameData()

    data class InformationAboutTeam1(
            val tier: String
    ) : LiveGameData()
    data class InformationAboutTeam2(
            val tier: String
    ) : LiveGameData()

    data class ChampInfo(
            val summonerName: String,
            val summonerId: String,
            val championID: Int,
            val summonerSpell1: Int,
            val summonerSpell2: Int,
            val rune1: Int,
            val rune2: Int,
            val rank1: String,
            val rank2: String
    ) : LiveGameData()
}