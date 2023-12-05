package com.app.sukasuka.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : ActivityBase<ActivitySplashScreenBinding>() {
    override fun getViewBindingInflater(inflater: LayoutInflater): ActivitySplashScreenBinding {
        return ActivitySplashScreenBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val myAnim = AnimationUtils.loadAnimation(this, R.anim.mytransition)

        binding.imgSplashLogo.startAnimation(myAnim)
        binding.lblSplashTitle.startAnimation(myAnim)
        binding.lblPowerBy.startAnimation(myAnim)
        binding.lblDesmond.startAnimation(myAnim)

        val signInIntent = Intent(this, SignInActivity::class.java)
        val welcomeIntent = Intent(this, WelcomeActivity::class.java)
        val timer = object : Thread()
        {
            override fun run()
            {
                try
                {
                    sleep(3000)
                } catch (e: InterruptedException)
                {
                    e.printStackTrace()
                } finally
                {
                    if (FirebaseAuth.getInstance().currentUser != null)
                    {
                        startActivity(welcomeIntent)
                    }
                    else
                    {
                        startActivity(signInIntent)
                    }

                    finish()
                }
            }
        }
        timer.start()
    }
}