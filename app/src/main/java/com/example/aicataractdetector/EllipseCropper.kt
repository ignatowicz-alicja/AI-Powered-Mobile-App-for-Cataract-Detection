package com.example.aicataractdetector.util

import android.graphics.*

class EllipseCropper {

    fun crop(source: Bitmap, points: List<PointF>): Bitmap {
        val maskBitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(maskBitmap)

        canvas.drawColor(Color.BLACK)

        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                close()
            }
        }

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        canvas.drawPath(path, paint)

        val maskPixels = IntArray(maskBitmap.width * maskBitmap.height)
        maskBitmap.getPixels(maskPixels, 0, maskBitmap.width, 0, 0, maskBitmap.width, maskBitmap.height)

        val sourcePixels = IntArray(source.width * source.height)
        source.getPixels(sourcePixels, 0, source.width, 0, 0, source.width, source.height)


        val outputBitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        for (i in sourcePixels.indices) {
            val color = if (maskPixels[i] != Color.BLACK) {
                sourcePixels[i] or (0xFF shl 24)
            } else {
                Color.TRANSPARENT
            }
            outputBitmap.setPixel(i % source.width, i / source.width, color)
        }

        return outputBitmap
    }
}
