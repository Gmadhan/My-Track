package com.mytrack.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mytrack.api.RetrofitClientInstance
import com.mytrack.model.WeatherResponse
import com.mytrack.utils.Constants
import com.mytrack.utils.Notify
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "MapViewModel"

    private val _weatherStatus = MutableLiveData<String>()
    val weatherStatus: LiveData<String> = _weatherStatus

    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng> = _currentLocation

    private val database = Firebase.database.reference.child("users")

    fun updateLocationInFirebase(lat: Double, lng: Double) {
        val phone = SessionSave.getSession(Constants.MOBILENO, getApplication())
        if (!phone.isNullOrEmpty()) {
            val updates = mapOf(
                "gps_Lat" to lat,
                "gps_Lng" to lng
            )
            database.child(phone).updateChildren(updates)
            SessionSave.saveSession(Constants.GPSLAT, lat.toString(), getApplication())
            SessionSave.saveSession(Constants.GPSLNG, lng.toString(), getApplication())
        }
    }

    fun callWeatherApi(lat: String, lng: String) {
        RetrofitClientInstance.apiCall?.getweather(lat, lng, Notify.Appid)?.enqueue(object : Callback<WeatherResponse?> {
            override fun onResponse(call: Call<WeatherResponse?>, response: Response<WeatherResponse?>) {
                if (response.isSuccessful) {
                    val weather = response.body()?.weather?.firstOrNull()?.main
                    weather?.let {
                        SessionSave.saveSession(Constants.WEATHER, it, getApplication())
                        _weatherStatus.value = it
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse?>, t: Throwable) {
                Utils.logger(TAG, "Weather API failure: ${t.message}")
            }
        })
    }
}
