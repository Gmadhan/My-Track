package com.mytrack

import android.annotation.SuppressLint
import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

@SuppressLint("StaticFieldLeak")
class MySingleton private constructor(private val ctx: Context) {
    private var requestQueue: RequestQueue?

    init {
        requestQueue = getRequestQueue()
    }

    fun getRequestQueue(): RequestQueue {
        if (requestQueue == null) {
            /** getApplicationContext() is key, it keeps you from leaking the
             * Activity or BroadcastReceiver if someone passes one in.  */
            requestQueue = Volley.newRequestQueue(ctx.applicationContext)
        }
        return requestQueue as RequestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>?) {
        getRequestQueue().add(req)
    }

    companion object {
        private var instance: MySingleton? = null

        @Synchronized
        fun getInstance(context: Context): MySingleton? {
            if (instance == null) {
                instance = MySingleton(context)
            }
            return instance
        }
    }
}
