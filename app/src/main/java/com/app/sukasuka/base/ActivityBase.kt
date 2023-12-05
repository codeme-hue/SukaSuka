package com.app.sukasuka.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class ActivityBase<VB: ViewBinding> : AppCompatActivity() {

    private lateinit var _binding : VB
    protected val binding get() = _binding

    protected val mContext: Context by lazy { this }

    abstract fun getViewBindingInflater(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = getViewBindingInflater(layoutInflater)
        setContentView(_binding.root)

    }
}