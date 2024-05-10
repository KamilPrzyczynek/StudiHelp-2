// TimetableFragment.kt
package com.example.studihelp.ui.Timetable

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.ArrayAdapter


class TimetableFragment : Fragment(), TimetableAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var timetableAdapter: TimetableAdapter
    private lateinit var addButton: FloatingActionButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private val timetableList = mutableListOf<TimetableItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timetable2, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewtimetable)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        timetableAdapter = TimetableAdapter(timetableList, this)
        recyclerView.adapter = timetableAdapter

        sharedPreferences = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference

        fetchDataFromFirebase()

        addButton = view.findViewById(R.id.btnAddTimetable)
        addButton.setOnClickListener {
            showAddTimetableDialog()
        }

        return view
    }

    private fun fetchDataFromFirebase() {
        val username = sharedPreferences.getString("username", null)
        val taskRef = database.child("users").child(username!!).child("Timetable")
        taskRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                timetableList.clear()
                for (timetableSnapshot in snapshot.children) {
                    val timetableItem = timetableSnapshot.getValue(TimetableItem::class.java)
                    timetableItem?.let { timetableList.add(it) }
                }
                timetableAdapter.submitList(timetableList.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle Firebase data retrieval error
            }
        })
    }

    private fun showAddTimetableDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_timetable, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Timetable Item")
            .setPositiveButton("Add") { dialog, _ ->
                val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditTextTimetable)
                val roomEditText = dialogView.findViewById<EditText>(R.id.roomEditTextTimetable)
                val startTimeButton = dialogView.findViewById<Button>(R.id.startTimeButtonTimetable)
                val endTimeButton = dialogView.findViewById<Button>(R.id.endTimeButtonTimetable)

                val dayOfWeekSpinner = dialogView.findViewById<Spinner>(R.id.dayOfWeekSpinner)
                val dayOfWeekAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.days_of_week_array, android.R.layout.simple_spinner_item)
                dayOfWeekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dayOfWeekSpinner.adapter = dayOfWeekAdapter

                val colorSpinner = dialogView.findViewById<Spinner>(R.id.colorSpinner)
                val colorAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.color_array, android.R.layout.simple_spinner_item)
                colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                colorSpinner.adapter = colorAdapter

                val name = nameEditText.text.toString()
                val room = roomEditText.text.toString()
                val startTime = startTimeButton.text.toString()
                val endTime = endTimeButton.text.toString()
                val dayOfWeek = dayOfWeekSpinner.selectedItemPosition + 1 // +1 because the day of the week starts from 1
                val color = colorSpinner.selectedItem.toString()

                val username = sharedPreferences.getString("username", null)
                val taskRef = database.child("users").child(username!!).child("Timetable").push()
                val timetableItem = TimetableItem(taskRef.key!!, name, room, startTime, endTime, dayOfWeek, getColorInt(color))
                taskRef.setValue(timetableItem)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.show()

        // Handle selection of start time
        val startTimeButton = dialogView.findViewById<Button>(R.id.startTimeButtonTimetable)
        startTimeButton.setOnClickListener {
            showTimePickerDialog(startTimeButton)
        }

        // Handle selection of end time
        val endTimeButton = dialogView.findViewById<Button>(R.id.endTimeButtonTimetable)
        endTimeButton.setOnClickListener {
            showTimePickerDialog(endTimeButton)
        }
    }


    private fun getColorInt(colorName: String): Int {
        return when (colorName) {
            "Red" -> R.color.red
            "Blue" -> R.color.blue
            "Green" -> R.color.green
            else -> R.color.black // Default color if none of the above matches
        }
    }

    private fun showTimePickerDialog(button: Button) {
        val currentTime = System.currentTimeMillis()
        val hour = TimeUtil.getHour(currentTime)
        val minute = TimeUtil.getMinute(currentTime)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                button.text = TimeUtil.getTimeString(hourOfDay, minute)
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }




    override fun onItemClick(position: Int) {
        val item = timetableList[position]
        showEditDeleteDialog(item)
    }
    private fun showEditDeleteDialog(item: TimetableItem) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditDialog(item)
                    1 -> deleteTimetableItem(item)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun showEditDialog(item: TimetableItem) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_timetable, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditTextTimetableEdit)
        val roomEditText = dialogView.findViewById<EditText>(R.id.roomEditTextTimetableEdit)
        val startTimeButton = dialogView.findViewById<Button>(R.id.startTimeButtonTimetableEdit)
        val endTimeButton = dialogView.findViewById<Button>(R.id.endTimeButtonTimetableEdit)
        val dayOfWeekSpinner = dialogView.findViewById<Spinner>(R.id.dayOfWeekSpinnerEdit)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.dayOfWeekSpinnerEdit)

        // Populate dialog fields with current item values
        nameEditText.setText(item.name)
        roomEditText.setText(item.room)
        startTimeButton.text = item.startTime
        endTimeButton.text = item.endTime
        dayOfWeekSpinner.setSelection(item.dayOfWeek - 1) // Subtract 1 to match array index
        colorSpinner.setSelection(getColorIndex(item.color)) // Get index of color in array

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Timetable Item")
            .setPositiveButton("Save") { dialog, _ ->
                val name = nameEditText.text.toString()
                val room = roomEditText.text.toString()
                val startTime = startTimeButton.text.toString()
                val endTime = endTimeButton.text.toString()
                val dayOfWeek = dayOfWeekSpinner.selectedItemPosition + 1
                val color = colorSpinner.selectedItem.toString()

                val updatedItem = TimetableItem(item.id, name, room, startTime, endTime, dayOfWeek, getColorInt(color))
                updateTimetableItem(updatedItem)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteTimetableItem(item: TimetableItem) {
        val username = sharedPreferences.getString("username", null)
        val taskRef = database.child("users").child(username!!).child("Timetable").child(item.id)
        taskRef.removeValue()
    }

    private fun getColorIndex(colorName: Int): Int {
        val colorArray = resources.getStringArray(R.array.color_array)
        for ((index, color) in colorArray.withIndex()) {
            if (getColorInt(color) == colorName) {
                return index
            }
        }
        return -1
    }



    private fun updateTimetableItem(item: TimetableItem) {
        val username = sharedPreferences.getString("username", null)
        val taskRef = database.child("users").child(username!!).child("Timetable").child(item.id)
        taskRef.setValue(item)
    }




}
