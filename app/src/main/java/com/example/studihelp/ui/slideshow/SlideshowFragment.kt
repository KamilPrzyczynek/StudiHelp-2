package com.example.studihelp.ui.slideshow

import NotesAdapter
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SlideshowFragment : Fragment(), NotesAdapter.OnNoteClickListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private var notesList: MutableList<Note> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_slideshow, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        notesRecyclerView = view.findViewById(R.id.notesRecyclerView)
        databaseReference = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        fetchNotesFromDatabase()
        val fabAddNote = view.findViewById<FloatingActionButton>(R.id.fabAddNote)
        fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
        return view
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(notesList, this)
        val layoutManager = GridLayoutManager(requireContext(), 3)
        notesRecyclerView.layoutManager = layoutManager
        notesRecyclerView.adapter = notesAdapter
    }


    private fun fetchNotesFromDatabase() {
        val currentUserUsername = sharedPreferences.getString("username", "")

        currentUserUsername?.let { username ->
            val notesRef = databaseReference.child("users").child(username).child("notes")

            notesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notes = mutableListOf<Note>()

                    for (noteSnapshot in snapshot.children) {
                        val title = noteSnapshot.child("title").getValue(String::class.java)
                        val content = noteSnapshot.child("content").getValue(String::class.java)
                        val timestamp = noteSnapshot.child("timestamp").getValue(Long::class.java)
                        val isPinned = noteSnapshot.child("isPinned").getValue(Boolean::class.java)

                        title?.let { t ->
                            content?.let { c ->
                                timestamp?.let { ts ->
                                    isPinned?.let { p ->
                                        val note = Note(t, c, ts, p, noteSnapshot.key!!)
                                        notes.add(note)
                                    }
                                }
                            }
                        }
                    }


                    notes.sortWith(compareBy({ !it.isPinned }, { -it.timestamp }))

                    notesList.clear()
                    notesList.addAll(notes)
                    notesAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to read notes.", error.toException())
                    Toast.makeText(requireContext(), "Failed to read notes.", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }


    override fun onNoteClick(note: Note) {
        showNoteOptionsDialog(note)
    }

    private fun showNoteOptionsDialog(note: Note) {
        val options = arrayOf("Edit", "Delete")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditNoteDialog(note)
                    1 -> showDeleteNoteDialog(note)
                }
            }
        builder.show()
    }

    private fun showEditNoteDialog(note: Note) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_note, null)
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val editTextContent = dialogView.findViewById<EditText>(R.id.editTextContent)
        val switchPin = dialogView.findViewById<Switch>(R.id.switchPin)

        editTextTitle.setText(note.title)
        editTextContent.setText(note.content)
        switchPin.isChecked = note.isPinned

        builder.setView(dialogView)
            .setTitle("Edit Note")
            .setPositiveButton("Save") { dialog, _ ->
                val newTitle = editTextTitle.text.toString().trim()
                val newContent = editTextContent.text.toString().trim()
                val newIsPinned = switchPin.isChecked
                updateNoteInDatabase(note, newTitle, newContent, newIsPinned)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showDeleteNoteDialog(note: Note) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteNoteFromDatabase(note)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun updateNoteInDatabase(
        note: Note,
        newTitle: String,
        newContent: String,
        newIsPinned: Boolean
    ) {
        val currentUserUsername = sharedPreferences.getString("username", "")

        currentUserUsername?.let { username ->
            val noteRef =
                databaseReference.child("users").child(username).child("notes").child(note.key)
            noteRef.child("title").setValue(newTitle)
            noteRef.child("content").setValue(newContent)
            noteRef.child("isPinned").setValue(newIsPinned)
            noteRef.child("timestamp").setValue(System.currentTimeMillis())
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Note updated successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update note.", e)
                    Toast.makeText(requireContext(), "Failed to update note.", Toast.LENGTH_SHORT)
                        .show()
                }
        }
        notesAdapter.notifyDataSetChanged()
    }


    private fun deleteNoteFromDatabase(note: Note) {
        val currentUserUsername = sharedPreferences.getString("username", "")

        currentUserUsername?.let { username ->
            val noteRef =
                databaseReference.child("users").child(username).child("notes").child(note.key)
            noteRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Note deleted successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to delete note.", e)
                    Toast.makeText(requireContext(), "Failed to delete note.", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun showAddNoteDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_note, null)
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val editTextContent = dialogView.findViewById<EditText>(R.id.editTextContent)
        val switchPin = dialogView.findViewById<Switch>(R.id.switchPin)

        builder.setView(dialogView)
            .setTitle("Add New Note")
            .setPositiveButton("Add") { dialog, _ ->
                val title = editTextTitle.text.toString().trim()
                val content = editTextContent.text.toString().trim()
                val isPinned = switchPin.isChecked

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    saveNoteToDatabase(title, content, isPinned)
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please enter both title and content.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun saveNoteToDatabase(title: String, content: String, isPinned: Boolean) {
        val currentUserUsername = sharedPreferences.getString("username", "")

        currentUserUsername?.let { username ->
            val noteData = mapOf(
                "title" to title,
                "content" to content,
                "timestamp" to System.currentTimeMillis(),
                "isPinned" to isPinned
            )
            databaseReference.child("users").child(username).child("notes").push()
                .setValue(noteData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Note added successfully.", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add note.", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    companion object {
        private const val TAG = "SlideshowFragment"
    }
}