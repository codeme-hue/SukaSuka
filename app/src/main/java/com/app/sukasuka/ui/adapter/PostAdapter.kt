package com.app.sukasuka.ui.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sukasuka.R
import com.app.sukasuka.model.PostModel
import com.app.sukasuka.model.UserModel
import com.app.sukasuka.ui.activity.CommentsActivity
import com.app.sukasuka.ui.activity.MainActivity
import com.app.sukasuka.ui.activity.ShowUsersActivity
import com.app.sukasuka.ui.dialogfragment.AddUserMessageDialog
import com.app.sukasuka.ui.fragment.PostDetailsFragment
import com.app.sukasuka.ui.fragment.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(private val mContext: Context, private val mPost: List<PostModel>) : RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser? = null
    private val secondMillis = 1000
    private val minuteMillis = 60 * secondMillis
    private val hourMillis = 60 * minuteMillis
    private val dayMillis = 24 * hourMillis

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var shareButton: ImageView
        var userName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView
        var dateTime: TextView
        var timeAgo: TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            shareButton = itemView.findViewById(R.id.post_image_share_btn)
            userName = itemView.findViewById(R.id.user_name_post)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)
            dateTime = itemView.findViewById(R.id.lblDateTime)
            timeAgo = itemView.findViewById(R.id.lblTimeAgo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return mPost.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]
        Picasso.get().load(post.postimage).into(holder.postImage)
        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.publisher)
        isLikes(post.postid, holder.likeButton)
        numberOfLikes(holder.likes, post.postid)
        getTotalComments(holder.comments, post.postid)
        checkSavedStatus(post.postid!!, holder.saveButton)

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

            editor.putString("postId", post.postid)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }

        holder.publisher.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

            editor.putString("profileId", post.publisher)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.profileImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

            editor.putString("profileId", post.publisher)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

            editor.putString("postId", post.postid)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }

        if (post.description.equals(""))
        {
            holder.description.visibility = View.GONE
        }
        else
        {
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.description
        }

        if (!post.dateTime.isNullOrEmpty())
        {
            holder.dateTime.text = getDate(post.dateTime!!.toLong(), "dd/MM/yyyy EEE")
            holder.timeAgo.text = getTimeAgo(post.dateTime!!.toLong())
        }

        holder.likeButton.setOnClickListener {
            if (holder.likeButton.tag == "Like")
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postid!!)
                    .child(firebaseUser!!.uid)
                    .setValue(true)

                addNotification(post.publisher!!, post.postid!!)
            }
            else
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postid!!)
                    .child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }
        }

        holder.commentButton.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.postid)
            intentComment.putExtra("publisherId", post.publisher)
            mContext.startActivity(intentComment)
        }

        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.postid)
            intentComment.putExtra("publisherId", post.publisher)
            mContext.startActivity(intentComment)
        }

        holder.saveButton.setOnClickListener {
            if (holder.saveButton.tag == "Save")
            {
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid)
                    .child(post.postid!!)
                    .setValue(true)
            }
            else
            {
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid)
                    .child(post.postid!!)
                    .removeValue()
            }
        }

        holder.shareButton.setOnClickListener {
            val manager: FragmentManager = (mContext as AppCompatActivity).supportFragmentManager
            AddUserMessageDialog.newInstance(post.postid!! + " SharePost").show(manager, null)
        }

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", post.postid)
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }
    }

    private fun getTimeAgo(time: Long): String?
    {
        var tempTime = time
        val now = System.currentTimeMillis()

        if (tempTime < 1000000000000L)
        {
            tempTime *= 1000
        }

        if (tempTime > now || tempTime <= 0)
        {
            return null
        }

        val diff: Int = now.toInt() - tempTime.toInt()

        return when
        {
            diff < minuteMillis ->
            {
                "just now"
            }
            diff < 2 * minuteMillis ->
            {
                "a minute ago"
            }

            diff < 50 * minuteMillis ->
            {
                val temp = diff / minuteMillis
                "$temp minutes ago"
            }
            diff < 90 * minuteMillis ->
            {
                "an hour ago"
            }
            diff < 24 * hourMillis ->
            {
                val temp = diff / hourMillis
                "$temp hours ago"
            }
            diff < 48 * hourMillis -> {
                "yesterday"
            }
            else ->
            {
                val temp = diff / dayMillis
                "$temp days ago"
            }
        }
    }

    private fun getDate(milliSeconds: Long, dateFormat: String?): String?
    {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds

        return formatter.format(calendar.time)
    }

    private fun numberOfLikes(likes: TextView, postid: String?)
    {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid!!)

        likesRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    likes.text = mContext.getString(R.string.POST_ADAPTER_NUMBER_OF_LIKES, p0.childrenCount.toString())
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun getTotalComments(comments: TextView, postid: String?)
    {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postid!!)

        commentsRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    when
                    {
                        p0.childrenCount.toInt() == 1 ->
                        {
                            comments.text = mContext.getString(R.string.POST_ADAPTER_TOTAL_COMMENT, p0.childrenCount.toString())
                        }

                        else ->
                        {
                            comments.text = mContext.getString(R.string.POST_ADAPTER_TOTAL_COMMENTS, p0.childrenCount.toString())
                        }
                    }
                }
                else
                {
                    comments.visibility = View.GONE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun isLikes(postId: String?, likeButton: ImageView)
    {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postId!!)

        likesRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.child(firebaseUser!!.uid).exists())
                {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                }
                else
                {
                    likeButton.setImageResource(R.drawable.baseline_favorite_border_black_36)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherId: String?)
    {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId!!)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    val user = p0.getValue<UserModel>(UserModel::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(profileImage)
                    userName.text = user.username
                    publisher.text = user.fullname
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun checkSavedStatus(postid: String, imageView: ImageView)
    {
        val savesRef= FirebaseDatabase.getInstance().reference.child("Saves")
            .child(firebaseUser!!.uid)

        savesRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.child(postid).exists())
                {
                    imageView.setImageResource(R.drawable.baseline_bookmark_black_36)
                    imageView.tag = "Saved"
                }
                else
                {
                    imageView.setImageResource(R.drawable.baseline_bookmark_border_black_36)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun addNotification(userId: String, postId: String)
    {
        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)
        val notificationMap = HashMap<String, Any>()

        notificationMap["userid"] = firebaseUser!!.uid
        notificationMap["text"] = "Liked your post"
        notificationMap["postid"] = postId
        notificationMap["ispost"] = true

        notificationRef.push().setValue(notificationMap)
    }
}