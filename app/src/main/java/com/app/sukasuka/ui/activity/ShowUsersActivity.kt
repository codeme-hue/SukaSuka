package com.app.sukasuka.ui.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityShowUsersBinding
import com.app.sukasuka.model.UserModel
import com.app.sukasuka.ui.adapter.UserAdapter
import com.app.sukasuka.ui.adapter.UserDirectMessageAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : ActivityBase<ActivityShowUsersBinding>() {

    var id: String? = ""
    var title: String? = ""
    var toolbarTitle: String? = ""
    var userAdapter: UserAdapter? = null
    var userDirectMessageAdapter: UserDirectMessageAdapter? = null
    var userList: List<UserModel>? = null
    var idList: List<String>? = null

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityShowUsersBinding {
        return ActivityShowUsersBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        id = intent.getStringExtra("id")
        title = intent.getStringExtra("title")
        toolbarTitle = intent.getStringExtra("toolbar_title")

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = toolbarTitle ?: title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<UserModel>, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()

        when (title) {
            "likes"     -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views"     -> getViews()
        }

    }

    private fun getViews() {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(id!!)
            .child(intent.getStringExtra("storyid")!!)
            .child("views")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (idList as ArrayList<String>)

                for (snapshot in p0.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getLikes() {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(id!!)

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (idList as ArrayList<String>).clear()

                    for (snapshot in p0.children) {
                        (idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun showUsers() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (userList as ArrayList<UserModel>).clear()

                for (snapshot in p0.children) {
                    val user = snapshot.getValue(UserModel::class.java)

                    for (id in idList!!) {
                        if (user?.uid == id) {
                            (userList as ArrayList<UserModel>).add(user)
                        }
                    }
                }

                when (toolbarTitle) {
                    "Direct Messages" -> {
                        userDirectMessageAdapter?.notifyDataSetChanged()
                    }

                    else              -> {
                        userAdapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun  getFollowing() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id!!)
            .child("Following")


        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (idList as ArrayList<String>)

                for (snapshot in p0.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id!!)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (idList as ArrayList<String>)

                for (snapshot in p0.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}