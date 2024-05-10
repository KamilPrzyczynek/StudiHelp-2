package com.example.studihelp.ui.expenses

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ExpensesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragmentList: MutableList<Fragment> = mutableListOf()

    fun addFragment(fragment: Fragment) {
        fragmentList.add(fragment)
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}
