package com.mytrack.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mytrack.R
import com.mytrack.databinding.ActivityMainBinding
import com.mytrack.ui.chat.ChatListFragment
import com.mytrack.ui.map.MapFragment
import com.mytrack.ui.onboard.OnBoardActivity
import com.mytrack.ui.profile.ProfileFragment
import com.mytrack.ui.weather.WeatherFragment
import com.mytrack.utils.Utils.showToast

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var currentFragment: Fragment
    private var doubleBackToExitPressedOnce:Boolean = false
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        init()
    }

    private fun init() {
        navigateToFragment(MapFragment())

        activityMainBinding.bottomNavi.setOnNavigationItemSelectedListener {
                item ->
            return@setOnNavigationItemSelectedListener when (item.itemId) {
                R.id.home -> {
                    item.isCheckable = true
                    navigateToFragment(MapFragment())
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.chat -> {
                    item.isCheckable = true
                    navigateToFragment(ChatListFragment())
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.weather -> {
                    item.isCheckable = true
                    navigateToFragment(WeatherFragment())
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.profile -> {
                    item.isCheckable = true
                    navigateToFragment(ProfileFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }

    }

    fun navigateToFragment(fragmentToNavigate: Fragment) {
        currentFragment = fragmentToNavigate
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_container, fragmentToNavigate)
        fragmentTransaction.commit()
    }

    fun movetoLogin() {
        val i = Intent(this, OnBoardActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finish()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            onBackPressedDispatcher.onBackPressed()
        }
        this.doubleBackToExitPressedOnce = true
        showToast(this, getString(R.string.please_click_again_to_exit))
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

}