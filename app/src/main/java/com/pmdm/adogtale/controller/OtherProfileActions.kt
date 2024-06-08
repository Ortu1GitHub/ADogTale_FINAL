package com.pmdm.adogtale.controller

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pmdm.adogtale.model.Profile
import com.pmdm.adogtale.model.User
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.CompletableFuture

class OtherProfileActions {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val feedFilter: FeedFilter = FeedFilter()

    // Initialize database instances
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


    // Obtain users compatible profiles
    fun getOtherProfiles(profile: Profile, callback: (List<Profile>) -> Unit) {
        // Inicializar Firebase
        initFirebase()

        // Retrieve profile collection
        db.collection("profile")
            .get()
            // Adding task listener
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profilesPassedFilter = mutableListOf<Profile>()

                    // Use Deferred list to save corrutines
                    val deferredList = mutableListOf<Deferred<Optional<Profile>>>()

                    // Iteration in obtained documents
                    for (document in task.result) {

                        Log.i("OtherProfileActions", "profile name: " + profile.name)
                        Log.i("OtherProfileActions", "document name: " + document.getString("name"))

                        if (profile.name.trim().equals(document.getString("name")?.trim())) {

                            continue
                        }

                        // Updates otherProfile in case filters are satisfied
                        val otherProfile = Profile(
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

                        // Corrutine to call ProfileFilter methods
                        val deferred = GlobalScope.async(Dispatchers.IO) {
                            var canPass = true

                            canPass = canPass && feedFilter.isSameLookingFor(profile, otherProfile)

                            canPass =
                                canPass && feedFilter.isInRangeOfDistance(profile, otherProfile)

                            if (profile.lookingFor.trim().lowercase().equals("pair")) {
                                canPass =
                                    canPass && feedFilter.isDifferentGender(profile, otherProfile)
                            }

                            if (profile.prefBreed.trim().lowercase().equals("same as mine")) {
                                canPass = canPass && feedFilter.isSameBreed(profile, otherProfile)
                            }

                            canPass =
                                canPass && feedFilter.isBetweenAgeInterval(profile, otherProfile)

                            canPass = canPass && !feedFilter.isAlreadyLikedBy(profile, otherProfile)

                            return@async if (canPass) Optional.of(otherProfile) else Optional.empty()
                        }

                        deferredList.add(deferred)
                    }

                    GlobalScope.launch(Dispatchers.Main) {
                        deferredList.awaitAll()
                            .stream()
                            .filter { it.isPresent() }
                            .map { it.get() }
                            .forEach { filteredProfileByAllFilters ->
                                profilesPassedFilter.add(filteredProfileByAllFilters)
                            }

                        // Callback to profile list that satisfy filters
                        callback(profilesPassedFilter)
                    }
                }
            }
    }
}

