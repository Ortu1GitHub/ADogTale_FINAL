package com.pmdm.adogtale.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.pmdm.adogtale.R
import com.pmdm.adogtale.model.ProfilesMatching
import com.pmdm.adogtale.ui.ChatActivity
import com.pmdm.adogtale.utils.FirebaseUtil
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*

class PendingMatchesRecyclerAdapter(
    private val matchedProfiles: List<ProfilesMatching>,
    private val context: Context
) :
    RecyclerView.Adapter<PendingMatchesRecyclerAdapter.PendingMatchesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingMatchesViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.pending_matches_recycler_row, parent, false)
        Log.i("onCreateViewHolder", "AQUI")
        return PendingMatchesViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingMatchesViewHolder, position: Int) {
        val profile = matchedProfiles[position]
        Log.i("onBindViewHolder profile", profile.user_original.toString())
        Log.i("PendingmatchesRecyclerAdapter", "onBindViewHolder position: " + position)
        val firebaseUtil = FirebaseUtil()
        val currentUser = firebaseUtil.getCurrentFirebaseUser()

        holder.usernameText.text = profile.profile_original

        Log.i(
            "PendingmatchesRecyclerAdapter",
            "holder.usernameText.text: " + holder.usernameText.text
        )

        GlobalScope.launch {
            val profileData = firebaseUtil.getOtherProfileData(profile.user_original.toString())

            if (Objects.isNull(profileData)) {
                Log.e("RecentChatRecyclerAdapter", "Profile is null")
                return@launch
            }

            val im1 = profileData?.pic1

            if (im1?.isBlank()!!) {
                return@launch
            }

            Log.i("picasso", "paso")

            withContext(Dispatchers.Main) {
                Log.i("picasso", "paso")
                Picasso.get()
                    .load(im1)
                    .fit()
                    .centerCrop()
                    .into(holder.pic)
            }
        }

        holder.itemView.setOnClickListener {

            val intent = Intent(context, ChatActivity::class.java)
            updatePendingMatches(matchedProfiles, position)

            firebaseUtil.getOtherUser(profile.user_original!!) { cardUser ->
                val replyObject = JSONObject()
                replyObject.put("userId", cardUser.userId)
                replyObject.put("username", cardUser.username)
                replyObject.put("email", cardUser.email)

                Log.i("PendingMatchesRecyclerAdapter", replyObject.toString());

                Log.i("PendingMatchesRecyclerAdapter", replyObject.toString())

                intent.putExtra("reply", replyObject.toString())

                context.startActivity(intent)
            }


        }

    }

    override fun getItemCount(): Int {
        return matchedProfiles.size
    }

    inner class PendingMatchesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.user_name_text)
        val pic: ImageView = itemView.findViewById(R.id.iv1)
    }

    private fun updatePendingMatches(userTargets: List<ProfilesMatching>, position: Int) {
        val user_original = matchedProfiles[position].user_original
        val user_target = matchedProfiles[position].user_target

        Log.d("ORTU5", "Processing userTargets: $userTargets")
        val db = FirebaseFirestore.getInstance()
        db.collection("profiles_matching")
            .whereEqualTo("user_original", user_original)
            .whereEqualTo("likeAlreadyChecked", false)
            .whereEqualTo("user_target", user_target)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val docRef = db.collection("profiles_matching").document(document.id)
                    docRef.update("likeAlreadyChecked", true)
                        .addOnSuccessListener {
                            Log.d("ORTU5", "likeAlreadyChecked updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ORTU5", "Error updating likeAlreadyChecked", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ORTU9", "Error getting documents: ", e)
            }

        // Inverse
        db.collection("profiles_matching")
            .whereEqualTo("user_original", user_target)
            .whereEqualTo("likeAlreadyChecked", false)
            .whereEqualTo("user_target", user_original)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val docRef = db.collection("profiles_matching").document(document.id)
                    docRef.update("likeAlreadyChecked", true)
                        .addOnSuccessListener {
                            Log.d("ORTU9", "likeAlreadyChecked updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ORTU9", "Error updating likeAlreadyChecked", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ORTU9", "Error getting documents: ", e)
            }
    }
}

