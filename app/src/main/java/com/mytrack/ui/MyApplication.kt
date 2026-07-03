package com.mytrack.ui

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.android.libraries.places.api.Places
import com.mytrack.R

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase once for the whole app
        FirebaseApp.initializeApp(this)
        
        // Initialize Google Places SDK once
        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_maps_key))
        }
    }
}
