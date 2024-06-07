package com.pmdm.adogtale.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class DeleteMethods {

    val firebaseUtil = FirebaseUtil()
    private lateinit var db: FirebaseFirestore

    fun deleteEverythingOfAnUser(email: String, password: String, context: Context) {
        try {
            Log.i("deleteEverythingOfAUser", email)

            deleteAccount(email, password)
            deleteUser(email)
            deleteProfileFromUser(email)
            deleteUserFromProfileMatching(email)
            deleteUserToken(email)
            deleteChatRoomWhereUserIsIn(email)

            Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(
                "DeleteEverything Error",
                "Error deleting everything for user $email: ${e.message}"
            )
            Toast.makeText(context, "Error eliminando cuenta", Toast.LENGTH_SHORT).show()
        }

    }

    fun deleteAccount(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        val currentUser = firebaseUtil.getCurrentFirebaseUser()
        Log.i("deleteAccount currentUser", currentUser!!.email.toString())
        currentUser.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    currentUser.delete()
                        .addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {

                            } else {
                            }
                        }
                } else {
                }
            }
    }

    fun deleteUser(email: String) {
        // Inicializar Firebase si es necesario
        db = firebaseUtil.initDB()!!


        // Referencia a la colección y al documento
        val userRef = db.collection("user").document(email)

        // Borrar el documento
        userRef.delete()
            .addOnSuccessListener {
                Log.i("User eliminado", "user eliminado correctamente")
            }
            .addOnFailureListener { error ->
                Log.i("Error eliminando user ", "User: " + email)
            }
    }

    fun deleteUserToken(email: String) {
        // Inicializar Firebase si es necesario
        db = firebaseUtil.initDB()!!

        // Referencia a la colección y al documento
        val userRef = db.collection("device_tokens").document(email)

        // Borrar el documento
        userRef.delete()
            .addOnSuccessListener {
                Log.i("Device token eliminado", "device token eliminado correctamente")
            }
            .addOnFailureListener { error ->
                Log.i("Error eliminando device token ", "User: " + email)
            }
    }

    fun deleteUserFromProfileMatching(email: String) {
        val db = FirebaseFirestore.getInstance()

        val userOriginal = db.collection("profile_matching")
            .whereEqualTo("user_original", email)
        val userTarget = db.collection("profile_matching")
            .whereEqualTo("user_target", email)

        userOriginal.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val documentId = document.id
                    db.collection("profile_matching").document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "UserOriginal Documents successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error deleting document", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }

        userTarget.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val documentId = document.id
                    db.collection("profile_matching").document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "UserTarget Documents successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error deleting document", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    fun deleteProfileFromUser(email: String) {
        // Inicializar Firebase si es necesario
        db = firebaseUtil.initDB()!!

        // Referencia a la colección y al documento
        val profileRef = db.collection("profile").document(email)

        // Borrar el documento
        profileRef.delete()
            .addOnSuccessListener {
                Log.i("User eliminado", "profile eliminado correctamente")
            }
            .addOnFailureListener { error ->
                Log.i("Error eliminando user ", "User: " + email)
            }
    }

    fun deleteChatRoomWhereUserIsIn(email: String) {
        val db = FirebaseFirestore.getInstance()
        val chatroomCollection = db.collection("chatrooms")

        chatroomCollection.whereArrayContains("userIds", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    querySnapshot.documents.forEach { document ->
                        Log.i("Chatroom document", document.reference.toString())
                        Log.i("Chatroom document id", document.reference.id)

                        // Eliminar la colección "chats" dentro del documento actual
                        val chatsCollection = document.reference.collection("chats")
                        chatsCollection.get()
                            .addOnSuccessListener { chatsSnapshot ->
                                if (!chatsSnapshot.isEmpty) {
                                    chatsSnapshot.documents.forEach { chatDocument ->
                                        chatDocument.reference.delete()
                                    }
                                }
                                // Después de eliminar todos los documentos de la subcolección,
                                // eliminar el documento de la colección "chatrooms"
                                document.reference.delete()
                            }
                            .addOnFailureListener { e ->
                                // Manejar errores al obtener la colección "chats"
                                println("Error fetching chats: $e")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Manejo de errores al obtener la colección "chatrooms"
                println("Error fetching chat rooms: $e")
            }
    }


}