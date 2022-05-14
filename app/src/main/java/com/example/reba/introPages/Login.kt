package com.example.reba.introPages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reba.MainActivity
import com.example.reba.R
import com.example.reba.homePage
import com.google.firebase.auth.FirebaseAuth




class login : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    lateinit var emailText : EditText
    lateinit var pwdText : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        mAuth = FirebaseAuth.getInstance();

        var signupbtn : Button = findViewById(R.id.button3)
        signupbtn.setOnClickListener{
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }

        var resetbtnbtn : Button = findViewById(R.id.button5)
        resetbtnbtn.setOnClickListener{
            val intent = Intent(this, resetpwd::class.java)
            startActivity(intent)
        }

        emailText =findViewById(R.id.editTextTextPersonName)
        pwdText =findViewById(R.id.editTextTextPassword)


        var cfmbtn : Button = findViewById(R.id.button)
        cfmbtn.setOnClickListener{
            val email = emailText.text.toString()
            val pwd = pwdText.text.toString()
            mAuth?.signInWithEmailAndPassword(email, pwd)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Info", "signInWithEmail:success")
                        val user = mAuth?.currentUser
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("User",user)
                        startActivity(intent)
                        onResume()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Info", "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        onResume()
                    }
                }
        }
    }
}
