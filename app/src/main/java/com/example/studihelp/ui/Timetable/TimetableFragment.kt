package com.example.studihelp.ui.Timetable

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.example.studihelp.ui.expenses.ExpensesFragment.Companion.TAG
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TimetableFragment : Fragment(), TimetableAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var timetableAdapter: TimetableAdapter
    private lateinit var addButton: FloatingActionButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private var timetableList: MutableList<TimetableItem> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timetable2, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewtimetable)
        val gridLayoutManager = GridLayoutManager(requireContext(), 10)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (timetableAdapter.getItemViewType(position) == TimetableAdapter.VIEW_TYPE_HEADER) 7 else 1
            }
        }
        recyclerView.layoutManager = gridLayoutManager
        timetableAdapter = TimetableAdapter(this)
        recyclerView.adapter = timetableAdapter

        sharedPreferences =
            requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
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
                val daysOfWeek = mutableSetOf<String>()
                for (timetableSnapshot in snapshot.children) {
                    val timetableItem = timetableSnapshot.getValue(TimetableItem::class.java)
                    timetableItem?.let {
                        timetableList.add(it)
                        daysOfWeek.add(it.dayOfWeek)
                    }
                }

                val sortedDaysOfWeek = daysOfWeek.sorted()
                val combinedList = mutableListOf<TimetableItem>()
                sortedDaysOfWeek.forEach { day ->
                    val itemsForDay = timetableList.filter { it.dayOfWeek == day }
                    val sortedItemsForDay = itemsForDay.sortedBy { timeToMinutes(it.startTime) }
                    combinedList.add(TimetableItem(dayOfWeek = day, isHeader = true))
                    combinedList.addAll(sortedItemsForDay)
                }
                timetableAdapter.submitList(combinedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TimetableFragment", "Error fetching data from Firebase: ${error.message}")
            }
        })
    }


    private fun showAddTimetableDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_timetable, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Timetable Item")
            .setPositiveButton("Add") { dialog, _ ->
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

                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val username = sharedPreferences.getString("username", null)
                val taskRef = database.child("users").child(username!!).child("Timetable").push()
                val timetableItem =
                    TimetableItem(taskRef.key!!, name, room, startTime, endTime, dayOfWeek, color)
                taskRef.setValue(timetableItem)
                alertDialog.dismiss()
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

    private fun showDayPickerDialog(dayOfWeekButton: Button) {
        val daysOfWeek =
            arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        val dayPickerDialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Day of Week")
            .setSingleChoiceItems(daysOfWeek, -1) { dialog, which ->
                dayOfWeekButton.text = daysOfWeek[which]
                dialog.dismiss()
            }
            .create()

        dayPickerDialog.show()
    }

    private fun showColorPickerDialog(colorButton: Button) {
        val colors = arrayOf("Red", "Blue", "Green", "Yellow", "Purple", "Orange")

        val colorPickerDialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Color")
            .setSingleChoiceItems(colors, -1) { dialog, which ->
                colorButton.text = colors[which]
                dialog.dismiss()
            }
            .create()

        colorPickerDialog.show()
    }

    private fun showTimePickerDialog(button: Button) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            button.text = time
        }, hour, minute, true).show()
    }

    override fun onItemClick(position: Int) {
        val item = timetableAdapter.currentList.getOrNull(position)
        if (item != null) {
            showEditDeleteDialog(item)
        } else {
            Log.e(TAG, "Invalid position clicked: $position")
        }
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
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_timetable, null)
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

                val updatedItem = TimetableItem(
                    item.id,
                    name,
                    room,
                    startTime,
                    endTime,
                    dayOfWeekButtonEdit.text.toString(),
                    colorButtonEdit.text.toString()
                )
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

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":").map { it.toInt() }
        return parts[0] * 60 + parts[1]
    }
}
