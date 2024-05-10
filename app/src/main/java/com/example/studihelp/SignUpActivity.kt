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

class SignUpActivity : AppCompatActivity() {

    var signupName: EditText? = null
    var signupEmail:EditText? = null
    var signupUsername:EditText? = null
    var signupPassword:EditText? = null
    var loginRedirectText: TextView? = null
    var signupButton: Button? = null
    var database: FirebaseDatabase? = null
    var reference: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        signupName = findViewById<EditText>(R.id.signup_name)
        signupEmail = findViewById<EditText>(R.id.signup_email)
        signupUsername = findViewById<EditText>(R.id.signup_username)
        signupPassword = findViewById<EditText>(R.id.signup_password)
        signupButton = findViewById<Button>(R.id.signup_button)
        loginRedirectText = findViewById<TextView>(R.id.loginRedirectText)

        signupButton?.setOnClickListener(View.OnClickListener {
            database = FirebaseDatabase.getInstance()
            reference = database?.getReference("users")
            val name: String = signupName?.text.toString()
            val email: String = signupEmail?.text.toString()
            val username: String = signupUsername?.text.toString()
            val password: String = signupPassword?.text.toString()
            val helperClass = HelperClass(name, email, username, password)
            reference?.child(username)?.setValue(helperClass)
            Toast.makeText(this@SignUpActivity, "You have signup successfully!", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(
                this@SignUpActivity,
                LoginActivity::class.java
            )
            startActivity(intent)
        })

        loginRedirectText?.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@SignUpActivity,
                LoginActivity::class.java
            )
            startActivity(intent)
        })

    }
}