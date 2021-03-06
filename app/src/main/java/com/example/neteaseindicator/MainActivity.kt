package com.example.neteaseindicator

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.funneteaseindicator.`interface`.OnItemSelectedListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val images = object : ArrayList<ImageView>() {}
        val imageView = object : ImageView(this) {}
        imageView.setBackgroundColor(Color.GREEN)
        val imageView2 = object : ImageView(this) {}
        imageView2.setBackgroundColor(Color.BLACK)
        val imageView3 = object : ImageView(this) {}
        imageView3.setBackgroundColor(Color.BLUE)
        images.add(imageView)
        images.add(imageView2)
        images.add(imageView3)
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return images.size
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view == `object`
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                super.destroyItem(container, position, `object`)
                container.removeViewAt(position)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                container.addView(images[position])
                return container.getChildAt(position)
            }
        }
        fni.setOnTitleSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(position: Int) {
                viewPager.setCurrentItem(position, true)
                Log.d("LQ1111","position:$position")
            }
        })
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                fni.onPagerScrolling(position, positionOffset)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {

            }

        })
    }
}
