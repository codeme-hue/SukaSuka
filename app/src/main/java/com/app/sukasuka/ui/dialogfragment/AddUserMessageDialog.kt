package com.app.sukasuka.ui.dialogfragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sukasuka.base.FragmentDialogBase
import com.app.sukasuka.databinding.DialogFragmentAddUserMessageBinding
import com.app.sukasuka.model.UserModel
import com.app.sukasuka.ui.adapter.UserDirectMessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddUserMessageDialog: FragmentDialogBase<DialogFragmentAddUserMessageBinding>(false) {

    private var idList: List<String>? = null
    private var userList: List<UserModel>? = null
    private var profileId: String = ""

    private var userDirectMessageAdapter: UserDirectMessageAdapter? = null

    override fun getViewBindingInflater(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentAddUserMessageBinding {
        return DialogFragmentAddUserMessageBinding.inflate(inflater, container, false)
    }

    override fun subscribeUI() {

        idList = ArrayList()
        userList = ArrayList()

        val recyclerView: RecyclerView = binding.recyclerviewAddUserMessage
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        userList = ArrayList()
        userDirectMessageAdapter =
            UserDirectMessageAdapter(mContext, userList as ArrayList<UserModel>, false)
        recyclerView.adapter = userDirectMessageAdapter

        showUsers()
    }

//    private fun getFollowing() {
//        val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
//
//        if (pref != null) {
//            profileId = pref.getString("profileId", "none").toString()
//
//            val followersRef = FirebaseDatabase.getInstance().reference
//                .child("Follow").child(profileId!!)
//                .child("Following")
//
//
//            followersRef.addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(p0: DataSnapshot) {
//                    (idList as ArrayList<String>)
//
//                    for (snapshot in p0.children) {
//                        (idList as ArrayList<String>).add(snapshot.key!!)
//                    }
//                    showUsers()
//                }
//
//                override fun onCancelled(p0: DatabaseError) {}
//            })
//        }
//    }

    private fun showUsers() {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                dataSnapshot.children.forEach {
                    it.getValue(UserModel::class.java).let {user ->
                        if (user != null) {
                            if (user.uid != FirebaseAuth.getInstance().uid) {
                                (userList as ArrayList<UserModel>).add(user)
                            }
                        }
                    }
                }
                userDirectMessageAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}