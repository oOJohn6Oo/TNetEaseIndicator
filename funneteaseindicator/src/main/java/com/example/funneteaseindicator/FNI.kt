package com.example.funneteaseindicator

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.funneteaseindicator.`interface`.OnTitleSelectedListener
import kotlinx.android.synthetic.main.layout_indicator.view.*


class FNI @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private var isViewScrolling = false
    private var onTitleSelectedListener: OnTitleSelectedListener? = null
    private var touchX = 0f
    private var outsideWidth = 0
    private var radiusRound = 0f

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FNI,defStyleAttr,0)
        val titleColor = typedArray.getColor(R.styleable.FNI_title_color, Color.RED)
        val titleTextSize = typedArray.getFloat(R.styleable.FNI_title_text_size, 18f)
        val titleFirstText = typedArray.getInt(R.styleable.FNI_title_first_text, R.string.text_title1)
        val titleSecondText = typedArray.getInt(R.styleable.FNI_title_second_text, R.string.text_title2)
        val selectedTitleColor = typedArray.getColor(R.styleable.FNI_selected_title_color, Color.WHITE)
        outsideWidth = typedArray.getLayoutDimension(R.styleable.FNI_android_layout_width, 300)
        val outsideHeight = typedArray.getLayoutDimension(R.styleable.FNI_android_layout_height, 80)
        radiusRound = typedArray.getDimension(R.styleable.FNI_radius_round, 30f)
        typedArray.recycle()

        LayoutInflater.from(context).inflate(R.layout.layout_indicator, this)


        this.minimumWidth = outsideWidth
        this.minimumHeight = outsideHeight
        //
        val drawable = object : GradientDrawable() {}
        drawable.setStroke(1, titleColor)
        drawable.cornerRadius = radiusRound
        this.background = drawable

        first_title.minWidth = (outsideWidth / 2)
        first_title.minHeight = outsideHeight
        first_title.textSize = titleTextSize
        first_title.setText(titleFirstText)
        first_title.setTextColor(titleColor)

        first_title_bk.minWidth = (outsideWidth / 2)
        first_title_bk.minHeight = outsideHeight
        first_title_bk.textSize = titleTextSize
        first_title_bk.setText(titleFirstText)
        first_title_bk.setTextColor(selectedTitleColor)

        second_title.minWidth = (outsideWidth / 2)
        second_title.minHeight = outsideHeight
        second_title.textSize = titleTextSize
        second_title.setText(titleSecondText)
        second_title.setTextColor(titleColor)

        second_title_bk.minWidth = (outsideWidth / 2)
        second_title_bk.minHeight = outsideHeight
        second_title_bk.textSize = titleTextSize
        second_title_bk.setText(titleSecondText)
        second_title_bk.setTextColor(selectedTitleColor)


        line_view.setBackgroundColor(titleColor)
        line_view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radiusRound)
            }
        }
        line_view.clipToOutline = true

        //文字显隐
        foreShow.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width / 2, view.height, radiusRound)
            }
        }
        foreShow.clipToOutline = true

        this.isClickable = true
        //init listener
        this.setOnTouchListener { _, event ->
            val params = line_view.layoutParams as LayoutParams
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (params.leftMargin == 0 && event.x > outsideWidth / 2) {
                        params.leftMargin += (outsideWidth / 2)
                        line_view.layoutParams = params
                        setViewOutLine(params.leftMargin, params.leftMargin + (outsideWidth / 2))
                    } else if (params.leftMargin == (outsideWidth / 2) && event.x <= outsideWidth / 2) {
                        params.leftMargin = 0
                        line_view.layoutParams = params
                        setViewOutLine(params.leftMargin, params.leftMargin + (outsideWidth / 2))
                    }
                    touchX = event.rawX
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val disX = event.rawX - touchX
                    if ((params.leftMargin + disX) >= 0 && (params.leftMargin + disX) <= outsideWidth / 2) {
                        params.leftMargin = (params.leftMargin + disX).toInt()
                        line_view.layoutParams = params
                        setViewOutLine(params.leftMargin, params.leftMargin + (outsideWidth / 2))
                    }
                    touchX = event.rawX
                    false
                }
                MotionEvent.ACTION_UP -> {
                    isViewScrolling = true
                    if (params.leftMargin > outsideWidth / 2 - params.leftMargin) {
                        params.leftMargin = (outsideWidth / 2)
                        if (onTitleSelectedListener != null)
                            onTitleSelectedListener?.titleSelected(1)
                    } else {
                        params.leftMargin = 0
                        if (onTitleSelectedListener != null)
                            onTitleSelectedListener?.titleSelected(0)
                    }
                    line_view.layoutParams = params
                    setViewOutLine(params.leftMargin, params.leftMargin + (outsideWidth / 2))

                    this.postDelayed({ isViewScrolling = false }, 300)
                    performClick()
                    true
                }
                else -> true
            }

        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    fun onPagerScrolling(position: Int, positionOffset: Float) {
        if (!isViewScrolling) {
            //View移动
            val lp = line_view.layoutParams as LayoutParams
            lp.marginStart = (this.width / 2 * position + positionOffset * this.width / 2).toInt()
            line_view.layoutParams = lp
            //文字显示
            setViewOutLine(lp.marginStart, (lp.marginStart + outsideWidth / 2))
        }
    }

    private fun setViewOutLine(left: Int, right: Int) {
        foreShow.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(left, 0, right, view.height, radiusRound)
            }
        }
    }

    fun setOnTitleSelectedListener(listener: OnTitleSelectedListener) {
        this.onTitleSelectedListener = listener
    }
}