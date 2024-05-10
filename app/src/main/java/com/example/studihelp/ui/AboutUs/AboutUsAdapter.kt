package com.example.studihelp.ui.AboutUs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R

class AboutUsAdapter(private val aboutUsInfoList: List<AboutUsInfo>) :
    RecyclerView.Adapter<AboutUsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val roleTextView: TextView = itemView.findViewById(R.id.roleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_about_us, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentAboutUsInfo = aboutUsInfoList[position]
        holder.nameTextView.text = currentAboutUsInfo.name
        holder.roleTextView.text = currentAboutUsInfo.role
        holder.descriptionTextView.text = currentAboutUsInfo.description
    }

    override fun getItemCount(): Int {
        return aboutUsInfoList.size
    }
}
