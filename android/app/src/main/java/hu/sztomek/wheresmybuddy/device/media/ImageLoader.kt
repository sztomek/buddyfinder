package hu.sztomek.wheresmybuddy.device.media

import android.graphics.Bitmap
import android.widget.ImageView
import io.reactivex.Single

interface ImageLoader {

    fun loadImage(path: String): Single<Bitmap>
    fun loadImageInto(path: String, view: ImageView)

}