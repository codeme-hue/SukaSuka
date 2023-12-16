package com.app.sukasuka.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityMainBinding
import com.app.sukasuka.ui.fragment.HomeFragment
import com.app.sukasuka.ui.fragment.NotificationFragment
import com.app.sukasuka.ui.fragment.ProfileFragment
import com.app.sukasuka.ui.fragment.SearchFragment
import com.app.sukasuka.utils.PermissionUtils


class MainActivity : ActivityBase<ActivityMainBinding>() {
    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        routeToFragment(HomeFragment())

        binding.navView.setOnItemSelectedListener { item ->
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            when (item.itemId) {
                R.id.nav_home -> {
                  routeToFragment(HomeFragment())
                }

                R.id.nav_search -> {
                    routeToFragment(SearchFragment())
                }

                R.id.nav_add_post  -> {
                    item.isChecked = false
                    startActivity(Intent(this, AddPostActivity::class.java))
                }

                R.id.nav_notifications -> {
                  routeToFragment(NotificationFragment())
                }

                R.id.nav_profile -> {
                   routeToFragment(ProfileFragment())
                }
            }
            true
        }
        checkPermission()
    }

    private fun checkPermission() {
        PermissionUtils.requestStoragePermission(this)
    }

    private fun routeToFragment(fragment: Fragment)
    {
        val routeFrag = supportFragmentManager.beginTransaction()
        routeFrag.replace(R.id.fragment_container, fragment)
        routeFrag.commit()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed()
    {
        //Don't do anything
    }
}