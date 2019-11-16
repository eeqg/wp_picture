package com.example.wp.wp_picture

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.wp.picture.widget.SimpleFloating
import kotlinx.android.synthetic.main.activity_sample2.*

class Sample2Activity : AppCompatActivity() {

    private lateinit var simpleFloating: SimpleFloating

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample2)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SampleListAdapter()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING,
                    RecyclerView.SCROLL_STATE_SETTLING ->
                        simpleFloating.startCollapseAnimation()
                    RecyclerView.SCROLL_STATE_IDLE ->
                        simpleFloating.startExpandAnimationDelay()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        val layoutParams = FrameLayout.LayoutParams(120, 120)
                .apply {
                    gravity = Gravity.BOTTOM or Gravity.END
                    rightMargin = 10
                    bottomMargin = 150
                }
        ImageView(this).apply {
            setImageDrawable(ColorDrawable(Color.parseColor("#90FF2323")))
            setOnClickListener {
                simpleFloating.hide()
            }

            simpleFloating = SimpleFloating(context, this, layoutParams)
                    .setCollapseSite(SimpleFloating.Site.RIGHT)
            simpleFloating.show()
        }
    }
}
