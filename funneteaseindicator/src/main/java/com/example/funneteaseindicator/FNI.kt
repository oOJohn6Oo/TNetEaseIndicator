package com.example.funneteaseindicator

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.example.funneteaseindicator.`interface`.OnItemSelectedListener
import com.example.funneteaseindicator.adapter.TitleAdapter
import kotlinx.android.synthetic.main.layout_indicator.view.*

class FNI @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    private val tag = "FNI"

    private var lineLeft = 0
    private var isViewScrolling = false
    private var bgdTextSize = 0f
    private var bgdTextColor = 0
    private var bgdRadius = 0f
    private var bgdRadiusPercent = 0f
    private var fgdTextSize = 0f
    private var fgdTextColor = 0
    private var fgdRadius = 0f
    private var fgdRadiusPercent = 0f
    private var mElevation = 0f
    private var layoutRadius = 0f
    private var layoutRadiusPercent = 0f
    private var titleWidth = 0
    private var enableElevation = false
    private var enableStroke = true
    private var titles: Array<CharSequence>

    private val bgdDrawable = object : GradientDrawable() {}

    private var isBgdPercentRadius = false
    private var isFgdPercentRadius = false
    private var onItemSelectedListener: OnItemSelectedListener? = null

    private var touchX = 0
    private var isMoveing = false


    /**
     * 默认按照网易云音乐样式处理
     */
    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FNI, defStyleAttr, 0)
        titles = try {
            typedArray.getTextArray(R.styleable.FNI_android_entries)
        } catch (e: IllegalStateException) {
            Log.e(tag, "Titles not set, demo titles loaded.")
            resources.getTextArray(R.array.demo_titles)
        }
        val bgdColor = typedArray.getColor(R.styleable.FNI_bgd_color, Color.WHITE)
        bgdTextSize = typedArray.getDimension(R.styleable.FNI_bgd_text_size, 15f)
        bgdTextColor = typedArray.getColor(R.styleable.FNI_bgd_text_color, Color.RED)

        val fgdColor = typedArray.getColor(R.styleable.FNI_fgd_color, Color.RED)
        fgdTextSize = typedArray.getDimension(R.styleable.FNI_fgd_text_size, 15f)
        fgdTextColor = typedArray.getColor(R.styleable.FNI_fgd_text_color, Color.WHITE)

        // 圆角逻辑
        layoutRadiusPercent = typedArray.getFraction(R.styleable.FNI_radius_percent, 1, 1, 0f)
        if (layoutRadiusPercent == 0f) { // 没有同时设定前后百分比radius
            layoutRadius = typedArray.getDimension(R.styleable.FNI_layout_radius, -1f)
            if (layoutRadius == -1f) { // 没有同时设定前后固定radius
                bgdRadiusPercent = typedArray.getDimension(R.styleable.FNI_bgd_radius_percent, -1f)
                if (bgdRadiusPercent == -1f) //没有设定背景百分比radius
                    bgdRadius = typedArray.getDimension(R.styleable.FNI_bgd_radius, 33f)
                else
                    isBgdPercentRadius = true

                fgdRadiusPercent = typedArray.getDimension(R.styleable.FNI_fgd_radius_percent, -1f)
                if (fgdRadiusPercent == -1f) //没有设定前景百分比radius
                    fgdRadius = typedArray.getDimension(R.styleable.FNI_fgd_radius, 33f)
                else
                    isFgdPercentRadius = true
            } else {
                bgdRadius = layoutRadius
                fgdRadius = layoutRadius
            }
        } else { // 全局百分比radius
            isBgdPercentRadius = true
            isFgdPercentRadius = true
            bgdRadiusPercent = layoutRadiusPercent
            fgdRadiusPercent = layoutRadiusPercent
        }

        enableStroke = typedArray.getBoolean(R.styleable.FNI_enable_stroke, true)
        val strokeWidth = typedArray.getDimension(R.styleable.FNI_stroke_width, 1f)
        enableElevation = typedArray.getBoolean(R.styleable.FNI_enable_elevation, false)
        mElevation = typedArray.getDimension(R.styleable.FNI_elevation, 3f)
        // 资源回收
        typedArray.recycle()

        LayoutInflater.from(context).inflate(R.layout.layout_indicator, this)

        // 如果有elevation，需要给边界留出空间显示阴影
        if (enableElevation) {
            //获取lp
            val params: FrameLayout.LayoutParams = recycler_view_fgd.layoutParams as FrameLayout.LayoutParams
            //设置Margin
            params.setMargins(elevation.toInt(), elevation.toInt(), elevation.toInt(), elevation.toInt())
            (recycler_view_bgd.layoutParams as FrameLayout.LayoutParams).setMargins(
                elevation.toInt(),
                elevation.toInt(),
                elevation.toInt(),
                elevation.toInt()
            )

            recycler_view_fgd.elevation = elevation
        }

        // 设置背景
        // 如果有边框
        if (enableStroke)
            bgdDrawable.setStroke(strokeWidth.toInt(), fgdColor)

        bgdDrawable.cornerRadius = bgdRadius
        bgdDrawable.setColor(bgdColor)
        recycler_view_bgd.background = bgdDrawable

        /**
        设置前景
         */
        recycler_view_fgd.setBackgroundColor(fgdColor)
        recycler_view_fgd.clipToOutline = true  // 只显示部分区域
       recycler_view_fgd.requestDisallowInterceptTouchEvent(false)// 由该view处理所有触摸操作

        recycler_view_bgd.layoutManager = GridLayoutManager(context, titles.size)
        recycler_view_fgd.layoutManager = GridLayoutManager(context, titles.size)
        recycler_view_fgd.adapter = TitleAdapter(context, fgdTextSize, fgdTextColor, titles)
        recycler_view_bgd.adapter = TitleAdapter(context, bgdTextSize, bgdTextColor, titles)
        setViewOutLine()

//  前景      手势监听
        recycler_view_fgd.setOnTouchListener { view, event ->



            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = event.rawX.toInt()
                    lineLeft = event.x.toInt()
                    setLocation()
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    isMoveing = true
                    val flaglenth = view.width/3
                    val disX = event.rawX - touchX
                    lineLeft = (lineLeft + disX).toInt()
                    if (lineLeft<=0)lineLeft = 0
                    else if (lineLeft>=flaglenth*2) lineLeft = flaglenth*2
                        setViewOutLine()
                    Log.e("lineLeft:"+ lineLeft,"disX"+disX + "view.width:" + view.width )
                    touchX = event.rawX.toInt()
                    false
                }
                MotionEvent.ACTION_UP -> {
          //           防止双向绑定导致的 view 闪现
                    if (isMoveing ){
                        isViewScrolling = true
                        isMoveing = false
                        setMoveLocation()
                        performClick()
                        this.postDelayed({ isViewScrolling = false }, 300)
                    }

                    false
                }
                else -> false
            }

        }
    }

    fun onPagerScrolling(position: Int, positionOffset: Float) {
        if (!isViewScrolling) {
            lineLeft = (titleWidth * (position + positionOffset)).toInt()
           setViewOutLine()
        }
    }

    fun setOnTitleSelectedListener(listener: OnItemSelectedListener) {
        onItemSelectedListener = listener
        (recycler_view_fgd.adapter as TitleAdapter).setOnItemClickListener(listener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (isBgdPercentRadius)
            bgdRadius = recycler_view_bgd.height * bgdRadiusPercent
        if (isFgdPercentRadius)
            fgdRadius = recycler_view_fgd.height * fgdRadiusPercent

        titleWidth = if (enableElevation)
            ((measuredWidth - mElevation * 2*resources.displayMetrics.density+0.5) / titles.size).toInt()
        else
            measuredWidth / titles.size
    }

    private fun setViewOutLine(){
        val pro = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(lineLeft, 0, lineLeft + titleWidth, view.height, fgdRadius)
            }
        }
        recycler_view_fgd.outlineProvider = pro
        recycler_view_fgd.invalidateOutline()
    }


   private fun setMoveLocation(){
       val flaglenth = recycler_view_bgd.width/6
       when{
           lineLeft < flaglenth-> {
               lineLeft = 0
               onItemSelectedListener?.onItemSelected(0)
           }
           lineLeft in flaglenth..3*flaglenth -> {
               lineLeft = flaglenth*2
               onItemSelectedListener?.onItemSelected(1)
           }
           lineLeft>3*flaglenth -> {
               lineLeft = flaglenth*4
               onItemSelectedListener?.onItemSelected(2)
           }
       }
       setViewOutLine()
   }


    private fun setLocation(){
        val flaglenth = recycler_view_bgd.width/3
        when{
            lineLeft < flaglenth-> {
                lineLeft = 0
                onItemSelectedListener?.onItemSelected(0)
            }
            lineLeft in flaglenth..2*flaglenth -> {
                lineLeft = flaglenth
                onItemSelectedListener?.onItemSelected(1)
            }
            lineLeft>2*flaglenth -> {
                lineLeft = flaglenth*2
                onItemSelectedListener?.onItemSelected(2)
            }
        }
        setViewOutLine()
    }

}