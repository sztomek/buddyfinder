package hu.sztomek.wheresmybuddy.presentation.screens.landing.discover

import android.graphics.Bitmap

interface MarkerImageCache {

    fun hasBitmap(id: String): Boolean
    fun getBitmap(id: String): Bitmap
    fun storeBitmap(id: String, bitmap: Bitmap)
    fun wipe()

}