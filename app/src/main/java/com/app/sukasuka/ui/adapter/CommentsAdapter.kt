package com.app.sukasuka.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.app.sukasuka.R
import com.app.sukasuka.model.CommentModel
import com.app.sukasuka.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter (private val mContext: Context, private val mComment: MutableList<CommentModel>?): RecyclerView.Adapter<CommentsAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comments_item_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return mComment!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val comment = mComment!![position]
        holder.txtComment.text = comment.getComment()
        getUserInfo(holder.imageProfile, holder.txtUsername, comment.getPublisher())
    }

    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView)
    {
        var imageProfile: CircleImageView
        var txtUsername: TextView
        var txtComment: TextView

        init {
            imageProfile = itemView.findViewById(R.id.user_profile_image_comment)
            txtUsername = itemView.findViewById(R.id.user_name_comment)
            txtComment = itemView.findViewById(R.id.comment_comment)
        }
    }

    private fun getUserInfo(imageProfile: CircleImageView, txtUsername: TextView, publisher: String?)
    {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisher!!)

        userRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    val user = p0.getValue(UserModel::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(imageProfile)
                    txtUsername.text = user.username
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}