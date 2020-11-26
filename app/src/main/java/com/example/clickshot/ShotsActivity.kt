package com.example.clickshot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


class ShotsActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    var shotsRecievedEmails: ArrayList<String> = ArrayList()
    var shots: ArrayList<DataSnapshot> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shots)

        val shotsListView = findViewById<ListView>(R.id.allShotsListView)
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, shotsRecievedEmails)
        shotsListView?.adapter = arrayAdapter

        //get the emails from firebaseDatabase
        mAuth.currentUser?.uid?.let {
            FirebaseDatabase.getInstance().reference.child("users")
                .child(it).child("shots").addChildEventListener(object: ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        try {
                            shotsRecievedEmails.add(snapshot.child("from").value as String)
                            shots.add(snapshot!!)
                            arrayAdapter.notifyDataSetChanged()
                        } catch (e:NullPointerException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        var index = 0

                        for(shot: DataSnapshot in shots) {
                            if (shot.key == snapshot.key){
                                shots.removeAt(index)
                                shotsRecievedEmails.removeAt(index)
                                arrayAdapter.notifyDataSetChanged()
                           }

                            index++;
                        }
                    }
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        //send intent to ViewShotsActivity when someone selects item in the list
        shotsListView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val snapshot = shots[position]

            var intent = Intent(this, ViewShotsActivity::class.java)

            intent.putExtra("imageName",snapshot.child("imageName")?.value as String?)
            intent.putExtra("imageURL",snapshot.child("imageURL")?.value as String?)
            intent.putExtra("message", snapshot.child("message")?.value as String?)
            intent.putExtra("shotKey", snapshot.key)

            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.createStory){

            val launchNextActivity = Intent(this, CreateStoryActivity::class.java)
            startActivity(launchNextActivity)

        } else if(item.itemId == R.id.logout){
            mAuth.signOut()
            val launchNextActivity = Intent(this, MainActivity::class.java)
            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(launchNextActivity)

        }

        return super.onOptionsItemSelected(item)
    }
}