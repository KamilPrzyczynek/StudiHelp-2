package com.example.studihelp.ui.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.databinding.FragmentHomeBinding
import com.example.studihelp.ui.Task.Task
import com.example.studihelp.ui.Task.TaskAdapter
import com.example.studihelp.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
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
    private lateinit var nextLessonTextView: TextView

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
        sharedPreferences =
            requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        val currentUserUsername = sharedPreferences.getString("username", "") ?: ""

        taskRecyclerView = binding.taskRecyclerView
        taskAdapter = TaskAdapter(requireContext(), this)
        taskRecyclerView.adapter = taskAdapter
        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        noTaskTextView = binding.noTask
        nextLessonTextView = binding.nextLesson

        loadTasks(currentUserUsername)
        loadNextLesson(currentUserUsername)

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
                        val description =
                            taskSnapshot.child("description").getValue(String::class.java)
                        val datetimeString =
                            taskSnapshot.child("datetime").getValue(String::class.java)

                        if (!datetimeString.isNullOrEmpty()) {
                            val dateFormat =
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
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
                        noTaskTextView.text = "No tasks"
                    } else {
                        taskAdapter.setTasks(tasks)
                        noTaskTextView.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Failed to load tasks", error.toException())
                }
            })
    }

    private fun loadNextLesson(username: String) {
        val lessonsRef = database.child("users").child(username).child("Timetable")

        lessonsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentDate = Calendar.getInstance()
                var nextLesson: String? = null
                var nextLessonDate: Date? = null

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                for (lessonSnapshot in snapshot.children) {
                    val lessonName = lessonSnapshot.child("name").getValue(String::class.java)
                    val room = lessonSnapshot.child("room").getValue(String::class.java)
                    val dayOfWeek = lessonSnapshot.child("dayOfWeekIndex").getValue(Int::class.java)
                    val startTime = lessonSnapshot.child("startTime").getValue(String::class.java)
                    val endTime = lessonSnapshot.child("endTime").getValue(String::class.java)

                    Log.d(
                        "HomeFragment",
                        "Checking lesson: $lessonName on day $dayOfWeek at $startTime"
                    )

                    if (lessonName != null && room != null && dayOfWeek != null && startTime != null && endTime != null) {
                        val lessonDate = getNextLessonDate(currentDate, dayOfWeek, startTime)

                        Log.d(
                            "HomeFragment",
                            "Parsed lesson date: ${dateFormat.format(lessonDate)}"
                        )

                        if (lessonDate != null && (nextLessonDate == null || lessonDate.before(
                                nextLessonDate
                            ))
                        ) {
                            nextLessonDate = lessonDate
                            nextLesson = "$lessonName at $startTime-$endTime in room $room"
                        }
                    } else {
                        Log.w(
                            "HomeFragment",
                            "Incomplete lesson data: name=$lessonName, room=$room, dayOfWeek=$dayOfWeek, startTime=$startTime, endTime=$endTime"
                        )
                    }
                }

                Log.d("HomeFragment", "Next lesson: $nextLesson")

                nextLessonTextView.text = nextLesson ?: "No lessons"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Failed to load next lesson", error.toException())
            }
        })
    }


    private fun getNextLessonDate(currentDate: Calendar, dayOfWeek: Int, startTime: String): Date? {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = dateFormat.parse(startTime)
        val lessonDate = Calendar.getInstance()
        lessonDate.time = currentDate.time

        lessonDate.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1)
        lessonDate.set(Calendar.HOUR_OF_DAY, time.hours)
        lessonDate.set(Calendar.MINUTE, time.minutes)
        lessonDate.set(Calendar.SECOND, 0)
        lessonDate.set(Calendar.MILLISECOND, 0)

        Log.d("HomeFragment", "Calculated lesson date: ${lessonDate.time}")

        if (lessonDate.before(currentDate)) {
            lessonDate.add(Calendar.WEEK_OF_YEAR, 1)
            Log.d("HomeFragment", "Lesson moved to next week: ${lessonDate.time}")
        }

        return lessonDate.time
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTaskClick(task: Task) {
        val bundle = Bundle().apply {
            putString("taskId", task.id)
        }
        //findNavController().navigate(R.id.nav_task, bundle)

    }
}
