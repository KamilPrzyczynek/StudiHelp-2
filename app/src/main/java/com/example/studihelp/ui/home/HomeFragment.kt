package com.example.studihelp.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.example.studihelp.R.id.nav_home
import com.example.studihelp.R.id.nav_task
import com.example.studihelp.databinding.FragmentHomeBinding
import com.example.studihelp.ui.Task.Task
import com.example.studihelp.ui.Task.TaskAdapter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(), TaskAdapter.OnTaskClickListener {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var noTaskTextView: TextView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        sharedPreferences = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        val currentUserUsername = sharedPreferences.getString("username", "") ?: ""

        taskRecyclerView = binding.taskRecyclerView
        taskAdapter = TaskAdapter(requireContext(), this)
        taskRecyclerView.adapter = taskAdapter
        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        noTaskTextView = binding.textHome

        loadTasks(currentUserUsername)

        return root
    }

    private fun loadTasks(username: String) {
        val tasksRef = database.child("users").child(username).child("Task")

        tasksRef.orderByChild("datetime")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task>()
                    val currentDate = Date()

                    for (taskSnapshot in snapshot.children) {
                        val title = taskSnapshot.child("title").getValue(String::class.java)
                        val description = taskSnapshot.child("description").getValue(String::class.java)
                        val datetimeString = taskSnapshot.child("datetime").getValue(String::class.java)

                        if (!datetimeString.isNullOrEmpty()) {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val datetime = dateFormat.parse(datetimeString)

                            if (datetime != null && datetime.after(currentDate)) {
                                title?.let { t ->
                                    description?.let { d ->
                                        val task = Task(t, d, datetimeString, taskSnapshot.key!!)
                                        tasks.add(task)
                                    }
                                }
                            }
                        }
                    }

                    if (tasks.isEmpty()) {

                        noTaskTextView.visibility = View.VISIBLE
                        noTaskTextView.text = "No task"
                    } else {
                        taskAdapter.setTasks(tasks)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTaskClick(task: Task) {
        val bundle = Bundle().apply {
            putString("taskId", task.id)
        }

    }






}
