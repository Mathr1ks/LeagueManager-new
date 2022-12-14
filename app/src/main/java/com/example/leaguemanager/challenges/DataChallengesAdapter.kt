package com.example.leaguemanager.teams

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.DataHistory
import com.example.leaguemanager.DataHistoryAdapter
import com.example.leaguemanager.Global
import com.example.leaguemanager.R
import com.example.leaguemanager.challenges.ChallangeDetailActivity
import com.example.leaguemanager.challenges.ChallengesActivity
import com.example.leaguemanager.challenges.DataChallenges
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class DataChallengesAdapter(var adapterData: MutableList<DataChallenges>) :
    RecyclerView.Adapter<DataChallengesAdapter.DataChallengesAdapterViewHolder>() {

    lateinit var context: Context
    var database =
        Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
    val storage = Firebase.storage
    val storageRef = storage.reference
    var leader = false
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataChallengesAdapterViewHolder {
        context = parent.context
        val layout = when (viewType) {
            TYPE_SENT -> R.layout.activity_send_challenge_short
            TYPE_INCOMING -> R.layout.activity_incoming_challenges_short
            TYPE_ACTIVE -> R.layout.active_challenges
            TYPE_MEMBER ->R.layout.team_member
            else -> throw IllegalArgumentException("Invalid type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return DataChallengesAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataChallengesAdapterViewHolder, position: Int) {
        holder.bind(adapterData[position], position)
    }

    override fun getItemCount(): Int = adapterData.size
    override fun getItemViewType(position: Int): Int {
        return when (adapterData[position]) {
            is DataChallenges.SentChallengesList -> DataChallengesAdapter.TYPE_SENT
            is DataChallenges.IncomingChallengesList -> DataChallengesAdapter.TYPE_INCOMING
            is DataChallenges.ActiveChallengesList -> DataChallengesAdapter.TYPE_ACTIVE
            is DataChallenges.MemberList -> DataChallengesAdapter.TYPE_MEMBER


        }
    }


    fun setData(data: List<DataChallenges>) {
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
        private const val TYPE_SENT = 0
        private const val TYPE_INCOMING = 1
        private const val TYPE_ACTIVE = 2
        private const val TYPE_MEMBER = 3


    }


    inner class DataChallengesAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindActiveChallenges(item: DataChallenges.ActiveChallengesList, position: Int) {
            itemView.findViewById<TextView>(R.id.teamNameActiveChallenges).text = item.teamEnemyName
            val rankRef = database.child("teams").child(item.teamEnemyName).child("rank")
            rankRef.get().addOnSuccessListener {
                itemView.findViewById<TextView>(R.id.teamRankActiveChallenges).text = it.value.toString()
            }
            val iconRef = database.child("teams").child(item.teamEnemyName).child("logoId")
            iconRef.get().addOnSuccessListener {
                storageRef.child("/TeamIcons/${it.value.toString()}.png").downloadUrl.addOnSuccessListener { temp ->
                    Picasso.get().load(temp.toString())
                        .into(itemView.findViewById<ImageView>(R.id.teamIconActiveChallenges))
                }
            }

            itemView.findViewById<ImageButton>(R.id.activeChallengeInfo).setOnClickListener{
                val intent = Intent(context, ChallangeDetailActivity::class.java)
                intent.putExtra("matchId", item.matchId)
                context.startActivity(intent)
            }


        }
        fun bindSentChallenges(item: DataChallenges.SentChallengesList, position: Int) {
            itemView.findViewById<TextView>(R.id.teamNameActiveChallengesSent).text = item.teamEnemyName
            val rankRef = database.child("teams").child(item.teamEnemyName).child("rank")
            rankRef.get().addOnSuccessListener {
                itemView.findViewById<TextView>(R.id.teamRankActiveChallengesSent).text = it.value.toString()
            }
            val iconRef = database.child("teams").child(item.teamEnemyName).child("logoId")
            iconRef.get().addOnSuccessListener {
                storageRef.child("/TeamIcons/${it.value.toString()}.png").downloadUrl.addOnSuccessListener { temp ->
                    Picasso.get().load(temp.toString())
                        .into(itemView.findViewById<ImageView>(R.id.teamIconActiveChallengesSent))
                }
            }

            itemView.findViewById<ImageButton>(R.id.activeChallengeSentDecline).setOnClickListener{
                val ref = database.child("teams").child(item.teamName).child("sentChallenges")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        var tp = 1

                        children.forEach {
                            if (it.value == item.teamEnemyName) {
                                ref.child(it.key.toString()).removeValue()

                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                val ref2 = database.child("teams").child(item.teamEnemyName).child("incomingChallenges")
                ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        var tp = 1

                        children.forEach {
                            if (it.value == item.teamName) {
                                ref2.child(it.key.toString()).removeValue()

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
        fun bindIncomingChallenges(item: DataChallenges.IncomingChallengesList, position: Int) {
            itemView.findViewById<TextView>(R.id.teamNameActiveChallengesIncoming).text = item.teamEnemyName
            val rankRef = database.child("teams").child(item.teamEnemyName).child("rank")
            rankRef.get().addOnSuccessListener {
                itemView.findViewById<TextView>(R.id.teamRankActiveChallengesIncoming).text = it.value.toString()
            }
            val iconRef = database.child("teams").child(item.teamEnemyName).child("logoId")
            iconRef.get().addOnSuccessListener {
                storageRef.child("/TeamIcons/${it.value.toString()}.png").downloadUrl.addOnSuccessListener { temp ->
                    Picasso.get().load(temp.toString())
                        .into(itemView.findViewById<ImageView>(R.id.teamIconActiveChallengesIncoming))
                }
            }




            itemView.findViewById<ImageButton>(R.id.incomingChallengeAccept).setOnClickListener{

                var flagaa=0
                val teamExistRef = database.child("teams")
                teamExistRef.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val ch = snapshot!!.children
                        ch.forEach{
                            if (it.key.toString()==item.teamEnemyName){
                                var flag=0;
                                val sentChallengeTeam0Ref = database.child("teams").child(item.teamName).child("activeChallenges")
                                val pendingIdTeam0Ref = database.child("teams").child(item.teamName).child("pendingid")
                                sentChallengeTeam0Ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val children = snapshot!!.children
                                        children.forEach {
                                            if(it.value.toString()==item.teamEnemyName) {
                                                flag = 1;
                                            }
                                        }

                                        if (flag==0){
                                            pendingIdTeam0Ref.get().addOnSuccessListener {
                                                val reff = database.child("teams").child(item.teamName).child("avtiveChallenges")
                                                reff.addListenerForSingleValueEvent(object :ValueEventListener{
                                                    override fun onDataChange(snapshot1: DataSnapshot) {
                                                        val children1 = snapshot1!!.children
                                                        children1.forEach{
                                                            if(it.value.toString()==item.teamEnemyName){
                                                                flagaa = 1
                                                            }

                                                        }
                                                        if (flagaa==0 ){
                                                            val pendingId = it.value.toString().toInt()+1
                                                            sentChallengeTeam0Ref.child(pendingId.toString()).setValue(item.teamEnemyName+item.teamName)
                                                            pendingIdTeam0Ref.setValue(pendingId.toString())


                                                            var flag1=0;
                                                            val sentChallengeTeam1Ref = database.child("teams").child(item.teamEnemyName).child("activeChallenges")
                                                            val pendingIdTeam1Ref = database.child("teams").child(item.teamEnemyName).child("pendingid")
                                                            sentChallengeTeam1Ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    val children = snapshot!!.children
                                                                    children.forEach {
                                                                        if(it.value.toString()==item.teamName) {
                                                                            flag1 = 1;
                                                                        }
                                                                    }

                                                                    if (flag1==0){
                                                                        pendingIdTeam1Ref.get().addOnSuccessListener {
                                                                            val pendingId = it.value.toString().toInt()+1
                                                                            sentChallengeTeam1Ref.child(pendingId.toString()).setValue(item.teamEnemyName+item.teamName)
                                                                            pendingIdTeam1Ref.setValue(pendingId.toString())

                                                                            val matchesRef = database.child("matches")
                                                                            var matchName = item.teamEnemyName+item.teamName
                                                                            matchesRef.child(matchName.toString()).child("matchId").setValue(matchName)
                                                                            matchesRef.child(matchName.toString()).child("team1").setValue(item.teamEnemyName)
                                                                            matchesRef.child(matchName.toString()).child("team2").setValue(item.teamName)
                                                                            matchesRef.child(matchName.toString()).child("password").setValue(getRandomString(12))

                                                                        }
                                                                    }


                                                                }

                                                                override fun onCancelled(error: DatabaseError) {
                                                                    TODO("Not yet implemented")
                                                                }


                                                            })
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

                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                val ref = database.child("teams").child(item.teamName).child("incomingChallenges")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        var tp = 1

                        children.forEach {
                            if (it.value == item.teamEnemyName) {
                                ref.child(it.key.toString()).removeValue()

                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                val ref2 = database.child("teams").child(item.teamEnemyName).child("sentChallenges")
                ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        var tp = 1

                        children.forEach {
                            if (it.value == item.teamName) {
                                ref2.child(it.key.toString()).removeValue()

                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                removeItem(position)

            }


            itemView.findViewById<ImageButton>(R.id.incomingChallengeDecline).setOnClickListener{
                val ref = database.child("teams").child(item.teamName).child("incomingChallenges")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        var tp = 1

                        children.forEach {
                            if (it.value == item.teamEnemyName) {
                                ref.child(it.key.toString()).removeValue()

                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                val ref2 = database.child("teams").child(item.teamEnemyName).child("sentChallenges")
                ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot!!.children
                        var tp = 1

                        children.forEach {
                            if (it.value == item.teamName) {
                                ref2.child(it.key.toString()).removeValue()

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
        fun bindMemberList(item: DataChallenges.MemberList, position: Int) {
            itemView.findViewById<TextView>(R.id.summonerNameTeams).text = item.summonerName
            itemView.findViewById<ImageButton>(R.id.removeMemberButton).visibility =
                View.GONE
            val database =
                Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
            var auth = Firebase.auth

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

        }



        fun bind(dataChallenges: DataChallenges, position: Int) {
            when (dataChallenges) {
                is DataChallenges.SentChallengesList -> bindSentChallenges(dataChallenges, position)
                is DataChallenges.IncomingChallengesList -> bindIncomingChallenges(dataChallenges, position)
                is DataChallenges.ActiveChallengesList -> bindActiveChallenges(dataChallenges, position)
                is DataChallenges.MemberList -> bindMemberList(dataChallenges, position)
            }

        }

    }
    fun getRandomString(length: Int) : String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return List(length) { charset.random() }
            .joinToString("")
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