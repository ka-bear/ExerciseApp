package com.example.reba

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class helppg : AppCompatActivity() {
    private val chpsList = ArrayList<infodata>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)



        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        chpsList.add(infodata("How to use", R.drawable.personrecording, "Place your camera in a location where the phone can see your entire body!"))
        chpsList.add(infodata("How to plank", R.drawable.planks, "To plank correctly, keep your back and legs completely straight and parallel to the ground!"))
        chpsList.add(infodata("How to wall-sit", R.drawable.wallsits, "To wall sit correctly, ensure that your hips, knees and back are all perpendicular to each other!"))

        val adapter = RecyclerAdapter(chpsList)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.button12).setOnClickListener { _ ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        }

    }
}