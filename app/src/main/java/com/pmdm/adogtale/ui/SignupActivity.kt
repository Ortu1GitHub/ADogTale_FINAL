package com.pmdm.adogtale.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pmdm.adogtale.R
import com.pmdm.adogtale.model.User

class SignUpActivity : AppCompatActivity() {
    private var backBtn: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val txtEmail: TextView = findViewById(R.id.etEmail)
        val txtPassword1: TextView = findViewById(R.id.etPassword)
        val txtPassword2: TextView = findViewById(R.id.etConfirmPassword)
        val username: TextView = findViewById(R.id.etUsername)
        val btnNext: Button = findViewById(R.id.btnNext)
        backBtn = findViewById(R.id.back_btn)

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }

        btnNext.setOnClickListener() {
            var pass1 = txtPassword1.text.toString().trim()
            var pass2 = txtPassword2.text.toString().trim()
            var email = txtEmail.text.toString().lowercase().trim()
            var username = username.text.toString().trim()

            if (pass1 == pass2 ) {
                if (email.isNotEmpty() && username.isNotEmpty() && pass1.isNotEmpty()) {
                    val user = User(username = username, email = email, password = pass1)
                    val intent = Intent(this, SignUpActivity2::class.java)
                    intent.putExtra("user", user)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Please, fill the fields", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                txtPassword2.requestFocus()
            }
        }
    }
}