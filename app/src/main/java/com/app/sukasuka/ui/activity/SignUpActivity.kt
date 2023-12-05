package com.app.sukasuka.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
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
import com.app.sukasuka.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : ActivityBase<ActivitySignUpBinding>() {
    override fun getViewBindingInflater(inflater: LayoutInflater): ActivitySignUpBinding {
        return ActivitySignUpBinding.inflate(inflater)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding.signinLinkBtn.setOnClickListener {
            finish()
        }

        binding.signupBtn.setOnClickListener {
            enableComponents(false)
            startLoadingView(true)
            createAccount()
        }

        binding.layoutSignUpRelative.setOnTouchListener { _, _ ->

            hideSoftKeyboard(this)
            binding.fullnameSignup.clearFocus()
            binding.usernameSignup.clearFocus()
            binding.emailSignup.clearFocus()
            binding.passwordSignup.clearFocus()

            false
        }

        setSignUpButtonEnabled(false)
        startLoadingView(false)
        enableComponents(true)
        initUI()
    }

    private fun initUI()
    {
        binding.fullnameSignup.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?)
            {
                if (binding.fullnameSignup.text.toString() != "" &&
                    binding.usernameSignup.text.toString() != "" &&
                    binding.emailSignup.text.toString() != "" &&
                    binding.passwordSignup.text.toString() != "")
                {
                    setSignUpButtonEnabled(true)
                }
                else
                {
                    setSignUpButtonEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (binding.fullnameSignup.text.toString() != "" &&
                    binding.usernameSignup.text.toString() != "" &&
                    binding.emailSignup.text.toString() != "" &&
                    binding.passwordSignup.text.toString() != "")
                {
                    setSignUpButtonEnabled(true)
                }
                else
                {
                    setSignUpButtonEnabled(false)
                }
            }
        })

        binding.usernameSignup.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?)
            {
                if (binding.fullnameSignup.text.toString() != "" &&
                    binding.usernameSignup.text.toString() != "" &&
                    binding.emailSignup.text.toString() != "" &&
                    binding.passwordSignup.text.toString() != "")
                {
                    setSignUpButtonEnabled(true)
                }
                else
                {
                    setSignUpButtonEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (binding.fullnameSignup.text.toString() != "" &&
                    binding.usernameSignup.text.toString() != "" &&
                    binding.emailSignup.text.toString() != "" &&
                    binding.passwordSignup.text.toString() != "")
                {
                    setSignUpButtonEnabled(true)
                }
                else
                {
                    setSignUpButtonEnabled(false)
                }
            }
        })

        binding.emailSignup.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?)
            {
                if (isEmailValid(binding.emailSignup.text.toString()))
                {
                    binding.lblInvalidEmail.visibility = View.GONE
                }
                else
                {
                    binding.lblInvalidEmail.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (binding.fullnameSignup.text.toString() != "" &&
                    binding.usernameSignup.text.toString() != "" &&
                    binding.emailSignup.text.toString() != "" &&
                    binding.passwordSignup.text.toString() != "")
                {
                    setSignUpButtonEnabled(true)
                }
                else
                {
                    setSignUpButtonEnabled(false)
                }
            }
        })

        binding.passwordSignup.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?)
            {
                if (binding.fullnameSignup.text.toString() != "" &&
                    binding.usernameSignup.text.toString() != "" &&
                    binding.emailSignup.text.toString() != "" &&
                    binding.passwordSignup.text.toString() != "")
                {
                    setSignUpButtonEnabled(true)
                }
                else
                {
                    setSignUpButtonEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (binding.fullnameSignup.text.toString() != "" &&
                    binding.usernameSignup.text.toString() != "" &&
                    binding.emailSignup.text.toString() != "" &&
                    binding.passwordSignup.text.toString() != "")
                {
                    setSignUpButtonEnabled(true)
                }
                else
                {
                    setSignUpButtonEnabled(false)
                }
            }
        })
    }

    private fun setSignUpButtonEnabled(isEnabled: Boolean)
    {
        if (isEnabled)
        {
            binding.signupBtn.isEnabled = true
            binding.signupBtn.isClickable = true
            binding.signupBtn.background = resources.getDrawable(R.drawable.rounded_corner_black)
        }
        else
        {
            binding.signupBtn.isEnabled = false
            binding.signupBtn.isClickable = false
            binding.signupBtn.background = resources.getDrawable(R.drawable.rounded_corner_light_gray)
        }
    }

    private fun isEmailValid(email: CharSequence): Boolean
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun startLoadingView(start: Boolean)
    {
        if (start)
        {
            binding.layoutLoadingViewSignIn.visibility = View.VISIBLE
            binding.animLoadingViewSignIn.setAnimation("paperplane_lottie_animation.json")
            binding.animLoadingViewSignIn.playAnimation()
            binding.animLoadingViewSignIn.loop(true)
        }
        else
        {
            binding.layoutLoadingViewSignIn.visibility = View.GONE
            binding.animLoadingViewSignIn.cancelAnimation()
        }
    }

    private fun enableComponents(isEnabled: Boolean)
    {
        if (isEnabled)
        {
            binding.signinLinkBtn.isEnabled = true
            binding.signupBtn.isEnabled = true
            binding.fullnameSignup.isEnabled = true
            binding.usernameSignup.isEnabled = true
            binding.emailSignup.isEnabled = true
            binding.passwordSignup.isEnabled = true
        }
        else
        {
            binding.signinLinkBtn.isEnabled = false
            binding.signupBtn.isEnabled = false
            binding.fullnameSignup.isEnabled = false
            binding.usernameSignup.isEnabled = false
            binding.emailSignup.isEnabled = false
            binding.passwordSignup.isEnabled = false
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

    private fun createAccount()
    {
        val fullName = binding.fullnameSignup.text.toString()
        val userName = binding.usernameSignup.text.toString()
        val email = binding.emailSignup.text.toString()
        val password = binding.passwordSignup.text.toString()

        when
        {
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Full Name is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "User Name is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email)    -> Toast.makeText(this, "Email is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required.", Toast.LENGTH_LONG).show()

            else                        ->
            {
                /*val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Sign Up")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()*/

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                        {
                            saveUserInfo(fullName, userName, email, password)

                        }
                        else
                        {
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error: $message" , Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            startLoadingView(false)
                            enableComponents(true)
                            //progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, password: String)
    {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()

        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "Welcome to my Instagram Clone App!"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/suka-suka-28347.appspot.com/o/profile.png?alt=media&token=671f345a-ccd6-4121-a426-a3ee23b1d60f"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener {task ->
                if (task.isSuccessful)
                {
                    //progressDialog.dismiss()
                    startLoadingView(false)
                    enableComponents(true)
                    setSignUpButtonEnabled(true)

                    Toast.makeText(this, "Account has been created successfully.", Toast.LENGTH_LONG).show()

                    FirebaseDatabase.getInstance().reference.child("Follow")
                        .child(currentUserID)
                        .child("Following")
                        .child(currentUserID)
                        .setValue(true)

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
                    setSignUpButtonEnabled(true)
                    //progressDialog.dismiss()
                }
            }
    }
}