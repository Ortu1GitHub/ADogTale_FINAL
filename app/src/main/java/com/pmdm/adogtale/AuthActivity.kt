package com.pmdm.adogtale

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.pmdm.adogtale.ui.CardSwipeActivity
import com.pmdm.adogtale.ui.ForgotPassActivity
import com.pmdm.adogtale.ui.SignUpActivity
import com.pmdm.adogtale.ui.statusbar.StatusbarColorHandler

class AuthActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private val statusbarColorHandler: StatusbarColorHandler = StatusbarColorHandler()
    private val preferences_file = "preferencias"
    private val USER_KEY = "USUARIO"
    lateinit var txtEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusbarColorHandler.setStatusbarBackgroundColor(
            this,
            resources.getColor(R.color.backgroundGreen)
        )
        setContentView(R.layout.activity_auth)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        txtEmail = findViewById(R.id.etUsername)
        val txtPassword: TextView = findViewById(R.id.etPassword)
        val btnSingup: Button = findViewById(R.id.btnSignUp)
        val btnRemember: Button = findViewById(R.id.btnRemember)
        val cbRemember: CheckBox = findViewById(R.id.cbRemember)
        firebaseAuth = Firebase.auth


        btnLogin.setOnClickListener() {
            signIn(
                txtEmail.text.toString().trim().lowercase(),
                txtPassword.text.toString().trim(),
                cbRemember.isChecked
            )
        }
        btnSingup.setOnClickListener() {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        btnRemember.setOnClickListener() {
            val intent = Intent(this, ForgotPassActivity::class.java)
            startActivity((intent))
        }

        loadUserName()
    }

    // Login function
    private fun signIn(email: String, password: String, remember: Boolean) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(
                        baseContext,
                        "Logged successfully in as: " + user?.email.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    if (remember) {
                        saveUserName(user?.email.toString())
                    } else {
                        saveUserName(null)
                    }
                    val intent = Intent(this, CardSwipeActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(baseContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserName(user: String?) {
        val sharedPreferences = getSharedPreferences(preferences_file, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USER_KEY, user)
        editor.apply()
    }

    private fun loadUserName() {
        val sharedPreferences = getSharedPreferences(preferences_file, MODE_PRIVATE)
        val user = sharedPreferences.getString(USER_KEY, null)
        if (user != null) {
            txtEmail.setText(user)
        }
    }
}
