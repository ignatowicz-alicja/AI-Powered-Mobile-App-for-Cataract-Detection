package com.example.aicataractdetector.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min
import androidx.core.graphics.toColorInt

class PupilOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = "#6FDE7C".toColorInt()
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply {
        color = "#00A6FF".toColorInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val controlPoints = mutableListOf<PointF>()
    private var activePointIndex = -1
    private val touchRadius = 50f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (controlPoints.size >= 2) {
            val path = Path().apply {
                moveTo(controlPoints[0].x, controlPoints[0].y)
                for (i in 1 until controlPoints.size) {
                    lineTo(controlPoints[i].x, controlPoints[i].y)
                }
                close()
            }
            canvas.drawPath(path, paint)
        }

        controlPoints.forEach {
            canvas.drawCircle(it.x, it.y, 10f, pointPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activePointIndex = controlPoints.indexOfFirst {
                    hypot((event.x - it.x).toDouble(), (event.y - it.y).toDouble()) < touchRadius
                }
                return activePointIndex != -1
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointIndex != -1) {
                    controlPoints[activePointIndex].x = event.x
                    controlPoints[activePointIndex].y = event.y
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                activePointIndex = -1
            }
        }
        return false
    }

    fun initializeEllipse(centerX: Float, centerY: Float, radius: Float) {
        controlPoints.clear()
        val angleStep = 2 * Math.PI / 20
        for (i in 0 until 20) {
            val angle = i * angleStep
            val x = centerX + radius * Math.cos(angle).toFloat()
            val y = centerY + radius * Math.sin(angle).toFloat()
            controlPoints.add(PointF(x, y))
        }
        invalidate()
    }



    fun getControlPoints(): List<PointF> = controlPoints.toList()

    fun extractEllipseRegion(source: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val maskPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val path = Path().apply {
            if (controlPoints.isNotEmpty()) {
                moveTo(controlPoints[0].x, controlPoints[0].y)
                for (i in 1 until controlPoints.size) {
                    lineTo(controlPoints[i].x, controlPoints[i].y)
                }
                close()
            }
        }


        val mask = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(mask).drawPath(path, maskPaint)

        val paint = Paint().apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }

        canvas.drawBitmap(source, 0f, 0f, null)
        canvas.drawBitmap(mask, 0f, 0f, paint)

        return result
    }

}


