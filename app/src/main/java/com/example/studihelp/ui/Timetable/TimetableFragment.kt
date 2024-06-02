package com.example.studihelp.ui.Timetable

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.content.DialogInterface

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
                Log.e("TimetableFragment", "Error fetching data from Firebase: ${error.message}")
            }
        })
    }

    private fun showAddTimetableDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_timetable, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Timetable Item")
            .setPositiveButton("Add") { dialog, _ -> // Do not dismiss dialog here
                // This will be overridden below to ensure fields are filled
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditTextTimetable)
            val roomEditText = dialogView.findViewById<EditText>(R.id.roomEditTextTimetable)
            val startTimeButton = dialogView.findViewById<Button>(R.id.startTimeButtonTimetable)
            val endTimeButton = dialogView.findViewById<Button>(R.id.endTimeButtonTimetable)
            val dayOfWeekButton = dialogView.findViewById<Button>(R.id.dayOfWeekButton)
            val colorButton = dialogView.findViewById<Button>(R.id.colorButton)

            val name = nameEditText.text.toString()
            val room = roomEditText.text.toString()
            val startTime = startTimeButton.text.toString()
            val endTime = endTimeButton.text.toString()
            val dayOfWeek = dayOfWeekButton.text.toString()
            val color = colorButton.text.toString()

            if (name.isEmpty() || room.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || dayOfWeek.isEmpty() || color.isEmpty()) {
                // Show error message if any field is empty
                // You can customize this message as needed
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // All fields are filled, proceed with adding timetable item
                val username = sharedPreferences.getString("username", null)
                val taskRef = database.child("users").child(username!!).child("Timetable").push()
                val timetableItem = TimetableItem(taskRef.key!!, name, room, startTime, endTime, dayOfWeek, color)

                taskRef.setValue(timetableItem)
                alertDialog.dismiss() // Dismiss dialog only if all fields are filled
            }
        }

        val dayOfWeekButton = dialogView.findViewById<Button>(R.id.dayOfWeekButton)
        dayOfWeekButton.setOnClickListener {
            showDayPickerDialog(dayOfWeekButton)
        }

        val colorButton = dialogView.findViewById<Button>(R.id.colorButton)
        colorButton.setOnClickListener {
            showColorPickerDialog(colorButton)
        }

        val startTimeButton = dialogView.findViewById<Button>(R.id.startTimeButtonTimetable)
        startTimeButton.setOnClickListener {
            showTimePickerDialog(startTimeButton)
        }

        val endTimeButton = dialogView.findViewById<Button>(R.id.endTimeButtonTimetable)
        endTimeButton.setOnClickListener {
            showTimePickerDialog(endTimeButton)
        }
    }



    private fun showOptionsDialog(options: Array<String>, onOptionSelected: (String) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Choose an option")
            .setItems(options) { _, which ->
                onOptionSelected(options[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun showDayPickerDialog(dayOfWeekButton: Button) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysOfWeek = resources.getStringArray(R.array.days_of_week_array)

        val dayPickerDialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Day of Week")
            .setSingleChoiceItems(daysOfWeek, dayOfWeek - 1) { dialog, which ->
                dayOfWeekButton.text = daysOfWeek[which]
                dialog.dismiss()
            }
            .create()

        dayPickerDialog.show()
    }

    private fun showColorPickerDialog(colorButton: Button) {
        val colors = resources.getStringArray(R.array.color_array)

        val colorPickerDialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Color")
            .setSingleChoiceItems(colors, -1) { dialog, which ->
                colorButton.text = colors[which]
                dialog.dismiss()
            }
            .create()

        colorPickerDialog.show()
    }
    private fun getColorName(color: Int): String {
        return when (color) {
            R.color.red -> "Red"
            R.color.blue -> "Blue"
            R.color.green -> "Green"
            else -> "Black"
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
        val dayOfWeekButtonEdit = dialogView.findViewById<Button>(R.id.dayOfWeekButtonEdit)
        val colorButtonEdit = dialogView.findViewById<Button>(R.id.colorButtonEdit)

        nameEditText.setText(item.name)
        roomEditText.setText(item.room)
        startTimeButton.text = item.startTime
        endTimeButton.text = item.endTime
        dayOfWeekButtonEdit.text = item.dayOfWeek
        colorButtonEdit.text = item.color

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Timetable Item")
            .setPositiveButton("Save") { dialog, _ ->
                val name = nameEditText.text.toString()
                val room = roomEditText.text.toString()
                val startTime = startTimeButton.text.toString()
                val endTime = endTimeButton.text.toString()

                val updatedItem = TimetableItem(item.id, name, room, startTime, endTime, dayOfWeekButtonEdit.text.toString(), colorButtonEdit.text.toString())


                updateTimetableItem(updatedItem)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()

        dayOfWeekButtonEdit.setOnClickListener {
            showDayPickerDialog(dayOfWeekButtonEdit)
        }

        colorButtonEdit.setOnClickListener {
            showColorPickerDialog(colorButtonEdit)
        }

        startTimeButton.setOnClickListener {
            showTimePickerDialog(startTimeButton)
        }

        endTimeButton.setOnClickListener {
            showTimePickerDialog(endTimeButton)
        }
    }


    private fun deleteTimetableItem(item: TimetableItem) {
        val username = sharedPreferences.getString("username", null)
        val taskRef = database.child("users").child(username!!).child("Timetable").child(item.id)
        taskRef.removeValue()
    }



    private fun updateTimetableItem(item: TimetableItem) {
        val username = sharedPreferences.getString("username", null)
        val taskRef = database.child("users").child(username!!).child("Timetable").child(item.id)
        taskRef.setValue(item)
    }




}
