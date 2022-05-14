package com.example.reba

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.reba.introPages.login
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.util.*


class splashScreen : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    lateinit var handler: Handler


    private val mNotificationTime = Calendar.getInstance().timeInMillis + 86400000 //Set after 24h from the current time.
    private var mNotified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        mAuth = FirebaseAuth.getInstance()

        val user = mAuth?.currentUser

        if (!mNotified) {
            NotificationUtils().setNotification(mNotificationTime, this)
        }


        if(user!=null){
            val snack = Snackbar.make(findViewById(R.id.imageView2),"Welcome back "+user.email,Snackbar.LENGTH_LONG)
            snack.show()
        }


        handler = Handler()
        handler.postDelayed({

            // Delay and Start Activity

            val prefs = getSharedPreferences("REBA", Context.MODE_PRIVATE)
            val firstTime = prefs.getBoolean("f", true)
            val editor: SharedPreferences.Editor = prefs.edit()
            if (firstTime) {
                val intent = Intent(this, obording::class.java)
                startActivity(intent)
                editor.putBoolean("f", false)
                editor.commit()
                finish()
            }
            else{
                if(user==null) {
                    val intent = Intent(this, login::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    finish()
                }
                else {
                    val intent = Intent(this, MainActivity::class.java)
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    startActivity(intent)
                }
            }
            finish()
        } , 2500)

    }
}
