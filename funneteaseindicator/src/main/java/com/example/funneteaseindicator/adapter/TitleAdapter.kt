package com.example.funneteaseindicator.adapter

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.example.funneteaseindicator.`interface`.OnItemSelectedListener

class TitleAdapter(
    private val context: Context?,
    private var textSize: Float,
    private var textColor: Int,
    private var titles: Array<CharSequence>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listener: OnItemSelectedListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val textView = TextView(context)
        val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        textView.layoutParams = lp
        textView.textSize = textSize
        textView.gravity = Gravity.CENTER
        textView.setTextColor(textColor)
        return TitleHolder(textView)
    }

    override fun getItemCount(): Int = titles.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TitleHolder).textView.text = titles[position]
        holder.textView.setOnClickListener{
            if(listener!=null)
                listener?.onItemSelected(position)
        }

    }

    fun setOnItemClickListener(onItemSelectedListener: OnItemSelectedListener){
        this@TitleAdapter.listener = onItemSelectedListener
    }
    inner class TitleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView as TextView
    }
}