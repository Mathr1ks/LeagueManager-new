package com.example.leaguemanager.teams

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.example.leaguemanager.R

class SpinnerAdapter(context: Context, iconList : List<DataSpiner>) : ArrayAdapter<DataSpiner>(context , 0, iconList){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position,convertView,parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position,convertView,parent)
    }


    private fun initView(position: Int,convertView: View?,parent: ViewGroup): View{

        val dataspiner = getItem(position)

        val view = LayoutInflater.from(context).inflate(R.layout.spiner_images, parent,false)
        view.findViewById<ImageView>(R.id.spinnerImageView).setImageResource(dataspiner!!.image)
        view.findViewById<TextView>(R.id.testSpinerText).text= dataspiner.ImageId

        return view
    }


}