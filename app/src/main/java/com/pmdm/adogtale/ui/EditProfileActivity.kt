package com.pmdm.adogtale.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pmdm.adogtale.R
import com.pmdm.adogtale.model.Profile
import com.pmdm.adogtale.ui.statusbar.StatusbarColorHandler
import com.pmdm.adogtale.utils.FirebaseUtil
import com.pmdm.adogtale.utils.ProfileMethods
import com.squareup.picasso.Picasso
import org.json.JSONObject

class EditProfileActivity : AppCompatActivity() {
    private val profileMethods = ProfileMethods()
    private val firebaseUtil = FirebaseUtil()
    private var profile: Profile? = null
    private var fUser: FirebaseUser? = null
    private var saveBtn: Button? = null
    private lateinit var acLookingFor: AutoCompleteTextView
    private lateinit var acPreferedBreed: AutoCompleteTextView
    private lateinit var prefDistance: Slider
    private lateinit var rSAge: RangeSlider
    private lateinit var something: EditText
    private lateinit var tvDistance: TextView
    private lateinit var lowAgeTextView: TextView
    private lateinit var highAgeTextView: TextView
    private lateinit var btnPic1: ImageButton
    private lateinit var btnPic2: ImageButton
    private lateinit var btnPic3: ImageButton
    private lateinit var btnPic4: ImageButton
    private var backBtn: ImageButton? = null
    private var selectedButton: ImageButton? = null
    private val buttonUris = HashMap<ImageButton, String>()
    private val profilePics = HashMap<String, String>()
    private val statusbarColorHandler: StatusbarColorHandler = StatusbarColorHandler()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        statusbarColorHandler.setStatusbarBackgroundColor(this, Color.WHITE)

        acLookingFor = findViewById(R.id.acLookingForEdit)

        acPreferedBreed = findViewById(R.id.acPrefBreed)

        saveBtn = findViewById(R.id.saveBtn)
        backBtn = findViewById(R.id.back_btn)

        prefDistance = findViewById(R.id.sPrefDistance)
        tvDistance = findViewById(R.id.tvDistance)
        something = findViewById(R.id.etSomething)
        lowAgeTextView = findViewById(R.id.tvPreferredLowAge1)
        highAgeTextView = findViewById(R.id.tvPreferredHighAge1)
        rSAge = findViewById(R.id.rSAge)
        btnPic1 = findViewById(R.id.ibPic1)
        btnPic2 = findViewById(R.id.ibPic2)
        btnPic3 = findViewById(R.id.ibPic3)
        btnPic4 = findViewById(R.id.ibPic4)


        profileMethods.getCurrentProfile { currentProfile ->
            val jsonProfileInfo = JSONObject()
            jsonProfileInfo.put("lookingFor", currentProfile.lookingFor)
            jsonProfileInfo.put("prefBreed", currentProfile.prefBreed)
            jsonProfileInfo.put("distance", currentProfile.prefDistance)
            Log.i("EditProfileActivity", jsonProfileInfo.toString())
            profile = currentProfile
            setprofileDataOnView(profile!!)
            setProfilePics(profile!!)
        }

        fUser = firebaseUtil.getCurrentFirebaseUser()

        rSAge.addOnChangeListener { slider, value, fromUser ->
            val values = slider.values
            val minValue = values[0].toInt()
            val maxValue = values[1].toInt()
            lowAgeTextView.text = minValue.toString()
            highAgeTextView.text = maxValue.toString()
        }

        prefDistance.addOnChangeListener { slider, value, fromUser ->
            tvDistance.text = value.toInt().toString() + "km"
        }

        saveBtn?.setOnClickListener {
            setProfile()
            profileMethods.updateProfile(profile!!, this, fast = true)
            val intent = Intent(this, CardSwipeActivity::class.java)
            startActivity(intent)
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

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }
    }

    fun setProfile() {
        profile?.lookingFor = acLookingFor.text.toString()
        profile?.prefBreed = acPreferedBreed.text.toString()
        profile?.prefDistance = tvDistance.text.toString()
        profile?.preferedLowAge = lowAgeTextView?.text.toString().toLong()
        profile?.preferedHighAge = highAgeTextView?.text.toString().toLong()
        profile?.something = something?.text.toString()
    }

    fun setprofileDataOnView(profile: Profile) {
        // Looking For
        acLookingFor?.setText(profile.lookingFor)
        val lookingFor = resources.getStringArray(R.array.lookingFor)
        val arrayLookingFor = ArrayAdapter(this, R.layout.dropdown_menu, lookingFor)
        acLookingFor.setAdapter(arrayLookingFor)

        // Prefered Breed
        Log.i("setProfileDataOnView prefBreed", profile.prefBreed)
        acPreferedBreed?.setText(profile.prefBreed)
        val preferedBreed = resources.getStringArray(R.array.prefBreed)
        val arrayPrefBreed = ArrayAdapter(this, R.layout.dropdown_menu, preferedBreed)
        acPreferedBreed.setAdapter(arrayPrefBreed)

        tvDistance.text = profile.prefDistance.lowercase()
        prefDistance.value = profile.prefDistance.lowercase().replace("km", "").toFloat()

        something?.setText(profile.something)
        lowAgeTextView?.text = profile.preferedLowAge.toString()
        highAgeTextView?.text = profile.preferedHighAge.toString()
    }

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
                EditProfileActivity.PERMISSION_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, EditProfileActivity.IMAGE_PICK_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            EditProfileActivity.PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == EditProfileActivity.IMAGE_PICK_CODE) {
            selectedButton?.setImageURI(data?.data)
            val imageUrl = data?.data
            imageButtonHavePicture(imageUrl!!)
        }
    }

    private fun imageButtonHavePicture(uri: Uri) {
        val email = fUser?.email.toString()
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

    private fun uploadNewImage(pictureReference: StorageReference, uri: Uri) {
        pictureReference.putFile(uri).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri
                Toast.makeText(this, "Upload done!", Toast.LENGTH_SHORT).show()
                profilePics[selectedButton!!.id.toString()] = downloadUrl.toString()
                when (selectedButton?.id) {
                    R.id.ibPic1 -> profile?.pic1 = downloadUrl.toString()
                    R.id.ibPic2 -> profile?.pic2 = downloadUrl.toString()
                    R.id.ibPic3 -> profile?.pic3 = downloadUrl.toString()
                    R.id.ibPic4 -> profile?.pic4 = downloadUrl.toString()
                }
                Log.i(
                    "UploadNewImage",
                    "Imagen subida para el botón ${selectedButton!!.id}, URL: $downloadUrl"
                )
                Log.i("uploadNewImage pic1", profile?.pic1.toString())
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Uploading error", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }

    fun setProfilePics(profile: Profile) {

        var im1 = profile.pic1
        var im2 = profile.pic2
        var im3 = profile.pic3
        var im4 = profile.pic4

        //Original and target images are loaded
        if (!im1.isNullOrEmpty()) {
            Picasso.get()
                .load(im1)
                .fit()
                .centerCrop()
                .into(btnPic1)
        }

        if (!im2.isNullOrEmpty()) {
            Picasso.get()
                .load(im2)
                .fit()
                .centerCrop()
                .into(btnPic2)
        }
        if (!im3.isNullOrEmpty()) {
            Picasso.get()
                .load(im3)
                .fit()
                .centerCrop()
                .into(btnPic3)
        }
        if (!im4.isNullOrEmpty()) {
            Picasso.get()
                .load(im4)
                .fit()
                .centerCrop()
                .into(btnPic4)
        }

    }

}