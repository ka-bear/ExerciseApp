package com.example.reba

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.onboarding.ViewPager2FragmentAdapter
import com.example.onboarding.onboardingFragments.FirstFragment
import com.example.onboarding.onboardingFragments.SecondFragment
import com.example.onboarding.onboardingFragments.ThirdFragment
import me.relex.circleindicator.CircleIndicator3

class obording : AppCompatActivity() {
    private val fragmentList = ArrayList<Fragment>()
    private lateinit var viewPager: ViewPager2
    private lateinit var indicator: CircleIndicator3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboard)
        castView()
        fragmentList.add(FirstFragment())
        fragmentList.add(SecondFragment())
        fragmentList.add(ThirdFragment())

        viewPager.adapter = ViewPager2FragmentAdapter(this, fragmentList)
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        indicator.setViewPager(viewPager)
    }

    private fun castView() {
        viewPager = findViewById(R.id.view_pager2)
        indicator = findViewById(R.id.indicator)
    }
}