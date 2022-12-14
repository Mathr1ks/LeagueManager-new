package com.example.leaguemanager

import android.widget.ImageView

sealed class Message {
    data class MyMessage(
        val messtext: String): Message()
    data class FriendMessage(
        val messtext: String,
        val avatar : String,
        val nickname: String): Message()


}