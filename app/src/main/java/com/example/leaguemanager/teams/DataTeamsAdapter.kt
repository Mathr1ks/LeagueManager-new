package com.example.leaguemanager.teams

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.*
import com.example.leaguemanager.challenges.ChallangeDetailActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.FileNotFoundException
import java.net.URL
import java.util.Observer

class DataTeamsAdapter(var adapterData: MutableList<DataTeams>) :
    RecyclerView.Adapter<DataTeamsAdapter.DataTeamsAdapterViewHolder>() {

    lateinit var context: Context
    var leader = false
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataTeamsAdapterViewHolder {
        context = parent.context
        val layout = when (viewType) {
            TYPE_MEMBER -> R.layout.team_member
            TYPE_INVITED_MEMBER -> R.layout.team_add_member_short
            TYPE_CHALLENGES_HISTORY -> R.layout.team_history
            else -> throw IllegalArgumentException("Invalid type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return DataTeamsAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataTeamsAdapterViewHolder, position: Int) {
        holder.bind(adapterData[position], position)
    }

    override fun getItemCount(): Int = adapterData.size
    override fun getItemViewType(position: Int): Int {
        return when (adapterData[position]) {
            is DataTeams.MemberList -> DataTeamsAdapter.TYPE_MEMBER
            is DataTeams.InvitedMemberList -> DataTeamsAdapter.TYPE_INVITED_MEMBER
            is DataTeams.ChallengesHistoryList -> DataTeamsAdapter.TYPE_CHALLENGES_HISTORY
        }
    }


    fun setData(data: List<DataTeams>) {
        adapterData.apply {
            clear()
            addAll(data)
        }
    }

    private fun removeItem(position: Int) {

        adapterData.removeAt(position)
        notifyDataSetChanged()


    }

    companion object {
        private const val TYPE_MEMBER = 0
        private const val TYPE_INVITED_MEMBER = 1
        private const val TYPE_CHALLENGES_HISTORY = 2

    }


    inner class DataTeamsAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindMemberList(item: DataTeams.MemberList, position: Int) {
            itemView.findViewById<TextView>(R.id.summonerNameTeams).text = item.summonerName
            val database =
                Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
            var auth = Firebase.auth

            database.child("users").child(auth.uid.toString()).child("leader").get().addOnSuccessListener {
                leader= it.value.toString().toBoolean()
                Log.d("leader: ",leader.toString())
                if(!leader){
                    database.child("users").child(auth.uid.toString()).child("summonername").get().addOnSuccessListener {
                        if (it.value.toString() != item.summonerName ){
                            itemView.findViewById<ImageButton>(R.id.removeMemberButton).visibility =
                                View.GONE
                        }
                    }
                }
            }

            itemView.findViewById<ConstraintLayout>(R.id.constraintTeamMember).setOnClickListener{
                val intent = Intent(context, FIndSummonerPage::class.java)
                intent.putExtra("Username", item.summonerName)
                intent.putExtra("Region", Global.user.region)
                intent.putExtra("Platform", Global.user.platform)
                context.startActivity(intent)

            }

            if (position == 0) {
                itemView.findViewById<ImageButton>(R.id.removeMemberButton).visibility =
                    View.GONE
            }

            var refUsers = database.child("users")

            refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot!!.children
                    children.forEach {
                        if (it.child("summonername").value.toString() == item.summonerName) {
                            var rankSolo = "U"
                            val refRankSolo = refUsers.child(it.key.toString()).child("rankSolo")
                            refRankSolo.get().addOnSuccessListener {
                                itemView.findViewById<TextView>(R.id.rank_solo_icon).text =
                                    it.value.toString()
                                setRankImage(
                                    itemView.findViewById<TextView>(R.id.rank_solo_icon),
                                    it.value.toString()
                                )
                            }
                            val refRankFlex = refUsers.child(it.key.toString()).child("rankFlex")
                            refRankFlex.get().addOnSuccessListener {
                                itemView.findViewById<TextView>(R.id.rank_flex_icon).text =
                                    it.value.toString()
                                setRankImage(
                                    itemView.findViewById<TextView>(R.id.rank_flex_icon),
                                    it.value.toString()
                                )
                            }
                            val iconId = refUsers.child(it.key.toString()).child("iconId")
                            iconId.get().addOnSuccessListener {
                                var icon = it.value.toString()

                                Picasso.get()
                                    .load(Global.dragonLink+"/img/profileicon/$icon.png")
                                    .into(itemView.findViewById<ImageView>(R.id.summonerIconTeams))
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            itemView.findViewById<ImageButton>(R.id.removeMemberButton).setOnClickListener {
                if(!leader){
                    val intent = Intent(context, UserWithoutTeamActivity::class.java)
                    context.startActivity(intent)
                }

                val ref = database.child("teams").child(item.teamName).child("members")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        var tp = 1

                        children.forEach {
                            if (it.value == item.summonerName) {
                                ref.child(it.key.toString()).removeValue()

                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                //zmiana uzytkownika
                val ref2 = database.child("users")
                ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        children.forEach {
                            if (it.child("summonername").value.toString() == item.summonerName) {
                                val ref3 = ref2.child(it.key.toString()).child("teamName")
                                ref2.child(it.key.toString()).child("teamName").removeValue()

                            }

                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })


                removeItem(position)


            }

        }
        fun bindInvitedMemberList(item: DataTeams.InvitedMemberList, position: Int) {
            itemView.findViewById<TextView>(R.id.inviteMemberName).text = item.summonerName

            val database =
                Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
            var refUsers = database.child("users")

            refUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot!!.children
                    children.forEach {
                        if (it.child("summonername").value.toString() == item.summonerName) {
                            var rankSolo = "U"
                            val refRankSolo = refUsers.child(it.key.toString()).child("rankSolo")
                            refRankSolo.get().addOnSuccessListener {
                                itemView.findViewById<TextView>(R.id.rank_solo_icon2).text =
                                    it.value.toString()
                                setRankImage(
                                    itemView.findViewById<TextView>(R.id.rank_solo_icon2),
                                    it.value.toString()
                                )
                            }
                            val refRankFlex = refUsers.child(it.key.toString()).child("rankFlex")
                            refRankFlex.get().addOnSuccessListener {
                                itemView.findViewById<TextView>(R.id.rank_flex_icon2).text =
                                    it.value.toString()
                                setRankImage(
                                    itemView.findViewById<TextView>(R.id.rank_flex_icon2),
                                    it.value.toString()
                                )
                            }
                            val iconId = refUsers.child(it.key.toString()).child("iconId")
                            iconId.get().addOnSuccessListener {
                                var icon = it.value.toString()

                                Picasso.get()
                                    .load(Global.dragonLink+"/img/profileicon/$icon.png")
                                    .into(itemView.findViewById<ImageView>(R.id.inviteMemberIcon))
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            itemView.findViewById<Button>(R.id.rejectInvitationMember).setOnClickListener {

                val ref = database.child("teams").child(item.teamName).child("pendingInvites")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        var tp = 1

                        children.forEach {
                            if (it.value == item.summonerName) {
                                ref.child(it.key.toString()).removeValue()

                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                //zmiana uzytkownika
                val ref2 = database.child("users")
                ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        children.forEach {
                            if (it.child("summonername").value.toString() == item.summonerName) {
                                val ref3 = ref2.child(it.key.toString()).child("pendingInvites")
                                ref3.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val ch = snapshot!!.children
                                        ch.forEach {
                                            if (it.value.toString() == item.teamName) {
                                                ref3.child(it.key.toString()).removeValue()
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

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                removeItem(position)
            }


        }

        fun bind(dataTeams: DataTeams, position: Int) {
            when (dataTeams) {
                is DataTeams.MemberList -> bindMemberList(dataTeams, position)
                is DataTeams.InvitedMemberList -> bindInvitedMemberList(dataTeams, position)
                is DataTeams.ChallengesHistoryList ->bindChallengesHistoryList(dataTeams,position)
            }

        }

        fun bindChallengesHistoryList(item: DataTeams.ChallengesHistoryList, position: Int) {

            itemView.findViewById<TextView>(R.id.teamName).text = item.team1
            itemView.findViewById<TextView>(R.id.matchTime).text = item.matchDuration
            itemView.findViewById<TextView>(R.id.challengeDate).text = item.matchDate
            if(item.win){
                var color = ContextCompat.getColor(context, R.color.green)
                itemView.findViewById<TextView>(R.id.resultText).text="W"
                itemView.findViewById<ConstraintLayout>(R.id.resultColor).setBackgroundColor(color)
            }
            else{
                var color = ContextCompat.getColor(context, R.color.red)
                itemView.findViewById<TextView>(R.id.resultText).text="L"
                itemView.findViewById<ConstraintLayout>(R.id.resultColor).setBackgroundColor(color)
            }
            var database = Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
            var ref = database.child("teams").child(item.team1).child("logoId")
            val storage = Firebase.storage
            val storageRef = storage.reference
            ref.get().addOnSuccessListener {
                storageRef.child("/TeamIcons/${it.value.toString()}.png").downloadUrl.addOnSuccessListener { temp ->
                    Picasso.get().load(temp.toString())
                        .into(itemView.findViewById<ImageView>(R.id.teamIcon))
                }
            }
            var ref2 = database.child("teams").child(item.team1).child("rank")
            ref.get().addOnSuccessListener {
                var rank = it.value.toString()
                var tp = itemView.findViewById<TextView>(R.id.rankTeam).text.toString() + rank
                itemView.findViewById<TextView>(R.id.rankTeam).text = tp
            }

            itemView.findViewById<ConstraintLayout>(R.id.matchDetailsButton).setOnClickListener{
                val intent = Intent(context, History::class.java)
                var match = Global.user.platform + "_" + item.matchId
                intent.putExtra("matchId", match)
                intent.putExtra("region", Global.user.region)
                intent.putExtra("platform", Global.user.platform)
                context.startActivity(intent)
            }


        }

    }


    fun setRankImage(image: TextView, rank: String) {
        when (rank[0].toString()) {
            "I" -> {
                image.setBackgroundColor(context.resources.getColor(R.color.iron))
            }
            "S" -> {
                image.setBackgroundColor(context.resources.getColor(R.color.silver))
            }
            "B" -> {
                image.setBackgroundColor(context.resources.getColor(R.color.bronze))
            }
            "G" -> {
                if (rank[1].toString() == "M") {
                    image.setBackgroundColor(context.resources.getColor(R.color.grandmaster))
                } else
                    image.setBackgroundColor(context.resources.getColor(R.color.gold))
            }
            "P" -> {
                image.setBackgroundColor(context.resources.getColor(R.color.platinum))
            }
            "D" -> {
                image.setBackgroundColor(context.resources.getColor(R.color.diamond))
            }
            "M" -> {
                image.setBackgroundColor(context.resources.getColor(R.color.master))
            }
            "C" -> {
                image.setBackgroundColor(context.resources.getColor(R.color.challenger))
            }

        }
    }


}