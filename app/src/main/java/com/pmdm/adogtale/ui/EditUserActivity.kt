package com.pmdm.adogtale.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.pmdm.adogtale.R
import com.pmdm.adogtale.controller.ProfileActions
import com.pmdm.adogtale.model.User
import com.pmdm.adogtale.utils.UserMethods
import com.pmdm.adogtale.utils.FirebaseUtil

class EditUserActivity : AppCompatActivity() {
    private val userMethods = UserMethods()
    private val firebaseUtil = FirebaseUtil()
    private var user: User? = null
    private var fUser: FirebaseUser? = null
    private var checkPass = false
    private var saveBtn: Button? = null
    private var backBtn: ImageButton? = null
    private var password: EditText? = null
    private var confirmPassword: EditText? = null
    private var name: EditText? = null
    private var username: EditText? = null
    private var surname: EditText? = null
    private var phone: EditText? = null
    private var town: EditText? = null
    private val profileActions: ProfileActions = ProfileActions()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        firebaseUtil.getCurrentUser { currentUser ->
            user = currentUser
            Log.i("editUserActivity user", user?.surname.toString())
            setUserDataOnView(user!!)
        }

        fUser = firebaseUtil.getCurrentFirebaseUser()

        saveBtn = findViewById(R.id.saveBtn)
        backBtn = findViewById(R.id.back_btn)
        password = findViewById(R.id.etPassword)
        confirmPassword = findViewById(R.id.etConfirmPassword)
        name = findViewById(R.id.etName)
        username = findViewById(R.id.etUsername)
        surname = findViewById(R.id.etSurname)
        phone = findViewById(R.id.etPhone)
        town = findViewById(R.id.etTown)

        saveBtn?.setOnClickListener {
            user = setUser(name, username, surname, phone, town)
            checkPassword(password, confirmPassword)
            if (checkPass) {
                userMethods.updateUserAccount(user!!, password!!.text.toString(), this)
                profileActions.updateProfileTown(user!!.town) {}
            }
            val intent = Intent(this, OptionsActivity::class.java)
            startActivity(intent)
        }
        backBtn?.setOnClickListener { v: View? -> onBackPressed() }
    }

    fun setUser(
        name: EditText?,
        username: EditText?,
        surname: EditText?,
        phone: EditText?,
        town: EditText?
    ): User {
        val user = User(
            username = username?.text.toString(),
            town = town?.text.toString(),
            phone = phone?.text.toString().takeIf { it.isNotEmpty() },
            name = name?.text.toString().takeIf { it.isNotEmpty() },
            surname = surname?.text.toString().takeIf { it.isNotEmpty() }
        )
        return user
    }

    fun checkPassword(password: EditText?, confirmPassword: EditText?): Boolean {
        var password = password?.text.toString().trim()
        var confirmPassword = confirmPassword?.text.toString().trim()

        if (confirmPassword == password) {
            checkPass = true
        } else {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
        }
        return checkPass
    }

    fun setUserDataOnView(user: User) {
        name?.setText(user.name)
        username?.setText(user.username)
        surname?.setText(user.surname)
        phone?.setText(user.phone)
        town?.setText(user.town)
    }

}