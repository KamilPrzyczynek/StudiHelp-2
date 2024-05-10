package com.example.studihelp.ui.Task

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.example.studihelp.ui.Notification.NotificationHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class TasksFragment : Fragment(), TaskAdapter.OnTaskClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var addTaskButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task, container, false)
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
        sharedPreferences = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        recyclerView = view.findViewById(R.id.tasksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TaskAdapter(requireContext(), this)
        recyclerView.adapter = adapter

        addTaskButton = view.findViewById(R.id.btnAddTask) as FloatingActionButton

        addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }
        val taskId = arguments?.getString("taskId")
        loadTasks()
        return view
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val dialogTitleEditText = dialogView.findViewById<EditText>(R.id.etTaskTitle)
        val dialogDescriptionEditText = dialogView.findViewById<EditText>(R.id.etTaskDescription)
        val dialogDateEditText = dialogView.findViewById<EditText>(R.id.etTaskDate)
        val dialogTimeEditText = dialogView.findViewById<EditText>(R.id.etTaskTime)
        val datePickerButton = dialogView.findViewById<Button>(R.id.btnSelectDate)
        val timePickerButton = dialogView.findViewById<Button>(R.id.btnSelectTime)

        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView(dialogDateEditText, calendar)
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            updateTimeInView(dialogTimeEditText, calendar)
        }

        datePickerButton.setOnClickListener {
            DatePickerDialog(requireContext(), dateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        timePickerButton.setOnClickListener {
            TimePickerDialog(requireContext(), timeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), false).show()
        }

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Task")
            .setPositiveButton("Add") { dialog, _ ->
                val title = dialogTitleEditText.text.toString().trim()
                val description = dialogDescriptionEditText.text.toString().trim()
                val date = dialogDateEditText.text.toString().trim()
                val time = dialogTimeEditText.text.toString().trim()

                if (title.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
                    addTaskToDatabase(title, description, date, time)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please enter title, date, and time", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun updateDateInView(dateEditText: EditText, calendar: Calendar) {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        dateEditText.setText(sdf.format(calendar.time))
    }

    private fun updateTimeInView(timeEditText: EditText, calendar: Calendar) {
        val myFormat = "HH:mm"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        timeEditText.setText(sdf.format(calendar.time))
    }

    private fun addTaskToDatabase(title: String, description: String, date: String, time: String) {
        val currentUserUid = auth.currentUser?.uid
        val username = sharedPreferences.getString("username", null)
        val taskRef = database.child("users").child(username!!).child("Task").push()

        val task = HashMap<String, Any>()
        task["title"] = title
        task["description"] = description
        task["datetime"] = "$date $time"
        task["timestamp"] = ServerValue.TIMESTAMP

        taskRef.setValue(task)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add task", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTasks() {
        val currentUserUid = auth.currentUser?.uid
        val username = sharedPreferences.getString("username", null)
        val tasksRef = database.child("users").child(username!!).child("Task")

        tasksRef.orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task>()
                    for (taskSnapshot in snapshot.children) {
                        val title = taskSnapshot.child("title").getValue(String::class.java)
                        val description = taskSnapshot.child("description").getValue(String::class.java)
                        val datetime = taskSnapshot.child("datetime").getValue(String::class.java)

                        title?.let { t ->
                            description?.let { d ->
                                datetime?.let { dt ->
                                    val task = Task(t, d, dt, taskSnapshot.key!!)
                                    tasks.add(task)
                                }
                            }
                        }
                    }

                    if (tasks.isEmpty()) {
                        val notificationHelper = NotificationHelper(requireContext())
                        notificationHelper.createNotification("No tasks", "You don't have any tasks scheduled")
                    } else {
                        adapter.setTasks(tasks)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            })
    }






    override fun onTaskClick(task: Task) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose an option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditTaskDialog(task)
                    1 -> deleteTask(task)
                }
            }
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_task, null)
        val dialogTitleEditText = dialogView.findViewById<EditText>(R.id.etEditTaskTitle)
        val dialogDescriptionEditText = dialogView.findViewById<EditText>(R.id.etEditTaskDescription)
        val dialogDateEditText = dialogView.findViewById<EditText>(R.id.etEditTaskDate)
        val dialogTimeEditText = dialogView.findViewById<EditText>(R.id.etEditTaskTime)
        val datePickerButton = dialogView.findViewById<Button>(R.id.btnSelectEditDate)
        val timePickerButton = dialogView.findViewById<Button>(R.id.btnSelectEditTime)

        dialogTitleEditText.setText(task.title)
        dialogDescriptionEditText.setText(task.description)
        val dateTimeParts = task.date.split(" ")
        if (dateTimeParts.size == 2) {
            dialogDateEditText.setText(dateTimeParts[0])
            dialogTimeEditText.setText(dateTimeParts[1])
        }

        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView(dialogDateEditText, calendar)
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            updateTimeInView(dialogTimeEditText, calendar)
        }

        datePickerButton.setOnClickListener {
            DatePickerDialog(requireContext(), dateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        timePickerButton.setOnClickListener {
            TimePickerDialog(requireContext(), timeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), false).show()
        }

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Task")
            .setPositiveButton("Save") { dialog, _ ->
                val title = dialogTitleEditText.text.toString().trim()
                val description = dialogDescriptionEditText.text.toString().trim()
                val date = dialogDateEditText.text.toString().trim()
                val time = dialogTimeEditText.text.toString().trim()

                if (title.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()) {
                    updateTaskInDatabase(task.id, title, description, date, time)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please enter title, date, and time", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    private fun updateTaskInDatabase(taskId: String, title: String, description: String, date: String, time: String) {
        val currentUserUid = auth.currentUser?.uid
        val username = sharedPreferences.getString("username", null)
        val taskRef = database.child("users").child(username!!).child("Task").child(taskId)

        val task = HashMap<String, Any>()
        task["title"] = title
        task["description"] = description
        task["datetime"] = "$date $time"
        task["timestamp"] = ServerValue.TIMESTAMP

        taskRef.updateChildren(task)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTask(task: Task) {
        val currentUserUid = auth.currentUser?.uid
        val username = sharedPreferences.getString("username", null)
        val taskRef = database.child("users").child(username!!).child("Task").child(task.id)

        taskRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
    }
}
