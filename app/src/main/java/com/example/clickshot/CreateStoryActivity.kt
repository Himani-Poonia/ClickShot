package com.example.clickshot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.util.*


class CreateStoryActivity : AppCompatActivity() {

    private var createShotImageView: ImageView? = null
    private var sayEditText: EditText? = null
    private val imageName = UUID.randomUUID().toString() + ".jpg"  //it creates random unique image id in storage for current image

//choose from gallery button onClick fun
    fun chooseImageClicked(view: View) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            getPhoto()
        }
    }

//Main Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_story_main)

        createShotImageView = findViewById(R.id.CreateShotImageView)
        sayEditText = findViewById(R.id.sayEditText)
    }

//functions regarding photo
    private fun getPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val selectedImage = data!!.data
        if (requestCode == 1 && resultCode == RESULT_OK) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                createShotImageView?.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

//next button onClick fun
    fun nextClicked(view: View){
        createShotImageView?.isDrawingCacheEnabled = true
        createShotImageView?.buildDrawingCache()
        val bitmap = (createShotImageView?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        //FirebaseStorage.getInstance().getReference().child("images").child(imageName)
                                //Above line will create reference to instance of storage in firebase for this app and is used below

        val uploadTask = FirebaseStorage.getInstance().getReference().child("images").child(
            imageName
        ).putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
//            Toast.makeText(this,"Upload Successful",Toast.LENGTH_SHORT).show()

            // if i want url of this image
            var url: String? = null
            val downloadUri = taskSnapshot.metadata?.reference?.downloadUrl
            downloadUri?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Task completed successfully
                    url = downloadUri.result.toString()
//                    Log.i("Url", url!!)
                } else {
                    // Task failed with an exception
                    task.exception
                }

                //intent to ShotsActivity
                val intent: Intent = Intent(this, ChooseUserActivity::class.java)
                intent.putExtra("imageURL", url)
                intent.putExtra("imageName", imageName)
                intent.putExtra("message", sayEditText?.text.toString())
                startActivity(intent)
            }
        }
    }
}