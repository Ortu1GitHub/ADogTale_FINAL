package com.pmdm.adogtale.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pmdm.adogtale.R
import com.pmdm.adogtale.model.User

class SignUpActivity2 : AppCompatActivity() {
    private var backBtn: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up2)
        val name: TextView = findViewById(R.id.etName)
        val surname: TextView = findViewById(R.id.etSurname)
        val town: TextView = findViewById(R.id.etTown)
        val phone: TextView = findViewById(R.id.etPhone)
        val btnNext: Button = findViewById(R.id.btnNext)
        backBtn = findViewById(R.id.back_btn)
        val user = intent.getSerializableExtra("user") as User

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }

        btnNext.setOnClickListener() {
            val nameStr = name.text.toString()
            val surnameStr = surname.text.toString()
            val townStr = town.text.toString()
            val phoneStr = phone.text.toString()
            user.name = nameStr
            user.surname = surnameStr
            user.town = townStr
            user.phone = phoneStr
            val intent = Intent(this, BuddyProfileActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }
    }
}