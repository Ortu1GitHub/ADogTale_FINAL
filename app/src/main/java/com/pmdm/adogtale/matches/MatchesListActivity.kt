package com.pmdm.adogtale.matches

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.pmdm.adogtale.R
import com.pmdm.adogtale.adapter.PendingMatchesRecyclerAdapter
import com.pmdm.adogtale.model.ProfilesMatching
import com.pmdm.adogtale.ui.topbar.match_list.MatchListTopbar
import com.pmdm.adogtale.utils.FirebaseUtil
import java.util.concurrent.CompletableFuture

class MatchesListActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private var adapter: PendingMatchesRecyclerAdapter? = null
    private lateinit var matchListTopbar: MatchListTopbar;
    private  val firebaseUtil: FirebaseUtil = FirebaseUtil()
    val matchesToDisplay = mutableListOf<ProfilesMatching>()
    var fUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches_list)

        recyclerView = findViewById(R.id.recyler_view2)
        setupRecyclerView()
        //obtainPendingMatches()
        matchListTopbar = MatchListTopbar(this)
        matchListTopbar.configureTopbar()

        firebaseUtil.getCurrentUser { currentUser ->
            firebaseUtil.getCountUnreadMessagesInAllChatrooms(currentUser.email)
                .thenAccept{ result ->
                    if(result > 0){
                        matchListTopbar.showBadge(MatchListTopbar.MatchListTopbarOption.CHAT)
                    }
                }
        }

    }

    private fun setupRecyclerView() {
        val firebaseUtil = FirebaseUtil()
        val db = FirebaseFirestore.getInstance()
        val currentUser = firebaseUtil.getCurrentFirebaseUser()

        // Crear una lista para almacenar los perfiles coincidentes
        val matchedProfiles = mutableListOf<ProfilesMatching>()

        currentUser?.let { user ->
            val currentUserEmail = user.email.toString()

            // Consultar perfiles donde el usuario logueado dio like
            db.collection("profiles_matching")
                .whereEqualTo("user_original", currentUserEmail)
                .whereEqualTo("likeAlreadyChecked", false)
                .get()
                .addOnSuccessListener { snapshot ->
                    val likedProfiles = snapshot.documents.mapNotNull { it.toObject(ProfilesMatching::class.java) }

                    val allResponseFutures = ArrayList<CompletableFuture<Void>>()

                    // Filtrar perfiles donde el usuario logueado también recibió like
                    likedProfiles.forEach { likedProfile ->

                        val currentFuture =
                            searchMutualLike(
                                db,
                                likedProfile,
                                currentUserEmail,
                                matchedProfiles,
                                likedProfiles
                            )

                        allResponseFutures.add(currentFuture)
                    }


                    CompletableFuture.allOf(*allResponseFutures.toTypedArray())
                        .thenAccept {
                            Log.i("MatchesListActivity", "setupRecyclerViewWithAdapter: " + matchedProfiles.size)
                            setupRecyclerViewWithAdapter(matchedProfiles)
                        }

                    // Configurar RecyclerView una vez que se hayan agregado todos los perfiles coincidentes

                }
                .addOnFailureListener { exception ->
                    Log.w("setupRecyclerView Error", "Error getting liked profiles: ", exception)
                }
        } ?: run {
            // Si el usuario actual es nulo, registrar un error
            Log.e("setupRecyclerView Error", "Current user is null")
        }

        firebaseUtil.getCurrentUser { user ->
            firebaseUtil.putAllMatchesToChecked(user.email)
        }
    }

    private fun searchMutualLike(
        db: FirebaseFirestore,
        likedProfile: ProfilesMatching,
        currentUserEmail: String,
        matchedProfiles: MutableList<ProfilesMatching>,
        likedProfiles: List<ProfilesMatching>
    ): CompletableFuture<Void> {

        val responseFuture = CompletableFuture<Void>()

        db.collection("profiles_matching")
            .whereEqualTo("user_original", likedProfile.user_target)
            .whereEqualTo("user_target", currentUserEmail)
            .get()
            .addOnSuccessListener { targetSnapshot ->
                val targetLiked = targetSnapshot.documents.isNotEmpty()

                if (!targetLiked) {
                    responseFuture.complete(null)
                    return@addOnSuccessListener
                }

                // Si hay un match, añadir el perfil a la lista
                Log.d("Match Found", "Match found with ${likedProfile.user_target}")
                val matchedProfile = targetSnapshot.documents.firstOrNull()
                    ?.toObject(ProfilesMatching::class.java)
                matchedProfile?.let { matchedProfiles.add(it) } // Aquí se agrega el perfil coincidente, no el perfil que dio like
                Log.i("matchedProfiles 1", matchedProfiles[0].user_original.toString())

                Log.i("MatchesListActivity", "matchedProfiles.size: " + matchedProfiles.size)
                Log.i("MatchesListActivity", "likedProfiles.size: " + likedProfiles.size)


                responseFuture.complete(null)
            }
            .addOnFailureListener { exception ->
                Log.w("setupRecyclerView Error", "Error getting liked profiles: ", exception)
                responseFuture.complete(null)
            }

        return responseFuture
    }

    private fun setupRecyclerViewWithAdapter(matchedProfiles: List<ProfilesMatching>) {
        val adapter = PendingMatchesRecyclerAdapter(matchedProfiles, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }


    override fun onStart() {
        super.onStart()
        //adapter?.startListening()
        Log.i("MatchesListActivity", "onStart metodo")
    }

    override fun onStop() {
        super.onStop()
        //adapter?.stopListening()
    }

    override fun onResume() {
        super.onResume()
        //setupRecyclerView()
        //adapter?.notifyDataSetChanged()
        Log.i("MatchesListActivity", "onResume metodo")
    }

    private fun obtainPendingMatches() {
        fUser = getCurrentFirebaseUser()

        val db = FirebaseFirestore.getInstance()
        Log.i("ORTU8", fUser?.email.toString())
        db.collection("profiles_matching")
            .whereEqualTo(
                "user_original",
                fUser?.email.toString()
            )
            .whereEqualTo("likeAlreadyChecked", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val pendingMatches = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    Log.d("ORTU7", "AQUI ")
                    val profileMatching = document.toObject(ProfilesMatching::class.java)
                    Log.d("ORTU6", "Processing profileMatching: $profileMatching")
                    profileMatching?.user_target?.let { pendingMatches.add(it) }
                }
                // Eliminar duplicados usando distinct()
                val distinctPendingMatches = pendingMatches.distinct()
                // Aquí tienes la lista de usuarios_target únicos que coinciden con los criterios
                Log.d("ORTU4", "Processing distinctPendingMatches: $distinctPendingMatches")
                processPendingMatches(distinctPendingMatches)
            }
            .addOnFailureListener { exception ->
                // Manejo de errores
            }
    }

    private fun processPendingMatches(userTargets: List<String>) {
        fUser = getCurrentFirebaseUser()
        Log.d("ORTU5", "Processing userTargets: $userTargets")
        for (userTarget in userTargets) {
            val db = FirebaseFirestore.getInstance()
            db.collection("profiles_matching")
                .whereEqualTo("user_original", userTarget)
                .whereEqualTo("likeAlreadyChecked", false)
                .whereEqualTo("user_target", fUser?.email.toString())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val profileMatching = document.toObject(ProfilesMatching::class.java)
                        profileMatching?.let {
                            matchesToDisplay.add(profileMatching)
                        }
                    }
                }
        }
        setupRecyclerViewWithAdapter(matchesToDisplay)
        //setupRecyclerView()
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        initFirebase()
        fUser = firebaseAuth.currentUser
        Log.i("getCurrentFirebaseUser", fUser.toString())
        return fUser
    }

    fun initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }
}


