package com.example.leaguemanager.teams

sealed class DataTeams{

    data class MemberList(
        val summonerName: String,
        val teamName: String
    ) : DataTeams()

    data class InvitedMemberList(
        val summonerName: String,
        val iconId: Int,
        val teamName: String,
        //val rank1: String,
        //val rank2: String
    ) : DataTeams()
    data class ChallengesHistoryList(
        val matchId : String,
        val team1 : String,
        val matchDuration : String,
        val matchDate : String,
        val win : Boolean

    ) : DataTeams()



}
