package com.pmdm.adogtale.ui

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pmdm.adogtale.R
import com.pmdm.adogtale.model.Preferences
import com.pmdm.adogtale.model.Profile

class ListTestActivity : AppCompatActivity() {
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseAuth: FirebaseAuth
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_test)

        storageReference = FirebaseStorage.getInstance().getReference()
        firebaseAuth = FirebaseAuth.getInstance()

        val tvTest: TextView = findViewById(R.id.tvTest)
        getCurrentUser()
        storageReference.listAll().addOnSuccessListener { listResult ->
            listResult.prefixes.forEach { folderReference ->

                folderReference.listAll().addOnSuccessListener { listResult ->
                    listResult.items.forEach { imageReference ->

                        // Obtiene URL de descarga de cada imagen
                        imageReference.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            Log.i("URL image", downloadUrl)

                            // Añade los enlaces al TextView
                            tvTest.append("$downloadUrl\n")
                        }
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("Firebase error", e.message!!)
        }
    }

    private fun getCurrentUser() {
        val user = firebaseAuth.currentUser
        user!!.reload().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                getProfile(user)
            } else {
                Toast.makeText(
                    this,
                    "There is a big problem with your user. Please, contact A Dog Tale support team",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    private fun getProfile(user: FirebaseUser) {
        var userEmail: String = "rancio@elcampico.org"//user.email.toString()
        Log.i("UserEmail getProfile", userEmail)
        db.collection("profile").document(userEmail).get()
            .addOnCompleteListener(this) { task ->
                val document = task.result
                if (document != null && document.exists()) {
                    val dogName = document.getString("name")
                    if (dogName != null) {
                        var preferenceDocument = userEmail + "-" + dogName
                        getPreferences(preferenceDocument)
                    }
                }
            }

    }

    private fun getPreferences(preferenceDocument: String) {
        db.collection("preferences").document(preferenceDocument).get()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val preferences: Preferences = Preferences("", "", "")
                        preferences.lookingFor = document.getString("lookingFor").toString()
                        preferences.prefBreed = document.getString("prefBreed").toString()
                        preferences.prefDistance = document.getString("prefDistance").toString()
                        Log.i("Preferences lookingFor ", preferences.lookingFor)
                        Log.i("Preferences breed ", preferences.prefBreed)
                        Log.i("Preferences distance ", preferences.prefDistance)

                        getProfileList(preferences)

                    } else {
                        Toast.makeText(this, "Document does not exists", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Problem loading the document", Toast.LENGTH_SHORT).show()
                }

            }
    }

    private fun getProfileList(preferences: Preferences) {
        Log.i("ProfileList", "dentro")
        db.collection("profile")
            .get()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i("CompleteListener", "dentro")
                    val profileList = ArrayList<Profile>()
                    for (document in task.result!!) {
                        Log.i("Document in result", "dentro")
                        val profile = document.toObject(Profile::class.java)
                        // Construye el nombre del documento de preferencias
                        val preferenceDocument = "${profile.userEmail}-${profile.name}"
                        Log.i("documento de acceso", preferenceDocument)
                        // Obtiene el documento de preferencias
                        db.collection("preferences").document(preferenceDocument).get()
                            .addOnSuccessListener { prefDoc ->
                                if (prefDoc.exists()) {
                                    val pref = prefDoc.toObject(Preferences::class.java)
                                    // Comprueba si las preferencias coinciden
                                    if (pref?.lookingFor == preferences.lookingFor && pref?.prefBreed == preferences.prefBreed) {
                                        profileList.add(profile)
                                    }
                                }
                            }
                    }
                    getCards(profileList)
                } else {
                    Log.d("Firestore", "Error al obtener los perfiles", task.exception)
                }
            }
    }

    /*
    private fun getProfileList(preferences: Preferences) {
        db.collection("profiles")
            .whereEqualTo("lookingFor", preferences.lookingFor)
            .whereEqualTo("breed", preferences.prefBreed)
            .get()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val profileList = ArrayList<Profile>()
                    for (document in task.result!!) {
                        val profile = document.toObject(Profile::class.java)
                        profileList.add(profile)
                    }
                    getCards(profileList)
                } else {
                    Log.d("Firestore", "Error al obtener los perfiles", task.exception)
                }
            }
    }
*/
    private fun getCards(profileList: ArrayList<Profile>) {
        for (Profile in profileList) {
            Log.i("Profile ", Profile.name)
        }

    }
}


