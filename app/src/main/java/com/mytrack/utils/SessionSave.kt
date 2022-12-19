package com.mytrack.utils

import android.app.Activity
import android.content.Context

object SessionSave {

    fun saveSession(key: String?, value: String?, context: Context?) {
        if (context != null) {
            val editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit()
            editor.putString(key, value)
            editor.commit()
        }
    }

    fun saveSession(key: String?, value: Boolean, context: Context?) {
        if (context != null) {
            val editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit()
            editor.putBoolean(key, value)
            editor.commit()
        }
    }

    fun saveSession(key: String?, value: Long?, context: Context?) {
        if (context != null) {
            val editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit()
            editor.putLong(key, value!!)
            editor.commit()
        }
    }


    fun saveSessionInt(key: String?, value: Int?, context: Context?) {
        if (context != null) {
            val editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit()
            editor.putInt(key, value!!)
            editor.commit()
        }
    }

    fun saveSessionFloat(key: String?, value: Float, context: Context?) {
        if (context != null) {
            val editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit()
            editor.putFloat(key, value)
            editor.apply()
        }
    }

    fun clearAllSession(context: Context?) {
        if (context != null) {
            val prefs = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE)
            prefs.all.clear()
            return
        } else {
            return
        }
    }

    fun getSession(key: String?, context: Context?): String? {
        return if (context != null) {
            val prefs = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE)
            prefs.getString(key, "")
        } else {
            ""
        }
    }

    fun getSessionInt(key: String?, context: Context?): Int? {
        return if (context != null) {
            val prefs = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE)
            prefs.getInt(key, -1)
        } else {
            -1
        }
    }

    fun getSessionLong(key: String?, context: Context?, a: Long): Long {
        return if (context != null) {
            val prefs = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE)
            prefs.getLong(key, 0)
        } else {
            0
        }
    }

    fun getSessionBoolean(key: String?, context: Context?): Boolean {
        return if (context != null) {
            val prefs = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE)
            prefs.getBoolean(key, false)
        } else {
            false
        }
    }

    fun getSessionFloat(key: String?, context: Context?, defaultValue: Float): Float {
        return if (context != null) {
            val prefs = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE)
            prefs.getFloat(key, defaultValue)
        } else {
            defaultValue
        }
    }

    fun clearSession(context: Context) {
        val editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit()
        editor.clear()
        editor.commit()
    }

    fun tutorialChk(isAsk: Boolean, con: Context) {
        val editor = con.getSharedPreferences("ASK", Context.MODE_PRIVATE).edit()
        editor.putBoolean("isAsk", isAsk)
        editor.commit()
    }

    fun isAsk(con: Context): Boolean {
        val sharedPreferences = con.getSharedPreferences("ASK", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isAsk", false)
    }

    fun saveArray(message: String?, con: Context): Boolean {
        val sp = con.getSharedPreferences("KEYARRAY", Context.MODE_PRIVATE)
        val mEdit1 = sp.edit()
        /* sKey is an array */mEdit1.putString("Status_size", message)
        return mEdit1.commit()
    }

    fun loadArray(con: Context): ArrayList<String?>? {
        val sKey = ArrayList<String?>()
        val mSharedPreference1 = con.getSharedPreferences("KEYARRAY", Context.MODE_PRIVATE)
        sKey.clear()
        val size = mSharedPreference1.getInt("Status_size", 0)
        for (i in 0 until size) {
            sKey.add(mSharedPreference1.getString("Status_$i", null))
        }
        return sKey
    }

    fun clearSessionOneTime(context: Context) {
        val prefs = context.getSharedPreferences("KEY_ONE_TIME", Activity.MODE_PRIVATE)
        prefs.edit().clear()
    }

    fun getSessionOneTime(key: String?, context: Context): String? {
        val prefs = context.getSharedPreferences("KEY_ONE_TIME", Activity.MODE_PRIVATE)
        val s = prefs.getString(key, "")
        return s ?: ""
    }

    fun saveSessionOneTime(key: String?, value: String?, context: Context?) {
        if (context != null) {
            val editor = context.getSharedPreferences("KEY_ONE_TIME", Activity.MODE_PRIVATE).edit()
            editor.putString(key, value)
            editor.commit()
        }
    }
}