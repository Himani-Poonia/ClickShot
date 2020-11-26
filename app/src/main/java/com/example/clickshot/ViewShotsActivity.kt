package com.example.clickshot

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ViewShotsActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_shots)

        var messageTextView = findViewById<TextView>(R.id.messageTextView)
        var shotImageView = findViewById<ImageView>(R.id.shotsImageView)

        //set message
        messageTextView?.text = intent.getStringExtra("message")

        //set image
        val task = ImageDownloader()
        val myImage: Bitmap

        try {
            myImage = task.execute(intent.getStringExtra("imageURL")).get()!!
            shotImageView.setImageBitmap(myImage)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //download image in the background from url
    class ImageDownloader :
        AsyncTask<String?, Void?, Bitmap?>() //bitmap is a way of returning images
    {
        override fun doInBackground(vararg urls: String?): Bitmap? {
            return try {
                val url = URL(urls[0])
                val connection = url.openConnection() as HttpsURLConnection
                connection.connect()
                val `in` = connection.inputStream
                BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        mAuth.currentUser?.uid?.let {
            intent.getStringExtra("shotKey")?.let { it1 ->
                FirebaseDatabase.getInstance().reference.child("users")
                    .child(it).child("shots").child(it1).removeValue()
            }
        }

        intent.getStringExtra("imageName")?.let {
            FirebaseStorage.getInstance().reference.child("images").child(
                it
            ).delete()
        }
    }
}