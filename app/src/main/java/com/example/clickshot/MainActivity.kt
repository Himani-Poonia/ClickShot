package com.example.clickshot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), View.OnClickListener,View.OnKeyListener{

    private var signupModeActive = false
    private var signupTextView: TextView? = null
    private var logoimageView: ImageView? = null
    private var backgroundLayout: ConstraintLayout? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        passwordEditText?.setOnKeyListener(this)
        signupTextView = findViewById(R.id.signupTextView)
        signupTextView?.setOnClickListener(this)
        logoimageView = findViewById(R.id.logoImageView)
        logoimageView?.setOnClickListener(this)
        backgroundLayout = findViewById(R.id.backgroundLayout)
        backgroundLayout?.setOnClickListener(this)

        if(mAuth.currentUser != null){    //if true directly move to the activity next to login page (also called home page sometimes)
            goToNextActivity()
        }

    }

//go to next activity
    private fun goToNextActivity() {
        val launchNextActivity: Intent = Intent(this, ShotsActivity::class.java)
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(launchNextActivity)
    }

    fun loginClick(view: View){
        if (emailEditText?.text.toString() == "" || passwordEditText?.text.toString() == "") {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
        } else {

            if (signupModeActive) {

                mAuth.createUserWithEmailAndPassword(
                    emailEditText?.text.toString(),
                    passwordEditText?.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
//                            Log.i("Successfull","signup")
                            //add signed up user directly to database
                            task.result?.user?.uid?.let {
                                FirebaseDatabase.getInstance().reference.child("users").child(
                                    it
                                ).child("email").setValue(emailEditText?.text.toString())
                            }
                            goToNextActivity()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(this,"Something went wrong",Toast.LENGTH_SHORT).show()
                        }
                    }

            } else {
                mAuth.signInWithEmailAndPassword(
                    emailEditText?.text.toString(),
                    passwordEditText?.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.i("Successfull","login")
                            goToNextActivity()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(this,"New User! Try signing up",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

//used to automatically login or signup if enter key pressed
override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
            loginClick(v!!)
        }
        return false
    }

//click text view
override fun onClick(v: View) {
        if (v.id == R.id.signupTextView) {
            var loginButton = findViewById<Button>(R.id.loginButton)
            if (signupModeActive) {
                emailEditText?.setText("")
                passwordEditText!!.setText("")
                signupModeActive = false
                loginButton.text = getString(R.string.login)
                signupTextView!!.text = getString(R.string.orSignup)
            } else {
                emailEditText?.setText("")
                passwordEditText!!.setText("")
                signupModeActive = true
                loginButton.text = getString(R.string.signup)
                signupTextView!!.text = getString(R.string.orLogin)
            }
        } else if (v.id == R.id.logoImageView || v.id == R.id.backgroundLayout) {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }
}