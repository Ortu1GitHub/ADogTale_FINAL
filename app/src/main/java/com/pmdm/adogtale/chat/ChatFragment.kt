package com.pmdm.adogtale.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.pmdm.adogtale.R
import com.pmdm.adogtale.adapter.RecentChatRecyclerAdapter
import com.pmdm.adogtale.model.ChatroomModel
import com.pmdm.adogtale.utils.FirebaseUtil

class ChatFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentChatRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.recyler_view)
        setupRecyclerView()
        Log.i("chatFragment", "onCreateView")
        return view
    }

    private fun setupRecyclerView() {
        val firebaseUtil = FirebaseUtil()
        val currentUser = firebaseUtil.getCurrentFirebaseUser()
        val query: Query = firebaseUtil.allChatroomCollectionReference()
            .whereArrayContains("userIds", currentUser?.email.toString())
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatroomModel>()
            .setQuery(query, ChatroomModel::class.java)
            .build()

        adapter = RecentChatRecyclerAdapter(options, requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        adapter.startListening()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
        Log.i("chatFragment", "onStart metodo")
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        Log.i("chatFragment", "onResume metodo")
    }
}
