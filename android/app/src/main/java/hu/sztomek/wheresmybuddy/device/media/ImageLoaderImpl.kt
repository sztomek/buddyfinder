package hu.sztomek.wheresmybuddy.device.media

import android.graphics.Bitmap
import android.widget.ImageView
import com.squareup.picasso.Picasso
import io.reactivex.Single

class ImageLoaderImpl(private val picasso: Picasso) : ImageLoader {

    override fun loadImage(path: String): Single<Bitmap> {
        return Single.create {
            emitter ->
            try {
                val bitmap = picasso.load(path).get()
                emitter.onSuccess(bitmap)
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    override fun loadImageInto(path: String, view: ImageView) {
        picasso.load(path).into(view)
    }

}