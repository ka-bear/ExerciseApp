package com.example.reba.introPages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reba.MainActivity
import com.example.reba.R
import com.example.reba.User
import com.example.reba.homePage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern
import kotlin.random.Random

private var mAuth: FirebaseAuth? = null

class Signup : AppCompatActivity() {
    lateinit var emailText : EditText
    lateinit var pwdText : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)

        Log.i("infoo", "hgjyjgcgcjgcfchgcvhc hfchgcgh")


        mAuth = FirebaseAuth.getInstance();

        emailText =findViewById(R.id.editTextTextPersonName2)
        pwdText =findViewById(R.id.editTextTextPassword3)

        var btn = findViewById<Button>(R.id.button2)
        btn.setOnClickListener{
            register()
        }

        var btn1 = findViewById<Button>(R.id.button4)
        btn1.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }

    }

    private fun register(){
        val email = emailText.text.toString()
        val pwd = pwdText.text.toString()

        if(email.isEmpty() ||  pwd.isEmpty()){
            return
        }

        if(pwd.length<6){
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return
        }

        var n = ""

        mAuth?.createUserWithEmailAndPassword(email, pwd)
            ?.addOnCompleteListener(this) { task ->
                Log.i("infoo", (mAuth==null).toString())
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Info", "createUserWithEmail:success")
                    val user = mAuth?.currentUser
                    Log.i("nigg12","NAME1: "+user?.email)

                    var link="https://reba-13fed-default-rtdb.asia-southeast1.firebasedatabase.app/"
                    var database = Firebase.database(link).reference

                    Log.i("nigg12","ID1: "+user?.uid)

                    user.let {
                        database.child(it?.uid+"/datetime").setValue("0,0") }

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("User",user)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.i("Infoo", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    onResume()
                }
            }
        /*
        mAuth = FirebaseAuth.getInstance();

        val user = mAuth?.currentUser
        Log.i("nigg12","NAME: "+user?.email)

        var link="https://reba-13fed-default-rtdb.asia-southeast1.firebasedatabase.app/"
        var database = Firebase.database(link).reference

        user.let {
            database.child(it?.uid+"/datetime").setValue("0,0") }

        database.child(user?.uid+"/datetime").get().addOnCompleteListener {
            Log.i("resultsmaybe?",it.result.toString())
        }

         */

        //Log.i("nigg12","" + user!!::class.java.typeName)



    }
        /*mAuth?.createUserWithEmailAndPassword(email,pwd)?.addOnCompleteListener{task ->
            if(task.isSuccessful){

                /*
                FirebaseAuth.getInstance().currentUser?.let {
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(it.uid).setValue(user).addOnCompleteListener{
                            if(task.isSuccessful){
                                Log.i("WORKED","registered!")
                            }
                            else{
                                Log.i("WORKED","not regisrered!")

                            }


                        }


                }
                 */



            }
        }
    }

         */
}