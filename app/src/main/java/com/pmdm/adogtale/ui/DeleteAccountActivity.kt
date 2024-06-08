package com.pmdm.adogtale.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.pmdm.adogtale.R
import com.pmdm.adogtale.utils.DeleteMethods
import com.pmdm.adogtale.utils.FirebaseUtil

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var deleteBtn: Button
    private var backBtn: ImageButton? = null
    val firebaseUtil = FirebaseUtil()
    val deleteMethods = DeleteMethods()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        val txtEmail: TextView = findViewById(R.id.etUsername)
        val txtPassword: TextView = findViewById(R.id.etPassword)
        deleteBtn = findViewById(R.id.deleteBtn)
        backBtn = findViewById(R.id.back_btn)


        deleteBtn.setOnClickListener {
            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()
            deleteMethods.deleteEverythingOfAnUser(email,password, this)
            firebaseUtil.killActivity(this)
        }

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }
    }
}