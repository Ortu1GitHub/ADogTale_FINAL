package com.pmdm.adogtale.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pmdm.adogtale.R
import com.pmdm.adogtale.model.Profile
import com.pmdm.adogtale.model.User

class BuddyProfileActivity : AppCompatActivity() {
    private var backBtn: ImageButton? = null
    private lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buddy_profile)
        val btnNext: Button = findViewById(R.id.btnNext)
        val name: EditText = findViewById(R.id.etName)
        val age: EditText = findViewById(R.id.etAge)
        backBtn = findViewById(R.id.back_btn)
        val something: EditText = findViewById(R.id.etSomething)
        val shortDescription: EditText = findViewById(R.id.etShortDescription)

        user = intent.getSerializableExtra("user") as User

        // Gender
        val gender = resources.getStringArray(R.array.gender)
        val arrayGender = ArrayAdapter(this, R.layout.dropdown_menu, gender)
        val acGender: AutoCompleteTextView = findViewById(R.id.acGender)
        acGender.setAdapter(arrayGender)

        // Breed
        val breed = resources.getStringArray(R.array.breed)
        val arrayBreed = ArrayAdapter(this, R.layout.dropdown_menu, breed)
        val acBreed: AutoCompleteTextView = findViewById(R.id.acBreed)
        acBreed.setAdapter(arrayBreed)

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }

        btnNext.setOnClickListener() {

            val nameStr = name.text.toString().trim()
            val ageStr = age.text.toString().trim()
            val selectedGenderStr = acGender.text.toString().trim()
            val selectedBreedStr = acBreed.text.toString().trim()
            val somethingStr = something.text.toString().trim()
            val shortDescriptionStr = shortDescription.text.toString().trim()
            val userEmail = user.email
            if (nameStr.isNotEmpty() && ageStr.isNotEmpty() && selectedBreedStr.isNotEmpty() && selectedBreedStr.isNotEmpty() && somethingStr.isNotEmpty() && shortDescriptionStr.isNotEmpty()) {
                val profile =
                    Profile(
                        name = nameStr,
                        age = ageStr,
                        gender = selectedGenderStr,
                        breed = selectedBreedStr,
                        something = somethingStr,
                        shortDescription = shortDescriptionStr,
                        userEmail = userEmail
                    )
                val intent = Intent(this, BuddyProfileActivity2::class.java)
                intent.putExtra("profile", profile)
                intent.putExtra("user", user)
                startActivity(intent)
            } else {
                Toast.makeText(this,"Please fill the fields",Toast.LENGTH_SHORT).show()
            }
        }
    }
}

