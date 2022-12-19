package com.mytrack.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import com.mytrack.utils.Constants.MOBILENO
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils
import com.mytrack.utils.Utils.logger
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class BroadcastService: BroadcastReceiver() {

    var connectivityReceiverListener: ConnectivityReceiverListener? = null
    private var context: Context? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationListener: LocationListener? = null
    private var locationManager: LocationManager? = null
    private var latLng: LatLng? = null
    private var intent: Intent? = null
    private var user_phno: String? = null
    private var state: String? = null
    private var datetime: String? = null
    private var addrs: String? = null
    private var action: String? = null
    private val TAG = "MyBroadcast"
    private val filename = "myfiles.txt"
    private val filename1 = "broadcast.txt"
    private var lat: Double? = null
    private  var lng:Double? = null
    private  var gps_lat:Double? = null
    private  var gps_lng:Double? = null
    var isConnected = false
    private val list1 = ArrayList<String>()
    private var outputStream: FileOutputStream? = null
    private  var outputStream1:FileOutputStream? = null
    private val newline = System.getProperty("line.separator")

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        this.intent = intent
        action = intent.action
        datetime = Utils.currentDateTime("dd-MM-yy hh:mm:ss aa")
        if (action.equals("my.action.string", ignoreCase = true)) {
            state = intent.extras!!.getString("key")
            if (state != null) {
                try {
                    outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
                    outputStream!!.write(state.toString().toByteArray())
                    outputStream!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        isConnected = Utils.isNetworkAvailable(context)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun getLocation() {
        locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context!!,
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
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f,
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lat = location.latitude
                    lng = location.longitude
                    latLng = LatLng(lat!!, lng!!)
                    addrs = latLng.toString()
                    try {
                        list1.add("$addrs , $datetime $action$newline")
                        outputStream1 = context!!.openFileOutput(filename1, Context.MODE_PRIVATE)
                        outputStream1!!.write(list1.toString().toByteArray())
                        outputStream1!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    logger(TAG, "Broadcast $addrs $datetime")
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }.also {
                locationListener = it
            })
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    fun gpsupdate() {
        try {
            val mFirebaseInstance = FirebaseDatabase.getInstance()
            val mFirebaseDatabase = mFirebaseInstance.getReference("users")
            user_phno = SessionSave.getSession(MOBILENO, context)
            if (user_phno != "") {
                if (gps_lat != null)
                    mFirebaseDatabase.child(user_phno!!).child("gps_Lat").setValue(gps_lat)
                if (gps_lng != null)
                    mFirebaseDatabase.child(user_phno!!).child("gps_Lng").setValue(gps_lng)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}