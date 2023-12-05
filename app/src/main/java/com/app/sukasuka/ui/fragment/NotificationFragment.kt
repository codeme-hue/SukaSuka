package com.app.sukasuka.ui.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.sukasuka.base.FragmentBase
import com.app.sukasuka.databinding.FragmentNotificationBinding
import com.app.sukasuka.model.NotificationModel
import com.app.sukasuka.ui.adapter.NotificationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections

class NotificationFragment : FragmentBase<FragmentNotificationBinding>() {

    private var notificationList: List<NotificationModel>? = null
    private var notificationAdapter: NotificationAdapter? = null

    override fun getViewBindingInflater(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNotificationBinding {
        return FragmentNotificationBinding.inflate(inflater, container, false)
    }

    override fun subscribeUI() {
        val recyclerView = binding.recyclerViewNotifications
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationList = ArrayList()
        notificationAdapter =
            NotificationAdapter(requireContext(), notificationList as ArrayList<NotificationModel>)
        recyclerView.adapter = notificationAdapter

        readNotifications()
    }

    private fun readNotifications()
    {
        val notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(
            FirebaseAuth.getInstance().currentUser!!.uid)

        notificationRef.addValueEventListener(object : ValueEventListener
        {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    (notificationList as ArrayList<NotificationModel>).clear()

                    for (snapshot in p0.children)
                    {
                        val notification = snapshot.getValue(NotificationModel::class.java)

                        (notificationList as ArrayList<NotificationModel>).add(notification!!)
                    }

                    binding.animLoadingViewNotification.visibility = View.GONE
                    binding.emptyNotification.visibility = View.GONE
                    binding.recyclerViewNotifications.visibility = View.VISIBLE

                    Collections.reverse(notificationList)
                    notificationAdapter!!.notifyDataSetChanged()
                }
                else
                {
                    binding.animLoadingViewNotification.visibility = View.VISIBLE
                    binding.emptyNotification.visibility = View.VISIBLE
                    binding.recyclerViewNotifications.visibility = View.GONE
                    binding.animLoadingViewNotification.setAnimation("13525-empty.json")
                    binding.animLoadingViewNotification.playAnimation()
                    binding.animLoadingViewNotification.loop(true)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}