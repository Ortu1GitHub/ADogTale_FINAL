package com.pmdm.adogtale.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.pmdm.adogtale.R
import com.pmdm.adogtale.checkout.CheckoutActivity
import com.pmdm.adogtale.utils.FirebaseUtil

class OptionsActivity : AppCompatActivity() {
    private lateinit var editProfileBtn: Button
    private lateinit var deleteAccountBtn: Button
    private lateinit var logoutBtn: Button
    private lateinit var checkoutButton: Button

    private var backBtn: ImageButton? = null
    val firebaseUtil = FirebaseUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        editProfileBtn = findViewById(R.id.editProfileBtn)
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn)
        logoutBtn = findViewById(R.id.logoutBtn)
        backBtn = findViewById(R.id.back_btn)
        checkoutButton = findViewById(R.id.checkoutButton)

        editProfileBtn.setOnClickListener {
            val intent = Intent(this, EditUserActivity::class.java)
            startActivity(intent)
        }

        deleteAccountBtn.setOnClickListener {
            val intent = Intent(this, DeleteAccountActivity::class.java)
            startActivity(intent)
        }

        checkoutButton.setOnClickListener {
            val intent = Intent(this, CheckoutActivity::class.java)
            startActivity(intent)
        }

        logoutBtn.setOnClickListener {
            Log.i("logoutBTN", "aqui")
            firebaseUtil.logout(this)
        }

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }
    }

}