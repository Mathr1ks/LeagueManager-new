package com.example.leaguemanager

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.LiveGameDataAdapter.LiveGameDataAdapterViewHolder
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import io.grpc.Context
import kotlin.coroutines.coroutineContext

class LiveGameDataAdapter :
    RecyclerView.Adapter<LiveGameDataAdapterViewHolder>() {
    private val adapterData = mutableListOf<LiveGameData>()
    lateinit var context:android.content.Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveGameDataAdapterViewHolder {
        context = parent.context

        val layout = when (viewType) {
            LiveGameDataAdapter.TYPE_CHAMP -> R.layout.champ_info_live
            LiveGameDataAdapter.TYPE_INFO -> R.layout.live_game_info_team
            LiveGameDataAdapter.TYPE_HEADER -> R.layout.header_livegame
            else -> throw IllegalArgumentException("Invalid type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return LiveGameDataAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiveGameDataAdapterViewHolder, position: Int) {
        holder.bind(adapterData[position])

    }

    override fun getItemCount(): Int = adapterData.size

    override fun getItemViewType(position: Int): Int {

        return when (adapterData[position]) {

            is LiveGameData.ChampInfo -> LiveGameDataAdapter.TYPE_CHAMP
            is LiveGameData.InformationAboutTeam1 -> LiveGameDataAdapter.TYPE_INFO
            is LiveGameData.InformationAboutTeam2 -> LiveGameDataAdapter.TYPE_INFO
            is LiveGameData.HeaderHistory -> LiveGameDataAdapter.TYPE_HEADER
            else -> {1}
        }
    }

    fun setData(data: List<LiveGameData>) {
        adapterData.apply {
            clear()
            addAll(data)

        }
    }

    companion object {
        private const val TYPE_CHAMP = 0
        private const val TYPE_INFO = 1
        private const val TYPE_HEADER = 2

    }

    inner class LiveGameDataAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private fun bindChampHistory(item: LiveGameData.ChampInfo) {
            itemView.findViewById<ConstraintLayout>(R.id.bg2).setOnClickListener{

                val intent = Intent(context, FIndSummonerPage::class.java)
                intent.putExtra("Username", item.summonerName)
                intent.putExtra("Region", Global.user.region)
                intent.putExtra("Platform", Global.user.platform)
                context.startActivity(intent)

            }

            itemView.findViewById<TextView>(R.id.name).text = item.summonerName
            val storage = Firebase.storage
            val storageRef = storage.reference
            storageRef.child("champions/" + item.championID + ".png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString())
                    .into(itemView.findViewById<ImageView>(R.id.champavatar))
            }.addOnFailureListener {
                // Handle any errors
            }
            storageRef.child("runes/" + item.rune1 + ".png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString())
                    .into(itemView.findViewById<ImageView>(R.id.rune1))
            }.addOnFailureListener {
                // Handle any errors
            }
            storageRef.child("runes/" + item.rune2 + ".png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString())
                    .into(itemView.findViewById<ImageView>(R.id.rune2))
            }.addOnFailureListener {
                // Handle any errors
            }

            storageRef.child("summonerSpells/${item.summonerSpell1}.png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString())
                    .into(itemView.findViewById<ImageView>(R.id.summoner1))
            }
            storageRef.child("summonerSpells/${item.summonerSpell2}.png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString())
                    .into(itemView.findViewById<ImageView>(R.id.summoner2))
            }

            setRankImage(itemView.findViewById<TextView>(R.id.rank), item.rank1, item.rank2)

        }

        private fun bindInformationAboutTeam1(item: LiveGameData.InformationAboutTeam1) {
            itemView.findViewById<TextView>(R.id.result).text = "Team1"
        }
        private fun bindInformationAboutTeam2(item: LiveGameData.InformationAboutTeam2) {
            itemView.findViewById<TextView>(R.id.result).text = "Team2"
            itemView.findViewById<ConstraintLayout>(R.id.bgcolor)
                .setBackgroundColor(Color.rgb(153, 0, 26))

        }
        private fun bindHeaderHistory(item: LiveGameData.HeaderHistory) {
            itemView.findViewById<TextView>(R.id.type).text = item.type
            //itemView.findViewById<TextView>(R.id.playTime).text = item.playTime
        }

        fun bind(liveGameData: LiveGameData) {
            when (liveGameData) {
                is LiveGameData.ChampInfo -> bindChampHistory(liveGameData)
                is LiveGameData.InformationAboutTeam1 -> bindInformationAboutTeam1(liveGameData)
                is LiveGameData.InformationAboutTeam2 -> bindInformationAboutTeam2(liveGameData)
                is LiveGameData.HeaderHistory -> bindHeaderHistory(liveGameData)
            }

        }

        fun setRankImage(image: TextView, rank: String, tier: String) {
            var shortTier = ""
            when (tier) {
                "IRON" -> {
                    image.setBackgroundResource(R.color.iron)
                    shortTier = "I"
                }
                "SILVER" -> {
                    image.setBackgroundResource(R.color.silver)
                    shortTier = "S"
                }
                "BRONZE" -> {
                    image.setBackgroundResource(R.color.bronze)
                    shortTier = "B"
                }
                "GOLD" -> {
                    image.setBackgroundResource(R.color.gold)
                    shortTier = "G"
                }
                "PLATINUM" -> {
                    image.setBackgroundResource(R.color.platinum)
                    shortTier = "P"
                }
                "DIAMOND" -> {
                    image.setBackgroundResource(R.color.diamond)
                    shortTier = "D"
                }
                "MASTER" -> {
                    image.setBackgroundResource(R.color.master)
                    shortTier = "M"
                }
                "GRANDMASTER" -> {
                    image.setBackgroundResource(R.color.grandmaster)
                    shortTier = "GM"
                }
                "CHALLENGER" -> {
                    image.setBackgroundResource(R.color.challenger)
                    shortTier = "C"
                }

            }
            var shortRank = ""
            when (rank) {
                "I" -> shortRank = "1"
                "II" -> shortRank = "2"
                "III" -> shortRank = "3"
                "IV" -> shortRank = "4"
            }
            var result = ""
            if (shortTier == "GM" || shortTier == "C")
                result = shortTier
            else
                result = shortTier + shortRank
            image.text = result
        }
    }
}
