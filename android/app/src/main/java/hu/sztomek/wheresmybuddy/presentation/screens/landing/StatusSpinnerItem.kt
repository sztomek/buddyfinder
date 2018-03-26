package hu.sztomek.wheresmybuddy.presentation.screens.landing

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import hu.sztomek.wheresmybuddy.R

sealed class StatusSpinnerItem(@StringRes val labelRes: Int, @DrawableRes val drawableRes: Int, val position: Int) {

    class OfflineStatusSpinnerItem : StatusSpinnerItem(R.string.label_offline, R.drawable.inactive_dot, 0)
    class OnlineStatusSpinnerItem : StatusSpinnerItem(R.string.label_online, R.drawable.active_dot, 1)

}