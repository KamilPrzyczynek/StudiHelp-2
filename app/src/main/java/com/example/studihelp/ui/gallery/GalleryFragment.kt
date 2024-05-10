package com.example.studihelp.ui.gallery

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GalleryFragment : Fragment(), OnHealthEntryClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HealthEntryAdapter
    private lateinit var addHealthButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        recyclerView = view.findViewById(R.id.healthEntriesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HealthEntryAdapter(requireContext(), this)
        recyclerView.adapter = adapter

        addHealthButton = view.findViewById(R.id.btnAddHealth)

        addHealthButton.setOnClickListener {
            showAddHealthDialog()
        }

        loadHealthEntries()

        return view
    }

    private fun showAddHealthDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_healty, null)
        val typeEditText = dialogView.findViewById<EditText>(R.id.healthTypeEditText)
        val durationEditText = dialogView.findViewById<EditText>(R.id.healthDurationEditText)
        val dateEditText = dialogView.findViewById<EditText>(R.id.healthDateEditText)
        val notesEditText = dialogView.findViewById<EditText>(R.id.healthNotesEditText)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Health Entry")
            .setPositiveButton("Add") { dialog, _ ->
                val type = typeEditText.text.toString().trim()
                val duration = durationEditText.text.toString().trim()
                val date = dateEditText.text.toString().trim()
                val notes = notesEditText.text.toString().trim() // Upewnij się, że pobierasz wartość notatek z pola EditText

                if (type.isNotEmpty() && duration.isNotEmpty() && date.isNotEmpty()) {
                    addHealthEntryToDatabase(type, duration, date, notes) // Przekazujesz notatki do funkcji addHealthEntryToDatabase
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please enter type, duration, and date", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }



    private fun addHealthEntryToDatabase(type: String, duration: String, date: String, notes: String) {
        val username = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).getString("username", null)

        val healthEntryRef = database.child("users").child(username!!).child("healthEntries").push()

        val healthEntry = mapOf(
            "type" to type,
            "duration" to duration,
            "date" to date,
            "notes" to notes
        )

        healthEntryRef.setValue(healthEntry)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Health entry added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add health entry", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadHealthEntries() {
        val username = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).getString("username", null)

        val healthEntriesRef = database.child("users").child(username!!).child("healthEntries")

        healthEntriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val healthEntries = mutableListOf<HealthEntry>()
                for (entrySnapshot in snapshot.children) {
                    val type = entrySnapshot.child("type").getValue(String::class.java)
                    val duration = entrySnapshot.child("duration").getValue(String::class.java)
                    val date = entrySnapshot.child("date").getValue(String::class.java)
                    val notes = entrySnapshot.child("notes").getValue(String::class.java)

                    type?.let { t ->
                        duration?.let { d ->
                            date?.let { dt ->
                                notes?.let { n ->
                                    val healthEntry = HealthEntry(t, d, dt, n)
                                    healthEntries.add(healthEntry)
                                }
                            }
                        }
                    }
                }

                adapter.setHealthEntries(healthEntries)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load health entries", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onHealthEntryClick(healthEntry: HealthEntry) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose an option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditHealthDialog(healthEntry)
                    1 -> deleteHealthEntry(healthEntry)
                }
            }
            .show()
    }

    private fun showEditHealthDialog(healthEntry: HealthEntry) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_healty, null)
        val typeEditText = dialogView.findViewById<EditText>(R.id.editHealthTypeEditText)
        val durationEditText = dialogView.findViewById<EditText>(R.id.editHealthDurationEditText)
        val dateEditText = dialogView.findViewById<EditText>(R.id.editHealthDateEditText)
        val notesEditText = dialogView.findViewById<EditText>(R.id.editHealthNotesEditText)

        typeEditText.setText(healthEntry.type)
        durationEditText.setText(healthEntry.duration)
        dateEditText.setText(healthEntry.date)
        notesEditText.setText(healthEntry.notes)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Health Entry")
            .setPositiveButton("Save") { dialog, _ ->
                val type = typeEditText.text.toString().trim()
                val duration = durationEditText.text.toString().trim()
                val date = dateEditText.text.toString().trim()
                val notes = notesEditText.text.toString().trim()

                if (type.isNotEmpty() && duration.isNotEmpty() && date.isNotEmpty()) {
                    // Aktualizuj wpis zdrowotny w bazie danych
                    updateHealthEntryInDatabase(healthEntry, type, duration, date, notes)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please enter type, duration, and date", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun updateHealthEntryInDatabase(
        healthEntry: HealthEntry,
        type: String,
        duration: String,
        date: String,
        notes: String
    ) {
        val username = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).getString("username", null)

        val healthEntryRef = database.child("users").child(username!!).child("healthEntries").child(healthEntry.id)

        val updatedHealthEntry = mapOf(
            "type" to type,
            "duration" to duration,
            "date" to date,
            "notes" to notes
        )

        healthEntryRef.updateChildren(updatedHealthEntry)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Health entry updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update health entry", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteHealthEntry(healthEntry: HealthEntry) {
        val username = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).getString("username", null)

        val healthEntryRef = database.child("users").child(username!!).child("healthEntries").child(healthEntry.id)

        healthEntryRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Health entry deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete health entry", Toast.LENGTH_SHORT).show()
            }
    }

}
