package com.example.studihelp.ui.Drive

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.squareup.picasso.Picasso


class DriveAdapter(private val context: Context, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<DriveAdapter.DriveViewHolder>() {

    private var driveItems: List<DriveItem> = emptyList()

    fun submitList(driveItems: List<DriveItem>) {
        this.driveItems = driveItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriveViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_drive, parent, false)
        return DriveViewHolder(view)
    }

    override fun onBindViewHolder(holder: DriveViewHolder, position: Int) {
        val currentItem = driveItems[position]
        holder.textViewTopic.text = currentItem.topic

        try {
            if (currentItem.imageUrl.isNotEmpty()) {
                Log.e(TAG, currentItem.imageUrl)
                Picasso.get().load(currentItem.imageUrl)
                    .placeholder(R.drawable.baseline_image_not_supported_24)
                    .error(R.drawable.baseline_image_not_supported_24)
                    .into(holder.imageView)
            } else {
                holder.imageView.setImageResource(R.drawable.baseline_image_24)
            }
        } catch (e: Exception) {
            Log.e("GooglePhotos", "Error accessing Google Photos: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun getItemCount() = driveItems.size

    inner class DriveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val textViewTopic: TextView = itemView.findViewById(R.id.tvDriveTopic)
        val imageView: ImageView = itemView.findViewById(R.id.ivDriveImage)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(driveItems[position])
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(driveItem: DriveItem)
    }

    companion object {
        const val TAG = "DriveAdapter"
    }
}