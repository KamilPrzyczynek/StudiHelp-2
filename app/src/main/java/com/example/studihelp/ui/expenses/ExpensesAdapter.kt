package com.example.studihelp.ui.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
class ExpensesAdapter(
    private val onItemClicked: (Expense) -> Unit
) : ListAdapter<Expense, ExpensesAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expenses_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentExpense = getItem(position)
        holder.bind(currentExpense)
        holder.itemView.setOnClickListener {
            onItemClicked(currentExpense)
        }
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val expenseNameTextView: TextView = itemView.findViewById(R.id.expensesNameTextView)
        private val expenseAmountTextView: TextView = itemView.findViewById(R.id.expensesAmountTextView)

        fun bind(expense: Expense) {
            expenseNameTextView.text = expense.name
            expenseAmountTextView.text = expense.amount.toString()
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.name == newItem.name && oldItem.amount == newItem.amount
        }
    }
}