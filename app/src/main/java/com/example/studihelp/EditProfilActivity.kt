package com.example.studihelp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditProfilActivity : AppCompatActivity() {


    var editName: EditText? = null
    var editEmail:EditText? = null
    var editUsername:EditText? = null
    var editPassword:EditText? = null
    var saveButton: Button? = null
    var nameUser: String? = null
    var emailUser:kotlin.String? = null
    var usernameUser:kotlin.String? = null
    var passwordUser:kotlin.String? = null
    var reference: DatabaseReference? = null
    private lateinit var backtohomeText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profil)
        reference = FirebaseDatabase.getInstance().getReference("users")
        editName = findViewById<EditText>(R.id.editName)
        editEmail = findViewById<EditText>(R.id.editEmail)
        editUsername = findViewById<EditText>(R.id.editUsername)
        editPassword = findViewById<EditText>(R.id.editPassword)
        saveButton = findViewById<Button>(R.id.saveButton)
        backtohomeText = findViewById(R.id.backtohomeText)


        showData()
        backtohomeText.setOnClickListener {
            val intent = Intent(this@EditProfilActivity, MainActivity2::class.java)
            startActivity(intent)
        }

        saveButton?.setOnClickListener(View.OnClickListener {
            if (isNameChanged() || isEmailChanged() || isPasswordChanged()) {
                Toast.makeText(this@EditProfilActivity, "Saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@EditProfilActivity, "No Changes Found", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    fun isNameChanged(): Boolean {
        return if (editName != null && nameUser != editName!!.text.toString()) {
            reference!!.child(usernameUser!!).child("name")
                .setValue(editName!!.text.toString())
            nameUser = editName!!.text.toString()
            true
        } else {
            false
        }
    }


    fun isEmailChanged(): Boolean {
        return if (emailUser != editName!!.getText().toString()) {
            reference!!.child(usernameUser!!).child("email")
                .setValue(editEmail!!.getText().toString())
            emailUser = editEmail!!.getText().toString()
            true
        } else {
            false
        }
    }

    fun isPasswordChanged(): Boolean {
        return if (passwordUser != editPassword!!.getText().toString()) {
            reference!!.child(usernameUser!!).child("password")
                .setValue(editPassword!!.getText().toString())
            passwordUser = editPassword!!.getText().toString()
            true
        } else {
            false
        }
    }

    fun showData() {
        val intent = intent
        nameUser = intent.getStringExtra("name")
        emailUser = intent.getStringExtra("email")
        usernameUser = intent.getStringExtra("username")
        passwordUser = intent.getStringExtra("password")
        editName!!.setText(nameUser)
        editEmail!!.setText(emailUser)
        editUsername!!.setText(usernameUser)
        editPassword!!.setText(passwordUser)
    }
}