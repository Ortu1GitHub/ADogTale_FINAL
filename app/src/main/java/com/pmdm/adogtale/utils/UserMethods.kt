package com.pmdm.adogtale.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pmdm.adogtale.model.User
import java.util.concurrent.Executors

class UserMethods {
    val firebaseUtil = FirebaseUtil()
    private lateinit var db: FirebaseFirestore
    private lateinit var fUser: FirebaseUser
    private val executor = Executors.newSingleThreadExecutor()

    fun updateUserAccount(user: User, newPassword: String, context: Context) {
        db = firebaseUtil.initDB()!!
        fUser = firebaseUtil.getCurrentFirebaseUser()!!
        updateUserData(user)
        if (newPassword.isNotEmpty()) {
            updateUserPassword(fUser, newPassword)
        }
        Toast.makeText(context, "User updated", Toast.LENGTH_SHORT).show()
    }

    fun updateUserData(user: User) {
        db = firebaseUtil.initDB()!!
        fUser = firebaseUtil.getCurrentFirebaseUser()!!
        var email = fUser?.email.toString()
        Log.i("updateUserAccount email", email)
        val userRef = db.collection("user").document(email)

        val updatedData = hashMapOf(
            "username" to user.username,
            "name" to user.name,
            "surname" to user.surname,
            "town" to user.town,
            "phone" to user.phone
        )

        Log.i("updateUserAccount map", updatedData["email"] ?: "No email provided")

        userRef.update(updatedData as Map<String, Any>).addOnCompleteListener(executor) { task ->
            if (task.isSuccessful) {
                Log.i("updateUserAccount", "Document successfully updated")
            } else {
                Log.e("updateUserAccount", "Error updating document", task.exception)
            }
        }
    }

    fun getCurrentUser(callback: (User) -> Unit) {
        var user: User
        fUser = firebaseUtil.getCurrentFirebaseUser()!!
        Log.i("userEmail", fUser?.email.toString())
        db.collection("user").document(fUser?.email.toString()).get()
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    user = User(
                        name = document.getString("name") ?: "",
                        phone = document.getString("phone") ?: "",
                        username = document.getString("username") ?: "",
                        surname = document.getString("surname") ?: "",
                        token = document.getString("token") ?: "",
                        town = document.getString("town") ?: "",
                    )
                    Log.i("getCurrentUser", user.name.toString())
                    callback(user)
                }
            }
    }

    fun updateUserPassword(user: FirebaseUser, newPassword: String) {
        Log.i("updateUserPassword password", newPassword)
        user?.let {
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("updateUserPassword", "Password modified successfully")
                    } else {
                        Log.i("updateUserPassword", "Error while modifying password")
                    }
                }
        }

    }
}