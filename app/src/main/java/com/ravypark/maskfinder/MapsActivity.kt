package com.ravypark.maskfinder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.ravypark.maskfinder.model.Constants.Companion.DEFAULT_LAT_LON
import com.ravypark.maskfinder.model.Constants.Companion.DEFAULT_ZOOM
import com.ravypark.maskfinder.model.Constants.Companion.REQ_GET_CURRENT_LOCATION
import com.ravypark.maskfinder.model.Preference
import com.ravypark.maskfinder.model.Stat
import com.ravypark.maskfinder.model.StoreItem
import com.ravypark.maskfinder.network.ApiManager
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private lateinit var mMap: GoogleMap

    private lateinit var clusterManager: ClusterManager<StoreItem>

    private val api = ApiManager.instance

    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!Preference.isShowNotice(applicationContext)) {
            showNoticeDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_notice -> {
                showNoticeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNoticeDialog() {
        NoticeDialogFragment().show(supportFragmentManager, null)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        clusterManager = ClusterManager(this, mMap)
        clusterManager.renderer = BikeStationRenderer(applicationContext, mMap, clusterManager)
        clusterManager.setOnClusterItemInfoWindowClickListener { storeItem ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse("geo:${storeItem.position.latitude},${storeItem.position.longitude}?q=${storeItem.store.addr}")
            startActivity(intent)
        }

        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)
        mMap.setOnInfoWindowClickListener(clusterManager)

        btn_search_store.setOnClickListener {
            mMap.projection?.visibleRegion?.latLngBounds?.center?.let {
                requestStoresByGeo(it.latitude, it.longitude)
            }
        }

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(
                this, "현재 위치를 가져오기 위해 위치 접근 권한이 필요합니다.", REQ_GET_CURRENT_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            isLocationAvailable(this, OnSuccessListener {
                handleLocationAvailable(it.isLocationAvailable)
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        setMyLocationUi(false)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        isLocationAvailable(this, OnSuccessListener {
            handleLocationAvailable(it.isLocationAvailable)
        })
    }

    private fun handleLocationAvailable(isLocationAvailable: Boolean) {
        setMyLocationUi(isLocationAvailable)
        moveCurrentLocation()
    }

    private fun setMyLocationUi(enableMyLocation: Boolean) {
        mMap.isMyLocationEnabled = enableMyLocation
        mMap.uiSettings?.isMyLocationButtonEnabled = enableMyLocation
    }

    private fun isLocationAvailable(
        context: Context,
        callback: OnSuccessListener<LocationAvailability>
    ) {
        LocationServices.getFusedLocationProviderClient(context).locationAvailability.addOnSuccessListener(
            callback
        )
    }

    private fun moveCurrentLocation() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location ->
            currentLocation = location

            if (location != null) {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), DEFAULT_ZOOM
                    )
                )
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다. 기본 위치로 설정합니다.", Toast.LENGTH_SHORT).show()
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LAT_LON, DEFAULT_ZOOM))
            }

            location?.let {
                requestStoresByGeo(it.latitude, it.longitude)
            }
        }
    }

    private fun requestStoresByGeo(lat: Double, lng: Double) = lifecycleScope.launch {
        runCatching {
            val result = api.getStoresByGeo(lat, lng)
            result.stores.map { StoreItem(it) }.run {
                clusterManager.addItems(this)
            }
            clusterManager.cluster()
        }.onFailure {
            Toast.makeText(
                applicationContext,
                "데이터를 불러올 수 없습니다. 잠시 후에 시도해 주세요.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

class BikeStationRenderer(
    private val context: Context?,
    map: GoogleMap?,
    clusterManager: ClusterManager<StoreItem>?
) : DefaultClusterRenderer<StoreItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: StoreItem, markerOptions: MarkerOptions) {
        markerOptions.icon(bitmapDescriptorFromVector(context, R.drawable.ic_default, item))
            .title(item.title)
            .snippet(item.snippet)
            .position(item.position)
    }

    private fun bitmapDescriptorFromVector(
        context: Context?,
        vectorId: Int,
        item: StoreItem
    ): BitmapDescriptor? {
        if (context == null) return null

        val vectorDrawable = ContextCompat.getDrawable(context, vectorId)
            ?: return null

        vectorDrawable.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        DrawableCompat.setTint(
            vectorDrawable, when (item.store.remainStat) {
                Stat.Plenty -> Color.GREEN
                Stat.Some -> Color.YELLOW
                Stat.Few -> Color.RED
                Stat.Empty -> Color.GRAY
                else -> Color.GRAY
            }
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

class NoticeDialogFragment() : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_notice, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Preference.setShowNotice(requireContext(), true)
    }
}
