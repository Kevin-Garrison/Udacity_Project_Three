package com.udacity.loadingbutton

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.withStyledAttributes
import com.udacity.R
import kotlinx.android.synthetic.main.content_main.view.*
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    private lateinit var buttonText: String
    private val textRect = Rect()
    private var backgroundClr = R.attr.backgroundColor

    private var arcPosition: Float = 0f
    private var valueAnimator = ValueAnimator()

    private var customProgress = false

    private var position = 0f


    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when(new) {

            ButtonState.Loading -> {
                buttonText = context.getString(R.string.button_status_loading)
                startAnimator()
            }

            ButtonState.Completed -> {
                buttonText = context.getString(R.string.button_status_download_complete)
                backgroundClr = context.getColor(R.color.colorPrimary)
                arcReset()
            }

            ButtonState.Clicked -> {
                custom_button.isEnabled = false
            }

            ButtonState.Idle -> {
                buttonText = context.getString(R.string.button_status_idle)
            }

            ButtonState.Download -> {
                buttonText = context.getString(R.string.button_status_download)
                custom_button.isEnabled = true
            }

            ButtonState.Test -> {
                buttonText = context.getString(R.string.button_status_test)
                startAnimator()
            }
        }
        invalidate()
    }

    init {
        context.withStyledAttributes(
            attrs,
            R.styleable.LoadingButton
            ) {
            buttonText = getString(R.styleable.LoadingButton_text).toString()
            backgroundClr = context.getColor(R.color.colorPrimary)
        }
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        color = context.getColor(R.color.textColor)
    }

    private val paintIdle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.colorPrimary)
    }

    private val paintLoading = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.colorPrimaryDark)
    }

    private val paintArc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.arcColor)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawColor(backgroundClr)
        paintText.getTextBounds(buttonText, 0, buttonText.length, textRect)
        canvas?.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paintIdle)

        if ((buttonState == ButtonState.Loading) || (buttonState == ButtonState.Test)) {
            var right = arcPosition * measuredWidth
            if(customProgress) {
                 right = position * measuredWidth
            }
            canvas?.drawRect(0f, 0f, right, measuredHeight.toFloat(), paintLoading)

            val arcDiameter = 20f

            var progress = arcPosition * 360f
            if(customProgress) {
                progress = position  * 360f
            }

            canvas?.drawArc(width.toFloat() - measuredHeight.toFloat() - arcDiameter,
                paddingTop.toFloat() + arcDiameter,
                width.toFloat() - arcDiameter,
                height.toFloat() - arcDiameter,
                270f,
                progress,
                true,
                paintArc)

            if(progress >= 360) {
                if(buttonState == ButtonState.Test) {
                    setState(ButtonState.Idle)
                } else {
                    setState(ButtonState.Completed)
                }
                valueAnimator.cancel()
            }
        }
        val centerX = measuredWidth.toFloat() / 2
        val centerY = measuredHeight.toFloat() / 2 - textRect.centerY()

        canvas?.drawText(buttonText,centerX, centerY, paintText)
    }

    fun setState(state: ButtonState) {
        buttonState = state
    }

    fun setCurrentPosition(pos: Float) {
        customProgress = true
        position = pos
        Log.i("loader", "position = $position")

        invalidate()
    }

    private fun arcReset() {
        customProgress = false
        arcPosition = 0f
        position = 0F
    }

    private fun startAnimator() {
        backgroundClr = context.getColor(R.color.colorPrimaryDark)
        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                arcPosition = animatedValue as Float
                if(!customProgress) invalidate()
            }
            if(!customProgress) duration = 2000L
            start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}