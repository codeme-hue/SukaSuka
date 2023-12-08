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

    var idList: List<String>? = null
    var userList: List<UserModel>? = null
    private var profileId: String = ""

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityNewMessageBinding {
        return ActivityNewMessageBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        idList = ArrayList()
        userList = ArrayList()

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Direct Messages"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        fetchUserMessage()

        binding.swiperefresh.setOnRefreshListener {
            fetchUserMessage()
        }

        binding.fabAddChat.setOnClickListener {
            AddUserMessageDialog().show(supportFragmentManager, null)
        }

    }


    private fun fetchUserMessage() {
        binding.swiperefresh.isRefreshing = true

        val ref = FirebaseDatabase.getInstance().reference.child("UserMessages/${FirebaseAuth.getInstance().currentUser?.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                dataSnapshot.children.forEach { child ->
                    child.children .forEach {
                        it.getValue(ChatMessage::class.java)?.let { dataChat ->
                            if (dataChat.fromId != FirebaseAuth.getInstance().currentUser?.uid!!) {
                                adapter.add(UserItem(dataChat, this@NewMessageActivity))
                            }
                        }
                    }
                }

                adapter.setOnItemClickListener { item, view ->
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

                binding.recyclerviewNewmessage.adapter = adapter
                binding.swiperefresh.isRefreshing = false
            }

        })
    }
}

class UserItem(val chat: ChatMessage, val context: Context) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val json = chat.senderData.trim('"')
        val receiverObject = Gson().fromJson(json, UserModel::class.java)

        viewHolder.itemView.findViewById<TextView>(R.id.user_name_message).text = receiverObject.username
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