
package com.example.leaguemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MessageAdapter(var adapterData: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageAdapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageAdapterViewHolder {
        val layout = when (viewType) {
            MY_MESSAGE -> R.layout.my_message
            FRIEND_MESSAGE -> R.layout.friend_message
            else -> throw IllegalArgumentException("Invalid type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageAdapterViewHolder, position: Int) {
        holder.bind(adapterData[position])
    }

    override fun getItemCount(): Int = adapterData.size

    override fun getItemViewType(position: Int): Int {
        return when (adapterData[position]) {
            is Message.MyMessage -> MY_MESSAGE
            is Message.FriendMessage -> FRIEND_MESSAGE

        }
    }



    companion object {
        private const val MY_MESSAGE = 0
        private const val FRIEND_MESSAGE = 1

    }

    class MessageAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private fun bindMyMessage(item: Message.MyMessage) {
            itemView.findViewById<TextView>(R.id.message).text = item.messtext
        }

        private fun bindFriendMessage(item: Message.FriendMessage) {
            itemView.findViewById<TextView>(R.id.message).text = item.messtext
            itemView.findViewById<TextView>(R.id.nickname).text= item.nickname
            Picasso.get().load(Global.dragonLink+"/img/profileicon/${item.avatar}.png").into(itemView.findViewById<ImageView>(R.id.avatar))
        }

        private fun bindHeaderHistory(item: DataHistory.HeaderHistory) {
            itemView.findViewById<TextView>(R.id.type).text = item.type
            itemView.findViewById<TextView>(R.id.playTime).text = item.playTime
            itemView.findViewById<TextView>(R.id.date).text = item.date
        }


        fun bind(message: Message) {
            when (message) {
                is Message.MyMessage -> bindMyMessage(message)
                is Message.FriendMessage -> bindFriendMessage(message)
            }

        }
    }

}

