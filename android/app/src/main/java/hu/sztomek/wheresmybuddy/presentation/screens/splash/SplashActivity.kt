package hu.sztomek.wheresmybuddy.presentation.screens.splash

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.location.ResolvableLocationException
import hu.sztomek.wheresmybuddy.device.notification.IntentPayloadHandler
import hu.sztomek.wheresmybuddy.device.notification.actions.*
import hu.sztomek.wheresmybuddy.domain.INotifications
import hu.sztomek.wheresmybuddy.presentation.common.BaseActivity
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.di.Injectable
import hu.sztomek.wheresmybuddy.presentation.model.persistable.SplashModel
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.service.LocationService
import pl.tajchert.nammu.Nammu
import pl.tajchert.nammu.PermissionCallback
import timber.log.Timber
import javax.inject.Inject

class SplashActivity : BaseActivity<SplashModel, SplashViewModel>(), Injectable {

    @Inject
    lateinit var router: IRouter
    @Inject
    lateinit var vmf: ViewModelProvider.Factory
    @Inject
    lateinit var notifications: INotifications

    companion object {
        private const val REQ_LOCATION_PERMISSION = 876

        var pendingException: ResolvableLocationException? = null
    }

    override fun getViewModelFactory(): ViewModelProvider.Factory {
        return vmf
    }

    override fun getViewModelClass(): Class<SplashViewModel> {
        return SplashViewModel::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkUser()
    }

    override fun handleStateChange(state: State<SplashModel>?) {
        state?.let { newState ->
            newState.data?.let {
                if (!newState.loading && newState.error == null) {
                    when (it.hasUser) {
                        true -> when (it.hasGcmToken) {
                            null -> viewModel.checkGcmToken()
                            else -> checkIntent()
                        }
                        false -> {
                            Toast.makeText(this, R.string.label_login_required, Toast.LENGTH_SHORT).show()
                            router.toLogin()
                        }
                    }
                }
            }
        }
    }

    private fun checkIntent() {
        val notificationAction = IntentPayloadHandler.getActionFromIntent(intent)
        if (notificationAction != null) {
            notifications.dismiss(notificationAction.notificationId)
            when (notificationAction) {
                is AcceptConnectionRequest -> {
                    viewModel.acceptRequest(notificationAction.requestId)
                }
                is DeclineConnectionRequest -> {
                    viewModel.declineRequest(notificationAction.requestId)
                }
                is NewConnection -> {
                    router.toProfile(notificationAction.fromUserId)
                    router.close()
                }
                is NewLocation -> {
                    router.toMap(notificationAction.locationId)
                    router.close()
                }
                is LocationPermissionRequired -> {
                    Nammu.askForPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, object : PermissionCallback {
                        override fun permissionGranted() {
                            retryOriginal()
                        }

                        override fun permissionRefused() {
                            // TODO show sad toast :(
                            router.close()
                        }
                    })
                }
                is LocationRecoverableError -> {
                    if (pendingException != null) {
                        pendingException?.resolve(this, REQ_LOCATION_PERMISSION)
                    } else {
                        Timber.d("Couldn't find exception in extras for action LocationRecoverableError")
                        router.close()
                    }
                }
                else -> {
                    Timber.d("Unknown notification action: [$notificationAction]")
                    router.toLanding()
                    router.close()
                }
            }
        } else {
            router.toLanding()
            router.close()
        }
    }

    private fun retryOriginal() {
        val originalAction = IntentPayloadHandler.getOriginalActionFromIntent(intent)
        originalAction?.let {
            val intent = Intent(this@SplashActivity, LocationService::class.java)
            IntentPayloadHandler.putActionToIntent(intent, it)
            startService(intent)
        }
        router.close()
    }

    override fun getPersistableUiState(): SplashModel {
        return SplashModel(null, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_LOCATION_PERMISSION) {
            retryOriginal()
            pendingException = null
        } else {
            router.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_splash
    }
}