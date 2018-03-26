package hu.sztomek.wheresmybuddy.device.location

import android.app.Activity
import com.google.android.gms.common.api.ResolvableApiException

class ResolvableLocationException(private val original: ResolvableApiException) : Exception() {

    fun resolve(activity: Activity, requestCode: Int ) {
        original.startResolutionForResult(activity, requestCode)
    }

}