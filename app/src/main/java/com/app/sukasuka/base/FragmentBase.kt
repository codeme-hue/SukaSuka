package com.app.sukasuka.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.app.sukasuka.R
import com.app.sukasuka.utils.NetworkUtils
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar

abstract class FragmentBase<VB: ViewBinding> : Fragment() {
    private lateinit var _binding: VB
    protected val binding get() = _binding

    private lateinit var _context: Context

    private val networkUtils by lazy { NetworkUtils(this.context ?: _context) }

    abstract fun getViewBindingInflater(inflater: LayoutInflater, container: ViewGroup?): VB

    abstract fun subscribeUI()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBindingInflater(inflater, container)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeNetworkState()
    }

    protected fun onSwipeRefresh(
        view: SwipeRefreshLayout?,
        pg : LinearProgressIndicator?,
    ) {
        networkUtils.networkDataStatus.observe(viewLifecycleOwner) {
            view?.setOnRefreshListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    subscribeUI()
                    pg?.visibility = View.GONE
                }, 2000)
            }
        }
    }

    private fun observeNetworkState() {
        networkUtils.networkDataStatus.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) subscribeUI()
            else Snackbar.make(_binding.root, getString(R.string.no_inet), Snackbar.LENGTH_SHORT).show()
        }
    }

    @JvmName("getBaseBinding")
    fun getBinding() = binding
}