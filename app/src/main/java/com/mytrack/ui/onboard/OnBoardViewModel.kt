package com.mytrack.ui.onboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mytrack.utils.Constants
import com.mytrack.utils.Notify
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils

class OnBoardViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "OnBoardViewModel"

    fun initializeFirebaseData() {
        try {
            val mFirebaseInstance = Firebase.database.reference
            mFirebaseInstance.child("app").setValue("My Track")
            
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Notify.DeviceToken = token
                    Utils.logger(TAG, "Token  : $token")
                    SessionSave.saveSession(Constants.TOKEN, token, getApplication())
                } else {
                    Utils.logger(TAG, "Fetching FCM registration token failed: ${task.exception}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
