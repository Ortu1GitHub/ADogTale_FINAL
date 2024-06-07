package com.pmdm.adogtale.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pmdm.adogtale.model.Profile
import com.pmdm.adogtale.model.User
import java.util.concurrent.Executors

class ProfileMethods {
    val firebaseUtil = FirebaseUtil()
    private var db = FirebaseFirestore.getInstance()
    private lateinit var fUser: FirebaseUser
    private val executor = Executors.newSingleThreadExecutor()

    fun updateProfile(profile: Profile, context: Context, fast:Boolean) {
        db = firebaseUtil.initDB()!!
        fUser = firebaseUtil.getCurrentFirebaseUser()!!

        if(fast) {
            fastUpdateProfileData(profile)
        } else {
            updateProfileAccount(profile)
        }

        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
    }
    fun fastUpdateProfileData(profile: Profile) {
        var email = fUser?.email.toString()
        Log.i("updateUserAccount email", email)
        val profileRef = db.collection("profile").document(email)

        val updatedData = hashMapOf(
            "something" to profile.something,
            "lookingFor" to profile.lookingFor,
            "prefBreed" to profile.prefBreed,
            "prefDistance" to profile.prefDistance,
            "pic1" to profile.pic1,
            "pic2" to profile.pic2,
            "pic3" to profile.pic3,
            "pic4" to profile.pic4,
            "prefLowestAge" to profile.preferedLowAge,
            "prefHighestAge" to profile.preferedHighAge,
        )
        Log.i("updateUserAccount pic1", profile.pic1)
        profileRef.update(updatedData as Map<String, Any>).addOnCompleteListener(executor) { task ->
            if (task.isSuccessful) {
                Log.i("fastUpdateProfileAccount", "Document successfully updated")
            } else {
                Log.e("fastUpdateProfileAccount", "Error updating document", task.exception)
            }
        }
    }

    fun updateProfileAccount(profile: Profile) {
        var email = fUser?.email.toString()
        db.collection("profile").document(email)
        val profileRef = db.collection("profile").document(email)

        val updatedData = hashMapOf(
            "userEmail" to profile.userEmail,
            "name" to profile.name,
            "age" to profile.age,
            "gender" to profile.gender,
            "breed" to profile.breed,
            "something" to profile.something,
            "shortDescription" to profile.shortDescription,
            "lookingFor" to profile.lookingFor,
            "prefBreed" to profile.prefBreed,
            "prefDistance" to profile.prefDistance,
            "pic1" to profile.pic1,
            "pic2" to profile.pic2,
            "pic3" to profile.pic3,
            "pic4" to profile.pic4,
            "prefLowestAge" to profile.preferedLowAge,
            "prefHighestAge" to profile.preferedHighAge,
            "town" to profile.town
        )
        profileRef.update(updatedData as Map<String, Any>).addOnCompleteListener(executor) { task ->
            if (task.isSuccessful) {
                Log.i("updateProfileAccount", "Document successfully updated")
            } else {
                Log.e("updateProfileAccount", "Error updating document", task.exception)
            }
        }
    }

    fun getCurrentProfile(callback: (Profile) -> Unit) {
        db = firebaseUtil.initDB()!!
        fUser = firebaseUtil.getCurrentFirebaseUser()!!
        var profile: Profile
        var email = fUser?.email.toString()
        db.collection("profile").document(email).get()
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    profile = Profile(
                        userEmail = document.getString("userEmail") ?: "",
                        name = document.getString("name") ?: "",
                        age = document.getString("age") ?: "",
                        gender = document.getString("gender") ?: "",
                        breed = document.getString("breed") ?: "" ,
                        something = document.getString("something") ?: "",
                        shortDescription = document.getString("shortDescription") ?: "",
                        lookingFor = document.getString("lookingFor") ?: "",
                        prefBreed = document.getString("prefBreed") ?: "",
                        prefDistance = document.getString("prefDistance") ?: "",
                        pic1 = document.getString("pic1") ?: "",
                        pic2 = document.getString("pic2") ?: "",
                        pic3 = document.getString("pic3") ?: "",
                        pic4 = document.getString("pic4") ?: "",
                        preferedLowAge = document.getLong("prefLowestAge") ?: 1,
                        preferedHighAge = document.getLong("prefHighestAge") ?: 15,
                        town = document.getString("town") ?: "",
                    )

                    Log.i("getCurrentProfile", profile.name)
                    callback(profile)
                }
            }
    }

}