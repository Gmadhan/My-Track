package com.mytrack.ui.map

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.provider.SettingsSlicesContract.KEY_LOCATION
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.common.api.internal.OnConnectionFailedListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.mytrack.R
import com.mytrack.databinding.FragmentMapBinding
import com.mytrack.ui.profile.EditProfileActivity
import com.mytrack.utils.*
import com.mytrack.utils.Utils.convertDateTime
import com.mytrack.utils.Utils.logger
import com.mytrack.utils.Utils.showToast
import java.util.*

class MapFragment: Fragment(), OnMapReadyCallback, LocationListener, View.OnClickListener, OnConnectionFailedListener {

    private lateinit var fragmentMapBinding: FragmentMapBinding
    private lateinit var viewModel: MapViewModel
    var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var placeAutoComplete: AutocompleteSupportFragment? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private var currentLatLng : LatLng? = null

    private var searchLatLng:LatLng? = null

    private val TAG = "MapFragment"
    private var locale : String? = null
    private var travel_mode : String? = "driving"

    private var gpsLat : Double? = 0.0
    private var gpsLng : Double? = 0.0

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100
    private val DEFAULT_ZOOM = 15F

    private var GpsEnabled = false
    private var NetworkEnabled = false
    private var nigthtime = false
    private var trfCheckBtn = false

    companion object {
        var distanceText:String? = null
        var durationText:String? = null
        var url_value:String? = null
        var pts:Boolean = false
        var nightmode_btn:Boolean = false
        var listlatlng: ArrayList<LatLng> = ArrayList<LatLng>()
        var polyLineOptions: PolylineOptions? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentMapBinding = FragmentMapBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        if (savedInstanceState != null) lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
        init()
        observeViewModel()
        return fragmentMapBinding.root
    }

    private fun observeViewModel() {
        viewModel.weatherStatus.observe(viewLifecycleOwner) {
            setWeather()
        }
    }

    private fun init() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        val mapFragment: SupportMapFragment? = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        mGoogleApiClient = GoogleApiClient.Builder(requireActivity())
            .addApi(LocationServices.API)
            .build()
        mGoogleApiClient!!.connect()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fragmentMapBinding.searchBar.main.btnGPS.setOnClickListener {
            if(currentLatLng != null) {
                getDeviceLocation()
            } else {
                showToast(requireActivity(), getString(R.string.please_check_gps))
            }
        }

        locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            requireActivity().resources.configuration.locales.get(0).country
        else
            requireActivity().resources.configuration.locale.country

        setAutoComplete()

        fragmentMapBinding.searchBar.ivUser.ivUser.setOnClickListener {
            val i = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(i)
        }

        fragmentMapBinding.searchBar.main.toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!Utils.isNetworkAvailable(activity)) {
                Toast.makeText(activity, getString(R.string.check_ur_internet), Toast.LENGTH_SHORT).show()
            } else {
                val animationSet = AnimatorSet()
                animationSet.duration = 160
                animationSet.playSequentially(
                    ObjectAnimator.ofFloat(fragmentMapBinding.searchBar.main.toggle, "rotation", 20f),
                    ObjectAnimator.ofFloat(fragmentMapBinding.searchBar.main.toggle, "rotation", -20f),
                    ObjectAnimator.ofFloat(fragmentMapBinding.searchBar.main.toggle, "rotation", 0f)
                )
                animationSet.start()
            }
            trfCheckBtn = isChecked
            maptools()
        }
    }

    fun maptools() {
        mMap?.let { map ->
            map.isTrafficEnabled = trfCheckBtn
            map.uiSettings.isCompassEnabled = true
            map.uiSettings.isIndoorLevelPickerEnabled = true
            map.uiSettings.isRotateGesturesEnabled = true
            map.uiSettings.isZoomGesturesEnabled = true
            map.uiSettings.isTiltGesturesEnabled = true
            map.uiSettings.isMapToolbarEnabled = false
            map.uiSettings.isMyLocationButtonEnabled = false
        }
    }

    fun setAutoComplete() {
        placeAutoComplete = childFragmentManager.findFragmentById(R.id.place_autocomplete) as AutocompleteSupportFragment?
        placesClient = Places.createClient(requireActivity())
        if (placeAutoComplete != null)
            placeAutoComplete!!.setHint(getString(R.string.Search))

        placeAutoComplete!!.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        placeAutoComplete!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
                try {
                    searchLatLng = place.latLng
                    if(searchLatLng != null) {
                        mMap!!.addMarker(
                            MarkerOptions().position(searchLatLng!!)
                                .title(place.address!!.toString()).icon(
                                Utils.bitmapDescriptionFromRes(
                                    requireActivity(),
                                    R.drawable.ic_end_point
                                )
                            )
                        )
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng!!, 16.0f))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        nightmap()
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
        viewModel.callWeatherApi(
            SessionSave.getSession(Constants.GPSLAT, requireActivity()).toString(),
            SessionSave.getSession(Constants.GPSLNG, requireActivity()).toString()
        )
        Utils.setImage(requireActivity(), SessionSave.getSession(Constants.IMAGE, context) ?: "", fragmentMapBinding.searchBar.ivUser.ivUser)
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful && task.result != null) {
                        lastKnownLocation = task.result
                        lastKnownLocation?.let {
                            currentLatLng = LatLng(it.latitude, it.longitude)
                            gpsLat = it.latitude
                            gpsLng = it.longitude
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, DEFAULT_ZOOM))
                            viewModel.updateLocationInFirebase(gpsLat!!, gpsLng!!)
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            logger(TAG,"Exception: " + e.message, true)
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (mMap == null) return
        try {
            if (locationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap!!.isMyLocationEnabled = false
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            logger(TAG, "Exception: " + e.message, true)
        }
    }

    override fun onLocationChanged(location: Location) {
        gpsLat = location.latitude
        gpsLng = location.longitude
        currentLatLng = LatLng(gpsLat!!, gpsLng!!)
        viewModel.updateLocationInFirebase(gpsLat!!, gpsLng!!)
    }

    private fun nightmap() {
        if (convertDateTime(Date(),"HH").toInt() >= 19 || convertDateTime(Date(),"HH").toInt() <= 6) {
            nightmode_btn = true
            nigthtime = true
            mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.night_style))
        }
    }

    fun setWeather() {
        val currentweather = SessionSave.getSession(Constants.WEATHER,requireActivity())
        if (!currentweather.isNullOrEmpty()) {
            val animFile = when (currentweather) {
                "Clouds" -> if (nigthtime) "weather-cloudynight.json" else "weather-partly-cloudy.json"
                "Mist", "Haze" -> "weather-mist.json"
                "Rain", "Drizzle" -> if (nigthtime) "weather-rainynight.json" else "weather-partly-shower.json"
                "Thunderstorm", "Lightning" -> if (nigthtime) "weather-storm.json" else "weather-stormshowersday.json"
                "Snow" -> if (nigthtime) "weather-snownight.json" else "weather-snow-sunny.json"
                else -> if (nigthtime) "weather-night.json" else "weather-sunny.json"
            }
            fragmentMapBinding.searchBar.main.weatherAnim.setAnimation(animFile)
        }
    }

    override fun onClick(v: View?) {}
    override fun onConnectionFailed(p0: ConnectionResult) {}
}
