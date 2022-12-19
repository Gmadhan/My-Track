package com.mytrack.utils

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class Singleton {
    /*private var instance: Singleton? = null
    private var requestQueue: RequestQueue? = null
    private var ctx: Context? = null

    private fun Singleton(context: Context) {
        ctx = context
        requestQueue = getRequestQueue()
    }

    @Synchronized
    fun getInstance(context: Context): Singleton? {
        if (instance == null) {
            instance = Singleton(context)
        }
        return instance
    }

    fun getRequestQueue(): RequestQueue {
        if (requestQueue == null) {
            *//** getApplicationContext() is key, it keeps you from leaking the
             * Activity or BroadcastReceiver if someone passes one in.  *//*
            requestQueue = Volley.newRequestQueue(ctx!!.applicationContext)
        }
        return requestQueue!!
    }

    fun <T> addToRequestQueue(req: Request<T>?) {
        getRequestQueue().add(req)
    }*/
}