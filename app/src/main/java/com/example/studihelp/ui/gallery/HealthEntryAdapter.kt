package com.example.studihelp.ui.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R

class HealthEntryAdapter(
    private val context: Context,
    private val listener: OnHealthEntryClickListener
) : RecyclerView.Adapter<HealthEntryAdapter.ViewHolder>() {

    private val healthEntries = mutableListOf<HealthEntry>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val notesTextView: TextView = itemView.findViewById(R.id.notesTextView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val healthEntry = healthEntries[position]
                    listener.onHealthEntryClick(healthEntry)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.health_entry_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentHealthEntry = healthEntries[position]
        holder.typeTextView.text = currentHealthEntry.type
        holder.durationTextView.text = currentHealthEntry.duration
        holder.dateTextView.text = currentHealthEntry.date
        holder.notesTextView.text = currentHealthEntry.notes
    }

    override fun getItemCount(): Int {
        return healthEntries.size
    }

    fun setHealthEntries(entries: List<HealthEntry>) {
        healthEntries.clear()
        healthEntries.addAll(entries)
        notifyDataSetChanged()
    }
}
