package com.app.sukasuka.ui.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.app.sukasuka.base.ActivityBase
import com.app.sukasuka.databinding.ActivityAddStoryBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class AddStoryActivity : ActivityBase<ActivityAddStoryBinding>() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageStoryPicRef: StorageReference? = null

    override fun getViewBindingInflater(inflater: LayoutInflater): ActivityAddStoryBinding {
        return ActivityAddStoryBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageStoryPicRef = FirebaseStorage.getInstance().reference.child("Story Pictures")

        CropImage.activity()
            .setAspectRatio(9,16)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            uploadStory()
        }
    }

    private fun uploadStory()
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
                progressDialog.setTitle("Adding Story")
                progressDialog.setMessage("Please wait, while we are adding your new story...")
                progressDialog.show()

                val fileRef = storageStoryPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")
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

                        val ref = FirebaseDatabase.getInstance().reference
                            .child("Story")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        val storyId = (ref.push().key).toString()
                        val storyMap = HashMap<String, Any>()
                        val timeEnd = System.currentTimeMillis() + 86400000 //One Day

                        storyMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
                        storyMap["timestart"] = ServerValue.TIMESTAMP
                        storyMap["timeend"] = timeEnd
                        storyMap["imageurl"] = myUrl
                        storyMap["storyid"] = storyId

                        ref.child(storyId).updateChildren(storyMap)

                        Toast.makeText(this, "Story has been uploaded successfully.", Toast.LENGTH_LONG).show()

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