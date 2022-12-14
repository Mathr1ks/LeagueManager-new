package com.example.leaguemanager

import android.widget.ImageView

data class ShortHistoryData (
   var matchId: String,
   var region: String,
   var platform: String,
   var matchDuration : String,
   var matchType : String,
   var matchDate : String,
   var win : Boolean,
   var championID : Int,
   var perksPrimaryStyle : Int,
   var perksSubStyle : Int,
   var summonerSpell1: Int,
   var summonerSpell2: Int,
   var kills: Int,
   var assists: Int,
   var deaths: Int,
   var item0: Int,
   var item1: Int,
   var item2: Int,
   var item3: Int,
   var item4: Int,
   var item5: Int,
   var itemWard: Int
)