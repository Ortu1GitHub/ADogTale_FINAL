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
    private lateinit var matchListTopbar: MatchListTopbar;
    private val firebaseUtil: FirebaseUtil = FirebaseUtil()
    val matchesToDisplay = mutableListOf<ProfilesMatching>()
    var fUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches_list)

        recyclerView = findViewById(R.id.recyler_view2)
        setupRecyclerView()
        matchListTopbar = MatchListTopbar(this)
        matchListTopbar.configureTopbar()

        firebaseUtil.getCurrentUser { currentUser ->
            firebaseUtil.getCountUnreadMessagesInAllChatrooms(currentUser.email)
                .thenAccept { result ->
                    if (result > 0) {
                        matchListTopbar.showBadge(MatchListTopbar.MatchListTopbarOption.CHAT)
                    }
                }
        }

    }

    private fun setupRecyclerView() {
        val firebaseUtil = FirebaseUtil()
        val db = FirebaseFirestore.getInstance()
        val currentUser = firebaseUtil.getCurrentFirebaseUser()

        // Create list to save matching profiles
        val matchedProfiles = mutableListOf<ProfilesMatching>()

        currentUser?.let { user ->
            val currentUserEmail = user.email.toString()

            // Query profiles in which user logged sent like
            db.collection("profiles_matching")
                .whereEqualTo("user_original", currentUserEmail)
                .whereEqualTo("likeAlreadyChecked", false)
                .get()
                .addOnSuccessListener { snapshot ->
                    val likedProfiles =
                        snapshot.documents.mapNotNull { it.toObject(ProfilesMatching::class.java) }

                    val allResponseFutures = ArrayList<CompletableFuture<Void>>()
                    // Filter profiles in which user logged receive like
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
                            Log.i(
                                "MatchesListActivity",
                                "setupRecyclerViewWithAdapter: " + matchedProfiles.size
                            )
                            setupRecyclerViewWithAdapter(matchedProfiles)
                        }
                    // Cofigure RecyclerView with matching profiles
                }
                .addOnFailureListener { exception ->
                    Log.w("setupRecyclerView Error", "Error getting liked profiles: ", exception)
                }
        } ?: run {
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

                // If match adding profile to list
                Log.d("Match Found", "Match found with ${likedProfile.user_target}")
                val matchedProfile = targetSnapshot.documents.firstOrNull()
                    ?.toObject(ProfilesMatching::class.java)
                matchedProfile?.let { matchedProfiles.add(it) }
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
        Log.i("MatchesListActivity", "onStart metodo")
    }

    override fun onResume() {
        super.onResume()
        Log.i("MatchesListActivity", "onResume metodo")
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


