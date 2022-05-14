package com.example.reba.introPages

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reba.R
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity

import com.google.android.gms.tasks.OnCompleteListener

import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception


class resetpwd : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resetpwd_page)


        var btn = findViewById<Button>(R.id.button6)
        btn.setOnClickListener{
            try {
                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(findViewById<TextView>(R.id.editTextTextPersonName3).text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this, "Email Sent",
                                Toast.LENGTH_SHORT
                            ).show()
                            onResume()
                        }
                    }
            } catch (e : Exception){
                Toast.makeText(this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        }

        var btn1 = findViewById<Button>(R.id.button11)
        btn1.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
    }
}