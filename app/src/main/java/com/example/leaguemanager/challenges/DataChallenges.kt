package com.example.leaguemanager.challenges

import com.example.leaguemanager.teams.DataTeams

sealed class DataChallenges{

    data class SentChallengesList(
        val teamName: String,
        val teamEnemyName: String
    ) : DataChallenges()

    data class IncomingChallengesList(
        val teamName: String,
        val teamEnemyName: String
    ) : DataChallenges()

    data class ActiveChallengesList(
        val teamName: String,
        val teamEnemyName: String,
        val matchId: String
    ) : DataChallenges()

    data class MemberList(
        val summonerName: String,
        val teamName: String
    ) : DataChallenges()

}
