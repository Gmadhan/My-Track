package com.mytrack.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.airbnb.lottie.LottieAnimationView
import com.mytrack.R
import com.mytrack.ui.MainActivity
import com.mytrack.ui.onboard.OnBoardActivity
import com.mytrack.utils.Constants
import com.mytrack.utils.Programs
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils
import com.mytrack.utils.Utils.logger
import com.mytrack.utils.Utils.setLanguage

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var app_icon: LottieAnimationView? = null
    private var isLogin = false
    private var langcountry: String? = null
    private var currentweather: String? = null
    private var gpslat: String? = null
    private var gpslng: String? = null
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        app_icon = findViewById(R.id.app_icon)
        isLogin = SessionSave.getSessionBoolean(Constants.ISLOGIN, this)
        langcountry = SessionSave.getSession(Constants.LANGUAGE, this)
        gpslat = SessionSave.getSession(Constants.GPSLAT, this)
        gpslng = SessionSave.getSession(Constants.GPSLNG, this)
        logger(TAG, "lang $langcountry")
        if (langcountry != null || !langcountry.equals("", ignoreCase = true)) setLanguage(this, langcountry!!)
        Handler(Looper.getMainLooper()).postDelayed({
            if (isLogin) {
                callIntent(MainActivity::class.java)
            } else {
                callIntent(OnBoardActivity::class.java)
            }
        }, 4000)
        getUserCountry(applicationContext)
    }

    private fun getUserCountry(context: Context): String? {
        try {
            val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            val simCountry = tm.simCountryIso
            if (simCountry != null && simCountry.length == 2) { // SIM country code is
                SessionSave.saveSession(Constants.SIMCODE, simCountry.lowercase(), this@SplashActivity)
                logger(TAG, Constants.SIMCODE + " " + simCountry.lowercase())
                return simCountry.lowercase()
            } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                val networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                    SessionSave.saveSession(
                        Constants.COUNTRYCODE,
                        networkCountry.lowercase(),
                        this@SplashActivity
                    )
                    logger(TAG, Constants.COUNTRYCODE + " " + networkCountry.lowercase())
                    return networkCountry.lowercase()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun callIntent(myActivity: Class<*>?) {
        val intent = Intent(this@SplashActivity, myActivity)
        startActivity(intent)
        finish()
    }

}