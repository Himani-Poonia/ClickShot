package com.example.clickshot

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class ChooseUserActivity : AppCompatActivity() {

    var emails: ArrayList<String> = ArrayList()
    var keys: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTitle("Send to")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_user)


        var listView = findViewById<ListView>(R.id.listView)
        var arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emails)
        listView?.adapter = arrayAdapter


        //fetch data from database
        FirebaseDatabase.getInstance().reference.child("users").addChildEventListener(object :
            ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val email = snapshot.child("email").value as String
                emails.add(email)
                snapshot.key?.let { keys.add(it) }
                arrayAdapter.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}

        })

        listView?.setOnItemClickListener { parent, view, position, id ->
            val shotMap: Map<String, String?> = mapOf("from" to FirebaseAuth.getInstance().currentUser!!.email!!,
                "imageName" to intent.getStringExtra("imageName"),
                "imageURL" to intent.getStringExtra("imageURL"), "message" to intent.getStringExtra("message"))

            //add shots to clicked email
            FirebaseDatabase.getInstance().reference.child("users").child(
                keys.get(position)
            ).child("shots").push().setValue(shotMap)
                       //push creates child with a random unique name

            val intent = Intent(this, ShotsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }
}


