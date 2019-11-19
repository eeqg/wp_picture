package com.wp.picture.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by wp on 2019/11/19.
 */
class VerificationCodeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val TAG = "VerificationCodeView"

    private var textPaint: Paint? = null
    private var linePaint: Paint? = null
    private var mTextSize = 50f
    private val codeNum = 4
    private lateinit var mText: String

    init {
        init()
    }

    private fun init() {
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        linePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        setOnClickListener {
            invalidate()
        }
    }

    fun getText(): String {
        return mText
    }

    fun setTextSize(size: Float) {
        mTextSize = size
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val measuredWidth = measuredWidth
        val measuredHeight = measuredHeight
//        Log.d(TAG, "-----w : $measuredWidth, h : $measuredHeight")

        //create code
        createCode()

        //draw background
        canvas.drawColor(Color.WHITE)

        //draw interfering line
        for (i in 1..codeNum) {
            linePaint!!.color = randomColor((100..200).random())
            linePaint!!.strokeWidth = (1..3).random().toFloat()
            canvas.drawLine((0..measuredWidth).random().toFloat(),
                    (0..measuredHeight).random().toFloat(),
                    (0..measuredWidth).random().toFloat(),
                    (0..measuredHeight).random().toFloat(),
                    linePaint!!)
        }

        //draw text
        val offset = 20
        val charWidth = textPaint!!.measureText(mText[0].toString()).toInt()
        var cX = offset
        mText.forEachIndexed { index, c ->
            textPaint!!.textSize = mTextSize + (-10..10).random()
            textPaint!!.color = randomColor()

            val restWidth = measuredWidth - offset - cX - (mText.length - index) * charWidth
//            Log.d(TAG, "-----restWidth = $restWidth")
            cX += (0..restWidth).random()
            val cY = (mTextSize.toInt()..measuredHeight).random().toFloat()
//            Log.d(TAG, "-----$index--cx=$cX, cy=$cY")
            canvas.drawText(c.toString(), cX.toFloat(), cY, textPaint!!)
            cX += charWidth
        }
    }

    private fun createCode() {
        val stringBuilder = StringBuilder()
        for (i in 1..codeNum) {
            stringBuilder.append((0..9).random())
        }
        mText = stringBuilder.toString()
    }

    private fun randomColor(): Int {
        return randomColor(255)
    }

    private fun randomColor(alpha: Int): Int {
        val red = (0..255).random()
        val green = (0..255).random()
        val blue = (0..255).random()
//        Log.d(TAG, "-----red=$red, green=$green, blue=$blue")
        return Color.argb(alpha, red, green, blue)
    }
}
