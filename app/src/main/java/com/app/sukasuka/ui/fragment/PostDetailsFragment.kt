package com.app.sukasuka.ui.fragment

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.sukasuka.base.FragmentBase
import com.app.sukasuka.databinding.FragmentPostDetailsBinding
import com.app.sukasuka.model.PostModel
import com.app.sukasuka.ui.activity.MainActivity
import com.app.sukasuka.ui.adapter.PostAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetailsFragment : FragmentBase<FragmentPostDetailsBinding>() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<PostModel>? = null
    private var postId: String = ""

    override fun getViewBindingInflater(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPostDetailsBinding {
        return FragmentPostDetailsBinding.inflate(inflater, container, false)
    }

    override fun subscribeUI() {
        val preferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        if (preferences != null) {
            postId = preferences.getString("postId", "none")!!
        }

        val recyclerView = binding.recyclerViewPostDetails
        recyclerView.setHasFixedSize(true)

        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<PostModel>) }
        recyclerView.adapter = postAdapter

        retrievePosts()

        binding.btnBackPostDetail.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun retrievePosts()
    {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                postList?.clear()
                val post = p0.getValue(PostModel::class.java)
                postList!!.add(post!!)
                postAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}