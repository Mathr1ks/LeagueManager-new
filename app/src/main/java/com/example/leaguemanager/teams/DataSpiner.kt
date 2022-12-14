package com.example.leaguemanager.teams

import com.example.leaguemanager.R
import com.squareup.picasso.Picasso

data class DataSpiner(val image: Int, val ImageId: String)

object SpinerImages {

    private val images = intArrayOf(
        R.drawable.teamicon0,
        R.drawable.teamicon1,
        R.drawable.teamicon2,
        R.drawable.teamicon3,
        R.drawable.teamicon4,
        R.drawable.teamicon5,
        R.drawable.teamicon6,
        R.drawable.teamicon7,
        R.drawable.teamicon8,
        R.drawable.teamicon9
    )

    private val imageID = arrayOf(
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10"
    )

    var list: ArrayList<DataSpiner>?=null
    get() {
        if(field != null)
            return field

        field = ArrayList()
        for(i in images.indices){
            val imageId = images[i]
            val imageID = imageID[i]

            val dataSpiner = DataSpiner(imageId,imageID)

            field!!.add(dataSpiner)
        }
        return field
    }
}
