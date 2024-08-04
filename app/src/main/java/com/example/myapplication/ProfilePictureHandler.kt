package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ProfilePictureHandler (private val imageResourceID: List<Int>): RecyclerView.Adapter<ProfilePictureHandler.ProfilePictureViewHolder>() {

    class ProfilePictureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageView: ImageView = itemView.findViewById(R.id.profilePictureImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePictureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profilePictureItem, parent, false)
        return ProfilePictureViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfilePictureViewHolder, position: Int) {
        holder.imageView.setImageResource(imageResourceID[position])
    }

    override fun getItemCount(): Int = imageResourceID.size

}