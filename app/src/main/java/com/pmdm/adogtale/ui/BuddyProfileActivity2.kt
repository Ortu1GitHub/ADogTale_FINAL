package com.pmdm.adogtale.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import android.widget.TextView
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pmdm.adogtale.R
import com.pmdm.adogtale.model.Preferences
import com.pmdm.adogtale.model.Profile
import com.pmdm.adogtale.model.User

class BuddyProfileActivity2 : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var btnPic1: ImageButton
    private lateinit var btnPic2: ImageButton
    private lateinit var btnPic3: ImageButton
    private lateinit var btnPic4: ImageButton
    private var selectedButton: ImageButton? = null
    private var backBtn: ImageButton? = null

    private lateinit var user: User
    private lateinit var profile: Profile
    private lateinit var preferences: Preferences

    private val buttonUris = HashMap<ImageButton, String>()
    private val profilePics = HashMap<String, String>()

    val db = FirebaseFirestore.getInstance()

    private lateinit var acLookingFor: AutoCompleteTextView
    private lateinit var acPreferedBreed: AutoCompleteTextView
    private lateinit var preferredDistanceSlider: Slider
    private lateinit var preferredDistanceTextView: TextView
    private lateinit var rSAge: RangeSlider
    private lateinit var lowAgeTextView: TextView
    private lateinit var highAgeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buddy_profile2)
        val btnDone: Button = findViewById(R.id.btnDone)
        backBtn = findViewById(R.id.back_btn)

        btnPic1 = findViewById(R.id.ibPic1)
        btnPic2 = findViewById(R.id.ibPic2)
        btnPic3 = findViewById(R.id.ibPic3)
        btnPic4 = findViewById(R.id.ibPic4)

        rSAge = findViewById(R.id.rSAge)

        lowAgeTextView = findViewById(R.id.tvPreferedLowAge)
        highAgeTextView = findViewById(R.id.tvPreferedHighAge)

        profile = intent.getSerializableExtra("profile") as Profile
        user = intent.getSerializableExtra("user") as User

        getFCMToken { token ->
            if (token != null) {
                Log.i("FCM token: ", token)
                user.token = token
            } else {
                Log.i("Could not obtain FCM token", "NULL")
            }
        }

        preferences = Preferences("", "", "")

        // Looking For
        val lookingFor = resources.getStringArray(R.array.lookingFor)
        val arrayLookingFor = ArrayAdapter(this, R.layout.dropdown_menu, lookingFor)
        acLookingFor = findViewById(R.id.acLookingFor)
        acLookingFor.setAdapter(arrayLookingFor)

        // Prefered Breed
        val preferedBreed = resources.getStringArray(R.array.prefBreed)
        val arrayPrefBreed = ArrayAdapter(this, R.layout.dropdown_menu, preferedBreed)
        acPreferedBreed = findViewById(R.id.acPrefBreed)
        acPreferedBreed.setAdapter(arrayPrefBreed)

        // Prefered Distace
        preferredDistanceTextView = findViewById(R.id.tvDistance)
        preferredDistanceSlider = findViewById(R.id.sPrefDistance)
        preferredDistanceSlider.addOnChangeListener { slider, value, fromUser ->
            preferredDistanceTextView.setText(value.toString() + "km")
        }

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }

        btnDone.setOnClickListener() {
            createUserAccount(user)
        }

        btnPic1.setOnClickListener() {
            selectedButton = btnPic1
            openGallery()
        }
        btnPic2.setOnClickListener() {
            selectedButton = btnPic2
            openGallery()
        }
        btnPic3.setOnClickListener() {
            selectedButton = btnPic3
            openGallery()
        }
        btnPic4.setOnClickListener() {
            selectedButton = btnPic4
            openGallery()
        }
        rSAge.addOnChangeListener { slider, value, fromUser ->
            val values = slider.values
            val minValue = values[0].toInt()
            val maxValue = values[1].toInt()
            lowAgeTextView.text = minValue.toString()
            highAgeTextView.text = maxValue.toString()
        }

        firebaseAuth = Firebase.auth
    }

    // Create dog profile in Firebase
    private fun createProfileAccount(profile: Profile) {
        profile.town = user.town;
        profile.lookingFor = acLookingFor.text.toString()
        profile.prefBreed = acPreferedBreed.text.toString()
        profile.prefDistance = preferredDistanceTextView.text.toString()
        profile.preferedLowAge = lowAgeTextView.text.toString().toLong()
        profile.preferedHighAge = highAgeTextView.text.toString().toLong()
        profile.pic1 = profilePics[btnPic1.id.toString()] ?: ""
        profile.pic2 = profilePics[btnPic2.id.toString()] ?: ""
        profile.pic3 = profilePics[btnPic3.id.toString()] ?: ""
        profile.pic4 = profilePics[btnPic4.id.toString()] ?: ""
        if (profile.lookingFor.isNotEmpty() && profile.prefBreed.isNotEmpty() && profile.prefBreed.isNotEmpty() &&
            profile.prefDistance.isNotEmpty()) {
            db.collection("profile").document(user.email).set(
                hashMapOf(
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
            ).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile created", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, CardSwipeActivity::class.java)
                    startActivity(intent)
                }
            }
        } else {
            Toast.makeText(this, "Please, fill the fields", Toast.LENGTH_SHORT).show()
        }

    }

    // Create user account in Firebase
    private fun createUserAccount(user: User) {
        firebaseAuth.createUserWithEmailAndPassword(user.email, user.password!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Created account", Toast.LENGTH_SHORT).show()
                    db.collection("user").document(user.email).set(
                        hashMapOf(
                            "username" to user.username,
                            "name" to user.name,
                            "surname" to user.surname,
                            "town" to user.town,
                            "phone" to user.phone,
                            "token" to user.token,
                        )
                    ).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            createProfileAccount(profile)
                        }
                    }
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }

    // Open device gallery to choose pictures
    fun openGallery() {
        Log.i("OpenGallery", "Abriendo la galería para el botón ${selectedButton!!.id}")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
    }

    // Set picture on image holder
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            selectedButton?.setImageURI(data?.data)
            val imageUrl = data?.data
            imageButtonHavePicture(imageUrl!!)
        }
    }

    // Check for external storage permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Check if ImageButton already has an image
    private fun imageButtonHavePicture(uri: Uri) {
        val email = user.email
        val imageName = uri.lastPathSegment
        val storagePath = "$email/$imageName"
        val storageReference = FirebaseStorage.getInstance().getReference()
        val pictureReference = storageReference.child(storagePath)

        val existingImageUri = buttonUris[selectedButton]
        Log.i("existingImageUri: ", existingImageUri.toString())
        if (existingImageUri != null) {
            val oldPictureReference =
                FirebaseStorage.getInstance().getReferenceFromUrl(existingImageUri)
            Log.i("oldImageRegerence: ", oldPictureReference.toString())

            oldPictureReference.delete().addOnCompleteListener {
                uploadNewImage(pictureReference, uri)
            }.addOnFailureListener { e ->
                Log.e("Delete image error", e.message!!)
            }
        } else {
            uploadNewImage(pictureReference, uri)
        }
    }

    // Upload selected image
    private fun uploadNewImage(pictureReference: StorageReference, uri: Uri) {
        pictureReference.putFile(uri).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri
                Toast.makeText(this, "Upload done!", Toast.LENGTH_SHORT).show()
                profilePics[selectedButton!!.id.toString()] = downloadUrl.toString()
                Log.i(
                    "UploadNewImage",
                    "Imagen subida para el botón ${selectedButton!!.id}, URL: $downloadUrl"
                )
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Uploading error", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFCMToken(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                callback(token)
            } else {
                callback(null)
            }
        }
    }
}
