package com.app.sukasuka.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityDetailGroupMessageBinding
import com.app.sukasuka.model.GroupMessage
import com.app.sukasuka.model.UserModel
import com.app.sukasuka.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class DetailGroupMessageActivity : ActivityBase<ActivityDetailGroupMessageBinding>() {

//    private var toId: String? = ""
//    private var fromId: String? = ""
//    private var receiverUsername: String? = ""
//    private var receiverData: UserModel? = null
//    private var senderData: UserModel? = null

    private var groupData: GroupMessage? = null
    private var senderName: String? = ""

    val adapter = GroupAdapter<ViewHolder>()

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityDetailGroupMessageBinding {
        return ActivityDetailGroupMessageBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        groupData = intent.getParcelableExtra("group_data")

        Log.d("receiverData", groupData.toString())

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = groupData?.groupName
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


    private fun listenForMessages() {

        binding.swiperefresh.isRefreshing = true
        binding.swiperefresh.isEnabled = true

        val ref =  FirebaseDatabase.getInstance().getReference("Messages/GroupMessage/unsia")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("GroupMessageActivity", "has children: " + dataSnapshot.hasChildren())
                if (!dataSnapshot.hasChildren()) {

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("GroupMessageActivity", "database error: " + databaseError.message)
            }
        })

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                dataSnapshot.getValue(GroupMessage::class.java)?.let {
                    if (it.senderId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatGroupFromItem(it.senderName, it.message, it.timestamp))
                    }
                    else {
                        adapter.add(ChatGroupToItem(it.senderName, it.message, it.timestamp))
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

        val reference = FirebaseDatabase.getInstance().getReference("Messages/GroupMessage/unsia").push()

        val groupMessage =
            GroupMessage(reference.key!!, FirebaseAuth.getInstance().uid!!, senderName!!, groupData!!.groupName, text, System.currentTimeMillis() / 1000)
        reference.setValue(groupMessage)
            .addOnSuccessListener {
                binding.edittextChatLog.text.clear()
//                binding.recyclerviewChatLog.smoothScrollToPosition(adapter.itemCount - 1)
            }

    }

    private fun senderInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().uid!!)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
              p0.getValue(UserModel::class.java).let {
                  senderName = it?.username
              }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

}

class ChatGroupFromItem(val username: String, val text: String, val timestamp: Long) :
    Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text
        viewHolder.itemView.findViewById<TextView>(R.id.fromUserName).text = username
        viewHolder.itemView.findViewById<TextView>(R.id.from_msg_time).text =
            DateUtils.getFormattedTimeChatLog(timestamp)
        val targetImageView =
            viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_from_row)
        targetImageView.visibility = View.GONE

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatGroupToItem(val username: String, val text: String, private val timestamp: Long) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text
        viewHolder.itemView.findViewById<TextView>(R.id.toUserName).text = username
        viewHolder.itemView.findViewById<TextView>(R.id.to_msg_time).text =
            DateUtils.getFormattedTimeChatLog(timestamp)

        val targetImageView =
            viewHolder.itemView.findViewById<ImageView>(R.id.imageview_chat_to_row)
        targetImageView.visibility = View.GONE

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}