package com.example.leaguemanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class DataHistoryAdapter :
    RecyclerView.Adapter<DataHistoryAdapter.DataHistoryAdapterViewHolder>() {

    private val adapterData = mutableListOf<DataHistory>()
    lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataHistoryAdapterViewHolder {
        context = parent.context
        val layout = when (viewType) {
            TYPE_CHAMP -> R.layout.champ_history
            TYPE_INFO -> R.layout.information_about_team
            TYPE_HEADER -> R.layout.header_history
            else -> throw IllegalArgumentException("Invalid type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return DataHistoryAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataHistoryAdapterViewHolder, position: Int) {
        holder.bind(adapterData[position])
    }

    override fun getItemCount(): Int = adapterData.size

    override fun getItemViewType(position: Int): Int {
        return when (adapterData[position]) {
            is DataHistory.ChampHistory -> TYPE_CHAMP
            is DataHistory.InformationAboutTeam -> TYPE_INFO
            is DataHistory.HeaderHistory -> TYPE_HEADER
        }
    }

    fun setData(data: List<DataHistory>) {
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

    inner class DataHistoryAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private fun bindChampHistory(item: DataHistory.ChampHistory) {

//            val summonerSpell1: Int,
//            val summonerSpell2: Int,

            itemView.findViewById<ConstraintLayout>(R.id.constraintSummonerHistory).setOnClickListener{
                val intent = Intent(context, FIndSummonerPage::class.java)
                intent.putExtra("Username", item.summonerName)
                intent.putExtra("Region", Global.user.region)
                intent.putExtra("Platform", Global.user.platform)
                context.startActivity(intent)

            }

            itemView.findViewById<TextView>(R.id.name).text = item.summonerName
            itemView.findViewById<TextView>(R.id.cs).text = item.minionScore.toString()
            itemView.findViewById<TextView>(R.id.stats).text =
                item.kill.toString() + "/" + item.death + "/" + item.assist
            var deathPom : Int = 0
            if(item.death == 0 )
                deathPom = 1
            else deathPom = item.death
            itemView.findViewById<TextView>(R.id.ratio).text =
                String.format("%.2f", (item.kill * 1.0 + item.assist * 1.0) / deathPom * 1.0)
            itemView.findViewById<TextView>(R.id.gold).text = item.gold.toString()
            itemView.findViewById<TextView>(R.id.damage).text = item.damage.toString()

            val imagesItem = listOf(
                itemView.findViewById<ImageView>(R.id.item1),
                itemView.findViewById<ImageView>(R.id.item2),
                itemView.findViewById<ImageView>(R.id.item3),
                itemView.findViewById<ImageView>(R.id.item4),
                itemView.findViewById<ImageView>(R.id.item5),
                itemView.findViewById<ImageView>(R.id.item6)
            )
            for (i in 0..5) {
                Picasso.get()
                    .load(Global.dragonLink+"/img/item/" + item.items[i] + ".png")
                    .into(imagesItem[i])
            }


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

            setRankImage(itemView.findViewById<TextView>(R.id.rank),item.rank1,item.rank2)


        }

        private fun bindInformationAboutTeam(item: DataHistory.InformationAboutTeam) {
            itemView.findViewById<TextView>(R.id.result).text = item.result
            itemView.findViewById<TextView>(R.id.teamStats).text = item.teamStats
            itemView.findViewById<TextView>(R.id.barons).text = item.barons
            itemView.findViewById<TextView>(R.id.dragons).text = item.dragons
            itemView.findViewById<TextView>(R.id.towers).text = item.towers
            if (item.result == "LOST")
                itemView.findViewById<ConstraintLayout>(R.id.bgcolor)
                    .setBackgroundColor(Color.argb(140,153, 0, 26))
        }

        private fun bindHeaderHistory(item: DataHistory.HeaderHistory) {
            itemView.findViewById<TextView>(R.id.type).text = item.type
            itemView.findViewById<TextView>(R.id.playTime).text = item.playTime
            itemView.findViewById<TextView>(R.id.date).text = item.date
        }


        fun bind(dataHistory: DataHistory) {
            when (dataHistory) {
                is DataHistory.ChampHistory -> bindChampHistory(dataHistory)
                is DataHistory.InformationAboutTeam -> bindInformationAboutTeam(dataHistory)
                is DataHistory.HeaderHistory -> bindHeaderHistory(dataHistory)
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

