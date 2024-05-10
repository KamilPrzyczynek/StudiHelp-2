package com.example.studihelp.ui.Timetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R

class TimetableAdapter(
    timetableList: MutableList<TimetableItem>,
    private val listener: OnItemClickListener
) :
    ListAdapter<TimetableItem, TimetableAdapter.TimetableViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timetable, parent, false)
        return TimetableViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TimetableViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TimetableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val roomTextView: TextView = itemView.findViewById(R.id.roomTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: TimetableItem) {
            nameTextView.text = item.name
            roomTextView.text = item.room
            timeTextView.text = "${item.startTime} - ${item.endTime}"
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    class DiffCallback : DiffUtil.ItemCallback<TimetableItem>() {
        override fun areItemsTheSame(oldItem: TimetableItem, newItem: TimetableItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimetableItem, newItem: TimetableItem): Boolean {
            return oldItem == newItem
        }
    }
}