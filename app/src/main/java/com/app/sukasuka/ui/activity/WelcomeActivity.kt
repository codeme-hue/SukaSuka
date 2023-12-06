package com.app.sukasuka.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityWelcomeBinding
import com.app.sukasuka.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WelcomeActivity : ActivityBase<ActivityWelcomeBinding>() {
    private lateinit var firebaseUser: FirebaseUser
    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityWelcomeBinding {
        return ActivityWelcomeBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding.animLoadingViewWelcome.setAnimation("18430-welcome-white.json")
        binding.animLoadingViewWelcome.playAnimation()
        binding.animLoadingViewWelcome.loop(true)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    val user = p0.getValue(UserModel::class.java)
                    binding.lblUsername.text = user?.username.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })

        val signInIntent = Intent(this, SignInActivity::class.java)
        val timer = object : Thread()
        {
            override fun run()
            {
                try
                {
                    sleep(4000)
                } catch (e: InterruptedException)
                {
                    e.printStackTrace()
                } finally
                {
                    startActivity(signInIntent)
                    finish()
                }
            }
        }
        timer.start()
    }
}