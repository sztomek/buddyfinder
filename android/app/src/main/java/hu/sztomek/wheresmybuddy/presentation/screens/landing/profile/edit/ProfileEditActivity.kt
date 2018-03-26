package hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.edit

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.common.BaseActivity
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.common.UiError
import hu.sztomek.wheresmybuddy.presentation.di.Injectable
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.view.LoadingErrorConstraintLayout
import pl.aprilapps.easyphotopicker.EasyImage
import pl.tajchert.nammu.Nammu
import pl.tajchert.nammu.PermissionCallback
import timber.log.Timber
import java.io.File
import java.lang.Exception
import javax.inject.Inject

class ProfileEditActivity : BaseActivity<ProfileModel, ProfileEditViewModel>(), Injectable {

    companion object {

        private const val EXTRA_PROFILE_ID = "profileId"

        fun starter(context: Context, profileId: String): Intent {
            val intent = Intent(context, ProfileEditActivity::class.java)
            intent.putExtra(EXTRA_PROFILE_ID, profileId)
            return intent
        }

        fun getIdFromIntent(intent: Intent) = intent.getStringExtra(EXTRA_PROFILE_ID)
    }

    @Inject
    lateinit var vmf: ViewModelProvider.Factory
    @Inject
    lateinit var router: IRouter
    @Inject
    lateinit var imageLoader: ImageLoader

    @BindView(R.id.leclRoot)
    lateinit var rootView: LoadingErrorConstraintLayout
    @BindView(R.id.ivProfile)
    lateinit var ivProfile: ImageView
    @BindView(R.id.tilDisplayName)
    lateinit var tilDisplayName: TextInputLayout
    @BindView(R.id.etDisplayName)
    lateinit var etDisplayName: TextInputEditText
    @BindView(R.id.tvDelete)
    lateinit var tvDelete: View
    @BindView(R.id.btnSubmit)
    lateinit var btnSubmit: View
    @BindView(R.id.tvChangePicture)
    lateinit var tvChangePicture: View

    private lateinit var latestProfile: ProfileModel

    override fun getLayoutId(): Int {
        return R.layout.activity_profile_edit
    }

    override fun getPersistableUiState(): ProfileModel {
        return latestProfile
    }

    override fun getViewModelFactory(): ViewModelProvider.Factory {
        return vmf
    }

    override fun getViewModelClass(): Class<ProfileEditViewModel> {
        return ProfileEditViewModel::class.java
    }

    override fun handleStateChange(state: State<ProfileModel>?) {
        state?.let {
            rootView.state = when {
                state.loading -> {
                    tilDisplayName.error = null
                    LoadingErrorConstraintLayout.STATE_LOADING
                }
                state.error != null -> {
                    if (state.error is UiError.FieldValidationError) {
                        tilDisplayName.error = state.error.message
                        LoadingErrorConstraintLayout.STATE_IDLE
                    } else {
                        rootView.errorMessage = state.error.message
                        LoadingErrorConstraintLayout.STATE_ERROR
                    }
                }
                else -> {
                    if (state.data != null) {
                        if (state.data.profilePicture != null) {
                            imageLoader.loadImageInto(state.data.profilePicture, ivProfile)
                        }
                        etDisplayName.setText(state.data.displayName)
                        latestProfile = state.data
                    } else {
                        router.restart()
                    }
                    LoadingErrorConstraintLayout.STATE_IDLE
                }
            }
            setUiEnabled(rootView.state != LoadingErrorConstraintLayout.STATE_LOADING)
        }
    }

    private fun setUiEnabled(enabled: Boolean) {
        btnSubmit.isEnabled = enabled
        tvDelete.isEnabled = enabled
        tvChangePicture.isEnabled = enabled
        tilDisplayName.isEnabled = enabled
        etDisplayName.isEnabled = enabled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnSubmit.setOnClickListener { _ -> viewModel.updateProfile(latestProfile.copy(displayName = etDisplayName.text.toString())) }
        tvDelete.setOnClickListener { _ ->
            val listener: ((dialogInterface: DialogInterface, buttonId: Int) -> Unit) = { dialogInterface, buttonId ->
                when (buttonId) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        viewModel.deleteAccount()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialogInterface.cancel()
                    }
                }
            }
            // TODO own dialogs
            AlertDialog.Builder(this)
                    .setTitle(R.string.label_please_confirm)
                    .setMessage(R.string.message_delete_account_confirmation)
                    .setPositiveButton(R.string.label_ok, listener)
                    .setNegativeButton(R.string.label_cancel, listener)
                    .create()
                    .show()
        }
        tvChangePicture.setOnClickListener { _ ->
            val permissionsNeeded = mutableListOf<String>()
            if (!Nammu.checkPermission(android.Manifest.permission.CAMERA)) {
                permissionsNeeded.add(android.Manifest.permission.CAMERA)
            }
            if (!Nammu.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (!permissionsNeeded.isEmpty()) {
                Nammu.askForPermission(this, permissionsNeeded.toTypedArray(), object: PermissionCallback {
                    override fun permissionGranted() {
                        takePicture()
                    }

                    override fun permissionRefused() {
                        Toast.makeText(this@ProfileEditActivity, R.string.error_permissions_required, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                takePicture()
            }
        }

        viewModel.getProfile(getIdFromIntent(intent))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object: EasyImage.Callbacks {
            override fun onImagesPicked(imageFiles: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
                viewModel.updateProfile(latestProfile.copy(profilePicture = imageFiles[0].absolutePath))
            }

            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                Timber.d("Failed to pick image: $e")
                Toast.makeText(this@ProfileEditActivity, R.string.avatar_change_failed, Toast.LENGTH_SHORT).show()
            }

            override fun onCanceled(source: EasyImage.ImageSource?, type: Int) {
                Toast.makeText(this@ProfileEditActivity, R.string.cancelled, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun takePicture() {
        EasyImage.openChooserWithDocuments(this@ProfileEditActivity, getString(R.string.title_select_picture), 0)
    }
}