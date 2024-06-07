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
    private lateinit var otherProfile: Profile
    private lateinit var profileFilter: ProfileFilter
    private var filteredByBreed: Profile? = null
    private var filteredByGender: Profile? = null
    private var filteredByAge: Profile? = null
    val profilesWithCards = mutableSetOf<Profile>()
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

        // Acceder a la colección "profile" en la base de datos
        db.collection("profile")
            .get()
            // Agregar un listener para completar la tarea
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profilesPassedFilter = mutableListOf<Profile>()

                    // Usar una lista de Deferred para almacenar las corrutinas
                    val deferredList = mutableListOf<Deferred<Optional<Profile>>>()

                    // Iterar sobre los documentos obtenidos
                    for (document in task.result) {

                        Log.i("OtherProfileActions", "profile name: " + profile.name)
                        Log.i("OtherProfileActions", "document name: " + document.getString("name"))

                        if (profile.name.trim().equals(document.getString("name")?.trim())) {

                            continue
                        }

                        // Actualizar otherProfile solo si la condición se cumple
                        val otherProfile = Profile(
                            // Asignar valores de los campos del documento al objeto Profile, con valores predeterminados si el campo es nulo
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

                        // Iniciar una corrutina para llamar a los métodos de ProfileFilter de forma asíncrona y almacenar el resultado en la lista deferredList
                        val deferred = GlobalScope.async(Dispatchers.IO) {
                            var canPass = true

                            canPass = canPass && feedFilter.isSameLookingFor(profile, otherProfile)

                            canPass = canPass && feedFilter.isInRangeOfDistance(profile, otherProfile)

                            if (profile.lookingFor.trim().lowercase().equals("pair")) {
                                canPass = canPass && feedFilter.isDifferentGender(profile, otherProfile)
                            }

                            if (profile.prefBreed.trim().lowercase().equals("same as mine")) {
                                canPass = canPass && feedFilter.isSameBreed(profile, otherProfile)
                            }

                            canPass = canPass && feedFilter.isBetweenAgeInterval(profile, otherProfile)

                            canPass = canPass && !feedFilter.isAlreadyLikedBy(profile, otherProfile)

                            return@async if (canPass) Optional.of(otherProfile) else Optional.empty()
                        }

                        deferredList.add(deferred)

                    }

                    // Esperar que todas las corrutinas se completen y agregar los perfiles que pasan los filtros a profilesPassedFilter

                    GlobalScope.launch(Dispatchers.Main) {
                        deferredList.awaitAll()
                            .stream()
                            .filter { it.isPresent() }
                            .map { it.get() }
                            .forEach { filteredProfileByAllFilters ->
                                profilesPassedFilter.add(filteredProfileByAllFilters)
                            }

                        // Llamar al callback con la lista de perfiles que pasan los filtros
                        callback(profilesPassedFilter)
                    }
                }
            }
    }
}

