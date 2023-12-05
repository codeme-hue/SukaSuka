package com.app.sukasuka.ui.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityAddPostBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class AddPostActivity : ActivityBase<ActivityAddPostBinding>() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityAddPostBinding {
        return ActivityAddPostBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        binding.saveNewPostBtn.setOnClickListener {
            uploadImage()
        }

        binding.closeAddPostBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        CropImage.activity()
            .setAspectRatio(2,2)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            binding.imagePost.setImageURI(imageUri)
        }
    }

    private fun uploadImage()
    {
        when (imageUri)
        {
            null ->
            {
                Toast.makeText(this, "Please select your picture.", Toast.LENGTH_LONG).show()
            }

            else ->
            {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, while we are adding your new post...")
                progressDialog.show()

                val fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
                val uploadTask: StorageTask<*>

                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                    if (!task.isSuccessful)
                    {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = ref.push().key
                        val postMap = HashMap<String, Any>()

                        postMap["postid"] = postId!!
                        postMap["description"] = binding.descriptionPost.text.toString().toLowerCase()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUrl
                        postMap["dateTime"] = System.currentTimeMillis().toString()

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this, "Post uploaded successfully.", Toast.LENGTH_LONG)
                            .show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else
                    {
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

}