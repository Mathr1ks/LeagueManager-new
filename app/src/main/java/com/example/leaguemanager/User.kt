package com.example.leaguemanager

import android.content.Intent
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@IgnoreExtraProperties
data class User(var iconId: String? = null,
                var init: Boolean? = false,
                var summonername: String? = null,
                var teamName: String? = null,
                var leader: Boolean? = null,
                var platform: String? = null,
                var region: String? = null,
                var rankFlex: String? = null,
                var rankSolo: String? = null,
                var pendingid: String?=null
) {

    fun fullHashToUser(map: HashMap<String,Any>){
        this.init= map.get("init") as Boolean?
        this.summonername= map.get("summonername") as String?
        this.teamName= map.get("teamid") as String?
        this.leader= map.get("leader") as Boolean?
        this.platform= map.get("platform") as String?
        this.region= map.get("region") as String?
        this.pendingid=map.get("pendingid") as String?
    }
    fun userReset(){
        this.iconId=null
        this.init=null
        this.summonername=null
        this.teamName=null
        this.leader=null
        this.platform=null
        this.rankFlex=null
        this.rankSolo=null
        this.region=null
        this.pendingid=null
    }

}