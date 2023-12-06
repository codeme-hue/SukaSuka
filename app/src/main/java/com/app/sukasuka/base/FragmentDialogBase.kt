package com.app.sukasuka.base

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.app.sukasuka.R
import com.app.sukasuka.utils.NetworkUtils
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar

abstract class FragmentDialogBase<VB: ViewBinding> constructor(private val isFullDialog: Boolean): AppCompatDialogFragment() {

    private lateinit var _binding: VB
    protected val binding get() = _binding
    abstract fun getViewBindingInflater(inflater: LayoutInflater, container: ViewGroup?): VB
    abstract fun subscribeUI()

    private lateinit var _context: Context


    private val networkUtils by lazy { NetworkUtils(this.context ?: _context) }

    private var finishListener: ((Int, String?) -> Unit)? = null

    protected val mContext: Context by lazy {
        requireContext()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFullDialog) {
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullscreen)
            return
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeNetworkState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = getViewBindingInflater(inflater, container)
        return _binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dialog?.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog
        _context = context
    }

    override fun onCancel(dialog: DialogInterface) {
        finishListener?.invoke(RESULT_CANCELED, null)
        super.onCancel(dialog)
    }

    protected fun finishDialog(status: Int, message: String?) {
        finishListener?.invoke(status, message)
        dismiss()
    }

    fun setCallback(callback: (statusCode: Int, message: String?) -> Unit) {
        finishListener = callback
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

    companion object {
        const val RESULT_OK = -1
        const val RESULT_FAILED = 0
        const val RESULT_CANCELED = 1
    }
}