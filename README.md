# Android仿iOS网易云音乐viewpager指示器

## 效果
![截图](./img/screenshot.png)
![动画](./img/gif.gif)
## 使用(ConstraintLayout还有些问题...)
    ```
    <!--xml-->
        <com.example.funneteaseindicator.FNI
            android:id="@+id/fni"
            android:layout_marginTop="2dp"
            android:layout_marginBottom="2dp"
            android:layout_width="300dp"
            app:radius_round="24dp"
            app:title_color="@android:color/holo_red_light"
            app:selected_title_color="@android:color/white"
            android:layout_height="45dp"/>

    //Java
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                fni.onPagerScrolling(position,positionOffset)
            }
            override fun onPageScrollStateChanged(state: Int) {
            }
            override fun onPageSelected(position: Int) {
            }
        })
    ```