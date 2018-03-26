package hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.details

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.presentation.common.BaseActivity
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.di.Injectable
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileDetailModel
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.view.LoadingErrorConstraintLayout
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class ProfileDetailsActivity : BaseActivity<ProfileDetailModel, ProfileDetailViewModel>(), Injectable {

    companion object {

        private const val KEY_PROFILE_ID = "profileId"

        fun starter(context: Context, profileId: String): Intent {
            val intent = Intent(context, ProfileDetailsActivity::class.java)
            intent.putExtra(KEY_PROFILE_ID, profileId)
            return intent
        }

        private fun getProfileIdFromIntent(intent: Intent) = intent.getStringExtra(KEY_PROFILE_ID)
    }

    @Inject
    lateinit var vmf: ViewModelProvider.Factory
    @Inject
    lateinit var router: IRouter
    @Inject
    lateinit var imageLoader: ImageLoader
    @Inject
    lateinit var dateFormat: DateFormat

    @BindView(R.id.leclRoot)
    lateinit var leclRoot: LoadingErrorConstraintLayout
    @BindView(R.id.ivProfile)
    lateinit var ivProfile: ImageView
    @BindView(R.id.tvName)
    lateinit var tvName: TextView
    @BindView(R.id.tvDetails)
    lateinit var tvDetails: TextView
    @BindView(R.id.clContent)
    lateinit var clContent: ConstraintLayout

    private lateinit var latestModel: ProfileDetailModel

    override fun getLayoutId(): Int {
        return R.layout.activity_profile
    }

    override fun getPersistableUiState(): ProfileDetailModel {
        return latestModel
    }

    override fun getViewModelFactory(): ViewModelProvider.Factory {
        return vmf
    }

    override fun getViewModelClass(): Class<ProfileDetailViewModel> {
        return ProfileDetailViewModel::class.java
    }

    override fun handleStateChange(state: State<ProfileDetailModel>?) {
        state?.let {
            leclRoot.state = when {
                state.loading -> {
                    setUiEnabled(false, clContent)
                    LoadingErrorConstraintLayout.STATE_LOADING
                }
                state.error != null -> {
                    leclRoot.errorMessage = state.error.message
                    setUiEnabled(true, clContent)
                    LoadingErrorConstraintLayout.STATE_ERROR
                }
                else -> {
                    setUiEnabled(true, clContent)
                    LoadingErrorConstraintLayout.STATE_IDLE
                }
            }
            it.data?.let {
                it.profileModel.profilePicture?.let {
                    imageLoader.loadImageInto(it, ivProfile)
                }
                tvName.text = it.profileModel.displayName ?: "Unknown"

                latestModel = it

                when {
                    it.connectionModel != null -> {
                        clContent.removeAllViews()
                        val inflated = layoutInflater.inflate(R.layout.layout_profile_connected_details, clContent, true)
                        inflated.findViewById<View>(R.id.ivLocate).setOnClickListener { _ -> viewModel.locate(latestModel.profileModel.id!!) }
                        val ivDelete = inflated.findViewById<View>(R.id.ivDelete)
                        ivDelete.visibility = View.VISIBLE
                        ivDelete.setOnClickListener { _ ->
                            // TODO own dialog
                            AlertDialog.Builder(this@ProfileDetailsActivity)
                                    .setTitle(R.string.label_please_confirm)
                                    .setMessage(R.string.message_delete_connection)
                                    .setPositiveButton(R.string.label_yes, { _, _ ->
                                        viewModel.deleteConnection(latestModel.profileModel.id!!)
                                    })
                                    .setNegativeButton(R.string.label_cancel, null)
                                    .create()
                                    .show()
                        }
                        val cbTrusted = inflated.findViewById<CheckBox>(R.id.cbTrusted)
                        cbTrusted.visibility = View.VISIBLE
                        cbTrusted.isChecked = it.connectionModel.trusted == true
                        cbTrusted.setOnCheckedChangeListener { _, b -> viewModel.updateTrustedState(b, latestModel.profileModel.id!!) }

                        tvDetails.text = getString(R.string.pattern_connected_since, dateFormat.format(Date(it.connectionModel.created!!)))
                    }
                    it.connectionRequestModel != null -> {
                        clContent.removeAllViews()
                        val layoutRes: Int = if (it.connectionRequestModel.fromUserId == it.profileModel.id) R.layout.layout_profile_pending_incoming else R.layout.layout_profile_pending_outgoing
                        val inflated = layoutInflater.inflate(layoutRes, clContent, true)
                        inflated.findViewById<View>(R.id.ivAccept)?.setOnClickListener { _ -> viewModel.acceptRequest(latestModel.connectionRequestModel?.id!!) }
                        inflated.findViewById<View>(R.id.ivDecline)?.setOnClickListener { _ -> viewModel.declineRequest(latestModel.connectionRequestModel?.id!!) }
                        inflated.findViewById<View>(R.id.ivCancel)?.setOnClickListener { _ -> viewModel.cancelRequest(latestModel.connectionRequestModel?.id!!) }

                        tvDetails.text = getString(R.string.pattern_request_sent_at, dateFormat.format(Date(it.connectionRequestModel.created!!)))
                    }
                    else -> {
                        clContent.removeAllViews()
                        val inflated = layoutInflater.inflate(R.layout.layout_profile_not_connected, clContent, true)
                        inflated.findViewById<View>(R.id.ivAdd).setOnClickListener { _ -> viewModel.sendRequest(latestModel.profileModel.id!!) }

                        tvDetails.text = getString(R.string.status_not_connected)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getProfileDetails(getProfileIdFromIntent(intent))
    }

    private fun setUiEnabled(enabled: Boolean, viewGroup: ViewGroup) {
        (0 until viewGroup.childCount)
                .map { viewGroup.getChildAt(it) }
                .forEach {
                    if (it is ViewGroup) {
                        setUiEnabled(enabled, it)
                    } else {
                        it.isEnabled = enabled
                    }
                }
    }
}