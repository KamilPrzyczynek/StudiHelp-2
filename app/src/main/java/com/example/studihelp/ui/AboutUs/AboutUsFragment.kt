package com.example.studihelp.ui.AboutUs

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R

class AboutUsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AboutUsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about_us, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = AboutUsAdapter(getSampleAboutUsInfoList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return view
    }

    private fun getSampleAboutUsInfoList(): List<AboutUsInfo> {
        return listOf(
            AboutUsInfo("Kamil Przyczynek", "Student", "Nr Albumu:36784"),
            AboutUsInfo("Piotr Ludwa", "Student", "Nr albumu:36391") ,
            AboutUsInfo("Max Rudnik", "Student", "Nr albumu:36406")
        )
    }
}