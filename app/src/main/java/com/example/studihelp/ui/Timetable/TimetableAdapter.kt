package com.example.studihelp.ui.Timetable

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R


class TimetableAdapter(private val listener: OnItemClickListener) : ListAdapter<TimetableItem, RecyclerView.ViewHolder>(TimetableDiffCallback()) {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_DATA = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timetable_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_timetable_data, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_DATA) {
            val dataHolder = holder as DataViewHolder
            val item = getItem(position)
            dataHolder.bind(item)
        } else {
            val headerHolder = holder as HeaderViewHolder
            val item = getItem(position)
            headerHolder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isHeader) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_DATA
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)

        fun bind(item: TimetableItem) {
            headerTextView.text = item.dayOfWeek
        }
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
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

            // Set background color
            val color = when (item.color) {
                "Red" -> Color.RED
                "Blue" -> Color.BLUE
                "Green" -> Color.GREEN
                "Yellow" -> Color.YELLOW
                "Purple" -> Color.MAGENTA
                "Orange" -> Color.parseColor("#FFA500") // Use Color.parseColor for custom colors
                else -> Color.TRANSPARENT
            }
            itemView.setBackgroundColor(color)
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

    class TimetableDiffCallback : DiffUtil.ItemCallback<TimetableItem>() {
        override fun areItemsTheSame(oldItem: TimetableItem, newItem: TimetableItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimetableItem, newItem: TimetableItem): Boolean {
            return oldItem == newItem
        }
    }
}