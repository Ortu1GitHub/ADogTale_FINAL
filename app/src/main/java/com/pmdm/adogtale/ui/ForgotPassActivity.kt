package com.pmdm.adogtale.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.pmdm.adogtale.AuthActivity
import com.pmdm.adogtale.R

class ForgotPassActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private var backBtn: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)
        val txtEmail: TextView = findViewById(R.id.etEmail)
        val btnRemember: Button = findViewById(R.id.btnRemember)
        backBtn = findViewById(R.id.back_btn)

        btnRemember.setOnClickListener() {
            sendPasswordReset(txtEmail.text.toString())
        }

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }
        firebaseAuth = Firebase.auth
    }

    private fun sendPasswordReset(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Mail sent", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AuthActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Mail not found", Toast.LENGTH_SHORT).show()
                }
            }
    }
}