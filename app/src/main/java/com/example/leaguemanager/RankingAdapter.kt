
package com.example.leaguemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class RankingAdapter(var adapterData: MutableList<RankingWpis>) :
    RecyclerView.Adapter<RankingAdapter.RankingAdapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RankingAdapterViewHolder {
        val layout = when (viewType) {
            MY_WPIS -> R.layout.activity_my_ranking
            else -> throw IllegalArgumentException("Invalid type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return RankingAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingAdapterViewHolder, position: Int) {
        holder.bind(adapterData[position])
    }

    override fun getItemCount(): Int = adapterData.size

    override fun getItemViewType(position: Int): Int {
        return  MY_WPIS
    }



    companion object {
        private const val MY_WPIS = 0

    }

    class RankingAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private fun bindMyMessage(item: RankingWpis.MyWpis) {
            itemView.findViewById<TextView>(R.id.textView).text = item.rank
            itemView.findViewById<TextView>(R.id.textView6).text = "TeamName: "+item.team
            itemView.findViewById<TextView>(R.id.textView7).text = "Ranking Points: "+item.RP
            val iconView=itemView.findViewById<ImageView>(R.id.champavatar)
            var database =
                Firebase.database("https://leaguemenager-default-rtdb.europe-west1.firebasedatabase.app").reference
            val storage = Firebase.storage
            val storageRef = storage.reference

            storageRef.child("/TeamIcons/${item.logoid.toString()}.png").downloadUrl.addOnSuccessListener { temp ->
                Picasso.get().load(temp.toString())
                    .into(iconView)
            }

        }




        fun bind(message: RankingWpis) {
            when (message) {
                is RankingWpis.MyWpis -> bindMyMessage(message)
            }

        }
    }

}

