package hu.sztomek.wheresmybuddy.presentation.model

import android.content.res.Resources
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.presentation.common.adapter.RecyclerViewItem
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel
import java.text.DateFormat
import java.util.*

data class PendingRequestItemModel(val profile: ProfileModel, val connectionRequestModel: ConnectionRequestModel, val infoModel: InfoModel) : IdProvider, RecyclerViewItem {

    override val id: String?
        get() = connectionRequestModel.id
}

data class InfoModel(val sent: Long) {

    companion object {
        const val ONE_HOUR = 60 * 60 * 1000
        const val ONE_DAY = 24 * ONE_HOUR
    }

    fun getSentText(resources: Resources, dateFormat: DateFormat): String {
        val diff = System.currentTimeMillis() - sent
        return if(diff > ONE_DAY) {
            resources.getString(R.string.format_request_sent_on, dateFormat.format(Date(sent)))
        } else {
            resources.getString(R.string.format_request_sent_hours_ago, (diff / ONE_HOUR).toString())
        }
    }
}