package com.app.sukasuka.ui.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Toast
import com.app.sukasuka.R
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityAccountSettingsBinding
import com.app.sukasuka.model.UserModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso

class AccountSettingsActivity : ActivityBase<ActivityAccountSettingsBinding>() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker: Boolean = false
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityAccountSettingsBinding {
        return ActivityAccountSettingsBinding.inflate(inflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Picture")

        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val pref = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            pref.edit().clear().apply()

            val intent = Intent(this, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.changeImageTextBtn.setOnClickListener {
            checker = true

            cropImage.launch(
                CropImageContractOptions(
                    uri = null,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = true
                    )
                )
            )
        }

        binding.profileImageViewProfileFrag.setOnClickListener {
            checker = true

            cropImage.launch(
                CropImageContractOptions(
                    uri = null,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = true
                    )
                )
            )
        }

        binding.saveProfileBtn.setOnClickListener {
            if (checker) {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }

        binding.closeProfileBtn.setOnClickListener {
            finish()
        }

        userInfo()
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            imageUri = result.uriContent
            val uriFilePath = result.getUriFilePath(this) // optional usage
            binding.profileImageViewProfileFrag.setImageURI(imageUri)
        } else {
            // An error occurred.
            val exception = result.error
        }
    }

    private fun userInfo() {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile)
                        .into(binding.profileImageViewProfileFrag)
                    binding.usernameProfileFrag.setText(user.username)
                    binding.fullNameProfileFrag.setText(user.fullname)
                    binding.bioProfileFrag.setText(user.bio)
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun updateUserInfoOnly() {
        when {
            TextUtils.isEmpty(binding.fullNameProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Full Name is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.usernameProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Username is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.bioProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Bio is Required!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
                val userMap = HashMap<String, Any>()

                userMap["fullname"] = binding.fullNameProfileFrag.text.toString().toLowerCase()
                userMap["username"] = binding.usernameProfileFrag.text.toString().toLowerCase()
                userMap["bio"] = binding.bioProfileFrag.text.toString()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(
                    this,
                    "Account Information has been updated successfully.",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun uploadImageAndUpdateInfo() {
        when {
            imageUri == null -> {
                Toast.makeText(this, "Please select your profile picture.", Toast.LENGTH_LONG)
                    .show()
            }

            TextUtils.isEmpty(binding.fullNameProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Full Name is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.usernameProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Username is Required!", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.bioProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Bio is Required!", Toast.LENGTH_LONG).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, while we are updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser.uid + ".jpg")
                val uploadTask: StorageTask<*>

                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()

                        userMap["fullname"] =
                            binding.fullNameProfileFrag.text.toString().toLowerCase()
                        userMap["username"] =
                            binding.usernameProfileFrag.text.toString().toLowerCase()
                        userMap["bio"] = binding.bioProfileFrag.text.toString()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(
                            this,
                            "Account Information has been updated successfully.",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    } else {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }
}