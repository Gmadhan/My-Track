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
import androidx.appcompat.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mytrack.R
import com.mytrack.api.ApiCall
import com.mytrack.api.RetrofitClientInstance
import com.mytrack.databinding.FragmentMapBinding
import com.mytrack.model.WeatherResponse
import com.mytrack.ui.profile.EditProfileActivity
import com.mytrack.utils.*
import com.mytrack.utils.Utils.convertDateTime
import com.mytrack.utils.Utils.logger
import com.mytrack.utils.Utils.showSnackBar
import com.mytrack.utils.Utils.showToast
import com.mytrack.utils.Utils.toString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MapFragment: Fragment(), OnMapReadyCallback, LocationListener, View.OnClickListener, OnConnectionFailedListener {

    private lateinit var fragmentMapBinding: FragmentMapBinding
    var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var placeAutoComplete: AutocompleteSupportFragment? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    private var cameraPosition: CameraPosition? = null
    private var mLocationRequest: LocationRequest? = null
    private var locationManager: LocationManager? = null

    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private var currentLatLng : LatLng? = null

    private var mFirebaseDatabase: DatabaseReference? = null
    private val polyLineOptions: PolylineOptions? = null
    private var polyline: Polyline? = null
    private var darkpolyline: Polyline? = null
//    private var downloadTask: ReadTask? = null
    private var latLngs: LatLng? = null
    private var markerlatlng:LatLng? = null
    private var Rmark_latlng:LatLng? = null
    private var searchLatLng:LatLng? = null

    private val TAG = "MapFragment"
    private var locale : String? = null
    private var Apikey : String? = null
    private var travel_mode : String? = "driving"

    private var gpsLat : Double? = 0.0
    private var gpsLng : Double? = 0.0

    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
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
        if (savedInstanceState != null) lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
        init()
        return fragmentMapBinding.root
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

        mFirebaseDatabase = Firebase.database.reference.child("users")

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
            // write here your code for example ...
            trfCheckBtn = isChecked
            maptools()
        }
    }

    fun maptools() {
        mMap!!.isTrafficEnabled = trfCheckBtn
        mMap!!.uiSettings.isCompassEnabled = false
        mMap!!.uiSettings.isIndoorLevelPickerEnabled = true
        mMap!!.uiSettings.isRotateGesturesEnabled = true
        mMap!!.uiSettings.isZoomGesturesEnabled = true
        mMap!!.uiSettings.isTiltGesturesEnabled = true
        mMap!!.uiSettings.isMapToolbarEnabled = false
        mMap!!.uiSettings.isMyLocationButtonEnabled = false
        mMap!!.uiSettings.isCompassEnabled = true
    }

    fun setAutoComplete() {
        placeAutoComplete = childFragmentManager.findFragmentById(R.id.place_autocomplete) as AutocompleteSupportFragment?
        Places.initialize(requireActivity(), "AIzaSyCA-kEh2UBqnNlDeBO-fuu929OdnKBqQT0")
        placesClient = Places.createClient(requireActivity())
        if (placeAutoComplete != null)
            placeAutoComplete!!.setHint(getString(R.string.Search))

        // Specify the types of place data to return.
        placeAutoComplete!!.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        // Set up a PlaceSelectionListener to handle the response.
        placeAutoComplete!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
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
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        nightmap()
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
        callWeatherApi()
        Utils.setImage(requireActivity(), SessionSave.getSession(Constants.IMAGE, context)!!,fragmentMapBinding.searchBar.ivUser.ivUser)
    }

    private fun tempLocation() {
        val latLng = LatLng(11.678, 77.678)
        val markerOptions = MarkerOptions().position(latLng).title("I am here!")
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        mMap?.addMarker(markerOptions)
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = mFusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            currentLatLng = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            logger(TAG, "Current location = $currentLatLng", true)
                            gpsLat = lastKnownLocation!!.latitude
                            gpsLng = lastKnownLocation!!.longitude
                            SessionSave.saveSession(Constants.GPSLAT,gpsLat.toString(),requireActivity())
                            SessionSave.saveSession(Constants.GPSLNG,gpsLng.toString(),requireActivity())
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude), DEFAULT_ZOOM))
                            gpsupdate()
                        }
                    } else {
                        logger(TAG, "Current location is null. Using defaults.", true)
                        logger(TAG, "Exception: %s " + task.exception, true)
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, DEFAULT_ZOOM))
                        mMap!!.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            logger(TAG,"Exception: %s " + e.message, true)
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap!!.isMyLocationEnabled = true
                mMap!!.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap!!.isMyLocationEnabled = false
                mMap!!.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            logger(TAG, "Exception: %s " + e.message, true)
        }
    }

    override fun onLocationChanged(location: Location) {
        try {
            if (!GpsEnabled && NetworkEnabled) {
                var locationrequest = LocationRequest.create().apply {
                    interval = 100
                    fastestInterval = 50
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                    maxWaitTime = 100
                }
                if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) !== PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!
                try {
                    gpsLat = location.latitude
                    gpsLng = location.longitude
                } catch (ne: NullPointerException) {
                    ne.printStackTrace()
                }
            } else {
                try {
                    gpsLat = location.latitude
                    gpsLng = location.longitude
                } catch (ne: NullPointerException) {
                    ne.printStackTrace()
                }
            }
            if (gpsLat != null && gpsLng != null) {
                currentLatLng = LatLng(gpsLat!!, gpsLng!!)
                SessionSave.saveSession(Constants.GPSLAT,gpsLat.toString(),requireActivity())
                SessionSave.saveSession(Constants.GPSLNG,gpsLng.toString(),requireActivity())
            } else {
                showToast(requireActivity(), getString(R.string.gps_accuracy_not_good))
            }
            gpsupdate()
            /*speed = location.speed * 18 / 5
            if (speed > 0) {
                distance()
                gpsupdate()
                gps_speed.setText(" " + speed.toInt() + " Kms/hr")
                gps_speed.setTextColor(
                    if (speed > 50) resources.getColor(R.color.red) else resources.getColor(
                        R.color.black
                    )
                )
            } else if (speed == 0f) gps_speed.setText(getString(R.string.speed))
            if (speed > 1 || gps_click_btn) mMap!!.animateCamera(
                CameraUpdateFactory.newLatLng(LatLng(gpsLat!!, gpsLng!!))
            )
            if (place_name != null) {
                geocoder()
                placeAutoComplete!!.setText(searchadrs)
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMapsApiDirectionsUrl(origin: LatLng, dest: LatLng): String? {
        Apikey = getString(R.string.google_maps_key)
        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
        // Sensor enabled
        val sensor = "sensor=false"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor"
        // Output format
        val output = "json"
        // Building the url to the web service
        val url =
            "https://maps.googleapis.com/maps/api/directions/$output?$parameters&mode=$travel_mode&key=$Apikey"
        println("distancessss : $url")
        return url
    }

    /*private fun polylineenable() {
        try {
            if (!pts) {
                downloadTask = ReadTask()
                downloadTask.execute(_url)
                distancecard.startAnimation(upanim)
                distancecard.setVisibility(View.VISIBLE)
                marker_distance.setVisibility(View.GONE)
                search_distance.setVisibility(View.VISIBLE)
                gpsadrs.setText(address)
            } else removepoly()
            dstntadrs.setText(if (Rmark_latlng != null) Strmarker_adrs else searchadrs)
            direction_btn = true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ReadTask : AsyncTask<String?, Void?, String>() {
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            url_value = result
            try {
                val jsonObject = JSONObject(result)
                val routes = jsonObject.getJSONArray("routes")
                val routes1 = routes.getJSONObject(0)
                val legs = routes1.getJSONArray("legs")
                val legs1 = legs.getJSONObject(0)
                val distance = legs1.getJSONObject("distance")
                val duration = legs1.getJSONObject("duration")
                distanceText = distance.getString("text")
                durationText = duration.getString("text")
                println("distance...$distanceText $durationText")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            ParserTask().execute(result)
        }

        override fun doInBackground(vararg params: String?): String {
            // TODO Auto-generated method stub
            var data = ""
            try {
                val http = MapHttpConnection()
                data = http.readUr(params[0]!!)
            } catch (e: java.lang.Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }
    }

    private class MapHttpConnection {
        @SuppressLint("LongLogTag")
        @Throws(IOException::class)
        fun readUr(mapsApiDirectionsUrl: String): String {
            var data = ""
            var istream: InputStream? = null
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL(mapsApiDirectionsUrl)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connect()
                istream = urlConnection!!.inputStream
                val br = BufferedReader(InputStreamReader(istream))
                val sb = StringBuffer()
                var line: String? = ""
                while (br.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                data = sb.toString()
                br.close()
            } catch (e: java.lang.Exception) {
                Log.d("Exception while reading url", e.toString())
            } finally {
                istream!!.close()
                urlConnection!!.disconnect()
            }
            return data
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ParserTask : AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {

        override fun onPostExecute(routes: List<List<HashMap<String, String>>>?) {
            var points: ArrayList<LatLng?>? = null
            for (i in routes!!.indices) {
                points = ArrayList()
                listlatlng = ArrayList()
                polyLineOptions = PolylineOptions()
                val path = routes[i]
                for (j in path.indices) {
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat, lng)
                    points.add(position)
                    pts = true
                }
                polyLineOptions!!.addAll(points)
            }
            if (polyLineOptions != null) {
                *//*Light line*//*
                polyLineOptions!!.width(8f)
                polyLineOptions!!.color(
                    if (nightmode_btn)
                        getResources().getColor(R.color.Red)
                    else getResources().getColor(R.color.applitecolor)
                )
                polyLineOptions!!.startCap(SquareCap())
                polyLineOptions!!.endCap(SquareCap())
                polyLineOptions!!.jointType(JointType.ROUND)
                polyline = mMap!!.addPolyline(polyLineOptions)
                distance_txt.setText(" Distance : $distanceText")
                time_txt.setText(" Time : $durationText")
                polyline.setClickable(true)

                *//*Dark line*//*
                val darkPolylineOption = PolylineOptions()
                darkPolylineOption.width(8f)
                darkPolylineOption.color(
                    if (nightmode_btn)
                        getResources().getColor(R.color.DarkRed)
                    else getResources().getColor(R.color.AppColor)
                )
                darkPolylineOption.startCap(SquareCap())
                darkPolylineOption.endCap(SquareCap())
                darkPolylineOption.jointType(JointType.ROUND)
                darkpolyline = mMap!!.addPolyline(darkPolylineOption)
                MapFragment.animatePolyLine(1000)
            }
        }

        public override fun doInBackground(vararg params: String?): List<List<HashMap<String, String>>>? {
            // TODO Auto-generated method stub
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(params[0])
                val parser = Dataparse()
                routes = parser.parse(jObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return routes
        }
    }

    fun animatePolyLine(duration: Long) {
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = duration
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animator1: ValueAnimator ->
            val latLngList: MutableList<LatLng> = polyline!!.points
            val initialPointSize = latLngList.size
            val animatedValue = animator1.animatedValue as Int
            val newPoints = animatedValue * listlatlng.size / 100
            if (initialPointSize < newPoints) {
                latLngList.addAll(listlatlng.subList(initialPointSize, newPoints))
                polyline!!.points = latLngList
            }
        }
        animator.addListener(polyLineAnimationListener)
        animator.start()
    }

    var polyLineAnimationListener: Animator.AnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animator: Animator) {
//            if (listlatlng.size() > 0)
//                addMarker(listlatlng.get(listlatlng.size() - 1));
        }

        override fun onAnimationEnd(animator: Animator) {
            val poly1: MutableList<LatLng> = polyline!!.points
            val poly2: MutableList<LatLng> = darkpolyline!!.points
            poly2.clear()
            poly2.addAll(poly1)
            poly1.clear()
            polyline!!.points = poly1
            darkpolyline!!.points = poly2
            polyline!!.zIndex = 2f
        }

        override fun onAnimationCancel(animator: Animator) {}
        override fun onAnimationRepeat(animator: Animator) {}
    }*/

    fun gpsupdate() {
        try {
            if (!SessionSave.getSession(Constants.MOBILENO, requireActivity()).isNullOrEmpty()) {
                if (gpsLat != null && gpsLng != null) {
                    mFirebaseDatabase!!.child(SessionSave.getSession(Constants.MOBILENO, requireActivity()).toString()).child("gps_Lat").setValue(gpsLat)
                    mFirebaseDatabase!!.child(SessionSave.getSession(Constants.MOBILENO, requireActivity()).toString()).child("gps_Lng").setValue(gpsLng)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun distance() {
        /*if (markerlatlng != null && gps_lng != null) {
            try {
                val Rad = 6371.23 //Earth's Radius In kilometers
                val dLat = Math.toRadians(markerlatlng.latitude - gps_lat)
                val dLon = Math.toRadians(markerlatlng.longitude - gps_lng)
                val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.sin(dLon / 2) * Math.sin(dLon / 2) *
                        Math.cos(Math.toRadians(latLngs.latitude)) *
                        Math.cos(Math.toRadians(markerlatlng.latitude))
                //                double c = 2 * Math.asin(Math.sqrt(a));
                val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                distance = (Rad * c).toFloat()
                fltdata = String.format("%.02f", distance)

                //Time etsimation
                val speedIs1KmMinute = 50
                Time = if ((speed == 0f)) (distance / speedIs1KmMinute) else (distance / speed)
                hours = Time.toInt()
                minutes = (60 * (Time - hours)).toInt()
                apxt_distance.setText(if (hours > 0) " $fltdata kms" else " $fltdata kms")
                apxt_time.setText(if (hours > 0) " $hours hrs $minutes min" else " $minutes min")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }*/
    }

    /*fun geocoder() {
        if (connection) {
            var geocoder: Geocoder? = null
            if (Geocoder.isPresent()) geocoder = Geocoder(this, Locale.getDefault())
            try {
                date()
                if (gps_lat != null) {
                    val addresses = geocoder!!.getFromLocation(gps_lat, gps_lng, 1)
                    address = addresses!![0].getAddressLine(0)
                    adrs_list.add(newline + address + " " + date)
                    list = adrs_list.toString()
                    val intent = Intent("my.action.string")
                    intent.putExtra("key", list)
                    sendBroadcast(intent)
                }
                if (marker_lat != null) {
                    val markaddress = geocoder!!.getFromLocation(marker_lat, marker_lng, 1)
                    Strmarker_adrs = markaddress!![0].getAddressLine(0)
                }
                if (place_lat != null) {
                    val searchaddress = geocoder!!.getFromLocation(place_lat, place_lng, 1)
                    searchadrs = searchaddress!![0].getAddressLine(0)
                }
                if (friend_lat != null && friend_lng != null) {
                    val searchaddress = geocoder!!.getFromLocation(friend_lat, friend_lng, 1)
                    friendadrs = searchaddress!![0].getAddressLine(0)
                    marker_distance.setVisibility(View.VISIBLE)
                    searchtext.setText(friendadrs)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }
        } else {
            connectionchange()
        }
    }*/

    fun mapstyle(connection: Boolean, i: Int, toast: String?) {
        if (connection) {
            showToast(requireActivity(),toast)
            mMap!!.mapType = i
        } else {
            showSnackBar(requireView(),getString(R.string.no_network_connection),requireActivity(),false)
        }
    }

    private fun nightmap() {
        if (convertDateTime(Date(),"HH").toInt() >= 19 || convertDateTime(Date(),"HH").toInt() <= 6) {
            nightmode_btn = true
            nigthtime = true
            mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.night_style))
        }
    }

    fun callWeatherApi() {
        try {
            /*Create handle for the RetrofitInstance interface*/
            val service: ApiCall =
                RetrofitClientInstance.getRetrofitInstance()!!.create(ApiCall::class.java)
            val call: Call<WeatherResponse> = service.getweather(
                SessionSave.getSession(Constants.GPSLAT, requireActivity()).toString(),
                SessionSave.getSession(Constants.GPSLNG, requireActivity()).toString(),
                Notify.Appid
            )
            call.enqueue(object : Callback<WeatherResponse?> {
                override fun onResponse(
                    call: Call<WeatherResponse?>,
                    response: Response<WeatherResponse?>
                ) {
                    try {
                        if (response.isSuccessful) {
                            val data: WeatherResponse? = response.body()
                            for (i in 0 until data!!.weather!!.size) {
                                SessionSave.saveSession(
                                    Constants.WEATHER,
                                    data!!.weather[i].main,
                                    requireActivity()
                                )
                            }
                            setWeather()
                            logger(TAG, "weatherActivity : success $response")
                        } else {
                            logger(TAG, "weatherActivity : isfailure $response")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse?>, t: Throwable) {
                    logger(TAG, "weatherActivity : failure $t")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setWeather() {
        if(!SessionSave.getSession(Constants.WEATHER,requireActivity()).toString().isNullOrEmpty()) {
            val currentweather = SessionSave.getSession(Constants.WEATHER,requireActivity()).toString()
            logger(TAG,"weather : $currentweather")
            if (currentweather.equals("Clouds", ignoreCase = true)) /* Clouds-id:"801",icon:"02n" */ fragmentMapBinding.searchBar.main.weatherAnim.setAnimation(
                if (nigthtime) "weather-cloudynight.json" else "weather-partly-cloudy.json") else if (currentweather.equals(
                    "Mist",
                    ignoreCase = true
                ) || currentweather.equals("Haze", ignoreCase = true)
            ) /* Mist-id:"701" , icon:"50n" haze-id:"721", icon:"50d" */ fragmentMapBinding.searchBar.main.weatherAnim.setAnimation(
                "weather-mist.json"
            ) else if (currentweather.equals(
                    "Rain",
                    ignoreCase = true
                ) || currentweather.equals("Drizzle", ignoreCase = true)
            ) /* Rain-"id":500" , "icon":"10d" drizzle-"id":300,"icon":"09n" */ fragmentMapBinding.searchBar.main.weatherAnim.setAnimation(
                if (nigthtime) "weather-rainynight.json" else "weather-partly-shower.json"
            ) else if (currentweather.equals(
                    "Thunderstorm",
                    ignoreCase = true
                ) || currentweather.equals("Lightning", ignoreCase = true)
            ) /* Rain-"id":500" , "icon":"10d" drizzle-"id":300,"icon":"09n" */ fragmentMapBinding.searchBar.main.weatherAnim.setAnimation(
                if (nigthtime) "weather-storm.json" else "weather-stormshowersday.json"
            ) else if (currentweather.equals(
                    "Snow",
                    ignoreCase = true
                )
            ) fragmentMapBinding.searchBar.main.weatherAnim.setAnimation(if (Utils.nigthtime) "weather-snownight.json" else "weather-snow-sunny.json") else fragmentMapBinding.searchBar.main.weatherAnim.setAnimation(
                if (nigthtime) "weather-night.json" else "weather-sunny.json"
            )
        }
    }

    override fun onClick(v: View?) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

}