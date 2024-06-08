import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.example.studihelp.ui.slideshow.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesAdapter(
    private val notes: List<Note>,
    private val listener: OnNoteClickListener
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.textViewContent)
        private val timestampTextView: TextView = itemView.findViewById(R.id.textViewTimestamp)
        val imageViewPin: ImageView = itemView.findViewById(R.id.imageViewPin)

        init {
            itemView.setOnClickListener(this)
            imageViewPin.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = notes[position]
                    note.isPinned = !note.isPinned
                    notifyItemChanged(position)
                }
            }
        }

        fun bind(note: Note) {
            titleTextView.text = note.title
            contentTextView.text = note.content
            timestampTextView.text = formatTimestamp(note.timestamp)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val note = notes[position]
                listener.onNoteClick(note)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.bind(currentNote)

        if (currentNote.isPinned) {
            holder.imageViewPin.visibility = View.VISIBLE
        } else {
            holder.imageViewPin.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    interface OnNoteClickListener {
        fun onNoteClick(note: Note)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
}
