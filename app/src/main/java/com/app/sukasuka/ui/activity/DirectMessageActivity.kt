package com.app.sukasuka.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityDirectMessageBinding
import com.app.sukasuka.model.ChatMessage
import com.app.sukasuka.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class DirectMessageActivity : ActivityBase<ActivityDirectMessageBinding>() {

    private var receiverUID: String? = ""
    private var senderUID: String? = ""
    private var receiverUsername: String? = ""
    private lateinit var firebaseUser: FirebaseUser

    val adapter = GroupAdapter<ViewHolder>()

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityDirectMessageBinding {
        return ActivityDirectMessageBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        senderUID = intent.getStringExtra("receiverUID")
        receiverUsername = intent.getStringExtra("receiverName")

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        receiverUID = firebaseUser.uid

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = receiverUsername
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.recyclerviewChatLog.adapter = adapter

        listenForMessages()

        binding.sendButtonChatLog.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages() {

        binding.swiperefresh.isRefreshing = true
        binding.swiperefresh.isEnabled = true

        val fromId = senderUID
        val toId = receiverUsername
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("DirectMessageActivity", "has children: " + dataSnapshot.hasChildren())
                if (!dataSnapshot.hasChildren()) {

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("DirectMessageActivity", "database error: " + databaseError.message)
            }
        })

        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                dataSnapshot.getValue(ChatMessage::class.java)?.let {
                    if (it.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(it.text, it.timestamp))
                    } else {
                        adapter.add(ChatToItem(it.text, it.timestamp))
                    }
                }
                binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount - 1)
                binding.swiperefresh.isRefreshing = false
                binding.swiperefresh.isEnabled = false
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun performSendMessage() {
        val text = binding.edittextChatLog.text.toString()
        if (text.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val fromId = senderUID
        val toId = receiverUsername

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId!!, toId!!, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("DirectMessageActivity", "Saved our chat message: ${reference.key}")
                binding.edittextChatLog.text.clear()
                binding.recyclerviewChatLog.smoothScrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

    }

}

class ChatFromItem(val text: String, val timestamp: Long) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

//        viewHolder.itemView.textview_from_row.text = text
//        viewHolder.itemView.from_msg_time.text = getFormattedTimeChatLog(timestamp)

        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text
        viewHolder.itemView.findViewById<TextView>(R.id.from_msg_time).text = DateUtils.getFormattedTimeChatLog(timestamp)

//        val targetImageView = viewHolder.itemView.imageview_chat_from_row

//        if (!user.profileImageUrl!!.isEmpty()) {
//
//            val requestOptions = RequestOptions().placeholder(R.drawable.no_image2)
//
//
//            Glide.with(targetImageView.context)
//                .load(user.profileImageUrl)
//                .thumbnail(0.1f)
//                .apply(requestOptions)
//                .into(targetImageView)
//
//        }
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text: String, val timestamp: Long) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text
        viewHolder.itemView.findViewById<TextView>(R.id.to_msg_time).text = DateUtils.getFormattedTimeChatLog(timestamp)
    }

    override fun getLayout(): Int {
        return  R.layout.chat_to_row
    }

}