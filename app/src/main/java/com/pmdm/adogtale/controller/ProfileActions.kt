package com.pmdm.adogtale.controller

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pmdm.adogtale.model.Profile
import com.pmdm.adogtale.model.User
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap

class ProfileActions {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profile: Profile

    fun initFirebase() {
        db = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        initFirebase()
        val fUser = firebaseAuth.currentUser
        Log.i("getCurrentFirebaseUser", fUser.toString())
        return fUser
    }

    fun currentUser(): CompletableFuture<Optional<User>> {
        val response = CompletableFuture<Optional<User>>()

        val fUser = getCurrentFirebaseUser()
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(fUser?.email.toString())
            .get()
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    response.complete(Optional.empty())
                }

                val document = task.result
                val user = User(
                    name = document.getString("name") ?: "",
                    phone = document.getString("phone") ?: "",
                    username = document.getString("username") ?: "",
                    token = document.getString("token") ?: "",
                    town = document.getString("town") ?: "",
                    userId = fUser?.email.toString(),
                    email = fUser?.email.toString()
                )
                response.complete(Optional.of(user))
            }

        return response
    }

    fun getCurrentProfile(callback: (Profile) -> Unit) {
        initFirebase()

        currentUser()
            .thenApply { user ->

                db.collection("profile")
                    .document(user.get().email)
                    .get()
                    .addOnCompleteListener() { task ->
                        if (task.isSuccessful) {
                            val document = task.result

                            profile = Profile(
                                name = document.getString("name") ?: "",
                                age = document.getString("age") ?: "",
                                gender = document.getString("gender") ?: "",
                                breed = document.getString("breed") ?: "",
                                shortDescription = document.getString("shortDescription") ?: "",
                                something = document.getString("something") ?: "",
                                userEmail = document.getString("userEmail") ?: "",
                                pic1 = document.getString("pic1") ?: "",
                                pic2 = document.getString("pic2") ?: "",
                                pic3 = document.getString("pic3") ?: "",
                                pic4 = document.getString("pic4") ?: "",
                                vid = document.getString("vid") ?: "",
                                lookingFor = document.getString("lookingFor") ?: "",
                                prefBreed = document.getString("prefBreed") ?: "",
                                prefDistance = document.getString("prefDistance") ?: "",
                                town = document.getString("town") ?: "",
                                preferedLowAge = document.getLong("prefLowestAge") ?: 99,
                                preferedHighAge = document.getLong("prefHighestAge") ?: 0
                            )
                            callback(profile)
                        }
                    }
            }

    }

    fun getProfileFromEmail(email: String, callback: (Profile) -> Unit) {

        FirebaseFirestore.getInstance()
            .collection("profile")
            .document(email)
            .get()
            .addOnCompleteListener() { task ->
                if (!task.isSuccessful) {
                    Log.i("ProfileActions", "getProfileFromEmail error " + email)
                    return@addOnCompleteListener
                }

                val document = task.result

                profile = Profile(
                    name = document.getString("name") ?: "",
                    age = document.getString("age") ?: "",
                    gender = document.getString("gender") ?: "",
                    breed = document.getString("breed") ?: "",
                    shortDescription = document.getString("shortDescription") ?: "",
                    something = document.getString("something") ?: "",
                    userEmail = document.getString("userEmail") ?: "",
                    pic1 = document.getString("pic1") ?: "",
                    pic2 = document.getString("pic2") ?: "",
                    pic3 = document.getString("pic3") ?: "",
                    pic4 = document.getString("pic4") ?: "",
                    vid = document.getString("vid") ?: "",
                    lookingFor = document.getString("lookingFor") ?: "",
                    prefBreed = document.getString("prefBreed") ?: "",
                    prefDistance = document.getString("prefDistance") ?: "",
                    town = document.getString("town") ?: "",
                    preferedLowAge = document.getLong("prefLowestAge") ?: 99,
                    preferedHighAge = document.getLong("prefHighestAge") ?: 0
                )
                callback(profile)
            }

    }

    fun updateProfileTown(town: String, callback: () -> Unit) {

        val currentFirebaseUser = getCurrentFirebaseUser()
        val email = currentFirebaseUser?.email!!

        val dataToUpdate = HashMap<String, String>()

        dataToUpdate["town"] = town

        FirebaseFirestore.getInstance()
            .collection("profile")
            .document(email)
            .update(dataToUpdate as Map<String, Any>)
            .addOnCompleteListener {
                callback()
            }
    }
}