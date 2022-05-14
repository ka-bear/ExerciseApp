package com.example.reba

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.view.ViewGroup
import android.widget.ImageView
import android.view.LayoutInflater
import com.google.android.material.snackbar.Snackbar

class RecyclerAdapter(val chpsList: ArrayList<infodata>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v : View = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_layout,parent,false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        holder.bindItems(chpsList[position])
    }
    override fun getItemCount() = chpsList.size

    // The class holding the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var itemTitle: TextView
        var itemDetails: TextView

        init {
            itemImage = itemView.findViewById(R.id.imageView3)
            itemTitle = itemView.findViewById(R.id.item_detail)
            itemDetails = itemView.findViewById(R.id.item_detail2)

        }
        fun bindItems(chp : infodata){
            itemTitle.text = chp.title
            itemDetails.text = chp.detail
            itemImage.setImageResource(chp.images)
        }
    }
}
