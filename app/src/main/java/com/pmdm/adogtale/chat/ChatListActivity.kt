package com.pmdm.adogtale.chat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.pmdm.adogtale.R
import com.pmdm.adogtale.adapter.RecentChatRecyclerAdapter
import com.pmdm.adogtale.model.ChatroomModel
import com.pmdm.adogtale.ui.topbar.chat_list.ChatListTobar
import com.pmdm.adogtale.utils.FirebaseUtil

class ChatListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentChatRecyclerAdapter
    private lateinit var chatListTobar: ChatListTobar;
    private val firebaseUtil: FirebaseUtil = FirebaseUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        recyclerView = findViewById(R.id.recyler_view)
        setupRecyclerView()
        Log.i("ChatListActivity", "onCreate")

        chatListTobar = ChatListTobar(this)
        chatListTobar.configureTopbar()

        firebaseUtil.getCurrentUser { currentUser ->

            firebaseUtil.getCountUnCheckedMatches(currentUser.email)
                .thenAccept { result ->
                    if (result > 0) {
                        this.chatListTobar.showBadge(ChatListTobar.ChatListTopbarOption.MATCHES)
                    }
                    Log.i("CardSwipeActivity", "finished count of matches: " + result)
                }
        }

    }

    private fun setupRecyclerView() {
        val firebaseUtil = FirebaseUtil()
        val currentUser = firebaseUtil.getCurrentFirebaseUser()
        val query: Query = firebaseUtil.allChatroomCollectionReference()
            .whereArrayContains("userIds", currentUser?.email.toString())
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        query.get().addOnSuccessListener { documents ->
            for (document in documents) {
                Log.i("setupRecyclerView query", "${document.id} => ${document.data}")
            }
        }.addOnFailureListener { exception ->
            Log.w("setupRecyclerView Error", "Error getting documents: ", exception)
        }
        val options = FirestoreRecyclerOptions.Builder<ChatroomModel>()
            .setQuery(query, ChatroomModel::class.java)
            .build()
        adapter = RecentChatRecyclerAdapter(options, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.startListening()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
        Log.i("ChatListActivity", "onStart metodo")
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        Log.i("ChatListActivity", "onResume metodo")
    }
}
