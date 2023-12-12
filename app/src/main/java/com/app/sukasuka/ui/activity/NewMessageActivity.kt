package com.app.sukasuka.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityNewMessageBinding
import com.app.sukasuka.model.ChatMessage
import com.app.sukasuka.model.GroupMessage
import com.app.sukasuka.model.UserModel
import com.app.sukasuka.ui.adapter.UserDirectMessageAdapter
import com.app.sukasuka.ui.dialogfragment.AddUserMessageDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class NewMessageActivity : ActivityBase<ActivityNewMessageBinding>() {

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityNewMessageBinding {
        return ActivityNewMessageBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Direct Messages"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        fetchUserMessage()
        fetchGroupMessage()

//        binding.swiperefresh.setOnRefreshListener {
//            fetchUserMessage()
//        }

        binding.fabAddChat.setOnClickListener {
            AddUserMessageDialog().show(supportFragmentManager, null)
        }

    }


    private fun fetchUserMessage() {
//        binding.swiperefresh.isRefreshing = true

        val ref =
            FirebaseDatabase.getInstance().reference.child("Messages/DirectMessage/${FirebaseAuth.getInstance().currentUser?.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val directMessageAdapter = GroupAdapter<ViewHolder>()
                val latestMessagesMap =
                    HashMap<String, ChatMessage>() // HashMap untuk menyimpan pesan terbaru dari setiap user

                dataSnapshot.children.forEach { child ->
                    child.children.forEach {
                        it.getValue(ChatMessage::class.java)?.let { dataChat ->
                            if (dataChat.fromId != FirebaseAuth.getInstance().currentUser?.uid!!) {
                                // Periksa apakah pesan terbaru dari pengguna sudah ada di HashMap
                                val existingLatestMessage = latestMessagesMap[dataChat.fromId]
                                if (existingLatestMessage == null || dataChat.timestamp > existingLatestMessage.timestamp) {
                                    latestMessagesMap[dataChat.fromId] =
                                        dataChat // Update pesan terbaru dari pengguna
                                }
                            }
                        }
                    }
                }

                // Tambahkan pesan terbaru dari setiap pengguna ke adapter
                latestMessagesMap.values.forEach { latestMessage ->
                    directMessageAdapter.add(UserItem(latestMessage, this@NewMessageActivity))
                }

                directMessageAdapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val jsonSender = userItem.chat.receiverData.trim('"')
                    val senderObject = Gson().fromJson(jsonSender, UserModel::class.java)

                    val jsonReceiver = userItem.chat.senderData.trim('"')
                    val receiverObject = Gson().fromJson(jsonReceiver, UserModel::class.java)

                    val intent = Intent(view.context, DetailMessageActivity::class.java)
                    intent.putExtra("senderData", senderObject)
                    intent.putExtra("receiverData", receiverObject)

                    startActivity(intent)
                    finish()
                }

                binding.recyclerviewNewmessage.adapter = directMessageAdapter
//                binding.swiperefresh.isRefreshing = false
            }

        })
    }

    private fun fetchGroupMessage() {
//        binding.swiperefresh.isRefreshing = true

        val ref =
            FirebaseDatabase.getInstance().reference.child("Messages/GroupMessage/unsia")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val groupMessageAdapter = GroupAdapter<ViewHolder>()
                val latestMessagesMap =
                    HashMap<String, GroupMessage>() // HashMap untuk menyimpan pesan terbaru dari setiap user

                dataSnapshot.children.forEach {
                    it.getValue(GroupMessage::class.java)?.let { dataChat ->
                        // Periksa apakah pesan terbaru dari pengguna sudah ada di HashMap
                        val existingLatestMessage = latestMessagesMap[dataChat.senderId]
                        if (existingLatestMessage == null || dataChat.timestamp > existingLatestMessage.timestamp) {
                            latestMessagesMap[dataChat.senderId] = dataChat // Update pesan terbaru dari pengguna
                        }
                    }
                }


                groupMessageAdapter.add(GroupItem(latestMessagesMap.getValue(latestMessagesMap.keys.last()), this@NewMessageActivity))

                groupMessageAdapter.setOnItemClickListener { item, view ->
                    val groupItem = item as GroupItem
                    val intent = Intent(view.context, DetailGroupMessageActivity::class.java)
                    intent.putExtra("group_data", groupItem.group)

                    startActivity(intent)
                    finish()
                }

                binding.recyclerviewGroupMessage.adapter = groupMessageAdapter
//                binding.swiperefresh.isRefreshing = false
            }

        })
    }
}

class UserItem(val chat: ChatMessage, val context: Context) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val json = chat.senderData.trim('"')
        val receiverObject = Gson().fromJson(json, UserModel::class.java)

        viewHolder.itemView.findViewById<TextView>(R.id.user_name_message).text =
            receiverObject.username
        viewHolder.itemView.findViewById<TextView>(R.id.message).text = chat.text

        val targetImageView =
            viewHolder.itemView.findViewById<ImageView>(R.id.user_profile_image_message)

        if (receiverObject.image != null) {
            Picasso.get().load(receiverObject.image).placeholder(R.drawable.profile)
                .into(targetImageView)
        }

    }

    override fun getLayout(): Int {
        return R.layout.user_item_message_layout
    }
}

class GroupItem(val group: GroupMessage, val context: Context) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

//        val json = chat.senderData.trim('"')
//        val receiverObject = Gson().fromJson(json, UserModel::class.java)

        viewHolder.itemView.findViewById<TextView>(R.id.groupName).text =
            group.groupName
        viewHolder.itemView.findViewById<TextView>(R.id.groupMessage).text = group.message

//        val targetImageView =
//            viewHolder.itemView.findViewById<ImageView>(R.id.groupProfile)
//
//        if (receiverObject.image != null) {
//            Picasso.get().load(receiverObject.image).placeholder(R.drawable.profile)
//                .into(targetImageView)
//        }

    }

    override fun getLayout(): Int {
        return R.layout.user_item_group_layout
    }
}