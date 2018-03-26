package hu.sztomek.wheresmybuddy.presentation.screens.landing.discover

import android.graphics.Bitmap

class MarkerImageCacheImpl : MarkerImageCache {

    private val map: HashMap<String, Bitmap> = hashMapOf()

    override fun hasBitmap(id: String): Boolean {
        return map.contains(id)
    }

    override fun getBitmap(id: String): Bitmap {
        return map[id]!!
    }

    override fun storeBitmap(id: String, bitmap: Bitmap) {
        map[id] = bitmap
    }

    override fun wipe() {
        map.clear()
    }
}