package com.app.sukasuka.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.sukasuka.base.FragmentBase
import com.app.sukasuka.databinding.FragmentHomeBinding
import com.app.sukasuka.model.PostModel
import com.app.sukasuka.model.StoryModel
import com.app.sukasuka.ui.activity.NewMessageActivity
import com.app.sukasuka.ui.adapter.PostAdapter
import com.app.sukasuka.ui.adapter.StoryAdapter
import com.app.sukasuka.utils.PermissionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : FragmentBase<FragmentHomeBinding>() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<PostModel>? = null
    private var followingList: MutableList<String>? = null
    private var storyAdapter: StoryAdapter? = null
    private var storyList: MutableList<StoryModel>? = null

    override fun getViewBindingInflater(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun subscribeUI() {
        //Recycler View Home
        val recyclerView = binding.recyclerViewHome
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = PostAdapter(requireContext(), postList as ArrayList<PostModel>)
        recyclerView.adapter = postAdapter
        recyclerView.setHasFixedSize(true)

        //Recycler View Story
        val recyclerViewStory = binding.recyclerViewStory
        val linearLayoutManager2 = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        ) // To set scroll with Horizontal
        recyclerViewStory.layoutManager = linearLayoutManager2

        storyList = ArrayList()
        storyAdapter = StoryAdapter(requireContext(), storyList as ArrayList<StoryModel>)
        recyclerViewStory.adapter = storyAdapter

        checkFollowings()
        onClick()
    }

    private fun onClick() {

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val profileId = pref?.getString("profileId", "none").toString()

        binding.directMessage.setOnClickListener {
            val intent = Intent(context, NewMessageActivity::class.java)
            startActivity(intent)

        }
    }

    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (followingList as ArrayList<String>).clear()

                    for (snapshot in p0.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }

                    retrievePosts()
                    retrieveStories()
                }
                else {
                    startAnimation(true)
                    binding.emptyPost.visibility = View.VISIBLE
                    binding.recyclerViewHome.visibility = View.GONE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()

                for (snapshot in p0.children) {
                    val post = snapshot.getValue(PostModel::class.java)

                    for (id in (followingList as ArrayList<String>)) {
                        if (post!!.publisher == id) {
                            postList!!.add(post)
                        }

                        postAdapter!!.notifyDataSetChanged()
                    }
                }

                if (postList.isNullOrEmpty()) {
                    startAnimation(true)
                    binding.emptyPost.visibility = View.VISIBLE
                    binding.recyclerViewHome.visibility = View.GONE
                }
                else {
                    startAnimation(false)
                    binding.emptyPost.visibility = View.GONE
                    binding.recyclerViewHome.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun startAnimation(isStartAnim: Boolean) {
        if (isStartAnim) {
            binding.animLoadingViewHome.visibility = View.VISIBLE
            binding.animLoadingViewHome.setAnimation("13525-empty.json")
            binding.animLoadingViewHome.playAnimation()
            binding.animLoadingViewHome.loop(true)
        }
    }

    private fun retrieveStories() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")

        storyRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                val currentTime = System.currentTimeMillis()

                (storyList as ArrayList<StoryModel>).clear()

                (storyList as ArrayList<StoryModel>).add(
                    StoryModel(
                        "",
                        0,
                        0,
                        "",
                        FirebaseAuth.getInstance().currentUser?.uid
                    )
                )

                for (id in followingList!!) {
                    var countStory = 0
                    var story: StoryModel? = null

                    for (snapshot in p0.child(id).children) {
                        story = snapshot.getValue(StoryModel::class.java)

                        if (currentTime > story!!.timestart!! && currentTime < story.timeend!!) {
                            countStory++
                        }
                    }

                    if (countStory > 0) {
                        (storyList as ArrayList<StoryModel>).add(story!!)
                    }
                }

                storyAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}