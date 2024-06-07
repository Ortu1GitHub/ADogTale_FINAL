package com.pmdm.adogtale.ui

import androidx.appcompat.app.AppCompatActivity

class UserProfileActivity : AppCompatActivity() {
    /*
        private lateinit var db: FirebaseFirestore
        private lateinit var userActions: UserActions

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_user_profile)
            val user = Firebase.auth.currentUser
            val email = user?.email
            db = FirebaseFirestore.getInstance()

            val btnLogout: Button = findViewById(R.id.btnLogout)
            val btnDelete: Button = findViewById(R.id.btnDelete)
            setUserParameters(email)

            btnLogout.setOnClickListener() {
                logoutUser()
            }
            btnDelete.setOnClickListener() {
                val builder = AlertDialog.Builder(this)

                builder.setTitle("Eliminar cuenta")
                builder.setMessage("¿Estás seguro de que quieres eliminar tu cuenta?")

                builder.setPositiveButton("Sí") { dialog, which ->
                    db.collection("user").document(email!!).delete()
                    db.collection("profile").document(email!!).delete()
                    deleteUser(user)

                }

                builder.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }

        // Fills object User with database account data
        private fun setUserParameters(email: String?) {
            if (email != null) {
                db.collection("user").document(email).get()
                    .addOnSuccessListener {
                        val username = it.get("username")?:""
                        val name = it.get("name")?:""
                        val surname = it.get("surname")?:""
                        val town = it.get("town")?:""
                        val phone = it.get("phone")?:""

                        val user = LocalUser(
                            username = username, email = email, town = town,
                            phone = phone, name = name, surname = surname
                        )

                        setTextParameters(user)

                    }
                    .addOnFailureListener { exception ->
                        Log.d("Firestore", "got failed with ", exception)
                    }
            } else {
                Toast.makeText(this, "User mail can't be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Fills text areas with User parameters obtained from database
        private fun setTextParameters(user: LocalUser) {
            val txtEmail: TextView = findViewById(R.id.etEmail)
            val txtUsername: TextView = findViewById(R.id.etUsername)
            val txtName: TextView = findViewById(R.id.etName)
            val txtSurname: TextView = findViewById(R.id.etSurname)
            val txtTown: TextView = findViewById(R.id.etTown)
            val txtPhone: TextView = findViewById(R.id.etPhone)

            txtEmail.setText(user.email)
            txtUsername.setText(user.username)
            txtName.setText(user.name)
            txtSurname.setText(user.surname)
            txtTown.setText(user.town)
            txtPhone.setText(user.phone)
        }

        // Logout user from A Dog Tale
        private fun logoutUser() {
            val auth = Firebase.auth
            auth.signOut()
            Toast.makeText(this, "User disconnected", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        // Delete user account from auth
        private fun deleteUser(user: FirebaseUser?) {
            user?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "User " + user.email + " deleted successfuly",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, AuthActivity::class.java)
                    startActivity(intent)
                }
            }
        }*/
}