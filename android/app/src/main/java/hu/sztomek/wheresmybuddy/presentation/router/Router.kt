package hu.sztomek.wheresmybuddy.presentation.router

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.firebase.ui.auth.AuthUI
import hu.sztomek.wheresmybuddy.presentation.screens.landing.LandingActivity
import hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.details.ProfileDetailsActivity
import hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.edit.ProfileEditActivity
import hu.sztomek.wheresmybuddy.presentation.screens.splash.SplashActivity
import hu.sztomek.wheresmybuddy.presentation.service.LocationService
import timber.log.Timber

private const val TAG_CURRENT = "current"

class Router(private var activity: FragmentActivity) : IRouter {

    companion object {
        const val RC_SIGN_IN = 1234
    }

    override fun close() {
        Timber.d("close()")
        activity.finish()
    }

    override fun back() {
        Timber.d("back()")
        if (activity.supportFragmentManager.backStackEntryCount > 0) {
            activity.supportFragmentManager.popBackStack()
        } else {
            close()
        }
    }

    override fun restart() {
        Timber.d("restart()")
        val intent = Intent(activity, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
    }

    override fun toLanding() {
        Timber.d("toLanding()")
        toLandingWithExtras(null)
    }

    private fun toLandingWithExtras(locationId: String?) {
        val intent = Intent(activity, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        locationId?.let {
            LandingActivity.putLocation(intent, locationId)
        }
        activity.startActivity(intent)
    }

    override fun toLogin() {
        Timber.d("toLogin()")
        activity.startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(listOf(
                                AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()
                        ))
                        .build(), RC_SIGN_IN
        )
    }

    override fun toProfileEdit(userId: String) {
        activity.startActivity(ProfileEditActivity.starter(activity, userId))
    }

    override fun toProfile(userId: String) {
        activity.startActivity(ProfileDetailsActivity.starter(activity, userId))
    }

    override fun toMap(locationId: String) {
        Timber.d("toMap locationId [$locationId]")
        toLandingWithExtras(locationId)
    }

    override fun onActivityResult(reqCode: Int, result: Int, data: Intent?) {
        if (reqCode == RC_SIGN_IN) {
            if (result == Activity.RESULT_OK) {
                Timber.d("onActivityResult: successful login")
                toLanding()
                close()
            } else {
                Timber.d("onActivityResult: login failed")
                restart()
                close()
            }
        }
    }

    override fun replaceContent(id: Int, fragment: Fragment) {
        Timber.d("replaceContent($id, $fragment)")
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(id, fragment, TAG_CURRENT)
        transaction.commit()
    }

    override fun getContent(): Fragment {
        Timber.d("getContent")
        return activity.supportFragmentManager.findFragmentByTag(TAG_CURRENT)
    }

    override fun startBroadcast() {
        Timber.d("startBroadcast")
        activity.startService(LocationService.startBroadcast(activity))
    }

    override fun stopBroadcast() {
        Timber.d("stopBroadcast")
        activity.startService(LocationService.stopBroadcast(activity))
    }
}