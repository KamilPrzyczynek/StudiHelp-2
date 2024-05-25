package com.example.studihelp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studihelp.MainActivity2
import com.example.studihelp.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditProfilActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editUsername: EditText
    private lateinit var editPassword: EditText
    private lateinit var saveButton: Button
    private lateinit var backtohomeText: TextView
    private lateinit var reference: DatabaseReference

    private var nameUser: String? = null
    private var emailUser: String? = null
    private var usernameUser: String? = null
    private var passwordUser: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profil)

        reference = FirebaseDatabase.getInstance().getReference("users")
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editUsername = findViewById(R.id.editUsername)
        editPassword = findViewById(R.id.editPassword)
        saveButton = findViewById(R.id.saveButton)
        backtohomeText = findViewById(R.id.backtohomeText)

        showData()

        backtohomeText.setOnClickListener {
            val intent = Intent(this@EditProfilActivity, MainActivity2::class.java)
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            if (isNameChanged() || isEmailChanged() || isPasswordChanged()) {
                Toast.makeText(this@EditProfilActivity, "Saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@EditProfilActivity, "No Changes Found", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun isNameChanged(): Boolean {
        return if (nameUser != editName.text.toString()) {
            reference.child(usernameUser!!).child("name")
                .setValue(editName.text.toString())
            nameUser = editName.text.toString()
            true
        } else {
            false
        }
    }

    private fun isEmailChanged(): Boolean {
        return if (emailUser != editEmail.text.toString()) {
            reference.child(usernameUser!!).child("email")
                .setValue(editEmail.text.toString())
            emailUser = editEmail.text.toString()
            true
        } else {
            false
        }
    }

    private fun isPasswordChanged(): Boolean {
        return if (passwordUser != editPassword.text.toString()) {
            reference.child(usernameUser!!).child("password")
                .setValue(editPassword.text.toString())
            passwordUser = editPassword.text.toString()
            true
        } else {
            false
        }
    }

    private fun showData() {
        val intent = intent
        nameUser = intent.getStringExtra("name")
        emailUser = intent.getStringExtra("email")
        usernameUser = intent.getStringExtra("username")
        passwordUser = intent.getStringExtra("password")

        editName.setText(nameUser)
        editEmail.setText(emailUser)
        editUsername.setText(usernameUser)
        editPassword.setText(passwordUser)
    }
}
