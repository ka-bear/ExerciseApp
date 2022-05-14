package com.example.reba

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.homepage.*
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.floatingactionbutton.FloatingActionButton


class homePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        var maxxx = 0f

        //Part1
        val entries = ArrayList<Entry>()

        val str = arrayListX.toString().subSequence(1, arrayListX.toString().length - 1).toString()

        if(str!=null) {
            var x: ArrayList<Double> = parseString(str)

            for (i in 0 until x.size step 2){
                entries.add(Entry(x.get(i).toFloat(), x.get(i+1).toFloat()))
                if(x.get(i).toFloat()>maxxx){
                    maxxx=x.get(i).toFloat();
                }
            }
        }

        val vl = LineDataSet(entries, "Duration of correct exercise")

        vl.setDrawValues(false)
        vl.setDrawFilled(true)
        vl.lineWidth = 3f

        lineChart.xAxis.labelRotationAngle = 0f

        lineChart.data = LineData(vl)


        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.axisMinimum = -0.1f
        lineChart.xAxis.axisMaximum = maxxx+0.1f

        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)

        lineChart.description.text = "Days"
        lineChart.setNoDataText("No data yet!")
        lineChart.animateX(1800, Easing.EaseInExpo)



        findViewById<TextView>(R.id.textView10).text = improvStr

        findViewById<Button>(R.id.button7).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button8).setOnClickListener {
            val intent = Intent(this, settings::class.java)
            startActivity(intent)
        }



        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "I just improved my exercise posture using REBA! Join me!")
            }

            startActivity(Intent.createChooser(intent, "Share via"))

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menue, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_gyro_lock -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.businessinsider.com/science-based-exercise-advice-2015-12"))
                startActivity(intent)
                true
            }
            else -> {
                true
            }
        }
    }
}