package com.example.leaguemanager.teams

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.*
import com.example.leaguemanager.databinding.PendingInviteBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class PendingInvitesAdapter(var pending: MutableList<PendingInvitesData>): RecyclerView.Adapter<PendingInvitesAdapter.PendingInvitesViewHolder>() {

    lateinit var context: Context;
    val storage = Firebase.storage
    val storageRef = storage.reference

    inner class PendingInvitesViewHolder(val binding: PendingInviteBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingInvitesAdapter.PendingInvitesViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = PendingInviteBinding.inflate(view, parent, false)
        context = parent.context;
        return PendingInvitesViewHolder(binding)
    }
    override fun onBindViewHolder(holder: PendingInvitesAdapter.PendingInvitesViewHolder, position: Int) {
        holder.binding.apply {
            val teamName = "Name: "
            TeamName.text = teamName+pending[position].teamName
            val storage = Firebase.storage
            val storageRef = storage.reference
            val logoId = pending[position].teamLogo
            storageRef.child("TeamIcons/$logoId.png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString()).into(TeamIcon)
            }
            val temp = "Rank: "
            TeamRank.text =temp+ pending[position].teamRank.toString()
            acceptInvitation.setOnClickListener{
                val acceptTeam = pending[position].teamName
                deleteItem(position)
                var auth=Firebase.auth
                var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
                val pendingsTeam = database.child("teams").child(acceptTeam).child("pendingInvites")
                var username = ""
                database.child("users").child(auth.uid.toString()).child("summonername").get().addOnSuccessListener {
                    username=it.value.toString()
                    pendingsTeam.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot!!.children

                            children.forEach{
                                if(it.value.toString() == username){
                                    Log.d("ELO: ",it.key.toString())
                                    pendingsTeam.child(it.key.toString()).removeValue()
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }

                val memberRef = database.child("teams").child(acceptTeam).child("members")
                val ref2 = database.child("teams").child(acceptTeam).child("memberId").get().addOnSuccessListener {
                    val memberId = it.value.toString().toInt()+1
                    memberRef.child(it.value.toString()).setValue(username)
                    database.child("teams").child(acceptTeam).child("memberId").setValue(memberId.toString())
                }
                database.child("users").child(auth.uid.toString()).child("teamName").setValue(acceptTeam)

                val pendings = database.child("users").child(auth.uid.toString()).child("pendingInvites")
                pendings.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        children.forEach {
                            val teamDelete = it.value.toString()
                            if(teamDelete == acceptTeam){
                                it.key?.let { it1 -> pendings.child(it1).removeValue() }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

                database.child("users").child(auth.uid.toString()).child("leader").setValue(false)

                Global.user.teamName = acceptTeam
                context.startActivity(Intent(context, TeamActivity::class.java))

            }
            rejectInvitation.setOnClickListener{
                val delTeam = pending[position].teamName
                deleteItem(position)
                var auth=Firebase.auth
                var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
                val pendingsTeam = database.child("teams").child(delTeam).child("pendingInvites")
                var username = ""
                database.child("users").child(auth.uid.toString()).child("summonername").get().addOnSuccessListener {
                    username=it.value.toString()
                    pendingsTeam.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot!!.children
                            children.forEach{
                                if(it.value.toString() == username){
                                    it.key?.let {it1 ->pendingsTeam.child(it1).removeValue() }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }

                val pendings = database.child("users").child(auth.uid.toString()).child("pendingInvites")
                pendings.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        children.forEach {
                            val teamDelete = it.value.toString()
                            if(teamDelete == delTeam){
                                it.key?.let { it1 -> pendings.child(it1).removeValue() }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                    })


            }
        }
    }
    override fun getItemCount(): Int {
        return pending.size
    }

    fun deleteItem(index: Int){
        pending.removeAt(index)
        notifyDataSetChanged()
    }
}