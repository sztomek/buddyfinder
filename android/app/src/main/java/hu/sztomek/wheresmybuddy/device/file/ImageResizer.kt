package hu.sztomek.wheresmybuddy.device.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils

class ImageResizer(private val dimension: Int) {

    fun resize(file: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(file)
        return ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
    }

}