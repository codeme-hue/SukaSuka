package com.app.sukasuka.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : ActivityBase<ActivitySignInBinding>() {
    override fun getViewBindingInflater(inflater: LayoutInflater): ActivitySignInBinding {
        return ActivitySignInBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        buttonOnClickListener()
        textFieldValidationChecking()

        setLoginButtonEnabled(false)
        startLoadingView(false)
        enableComponents(true)
    }

    override fun onStart()
    {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser != null)
        {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buttonOnClickListener()
    {
        binding.signupLinkBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            binding.emailLogin.text = null
            binding.passwordLogin.text = null
            binding.lblInvalidEmail.visibility = View.GONE
        }

        binding.loginBtn.setOnClickListener {
            enableComponents(false)
            setLoginButtonEnabled(false)
            startLoadingView(true)
            loginUser()
        }

        binding.layoutSignInRelative.setOnTouchListener { _, _ ->
            if (binding.emailLogin.text.toString() != "" && !isEmailValid(binding.emailLogin.text.toString()))
            {
                binding.lblInvalidEmail.visibility = View.VISIBLE
            }
            else
            {
                binding.lblInvalidEmail.visibility = View.GONE
            }

            hideSoftKeyboard(this)
            binding.emailLogin.clearFocus()
            binding.passwordLogin.clearFocus()
            false
        }
    }

    private fun textFieldValidationChecking()
    {
        binding.emailLogin.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?)
            {
                if (!isEmailValid(binding.emailLogin.text.toString()))
                {
                    binding.lblInvalidEmail.visibility = View.VISIBLE
                }
                else
                {
                    binding.lblInvalidEmail.visibility = View.GONE
                    if (binding.emailLogin.text.toString() != "" && binding.passwordLogin.text.toString() != "")
                    {
                        setLoginButtonEnabled(true)
                    }
                    else
                    {
                        setLoginButtonEnabled(false)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (binding.emailLogin.text.toString() != "" && binding.passwordLogin.text.toString() != "")
                {
                    setLoginButtonEnabled(true)
                }
                else
                {
                    setLoginButtonEnabled(false)
                }
            }
        })

        binding.passwordLogin.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?)
            {
                if (binding.emailLogin.text.toString() != "" && binding.passwordLogin.text.toString() != "")
                {
                    setLoginButtonEnabled(true)
                }
                else
                {
                    setLoginButtonEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (binding.emailLogin.text.toString() != "" && binding.passwordLogin.text.toString() != "")
                {
                    setLoginButtonEnabled(true)
                }
                else
                {
                    setLoginButtonEnabled(false)
                }
            }
        })
    }

    fun isEmailValid(email: CharSequence): Boolean
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoginButtonEnabled(isEnabled: Boolean)
    {
        if (isEnabled)
        {
            binding.loginBtn.isEnabled = true
            binding.loginBtn.isClickable = true
            binding.loginBtn.background = resources.getDrawable(R.drawable.rounded_corner_black)
        }
        else
        {
            binding.loginBtn.isEnabled = false
            binding.loginBtn.isClickable = false
            binding.loginBtn.background = resources.getDrawable(R.drawable.rounded_corner_light_gray)
        }
    }

    private fun hideSoftKeyboard(activity: Activity)
    {
        val inputMethodManager: InputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            activity.currentFocus?.windowToken, 0
        )
    }

    private fun startLoadingView(start: Boolean)
    {
        if (start)
        {
            binding.layoutLoadingView.visibility = View.VISIBLE
            binding.animLoadingView.setAnimation("paperplane_lottie_animation.json")
            binding.animLoadingView.playAnimation()
            binding.animLoadingView.loop(true)
        }
        else
        {
            binding.layoutLoadingView.visibility = View.GONE
            binding.animLoadingView.cancelAnimation()
        }
    }

    private fun enableComponents(isEnabled: Boolean)
    {
        if (isEnabled)
        {
            binding.signupLinkBtn.isEnabled = true
            binding.emailLogin.isEnabled = true
            binding.passwordLogin.isEnabled = true

        }
        else
        {
            binding.signupLinkBtn.isEnabled = false
            binding.emailLogin.isEnabled = false
            binding.passwordLogin.isEnabled = false
        }
    }

    private fun loginUser()
    {
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()

        when
        {
            TextUtils.isEmpty(email)    -> Toast.makeText(this, "Email is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required.", Toast.LENGTH_LONG).show()

            else                        ->
            {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Login")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                //progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        progressDialog.dismiss()
                        startLoadingView(false)
                        enableComponents(true)
                        setLoginButtonEnabled(true)

                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    else
                    {
                        val message = task.exception!!.toString()
                        Toast.makeText(this, "Error: $message" , Toast.LENGTH_LONG).show()
                        FirebaseAuth.getInstance().signOut()
                        startLoadingView(false)
                        enableComponents(true)
                        setLoginButtonEnabled(true)
                        //progressDialog.dismiss()
                    }
                }
            }
        }
    }
}