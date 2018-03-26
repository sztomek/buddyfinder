package hu.sztomek.wheresmybuddy.presentation.model

import android.content.res.Resources
import hu.sztomek.wheresmybuddy.R
import java.text.DateFormat

data class StatusInfoModel(val lastSeen: Long?, val currentStatus: Int?, val lastLocation: LocationModel?) {

    fun getStatusMessage(resources: Resources, dateFormat: DateFormat): String {
        return when {
            currentStatus != null -> resources.getString(R.string.label_currently_active)
            lastSeen != null -> resources.getString(R.string.format_last_seen, dateFormat.format(lastSeen))
            else -> "TODO"
        }
    }

    fun getDistanceMessage(resources: Resources): String {
        val distance = calculateDistance(lastLocation)
        return when{
            distance == null -> "N/A"
            distance > 1_000 -> resources.getString(R.string.format_kilometers_away, distance / 1_000)
            else -> resources.getString(R.string.format_meters_away, distance)
        }
    }

    fun calculateDistance(lastLocation: LocationModel?): Int? {
        lastLocation?.let {
            return 900
        }

        return null
    }

}