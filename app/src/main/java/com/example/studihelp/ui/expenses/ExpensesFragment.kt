package com.example.studihelp.ui.expenses

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.studihelp.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpensesFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var expensesRecyclerView: RecyclerView
    private var currentUserUsername: String = ""
    private var expensesList: MutableList<Expense> = mutableListOf()
    private var isDataFetched = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expenses, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        viewPager = view.findViewById(R.id.viewPager)
        expensesRecyclerView = view.findViewById(R.id.expensesRecyclerView)
        expensesAdapter = ExpensesAdapter()
        expensesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val addExpenseButton: ImageButton = view.findViewById(R.id.addExpenseButton)
        addExpenseButton.setOnClickListener {
            showAddExpenseDialog()
        }

        currentUserUsername = sharedPreferences.getString("username", "") ?: ""
        Log.i("Expenses", "Current user: $currentUserUsername")

        tabLayout = view.findViewById(R.id.tabLayout)

        fetchExpensesFromDatabase()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isDataFetched) {
            setupTabLayout()
            Log.d(TAG, "Data has already been fetched, no need to fetch again.")
        }
        expensesRecyclerView.adapter = expensesAdapter
    }
    private val expensesPagerAdapter: ExpensesPagerAdapter by lazy {
        ExpensesPagerAdapter(this)
    }


    private fun fetchExpensesFromDatabase() {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val currentUserUsername = sharedPreferences.getString("username", "")

        currentUserUsername?.let { username ->
            val expensesRef = databaseReference.child("users").child(username).child("expenses")

            expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenseList = mutableListOf<Expense>()

                    for (expenseSnapshot in snapshot.children) {
                        val expenseName = expenseSnapshot.child("name").getValue(String::class.java)
                        expenseName?.let {
                            val expense = Expense(it, 0.0)
                            expenseList.add(expense)
                        }
                    }

                    Log.d(TAG, "Fetched ${expenseList.size} expenses from database")

                    expensesList.addAll(expenseList)
                    expensesAdapter.submitList(expensesList)

                    isDataFetched = true

                    viewPager.adapter = expensesPagerAdapter
                    setupTabLayout()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to read expenses.", error.toException())
                }
            })
        } ?: run {
            Log.e(TAG, "Current user username is null")
        }
    }


    private fun setupTabLayout() {
        if (expensesList.isNotEmpty()) {
            val tabNames = expensesList.map { it.name }
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = tabNames[position]
            }.attach()
        } else {
            Log.d(TAG, "expensesList is empty, cannot setup TabLayout")
        }
    }

    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_expense, null)
        val editTextExpenseName = dialogView.findViewById<EditText>(R.id.editTextExpenseName)

        builder.setView(dialogView)
            .setTitle("Add New Expense")
            .setPositiveButton("Add") { _, _ ->
                val expenseName = editTextExpenseName.text.toString().trim()
                if (expenseName.isNotEmpty()) {
                    saveExpenseToDatabase(expenseName)
                } else {
                    Toast.makeText(requireContext(), "Expense name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun saveExpenseToDatabase(expenseName: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val currentUserUsername = sharedPreferences.getString("username", "")

        if (!currentUserUsername.isNullOrEmpty()) {
            val expenseData = mapOf("name" to expenseName)
            databaseReference.child("users").child(currentUserUsername).child("expenses").push()
                .setValue(expenseData)
                .addOnSuccessListener {
                    Log.d(TAG, "Expense added to the database: $expenseName")
                    Toast.makeText(requireContext(), "Expense named \"$expenseName\" added.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add expense to the database: $expenseName", e)
                    Toast.makeText(requireContext(), "Failed to add expense.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e(TAG, "Current user username is null")
        }
    }

    companion object {
        private const val TAG = "ExpensesFragment"
    }
}
