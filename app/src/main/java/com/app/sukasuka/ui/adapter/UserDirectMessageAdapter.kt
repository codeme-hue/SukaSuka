package com.app.sukasuka.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.app.sukasuka.R
import com.app.sukasuka.model.UserModel
import com.app.sukasuka.ui.activity.DetailMessageActivity
import com.app.sukasuka.ui.activity.MainActivity
import com.app.sukasuka.ui.fragment.ProfileFragment
import com.squareup.picasso.Picasso

class UserDirectMessageAdapter(
    private var mContext: Context,
    private var mUser: List<UserModel>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<UserDirectMessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.user_item_add_direct_message_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]

        holder.userNameTextView.text = user.username
        holder.userFullnameTextView.text = user.fullname
        Picasso.get().load(user.image).placeholder(R.drawable.profile)
            .into(holder.userProfileImage)

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, DetailMessageActivity::class.java)
            intent.putExtra("receiverData", user)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mUser.size
    }


    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTextView: TextView = itemView.findViewById(R.id.user_name_dm)
        var userFullnameTextView: TextView = itemView.findViewById(R.id.user_full_name_dm)
        var userProfileImage: ImageView = itemView.findViewById(R.id.user_profile_image_dm)
        var sendDirectMessageButton: ImageView = itemView.findViewById(R.id.sendMessage)
    }
}