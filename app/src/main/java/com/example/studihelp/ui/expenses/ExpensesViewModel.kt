package com.example.studihelp.ui.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExpensesViewModel : ViewModel() {

    private val _expensesLiveData = MutableLiveData<List<Expense>>()
    val expensesLiveData: LiveData<List<Expense>> = _expensesLiveData

    fun setExpenses(expenses: List<Expense>) {
        _expensesLiveData.value = expenses
    }
}
