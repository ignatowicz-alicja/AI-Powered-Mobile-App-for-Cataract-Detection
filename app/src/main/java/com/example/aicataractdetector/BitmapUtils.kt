package com.example.aicataractdetector.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

object BitmapUtils {

    fun getBitmapFixed(context: Context, uri: Uri): Bitmap {
        val raw = context.contentResolver.openInputStream(uri)!!.use {
            BitmapFactory.decodeStream(it)
        }
        val orient = context.contentResolver.openInputStream(uri)!!.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
        val m = Matrix()
        when (orient) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL   -> m.setScale( 1f,-1f)
            ExifInterface.ORIENTATION_ROTATE_90       -> m.setRotate( 90f)
            ExifInterface.ORIENTATION_ROTATE_180      -> m.setRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270      -> m.setRotate(270f)
            ExifInterface.ORIENTATION_TRANSPOSE       -> { m.setRotate( 90f); m.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE      -> { m.setRotate(270f); m.postScale(-1f, 1f) }
        }



        return Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
    }
}

