package com.example.studihelp.ui.Drive

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studihelp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class DriveFragment : Fragment(), DriveAdapter.OnItemClickListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUserUsername: String
    private lateinit var driveAdapter: DriveAdapter
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_drive, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        currentUserUsername = sharedPreferences.getString("username", "") ?: ""

        databaseReference = FirebaseDatabase.getInstance().reference

        val addDriveButton = view.findViewById<FloatingActionButton>(R.id.addDriveButton)
        addDriveButton.setOnClickListener {
            showAddDriveDialog(imageUri)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.driveRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        driveAdapter = DriveAdapter(requireContext(), this)
        recyclerView.adapter = driveAdapter

        fetchDriveItemsFromDatabase()

        return view
    }

    private fun fetchDriveItemsFromDatabase() {
        val query = databaseReference.child("users").child(currentUserUsername).child("drive")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val driveItems = mutableListOf<DriveItem>()
                for (postSnapshot in snapshot.children) {
                    val driveItem = postSnapshot.getValue(DriveItem::class.java)
                    driveItem?.let { driveItems.add(it) }
                }
                driveAdapter.submitList(driveItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching drive items: ${error.message}")
            }
        })
    }

    private fun showAddDriveDialog(imageUri: Uri?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_drive, null)
        val topicEditText = dialogView.findViewById<EditText>(R.id.etDriveTopic)
        val ivSelectedImagePreview = dialogView.findViewById<ImageView>(R.id.ivSelectedImagePreview)

        if (imageUri != null) {
            ivSelectedImagePreview.visibility = View.VISIBLE
            ivSelectedImagePreview.setImageURI(imageUri)
        } else {
            ivSelectedImagePreview.visibility = View.GONE
        }

        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectImage)
        btnSelectImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, PICK_IMAGE_REQUEST)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Drive Item")
            .setPositiveButton("Add") { dialog, _ ->
                val topic = topicEditText.text.toString().trim()
                if (topic.isNotEmpty()) {
                    val username = sharedPreferences.getString("username", null)
                    if (username != null) {
                        val driveItemRef = databaseReference.child("users").child(username).child("drive").push()
                        val driveItem = DriveItem(driveItemRef.key!!, topic, imageUri.toString()) // Zapis adresu URL obrazu
                        driveItemRef.setValue(driveItem)
                        Toast.makeText(requireContext(), "Drive item added successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to get username", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter topic", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditDriveDialog(driveItem: DriveItem) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_drive, null)
        val topicEditText = dialogView.findViewById<EditText>(R.id.etDriveTopic)
        val ivSelectedImagePreview = dialogView.findViewById<ImageView>(R.id.ivSelectedImagePreview)

        topicEditText.setText(driveItem.topic)
        if (driveItem.imageUrl.isNotEmpty()) {
            ivSelectedImagePreview.visibility = View.VISIBLE
            ivSelectedImagePreview.setImageURI(Uri.parse(driveItem.imageUrl))
        } else {
            ivSelectedImagePreview.visibility = View.GONE
        }

        val btnSelectImage = dialogView.findViewById<Button>(R.id.btnSelectImage)
        btnSelectImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, PICK_IMAGE_REQUEST)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Drive Item")
            .setPositiveButton("Update") { dialog, _ ->
                val updatedTopic = topicEditText.text.toString().trim()
                if (updatedTopic.isNotEmpty()) {
                    val updatedDriveItem = driveItem.copy(topic = updatedTopic, imageUrl = imageUri.toString())
                    databaseReference.child("users").child(currentUserUsername).child("drive").child(driveItem.id).setValue(updatedDriveItem)
                    Toast.makeText(requireContext(), "Drive item updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter topic", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Delete") { dialog, _ ->
                databaseReference.child("users").child(currentUserUsername).child("drive").child(driveItem.id).removeValue()
                Toast.makeText(requireContext(), "Drive item deleted successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                // Przechowujemy wybrany obraz w zmiennej klasy
                this.imageUri = imageUri
                // Wyświetlamy podgląd obrazu w dialogu
                showAddDriveDialog(imageUri)
            }
        }
    }

    override fun onItemClick(driveItem: DriveItem) {
        // Obsługa kliknięcia na element listy
        showEditDriveDialog(driveItem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Rejestrujemy fragment jako odbiorcę rezultatów dla akcji wyboru obrazu
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(PICK_IMAGE_REQUEST, result.resultCode, result.data)
        }
    }

    companion object {
        private const val TAG = "DriveFragment"
        private const val PICK_IMAGE_REQUEST = 1
    }
}