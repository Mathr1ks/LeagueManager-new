package com.example.leaguemanager.teams

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.R
import com.example.leaguemanager.databinding.TeamMemberBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class TeamMemberAdapter () {

//    lateinit var context: Context;
//    val storage = Firebase.storage
//    val storageRef = storage.reference
//    inner class TeamMemberViewHolder(val binding: TeamMemberBinding): RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMemberAdapter.TeamMemberViewHolder {
//        val view = LayoutInflater.from(parent.context)
//        val binding = TeamMemberBinding.inflate(view,parent,false)
//        context= parent.context
//
//        return TeamMemberViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: TeamMemberAdapter.TeamMemberViewHolder, position: Int) {
//        holder.binding.apply {
//            Log.d("siemensadasdasdasdasd","kauwegad")
//            summonerNameTeams.text = pending[position].summonerName
//        }
//
//    }
//    override fun getItemCount(): Int {
//        return pending.size
//    }
//
//
//
//    fun deleteItem(index: Int){
//        pending.removeAt(index)
//        notifyDataSetChanged()
//    }
}