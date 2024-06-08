package com.pmdm.adogtale.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.pmdm.adogtale.R
import com.pmdm.adogtale.adapter.ChatRecyclerAdapter
import com.pmdm.adogtale.description.DescriptionActivity
import com.pmdm.adogtale.model.ChatMessageModel
import com.pmdm.adogtale.model.ChatroomModel
import com.pmdm.adogtale.model.User
import com.pmdm.adogtale.push_notification.PushNotificationSender
import com.pmdm.adogtale.utils.FirebaseUtil
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.*

class ChatActivity : AppCompatActivity() {
    val firebaseUtil = FirebaseUtil()
    var chatroomId: String? = null
    var chatroomModel: ChatroomModel? = null
    var adapter: ChatRecyclerAdapter? = null
    var messageInput: EditText? = null
    var backBtn: ImageButton? = null
    var otherUsername: TextView? = null
    var recyclerView: RecyclerView? = null
    var imageView: ImageView? = null
    val fUser = firebaseUtil.getCurrentFirebaseUser()
    var targetEmail: String? = null
    var otherUser: User? = null
    val pushNotificationSender: PushNotificationSender = PushNotificationSender();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        imageView = findViewById(R.id.iv1)
        //get User
        val replyToInformation = intent.getStringExtra("reply") as String
        val replyToObj = JSONObject(replyToInformation)
        targetEmail = replyToObj.getString("email")
        Log.i("targetEmail2", targetEmail.toString())
        //get User
        firebaseUtil.getOtherUser(targetEmail!!) { user ->
            otherUser = user
            Log.i("otherUserNameChat", otherUser!!.name.toString())
            otherUsername!!.setText(otherUser?.username)
            Log.i("username", otherUser?.username.toString())
        }
        GlobalScope.launch {
            val profile = firebaseUtil.getOtherProfileData(targetEmail!!)
            if (profile != null) {
                val im1 = profile.pic1
                if (!im1.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Picasso.get()
                            .load(im1)
                            .fit()
                            .centerCrop()
                            .into(imageView)
                    }
                }
            } else {
                Log.e("RecentChatRecyclerAdapter", "Profile is null")
            }
        }
        chatroomId = firebaseUtil.getChatroomId(fUser?.email.toString(), targetEmail!!)
        messageInput = findViewById(R.id.chat_message_input)

        val sendMessageBtn: ImageButton = findViewById(R.id.message_send_btn) as ImageButton
        backBtn = findViewById(R.id.back_btn)
        otherUsername = findViewById(R.id.other_username)
        recyclerView = findViewById(R.id.chat_recycler_view)

        imageView!!.setOnClickListener {
            val intentForDescription = Intent(this, DescriptionActivity::class.java)
            intentForDescription.putExtra("targetEmail", targetEmail)
            startActivity(intentForDescription)
        }

        backBtn?.setOnClickListener { v: View? -> onBackPressed() }

        sendMessageBtn.setOnClickListener(View.OnClickListener { v: View? ->
            val message = messageInput!!.getText().toString().trim { it <= ' ' }
            if (message.isEmpty()) return@OnClickListener
            sendMessageToUser(message)
        })

        getOrCreateChatroomModel()
        setupChatRecyclerView()

        firebaseUtil.getCurrentUser { user ->

            firebaseUtil.putReadAllMessages(user.email, chatroomId!!)

            messageInput!!.setOnClickListener { _ ->
                firebaseUtil.putReadAllMessages(user.email, chatroomId!!)
            }
        }
        Log.i("onCreate msg", "final")
    }

    fun setupChatRecyclerView() {
        Log.i("setupChat", "dentro")
        val query: Query = firebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
        Log.i("setupChat query", query.toString())
        val options: FirestoreRecyclerOptions<ChatMessageModel?> =
            FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel::class.java).build()
        Log.i("setupChat options", options.toString())
        adapter = ChatRecyclerAdapter(options, applicationContext)
        Log.i("setupChat adapter", adapter.toString())
        val manager = LinearLayoutManager(this)
        Log.i("setupChat manager", manager.toString())
        manager.reverseLayout = true
        recyclerView!!.layoutManager = manager
        recyclerView!!.adapter = adapter
        adapter!!.startListening()
        adapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView!!.smoothScrollToPosition(0)
                Log.i("setupChat itemrange inserted", "AQUI")
            }
        })
    }

    fun sendMessageToUser(message: String?) {
        Log.i("msg message", message.toString())
        chatroomModel?.lastMessageTimestamp = Timestamp.now()
        Log.i("msg chatroom", chatroomModel?.chatroomId.toString())
        val userIdsList = chatroomModel?.userIds?.toMutableList() ?: mutableListOf()
        Log.i("msg userIdsList", userIdsList.toString())
        chatroomModel?.userIds = userIdsList
        Log.i("msg chatroom IDs", chatroomModel?.userIds.toString())
        chatroomModel?.lastMessage = message
        Log.i("msg chatroom ID", chatroomId.toString())
        Log.i("msg chatroom Model", chatroomModel.toString())
        firebaseUtil.getChatroomReference(chatroomId).set(chatroomModel!!)

        val chatMessageModel =
            ChatMessageModel(message, fUser?.email.toString(), Timestamp.now(), false)
        Log.i("msg chatMessageModel", chatMessageModel.senderId.toString())
        Log.i("msg chatMessageModel", chatMessageModel.message.toString())
        Log.i("msg chatMessageModel", chatMessageModel.timestamp.toString())
        firebaseUtil.getChatroomMessageReference(chatroomId)
            .add(chatMessageModel)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }

                Log.i("task", "dentro")
                Log.i("beforeSendNotification", "username: " + otherUser!!.username);
                Log.i("beforeSendNotification", "email: " + otherUser!!.email);
                messageInput!!.setText("")

                firebaseUtil.currentUserDetails()
                    .get()
                    .addOnCompleteListener { currentUserTask ->

                        if (!currentUserTask.isSuccessful()) {
                            return@addOnCompleteListener;
                        }

                        firebaseUtil.getCurrentUser { currentUser ->
                            pushNotificationSender.sendNotification(
                                message,
                                currentUser,
                                otherUser!!
                            )
                        }
                    }

            }
    }

    fun getOrCreateChatroomModel() {
        Log.i("create chatroom", "dentro")
        firebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener { task ->
            Log.i("create chatroom task", task.toString())
            if (task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel::class.java)
                Log.i("create chatroom 1", chatroomModel.toString())
                if (chatroomModel == null) {
                    //first time chat
                    chatroomModel = ChatroomModel(
                        chatroomId,
                        Arrays.asList(fUser?.email.toString(), targetEmail),
                        Timestamp.now(),
                        "",
                    )
                    Log.i("create chatroom 3", otherUser?.email.toString())
                    firebaseUtil.getChatroomReference(chatroomId).set(chatroomModel!!)
                    Log.i("create chatroom 2", chatroomModel.toString())
                }
            }
        }
    }

}