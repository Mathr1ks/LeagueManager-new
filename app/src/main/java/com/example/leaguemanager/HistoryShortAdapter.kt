package com.example.leaguemanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.leaguemanager.databinding.ChampHistoryShortBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class HistoryShortAdapter(var history: List<ShortHistoryData>) : RecyclerView.Adapter<HistoryShortAdapter.HistoryViewHolder>() {
    lateinit var context: Context;
    val storage = Firebase.storage
    val storageRef = storage.reference

    inner class HistoryViewHolder(val binding: ChampHistoryShortBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ChampHistoryShortBinding.inflate(view, parent, false)
        context = parent.context;
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.binding.apply {
            matchTime.text=history[position].matchDuration
            matchType.text = history[position].matchType
            matchDate.text = history[position].matchDate
            playerKills.text = history[position].kills.toString()
            playerAssists.text = history[position].assists.toString()
            playerDeaths.text = history[position].deaths.toString()
            var kda = ""
            if (history[position].deaths == 0){
                kda = "KDA: "+String.format("%.2f",(history[position].kills+history[position].assists)*1.0/1)

            }
            else {
                kda = "KDA: " + String.format(
                    "%.2f",
                    (history[position].kills + history[position].assists) * 1.0 / history[position].deaths
                )
            }
            playerKDA.text=kda

            if(history[position].win){

                var color = ContextCompat.getColor(context, R.color.green)
                resultText.text="W"
                resultColor.setBackgroundColor(color)
            }
            else{
                var color = ContextCompat.getColor(context, R.color.red)
                resultText.text="L"
                resultColor.setBackgroundColor(color)
            }
            if( history[position].perksPrimaryStyle!=-1){
                val perks = history[position].perksPrimaryStyle
                storageRef.child("runes/$perks.png").downloadUrl.addOnSuccessListener { temp ->
                    Picasso.get().load(temp.toString()).into(summonerPrimaryPerks)
                }
            }
            if( history[position].perksSubStyle!=-1){
                val perks = history[position].perksSubStyle
                storageRef.child("runes/$perks.png").downloadUrl.addOnSuccessListener { temp ->
                    Picasso.get().load(temp.toString()).into(summonerSubPerks)
                }
            }
            var spell1 = history[position].summonerSpell1
            storageRef.child("summonerSpells/$spell1.png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString()).into(summonerSpell1)
            }
            var spell2 = history[position].summonerSpell2
            storageRef.child("summonerSpells/$spell2.png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString()).into(summonerSpell2)
            }
            var champId = history[position].championID
            Log.d("champId",champId.toString())
            storageRef.child("champions/$champId.png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString()).into(championIcon)
            }

            var item0 = history[position].item0
            Picasso.get().load(Global.dragonLink+"/img/item/$item0.png").into(buildItem1)
            var item1 = history[position].item1
            Picasso.get().load(Global.dragonLink+"/img/item/$item1.png").into(buildItem2)
            var item2 = history[position].item2
            Picasso.get().load(Global.dragonLink+"/img/item/$item2.png").into(buildItem3)
            var item3 = history[position].item3
            Picasso.get().load(Global.dragonLink+"/img/item/$item3.png").into(buildItem4)
            var item4 = history[position].item4
            Picasso.get().load(Global.dragonLink+"/img/item/$item4.png").into(buildItem5)
            var item5 = history[position].item5
            Picasso.get().load(Global.dragonLink+"/img/item/$item5.png").into(buildItem6)
            var itemWard = history[position].itemWard
            Picasso.get().load(Global.dragonLink+"/img/item/$itemWard.png").into(buildTotem)

            buttonArrow.setOnClickListener{
                val intent = Intent(context,History::class.java)
                intent.putExtra("matchId",history[position].matchId)
                intent.putExtra("platform",history[position].platform)
                intent.putExtra("region",history[position].region)
                context.startActivity(intent)
            }



        }
    }

    override fun getItemCount(): Int {
        return history.size
    }
}