package com.app.sukasuka.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityStoryBinding
import com.app.sukasuka.model.StoryModel
import com.app.sukasuka.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView

class StoryActivity : ActivityBase<ActivityStoryBinding>(), StoriesProgressView.StoriesListener {

    private var currentUserId: String = ""
    private var userId: String? = ""
    private var pressTime = 0L
    private var limit = 500L
    var imageList: List<String>? = null
    var storyIdsList: List<String>? = null
    var storiesProgressView: StoriesProgressView? = null
    var counter = 0

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityStoryBinding {
        return ActivityStoryBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userId")

        storiesProgressView = findViewById(R.id.stories_progress)

        binding.layoutSeen.visibility = View.GONE
        binding.storyDelete.visibility = View.GONE

        if (userId == currentUserId)
        {
            binding.layoutSeen.visibility = View.VISIBLE
            binding.storyDelete.visibility = View.VISIBLE
        }

        getStories(userId!!)
        userInfo(userId!!)

        val reverse: View = findViewById(R.id.reverse)
        reverse.setOnClickListener { storiesProgressView?.reverse() }
        reverse.setOnTouchListener(onTouchListener)

        val skip: View = findViewById(R.id.skip)
        skip.setOnClickListener { storiesProgressView?.skip() }
        skip.setOnTouchListener(onTouchListener)

        binding.layoutSeen.setOnClickListener {
            val intent = Intent(this, ShowUsersActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("storyid", storyIdsList!![counter])
            intent.putExtra("title", "views")
            startActivity(intent)
        }

        binding.storyDelete.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().reference
                .child("Story")
                .child(userId!!)
                .child(storyIdsList!![counter])

            ref.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    Toast.makeText(this, "Deleted...", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { _, motionEvent ->

        when (motionEvent.action)
        {
            MotionEvent.ACTION_DOWN ->
            {
                pressTime = System.currentTimeMillis()
                storiesProgressView?.pause()

                return@OnTouchListener false
            }

            MotionEvent.ACTION_UP   ->
            {
                val now = System.currentTimeMillis()
                storiesProgressView?.resume()

                return@OnTouchListener limit < now - pressTime
            }
        }

        false
    }

    private fun seenNumber(storyId: String)
    {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId!!)
            .child(storyId)
            .child("views")

        ref.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                binding.seenNumber.text = getString(R.string.STORY_ACTIVITY_LBL_SEEN_NUMBER, p0.childrenCount.toString())
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun addViewToStory(storyId: String)
    {
        FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId!!)
            .child(storyId)
            .child("views")
            .child(currentUserId)
            .setValue(true)
    }

    private fun userInfo(userId: String)
    {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    val user = p0.getValue(UserModel::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(binding.storyProfileImage)

                    binding.storyUsername.text = user.getUsername()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getStories(userId: String)
    {
        imageList = ArrayList()
        storyIdsList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference
            .child("Story")
            .child(userId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot)
            {
                (imageList as ArrayList<String>).clear()
                (storyIdsList as ArrayList<String>).clear()

                for (snapshot in p0.children)
                {
                    val story: StoryModel? = snapshot.getValue(StoryModel::class.java)
                    val timeCurrent = System.currentTimeMillis()

                    if (timeCurrent > story?.getTimeStart()!! && timeCurrent < story.getTimeEnd()!!)
                    {
                        (imageList as ArrayList<String>).add(story.getImageUrl()!!)
                        (storyIdsList as ArrayList<String>).add(story.getStoryId()!!)
                    }
                }

                storiesProgressView?.setStoriesCount((imageList as ArrayList<String>).size)
                storiesProgressView?.setStoryDuration(6000L)
                storiesProgressView?.setStoriesListener(this@StoryActivity)
                storiesProgressView?.startStories(counter)
                Picasso.get().load(imageList?.get(counter)).placeholder(R.color.blackColor).into(binding.imageStory)

                addViewToStory(storyIdsList!![counter])
                seenNumber(storyIdsList!![counter])
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    override fun onComplete()
    {
        //Desmond ToDo: Add Continue Viewing Story Feature.
        finish()
    }

    override fun onPrev()
    {
        if (counter - 1 < 0) return
        Picasso.get().load(imageList!![--counter]).placeholder(R.color.blackColor).into(binding.imageStory)
        seenNumber(storyIdsList!![counter])
    }

    override fun onNext()
    {
        Picasso.get().load(imageList!![++counter]).placeholder(R.color.blackColor).into(binding.imageStory)
        addViewToStory(storyIdsList!![counter])
        seenNumber(storyIdsList!![counter])
    }

    override fun onDestroy()
    {
        super.onDestroy()
        storiesProgressView?.destroy()
    }

    override fun onResume()
    {
        super.onResume()
        storiesProgressView?.resume()
    }

    override fun onPause()
    {
        super.onPause()
        storiesProgressView?.pause()
    }
}