package com.example.funneteaseindicator

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
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
                    bgdRadius = typedArray.getDimension(R.styleable.FNI_bgd_radius, 24f)
                else
                    isBgdPercentRadius = true

                fgdRadiusPercent = typedArray.getDimension(R.styleable.FNI_fgd_radius_percent, -1f)
                if (fgdRadiusPercent == -1f) //没有设定前景百分比radius
                    fgdRadius = typedArray.getDimension(R.styleable.FNI_fgd_radius, 24f)
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
        recycler_view_fgd.requestDisallowInterceptTouchEvent(true)// 由该view处理所有触摸操作

        recycler_view_bgd.layoutManager = GridLayoutManager(context, titles.size)
        recycler_view_fgd.layoutManager = GridLayoutManager(context, titles.size)
        recycler_view_fgd.adapter = TitleAdapter(context, fgdTextSize, fgdTextColor, titles)
        recycler_view_bgd.adapter = TitleAdapter(context, bgdTextSize, bgdTextColor, titles)


        val pro = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(lineLeft, 0, lineLeft + titleWidth, view.height, fgdRadius)
            }
        }
        recycler_view_fgd.outlineProvider = pro
//        手势监听
//        recycler_view_fgd.setOnTouchListener { view, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    if (lineLeft == 0 && event.x > titleWidth) {
//                        lineLeft += titleWidth/2
//                        setViewOutLine(lineLeft)
//                    } else if (lineLeft == titleWidth && event.x <= titleWidth) {
//                        lineLeft = 0
//                        line_view.layoutParams = params
//                        setViewOutLine(lineLeft, lineLeft + (titleWidth))
//                    }
//                    touchX = event.rawX
//                    false
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    val disX = event.rawX - touchX
//                    if ((lineLeft + disX) >= 0 && (lineLeft + disX) <= view.width) {
//                        lineLeft = (lineLeft + disX).toInt()
//                        setViewOutLine()
//                    }
//                    touchX = event.rawX
//                    false
//                }
//                MotionEvent.ACTION_UP -> {
//                     防止双向绑定导致的 view 闪现
//                    isViewScrolling = true
//                    val selectedItem = lineLeft/titleWidth
//                    if (onItemSelectedListener != null)
//                        onItemSelectedListener?.onItemSelected(0)
//                    this.postDelayed({ isViewScrolling = false }, 300)
//                    performClick()
//                    true
//                }
//                else -> true
//            }
//
//        }
    }


    fun onPagerScrolling(position: Int, positionOffset: Float) {
        if (!isViewScrolling) {
            lineLeft = (titleWidth * (position + positionOffset)).toInt()
            recycler_view_fgd.refreshDrawableState()
//            setViewOutLine()
        }
    }

    private fun setViewOutLine() {
        recycler_view_fgd.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(lineLeft, 0, lineLeft + titleWidth, view.height, fgdRadius)
            }
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
            ((measuredWidth - mElevation * 2) / titles.size).toInt()
        else
            measuredWidth / titles.size
    }

}