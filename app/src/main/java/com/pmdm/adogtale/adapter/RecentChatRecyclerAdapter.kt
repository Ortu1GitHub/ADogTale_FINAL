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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.pmdm.adogtale.R
import com.pmdm.adogtale.model.ChatroomModel
import com.pmdm.adogtale.ui.ChatActivity
import com.pmdm.adogtale.utils.FirebaseUtil
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RecentChatRecyclerAdapter(
    options: FirestoreRecyclerOptions<ChatroomModel>,
    private val context: Context
) :
    FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder>(
        options
    ) {

    init {
        Log.i("RecentChatRecyclerAdapter", "Options: $options")
    }

    override fun onBindViewHolder(
        holder: ChatroomModelViewHolder,
        position: Int,
        model: ChatroomModel
    ) {
        Log.i("onBindViewHolder ChatroomUserId", model.userIds.toString())
        Log.i("onBindViewHolder ChatroomId", model.chatroomId.toString())
        val firebaseUtil = FirebaseUtil()
        val currentUser = firebaseUtil.getCurrentFirebaseUser()
        Log.i("onBindViewHolder currentUser", currentUser?.email.toString())
        val targetEmail: String = firebaseUtil.getOtherUserFromChatroom(model.userIds!!)

        firebaseUtil.getOtherUser(targetEmail) { otherUser ->
            holder.usernameText.text = otherUser.username
            val lastMessageSentByMe = model.lastMessageSenderId == currentUser?.email
            holder.lastMessageText.text =
                if (lastMessageSentByMe) "You : ${model.lastMessage}" else model.lastMessage
            holder.lastMessageTime.text =
                firebaseUtil.timestampToString(model.lastMessageTimestamp!!)

            GlobalScope.launch {
                val profile = firebaseUtil.getOtherProfileData(targetEmail)
                if (profile != null) {
                    val im1 = profile.pic1
                    if (!im1.isNullOrEmpty()) {
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
                } else {
                    Log.e("RecentChatRecyclerAdapter", "Profile is null")
                }
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                Log.i("onBindViewHolder targetEmail", targetEmail)

                val replyObject = JSONObject()
                replyObject.put("userId", otherUser.userId)
                replyObject.put("username", otherUser.username)
                replyObject.put("email", otherUser.email)

                intent.putExtra("reply", replyObject.toString())
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomModelViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatroomModelViewHolder(view)
    }

    inner class ChatroomModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.user_name_text)
        val lastMessageText: TextView = itemView.findViewById(R.id.last_message_text)
        val lastMessageTime: TextView = itemView.findViewById(R.id.last_message_time_text)
        val pic: ImageView = itemView.findViewById(R.id.iv1)
    }
}
