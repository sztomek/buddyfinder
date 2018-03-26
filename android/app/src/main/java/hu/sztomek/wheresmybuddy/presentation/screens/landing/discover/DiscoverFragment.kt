package hu.sztomek.wheresmybuddy.presentation.screens.landing.discover

import android.arch.lifecycle.ViewModelProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import butterknife.BindView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.device.notification.IntentPayloadHandler
import hu.sztomek.wheresmybuddy.device.notification.actions.BroadcastStopped
import hu.sztomek.wheresmybuddy.device.notification.actions.NewLocation
import hu.sztomek.wheresmybuddy.device.notification.actions.NewSelfLocation
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.presentation.common.BaseFragment
import hu.sztomek.wheresmybuddy.presentation.common.Helpers
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.di.Injectable
import hu.sztomek.wheresmybuddy.presentation.model.MarkerModel
import hu.sztomek.wheresmybuddy.presentation.model.persistable.DiscoverModel
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.view.LoadingErrorConstraintLayout
import timber.log.Timber
import javax.inject.Inject

class DiscoverFragment : BaseFragment<DiscoverModel, DiscoverViewModel>(), OnMapReadyCallback, Injectable {

    companion object {

        private const val KEY_LOCATION_ID = "buddyfinder.location"

        fun createForLocationResponse(locationId: String): DiscoverFragment {
            val discoverFragment = DiscoverFragment()
            discoverFragment.arguments = argumentsWithLocationId(locationId)
            return discoverFragment
        }

        private fun argumentsWithLocationId(locationId: String): Bundle {
            val bundle = Bundle()
            bundle.putString(KEY_LOCATION_ID, locationId)
            return bundle
        }

        private fun readLocationIdFromArguments(bundle: Bundle?): String? {
            return bundle?.getString(KEY_LOCATION_ID)
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            (Helpers.safeCastTo<NewSelfLocation>(IntentPayloadHandler.getActionFromIntent(intent)))?.let {
                viewModel.updateSelfLocation(it.longitude, it.latitude)
            }
        }
    }


    @Inject
    lateinit var vmf: ViewModelProvider.Factory
    @Inject
    lateinit var infoWindowAdapter: InfoWindowAdapter
    @Inject
    lateinit var imageCache: MarkerImageCache
    @Inject
    lateinit var imageLoader: ImageLoader
    @Inject
    lateinit var appSchedulers: AppSchedulers
    @Inject
    lateinit var router: IRouter

    @BindView(R.id.mapsView)
    lateinit var mapView: MapView
    @BindView(R.id.leclRoot)
    lateinit var leclRoot: LoadingErrorConstraintLayout
    private var latestModel: DiscoverModel? = null

    private var mapsReadyRunnable: Runnable? = null
    private var maps: GoogleMap? = null
    private var markers: MutableList<Marker> = mutableListOf()

    override fun getLayoutId(): Int {
        return R.layout.fragment_discovery
    }

    override fun getPersistableUiState(): DiscoverModel {
        return latestModel ?: DiscoverModel(readLocationIdFromArguments(arguments ?: Bundle()))
    }

    override fun getViewModelFactory(): ViewModelProvider.Factory {
        return vmf
    }

    override fun getViewModelClass(): Class<DiscoverViewModel> {
        return DiscoverViewModel::class.java
    }

    override fun handleStateChange(state: State<DiscoverModel>?) {
        state?.let {
            leclRoot.state = when {
                it.loading -> LoadingErrorConstraintLayout.STATE_LOADING
                it.error != null -> {
                    leclRoot.errorMessage = it.error.message
                    LoadingErrorConstraintLayout.STATE_ERROR
                }
                else -> {
                    latestModel = state.data
                    mapsReadyRunnable = Runnable {
                        maps?.let {

                            markers.forEach {
                                it.remove()
                            }
                            markers.clear()

                            if (latestModel?.targetMarker != null) {
                                val latitude = latestModel?.targetMarker?.location?.latitude
                                val longitude = latestModel?.targetMarker?.location?.longitude
                                if (latitude != null && longitude != null) {
                                    val marker: Marker = it.addMarker(createMarker(latestModel?.targetMarker!!))
                                    marker.tag = latestModel?.targetMarker?.profile
                                    markers.add(marker)
                                }
                            }
                            if (latestModel?.selfLatitude != null && latestModel?.selfLongitude != null) {
                                val marker: Marker = it.addMarker(
                                        createMarker(latestModel?.selfLatitude!!, latestModel?.selfLongitude!!, "Me", "")
                                            .flat(true)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                )
                                markers.add(marker)
                            }

                            latestModel?.markers?.forEach { model ->
                                val marker = it.addMarker(createMarker(model))
                                marker.tag = model.profile
                                markers.add(marker)
                            }

                            if (markers.size == 1) {
                                it.animateCamera(CameraUpdateFactory.newLatLngZoom(markers[0].position, 20.0f))
                            }
                            if (markers.size > 1) {
                                val builder = LatLngBounds.Builder()
                                markers.forEach {
                                    builder.include(it.position)
                                }
                                it.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50))
                            }
                        }
                    }
                    if (maps != null) {
                        mapsReadyRunnable?.run()
                        mapsReadyRunnable = null
                    }
                    LoadingErrorConstraintLayout.STATE_IDLE
                }
            }
        }
    }

    private fun createMarker(latitude: Double, longitude: Double, title: String?, snippet: String?): MarkerOptions {
        return MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title(title)
                .snippet(snippet)
    }

    private fun createMarker(model: MarkerModel): MarkerOptions {
        return createMarker(model.location.latitude!!, model.location.longitude!!, model.profile?.displayName, model.info)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        val targetLocation = readLocationIdFromArguments(arguments)
        if(targetLocation == null) {
            viewModel.discover()
        } else {
            viewModel.getLocation(targetLocation)
        }
        viewModel.getSelfLocation()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(broadcastReceiver, IntentFilter(IntentPayloadHandler.ACTION_NEW_LOCATION))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        mapView.onStop()
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(broadcastReceiver)
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        imageCache.wipe()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onMapReady(p0: GoogleMap?) {
        maps = p0
        infoWindowAdapter.imageCache = imageCache
        maps?.setInfoWindowAdapter(infoWindowAdapter)
        maps?.setOnMarkerClickListener {
            val profilePicture = Helpers.safeCastTo<ProfileModel>(it.tag)?.profilePicture
            if (profilePicture != null) {
                if (imageCache.hasBitmap(profilePicture)) {
                    it.showInfoWindow()
                } else {
                    imageLoader.loadImage(profilePicture)
                            .observeOn(appSchedulers.ui)
                            .subscribeOn(appSchedulers.io)
                            .subscribe(
                                    { bitmap ->
                                        imageCache.storeBitmap(profilePicture, bitmap)
                                        it.showInfoWindow()
                                    },
                                    { error -> Timber.d(error) }
                            )
                }
            } else {
                it.showInfoWindow()
            }
            true
        }
        maps?.setOnInfoWindowClickListener {
            if (it.tag is ProfileModel) {
                router.toProfile((it.tag as ProfileModel).id!!)
            }
        }
        if (mapsReadyRunnable != null) {
            mapsReadyRunnable?.run()
            mapsReadyRunnable = null
        }
    }

}
