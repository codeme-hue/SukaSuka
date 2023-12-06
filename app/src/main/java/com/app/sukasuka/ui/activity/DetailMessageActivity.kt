package com.app.sukasuka.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityDirectMessageBinding
import com.app.sukasuka.model.ChatMessage
import com.app.sukasuka.model.UserModel
import com.app.sukasuka.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class DetailMessageActivity : ActivityBase<ActivityDirectMessageBinding>() {

    private var toId: String? = ""
    private var fromId: String? = ""
    private var receiverUsername: String? = ""
    private var receiverData: UserModel? = null
    private var senderData: UserModel? = null

    val adapter = GroupAdapter<ViewHolder>()

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityDirectMessageBinding {
        return ActivityDirectMessageBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        receiverData = intent.getParcelableExtra("receiverData")
        toId = receiverData?.uid
        receiverUsername = receiverData?.username

        Log.d("receiverData", receiverData?.username!!)

        fromId = FirebaseAuth.getInstance().currentUser!!.uid

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = receiverUsername
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.recyclerviewChatLog.adapter = adapter

        senderInfo()
        listenForMessages()
        initClick()
    }

    private fun initClick() {
        binding.sendButtonChatLog.setOnClickListener {
            performSendMessage()
        }
    }

    private fun senderInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromId!!)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    senderData = p0.getValue(UserModel::class.java)

                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun listenForMessages() {

        binding.swiperefresh.isRefreshing = true
        binding.swiperefresh.isEnabled = true

        val ref = FirebaseDatabase.getInstance().getReference("Messages")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("DirectMessageActivity", "has children: " + dataSnapshot.hasChildren())
                if (!dataSnapshot.hasChildren()) {

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("DirectMessageActivity", "database error: " + databaseError.message)
            }
        })

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                dataSnapshot.getValue(ChatMessage::class.java)?.let {
                    if (it.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(senderData!!, it.text, it.timestamp))
                    }
                    else {
                        adapter.add(ChatToItem(receiverData!!, it.text, it.timestamp))
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

        val reference = FirebaseDatabase.getInstance().getReference("Messages").push()

        val convertJsonSenderData = Gson().toJson(senderData)
        val convertJsonReceiverData = Gson().toJson(receiverData)
//        val senderObject = Gson().fromJson(convertJsonSenderData,UserModel::class.java)

        val chatMessage =
            ChatMessage(text, fromId!!, toId!!, convertJsonSenderData, convertJsonReceiverData,System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("DirectMessageActivity", "Saved our chat message: ${reference.key}")
                binding.edittextChatLog.text.clear()
//                binding.recyclerviewChatLog.smoothScrollToPosition(adapter.itemCount - 1)
            }

    }

}

class ChatFromItem(val user: UserModel, val text: String, val timestamp: Long) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text
        viewHolder.itemView.findViewById<TextView>(R.id.from_msg_time).text =
            DateUtils.getFormattedTimeChatLog(timestamp)

        val targetImageView =
            viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_from_row)

        Picasso.get().load(user.image).placeholder(R.drawable.profile).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val user: UserModel, val text: String, private val timestamp: Long) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text
        viewHolder.itemView.findViewById<TextView>(R.id.to_msg_time).text =
            DateUtils.getFormattedTimeChatLog(timestamp)

        val targetImageView =
            viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_to_row)

        Picasso.get().load(user.image).placeholder(R.drawable.profile).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}