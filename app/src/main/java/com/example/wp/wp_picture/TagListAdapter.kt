package com.example.wp.wp_picture

import android.content.Context
import android.view.View
import android.widget.TextView
import com.wp.picture.widget.TagLayoutView

/**
 * Created by wp on 2021/2/2.
 */
class TagListAdapter(private val context: Context) : TagLayoutView.TagViewAdapter<String>() {
    init {
        currentPosition = 0
    }

    override fun createView(): View {
        return View.inflate(context, R.layout.tag_banner_item_list, null)
    }

    override fun bindView(holder: ViewHolder, position: Int) {
        val tag = getItem(position)
        val tvTitle = holder.itemView.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = tag
    }
}