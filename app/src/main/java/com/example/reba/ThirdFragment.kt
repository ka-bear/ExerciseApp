package com.example.onboarding.onboardingFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.reba.MainActivity
import com.example.reba.R
import com.example.reba.introPages.login
import com.google.firebase.auth.FirebaseAuth

class ThirdFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    override fun onStart() {
        super.onStart()
        val button = requireView().findViewById<Button>(R.id.button)

        mAuth = FirebaseAuth.getInstance()

        val user = mAuth?.currentUser


        button.setOnClickListener{
            if(user==null) {
                val intent = Intent(context, login::class.java)
                startActivity(intent)
            }
            else {
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}