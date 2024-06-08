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
import android.widget.TextView
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
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var totalExpensesTextView: TextView
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
        expensesRecyclerView = view.findViewById(R.id.expensesRecyclerView)
        totalExpensesTextView = view.findViewById(R.id.totalExpensesTextView)
        expensesAdapter = ExpensesAdapter { expense -> showEditDeleteExpenseDialog(expense) }
        expensesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val addExpenseButton: ImageButton = view.findViewById(R.id.addExpenseButton)
        addExpenseButton.setOnClickListener {
            showAddExpenseDialog()
        }

        currentUserUsername = sharedPreferences.getString("username", "") ?: ""
        Log.i("Expenses", "Current user: $currentUserUsername")

        fetchExpensesFromDatabase()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isDataFetched) {
            Log.d(TAG, "Data has already been fetched, no need to fetch again.")
        }
        expensesRecyclerView.adapter = expensesAdapter
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
                        val expenseAmount = expenseSnapshot.child("amount").getValue(Double::class.java) ?: 0.0
                        expenseName?.let {
                            val expense = Expense(it, expenseAmount)
                            expenseList.add(expense)
                        }
                    }

                    Log.d(TAG, "Fetched ${expenseList.size} expenses from database")

                    expensesList.clear()
                    expensesList.addAll(expenseList)
                    expensesAdapter.submitList(expenseList.toList())
                    updateTotalExpenses()

                    isDataFetched = true
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to read expenses.", error.toException())
                }
            })
        } ?: run {
            Log.e(TAG, "Current user username is null")
        }
    }


    private fun updateTotalExpenses() {
        val total = expensesList.sumByDouble { it.amount }
        totalExpensesTextView.text = "Total: $total"
    }

    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_expense, null)
        val editTextExpenseName = dialogView.findViewById<EditText>(R.id.editTextExpenseName)
        val editTextExpenseAmount = dialogView.findViewById<EditText>(R.id.editTextExpenseAmount)

        builder.setView(dialogView)
            .setTitle("Add New Expense")
            .setPositiveButton("Add") { _, _ ->
                val expenseName = editTextExpenseName.text.toString().trim()
                val expenseAmount = editTextExpenseAmount.text.toString().trim().toDoubleOrNull() ?: 0.0
                if (expenseName.isNotEmpty() && expenseAmount > 0.0) {
                    saveExpenseToDatabase(expenseName, expenseAmount)
                } else {
                    Toast.makeText(requireContext(), "Please enter valid expense details", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun saveExpenseToDatabase(expenseName: String, expenseAmount: Double) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val currentUserUsername = sharedPreferences.getString("username", "")

        if (!currentUserUsername.isNullOrEmpty()) {
            val expenseData = mapOf("name" to expenseName, "amount" to expenseAmount)
            databaseReference.child("users").child(currentUserUsername).child("expenses").push()
                .setValue(expenseData)
                .addOnSuccessListener {
                    Log.d(TAG, "Expense added to the database: $expenseName")
                    Toast.makeText(requireContext(), "Expense added.", Toast.LENGTH_SHORT).show()
                    fetchExpensesFromDatabase()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add expense to the database: $expenseName", e)
                    Toast.makeText(requireContext(), "Failed to add expense.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e(TAG, "Current user username is null")
        }
    }

    private fun showEditDeleteExpenseDialog(expense: Expense) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_delete_expense, null)
        val editTextExpenseName = dialogView.findViewById<EditText>(R.id.editTextExpenseName)
        val editTextExpenseAmount = dialogView.findViewById<EditText>(R.id.editTextExpenseAmount)
        editTextExpenseName.setText(expense.name)
        editTextExpenseAmount.setText(expense.amount.toString())

        builder.setView(dialogView)
            .setTitle("Edit or Delete Expense")
            .setPositiveButton("Update") { _, _ ->
                val expenseName = editTextExpenseName.text.toString().trim()
                val expenseAmount = editTextExpenseAmount.text.toString().trim().toDoubleOrNull() ?: 0.0
                if (expenseName.isNotEmpty() && expenseAmount > 0.0) {
                    updateExpenseInDatabase(expense, expenseName, expenseAmount)
                } else {
                    Toast.makeText(requireContext(), "Please enter valid expense details", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                deleteExpenseFromDatabase(expense)
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun updateExpenseInDatabase(expense: Expense, newName: String, newAmount: Double) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val currentUserUsername = sharedPreferences.getString("username", "")

        if (!currentUserUsername.isNullOrEmpty()) {
            val expenseQuery = databaseReference.child("users").child(currentUserUsername).child("expenses")
                .orderByChild("name").equalTo(expense.name)
            expenseQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (expenseSnapshot in snapshot.children) {
                        expenseSnapshot.ref.child("name").setValue(newName)
                        expenseSnapshot.ref.child("amount").setValue(newAmount)
                    }
                    Log.d(TAG, "Expense updated in the database: $newName")
                    fetchExpensesFromDatabase()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to update expense in the database: $newName", error.toException())
                    Toast.makeText(requireContext(), "Failed to update expense.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.e(TAG, "Current user username is null")
        }
    }

    private fun deleteExpenseFromDatabase(expense: Expense) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val currentUserUsername = sharedPreferences.getString("username", "")

        if (!currentUserUsername.isNullOrEmpty()) {
            val expenseQuery = databaseReference.child("users").child(currentUserUsername).child("expenses")
                .orderByChild("name").equalTo(expense.name)
            expenseQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (expenseSnapshot in snapshot.children) {
                        expenseSnapshot.ref.removeValue()
                    }
                    Log.d(TAG, "Expense deleted from the database: ${expense.name}")
                    fetchExpensesFromDatabase()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to delete expense from the database: ${expense.name}", error.toException())
                    Toast.makeText(requireContext(), "Failed to delete expense.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.e(TAG, "Current user username is null")
        }
    }

    companion object {
        const val TAG = "ExpensesFragment"
    }
}