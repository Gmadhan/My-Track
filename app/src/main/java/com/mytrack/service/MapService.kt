package com.mytrack.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mytrack.R
import com.mytrack.utils.Constants
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils.logger
import java.text.DateFormat
import java.util.*

class MapService: Service(), ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private var mGoogleApiClient: GoogleApiClient? = null
    private val TAG = "Mapservice"
    private var mFirebaseDatabase: DatabaseReference? = null
    var isRunning = false
    private var gps_lat: Double? = null
    private var gps_lng: Double? = null
    private var date: String? = null
    private var user_phno: String? = null
    private val CHANNEL_ID = "service"

    override fun onCreate() {
        logger(TAG, "Service onCreate")
        isRunning = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification: Notification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service running").build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger(TAG, "Service onStartCommand")
        try {
            val mFirebaseInstance = FirebaseDatabase.getInstance()
            mFirebaseDatabase = mFirebaseInstance.getReference("users")
        } catch (e: Exception) {
            logger(TAG, Log.getStackTraceString(e), true)
        }
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        logger(TAG, "Service onBind")
        return null
    }

    override fun onDestroy() {
        logger(TAG, "Service onDestroy")
        isRunning = false
        mGoogleApiClient!!.disconnect()
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient!!, this)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) stopForeground(true) else stopSelf()
    }

    override fun onConnected(bundle: Bundle?) {
        logger(TAG, "Onconnected")
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 5000 // Update location every second
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        try {
            if (mGoogleApiClient == null) {
                mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                    .addApi(LocationServices.API)
                    .addOnConnectionFailedListener(this)
                    .build()
                mGoogleApiClient!!.connect()
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
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
            if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected)
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient!!, mLocationRequest, this)
        } catch (se: Exception) {
            logger(TAG, "Go into settings and find Gps Tracker app and enable Location.", true)
        }
    }

    override fun onConnectionSuspended(i: Int) {
        logger(TAG, "Connection Suspended")
    }

    override fun onLocationChanged(location: Location) {
        logger(TAG, "LocationChanged")
        gps_lat = location.latitude
        gps_lng = location.longitude
        if (gps_lat != null) {
            gpsupdate()
        }
        val d = Date()
        date = DateFormat.getDateTimeInstance().format(d)
        logger(TAG,"onLocation $gps_lat,  $gps_lng $date", true)
    }


    fun gpsupdate() {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetwork = cm?.activeNetworkInfo
        val connect = activeNetwork != null && activeNetwork.isConnectedOrConnecting
        if (connect) {
            try {
                user_phno = SessionSave.getSession(Constants.MOBILENO, this)
                if (user_phno != "") {
                    if (gps_lat != null && gps_lng != null) {
                        mFirebaseDatabase!!.child(user_phno!!).child("gps_Lat").setValue(gps_lat)
                        mFirebaseDatabase!!.child(user_phno!!).child("gps_Lng").setValue(gps_lng)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

}