package com.mytrack.ui.onboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.messaging.FirebaseMessaging
import com.mytrack.R
import com.mytrack.databinding.ActivityOnboardBinding
import com.mytrack.ui.MainActivity
import com.mytrack.ui.login.SigninFragment
import com.mytrack.ui.login.SignupFragment
import com.mytrack.utils.*
import com.mytrack.utils.Utils.showToast


class OnBoardActivity: AppCompatActivity() {

    private lateinit var activityOnboardBinding: ActivityOnboardBinding
    private var mFirebaseDatabase: DatabaseReference? = null
    private var TAG: String? = "OnBoardActivity"
    val MY_PERMISSIONS_REQUEST = 110
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityOnboardBinding = ActivityOnboardBinding.inflate(layoutInflater)
        setContentView(activityOnboardBinding.root)
        init()
    }

    fun init() {
        getPermission()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        FirebaseApp.initializeApp(this)
        try {
            val mFirebaseInstance = Firebase.database.reference
            mFirebaseInstance.child("app").setValue("My Track")
            mFirebaseDatabase = mFirebaseInstance.child("users")
            Notify.DeviceToken = FirebaseMessaging.getInstance().token.result
            Utils.logger(TAG.toString(), "Token  : " + Notify.DeviceToken)
            SessionSave.saveSession(Constants.TOKEN, Notify.DeviceToken, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPagerAdapter.add(SigninFragment(), "Sign In")
        viewPagerAdapter.add(SignupFragment(), "Sign Up")
        activityOnboardBinding.vpLoginPage.offscreenPageLimit = 1

        activityOnboardBinding.vpLoginPage.adapter = viewPagerAdapter
        TabLayoutMediator(
            activityOnboardBinding.tlLogin,
            activityOnboardBinding.vpLoginPage
        ) { tab, position ->
            tab.text = viewPagerAdapter.getTabTitle(position)
        }.attach()

    }

    fun movetoHome() {
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finish()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        showToast(this, getString(R.string.please_click_again_to_exit))
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    private fun getPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    !== PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                application,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
                    ) || ActivityCompat.checkSelfPermission(
                application,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(
                this@OnBoardActivity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                MY_PERMISSIONS_REQUEST
            )
        }
//        || ActivityCompat.checkSelfPermission(application, Manifest.permission.RECORD_AUDIO) !== PackageManager.PERMISSION_GRANTED
//        Manifest.permission.RECORD_AUDIO

    }

}