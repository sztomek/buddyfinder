package hu.sztomek.wheresmybuddy.presentation.screens.landing

import android.arch.lifecycle.ViewModelProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import butterknife.BindView
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.device.notification.IntentPayloadHandler
import hu.sztomek.wheresmybuddy.device.notification.actions.BroadcastStopped
import hu.sztomek.wheresmybuddy.presentation.common.BaseActivity
import hu.sztomek.wheresmybuddy.presentation.common.Helpers
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.di.Injectable
import hu.sztomek.wheresmybuddy.presentation.model.persistable.LandingModel
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.screens.landing.discover.DiscoverFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.PeopleFragment
import hu.sztomek.wheresmybuddy.presentation.screens.landing.search.SearchFragment
import timber.log.Timber
import javax.inject.Inject

class LandingActivity : BaseActivity<LandingModel, LandingViewModel>(), HasSupportFragmentInjector, Injectable {

    companion object {

        private const val KEY_LOCATION = "buddyfinder.location"

        fun putLocation(intent: Intent, locationId: String) {
            intent.putExtra(KEY_LOCATION, locationId)
        }

        private fun getLocation(intent: Intent): String? {
            val locationExtra = intent.getStringExtra(KEY_LOCATION)
            intent.removeExtra(KEY_LOCATION)
            return locationExtra
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            (Helpers.safeCastTo<BroadcastStopped>(IntentPayloadHandler.getActionFromIntent(intent)))?.let {
                viewModel.getBroadcastState()
            }
        }
    }


    @Inject
    lateinit var router: IRouter
    @Inject
    lateinit var injector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var vmf: ViewModelProvider.Factory
    @Inject
    lateinit var imageLoader: ImageLoader
    @Inject
    lateinit var statusAdapter: StatusSpinnerAdapter

    @BindView(R.id.drawer_layout)
    lateinit var drawerLayout: DrawerLayout
    @BindView(R.id.nav_view)
    lateinit var navigationView: NavigationView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    lateinit var ivProfile: ImageView
    lateinit var tvName: TextView
    lateinit var spinnerStatus: Spinner

    private var latestModel: LandingModel? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_landing
    }

    override fun getViewModelFactory(): ViewModelProvider.Factory {
        return vmf
    }

    override fun getViewModelClass(): Class<LandingViewModel> {
        return LandingViewModel::class.java
    }

    override fun handleStateChange(state: State<LandingModel>?) {
        latestModel = state?.data
        if (state?.loading == false) {
            if (state.error != null) {
                tvName.text = getString(R.string.default_profile_displayName)
            } else {
                if (state.data == null) {
                    router.restart()
                } else {
                    val profileModel = state.data.profileModel
                    tvName.text = profileModel?.displayName
                    if (profileModel?.profilePicture != null) {
                        imageLoader.loadImageInto(profileModel.profilePicture, ivProfile)
                    }
                    when (latestModel?.isBroadcasting) {
                        true -> {
                            router.startBroadcast()
                            spinnerStatus.setSelection(StatusSpinnerItem.OnlineStatusSpinnerItem().position)
                        }
                        false -> {
                            router.stopBroadcast()
                            spinnerStatus.setSelection(StatusSpinnerItem.OfflineStatusSpinnerItem().position)
                        }
                    }
                }
            }
        }
    }

    override fun getPersistableUiState(): LandingModel {
        return LandingModel(
                profileModel = ProfileModel(
                        id = latestModel?.profileModel?.id,
                        displayName = latestModel?.profileModel?.displayName,
                        profilePicture = latestModel?.profileModel?.profilePicture
                ),
                isBroadcasting = false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getBroadcastState()

        tvName = navigationView.getHeaderView(0).findViewById(R.id.tvName)
        ivProfile = navigationView.getHeaderView(0).findViewById(R.id.ivProfile)
        spinnerStatus = navigationView.getHeaderView(0).findViewById(R.id.spinnerStatus)
        spinnerStatus.adapter = statusAdapter
        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val item = statusAdapter.getItem(position)
                when (item) {
                    is StatusSpinnerItem.OnlineStatusSpinnerItem -> {
                        viewModel.startBroadcast()
                    }
                    is StatusSpinnerItem.OfflineStatusSpinnerItem -> {
                        viewModel.stopBroadcast()
                    }
                    else -> {
                        Timber.d("Unhandled status item: [$item]")
                    }
                }
            }
        }

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                viewModel.getProfile()
            }

            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerClosed(drawerView: View) {
            }
        })
        toggle.syncState()

        val listener = NavigationView.OnNavigationItemSelectedListener {
            val fragment: Fragment? = when (it.itemId) {
                R.id.nav_discover -> {
                    val location = getLocation(intent)
                    if (location == null) {
                        DiscoverFragment()
                    } else {
                        DiscoverFragment.createForLocationResponse(location)
                    }
                }
                R.id.nav_search -> {
                    SearchFragment()
                }
                R.id.nav_my_connections -> {
                    PeopleFragment()
                }
                R.id.nav_settings -> {
                    DummyFragment()
                }
                R.id.nav_info -> {
                    DummyFragment()
                }
                R.id.nav_logout -> {
                    viewModel.logout()
                    null
                }
                else -> {
                    Timber.d("Unhandled navigation item id [${it.itemId}]")
                    null
                }
            }

            if (fragment != null) {
                router.replaceContent(R.id.content, fragment)
                if (fragment is TitleProvider)
                    toolbar.setTitle(fragment.titleRes)
                else
                    toolbar.setTitle(R.string.app_name)
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            return@OnNavigationItemSelectedListener true
        }

        navigationView.setNavigationItemSelectedListener(listener)
        navigationView.setCheckedItem(R.id.nav_discover)
        navigationView.menu.performIdentifierAction(R.id.nav_discover, 0)

        val profileOnClick: (View) -> Unit = { _ ->
            val model = latestModel
            val userId = model?.profileModel?.id
            if (userId != null) {
                router.toProfileEdit(userId)
            } else {
                //TODO no profile yet ...
            }
        }
        ivProfile.setOnClickListener(profileOnClick)
        tvName.setOnClickListener(profileOnClick)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            router.back()
        }
    }

    override fun supportFragmentInjector() = injector

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter(IntentPayloadHandler.ACTION_NEW_BROADCAST_STOPPED))
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onStop()
    }
}
