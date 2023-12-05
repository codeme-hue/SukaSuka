package com.app.sukasuka.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityCommentsBinding
import com.app.sukasuka.model.CommentModel
import com.app.sukasuka.model.UserModel
import com.app.sukasuka.ui.adapter.CommentsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CommentsActivity : ActivityBase<ActivityCommentsBinding>() {

    private var postId: String? = null
    private var publisherId: String? = null
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentsAdapter? = null
    private var commentList: MutableList<CommentModel>? = null

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityCommentsBinding {
        return ActivityCommentsBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_comments)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComments()
        getPostImage()

        binding.postComment.setOnClickListener{
            if (binding.addComment.text.toString() == "")
            {
                Toast.makeText(this, "Please write your comment.", Toast.LENGTH_LONG).show()
            }
            else
            {
                addComment()
            }
        }

        binding.btnBackComment.setOnClickListener {
            finish()
        }

    }

    private fun userInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    val user = p0.getValue(UserModel::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(binding.profileImageComment)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun addComment()
    {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId!!)
        val commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = binding.addComment.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        //For Notification Use.
        addNotification()

        binding.addComment.text.clear()
    }

    private fun readComments()
    {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId!!)

        commentsRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    commentList!!.clear()

                    for (snapshot in p0.children)
                    {
                        val comment = snapshot.getValue(CommentModel::class.java)
                        commentList!!.add(comment!!)
                    }

                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getPostImage()
    {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId!!).child("postimage")

        postRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    val image = p0.value.toString()

                    Picasso.get().load(image).placeholder(R.drawable.profile).into(binding.postImageComment)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun addNotification()
    {
        val notifRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(publisherId!!)
        val notifMap = HashMap<String, Any>()

        notifMap["userid"] = firebaseUser!!.uid
        notifMap["text"] = "Commented: " + binding.addComment.text.toString()
        notifMap["postid"] = postId!!
        notifMap["ispost"] = true

        notifRef.push().setValue(notifMap)
    }
}