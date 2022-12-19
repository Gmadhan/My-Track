package com.mytrack.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.snackbar.Snackbar
import com.mytrack.R
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.Key
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.spec.SecretKeySpec

object Utils {
    var progress: Dialog? = null
    val ALGO = "AES"
    val keyValue = byteArrayOf(
        'Q'.code.toByte(),
        '8'.code.toByte(),
        'G'.code.toByte(),
        'R'.code.toByte(),
        'A'.code.toByte(),
        'N'.code.toByte(),
        'D'.code.toByte(),
        'L'.code.toByte(),
        'I'.code.toByte(),
        'M'.code.toByte(),
        'O'.code.toByte(),
        '@'.code.toByte(),
        '1'.code.toByte(),
        '2'.code.toByte(),
        '3'.code.toByte(),
        '4'.code.toByte()
    )
    var addressRegex = "^[ A-Za-z0-9_@.,/#)(&+-]*$".toRegex()
    var mobileNoRegex = "^[6-9][0-9]{9}$".toRegex()
    var nigthtime = false

    fun isValidMobileNumber(mobileNumber: String): Boolean {
        return !TextUtils.isEmpty(mobileNumber) && android.util.Patterns.PHONE.matcher(mobileNumber)
            .matches()
    }

    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    fun isValidPhone(length: Int): Boolean {
        return length >= 10
    }

    fun isValidOtp(otpValue: String): Boolean {
        return isAnInteger(otpValue) && otpValue.length == 6
    }

    @SuppressLint("SimpleDateFormat")
    fun currentDateTime(format: String?): String {
        val c = Calendar.getInstance()
        val dateformat = SimpleDateFormat(format ?: "dd-MM-yy hh:mm:ss aa")
        return dateformat.format(c.time)
    }

    @SuppressLint("SimpleDateFormat")
    fun convertDateTime(data: Date?, format: String?): String {
        val dateformat = SimpleDateFormat(format ?: "hh:mm a")
        return dateformat.format(data)
    }

    fun color(context: Context, color: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(color, null)
        } else {
            context.resources.getColor(color)
        }
    }

    fun isNetworkAvailable(context: Context?): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = cm.activeNetwork ?: return false
            val actNw = cm.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return cm.activeNetworkInfo?.isConnected ?: false
        }
    }

    fun encodeBitmapAndSaveToFirebase(bitmap: Bitmap?): String {
        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    @Throws(IOException::class)
    fun decodeFromFirebaseBase64(user_picturePath: String): Bitmap {
        val decodedByteArray = Base64.decode(user_picturePath, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }

    fun setImage(context: Context?, image: String?, imageView: ImageView) {
        try {
            if (!image.isNullOrEmpty()) {
                val imageBitmap = decodeFromFirebaseBase64(image!!)
                Glide.with(context!!).load(imageBitmap).placeholder(R.drawable.ic_profileimage)
                    .error(R.drawable.ic_profileimage).into(imageView)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager.isActive) {
            if (activity.currentFocus != null) {
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            }
        }
    }

    fun showToast(context: Context, msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    fun showSnackBar(view: View, message: String, context: Context, isSuccess: Boolean) {
        val snackBar = Snackbar.make(
            view, message, Snackbar.LENGTH_LONG
        )
        snackBar.changeFont()
        snackBar.setBackgroundTint(
            ContextCompat.getColor(
                context,
                if (isSuccess) R.color.Green else R.color.DarkRed
            )
        )
        snackBar.setTextColor(ContextCompat.getColor(context, R.color.White))

        var view = snackBar.view
        val params: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackBar.show()
    }

    private fun Snackbar.changeFont() {
        val tv = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        val font = Typeface.createFromAsset(context.assets, "font/rapor_regular.ttf")
        tv.typeface = font
    }

    fun generateKey(): Key {
        return SecretKeySpec(keyValue, ALGO)
    }

    fun getDeviceInfo(): String {
        var deviceInfo = ""
        try {
            deviceInfo = Build.MANUFACTURER + " " + Build.MODEL
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return deviceInfo
    }

    fun showloader(context: Context) {
        progress = Dialog(context, android.R.style.Theme_Black)
        val views: View = LayoutInflater.from(context).inflate(R.layout.loading_page, null)
        progress!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progress!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        progress!!.setContentView(views)
        progress!!.show()
    }

    fun dismissLoader() {
        if (progress != null)
            progress!!.dismiss()
    }

    fun setLanguage(context: Context, localeName: String) {
        try {
            val myLocale = Locale(localeName)
            val res: Resources = context.resources
            val dm: DisplayMetrics = res.displayMetrics
            val conf: Configuration = res.configuration
            conf.locale = myLocale
            res.updateConfiguration(conf, dm)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadImage(
        imageView: ImageView,
        url: String?,
        placeholder: Drawable?,
        errorDrawable: Drawable?
    ) {
        url.let {
            Glide.with(imageView.context)
                .load(it)
                .centerCrop()
                .placeholder(placeholder)
                .error(errorDrawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .thumbnail(0.5f)
                .dontAnimate()
                .skipMemoryCache(false)
                .into(imageView)
        }
    }

    fun destroyglide(context: Context) {
        Glide.get(context).clearMemory()
    }

    /* convert Vector image to map marker */
    fun bitmapDescriptionFromRes(context: Context?, resId: Int): BitmapDescriptor? {
        val drawable: Drawable = ContextCompat.getDrawable(context!!, resId)!!
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun capitalizeWords(input: String): String {
        var output = ""
        if (!input.isNullOrEmpty()) {
            val words = input.split(" ").toMutableList()
            for (word in words) {
                output += word.capitalize() + " "
            }
        }
        return output.trim()
    }

    fun isAnInteger(integer: String): Boolean {
        var result = false
        try {
            integer.toInt()
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun Any?.toString(): String {
        if (this == null) return "null"
        return toString()
    }

    fun String?.notNull(): String? {
        if (this != null && !this.isNullOrBlank() && this != "null") {
            return this
        }
        return null
    }

    fun String?.nulltodashChecker(): String {
        if (this != null && !this.isNullOrBlank() && !this.isNullOrEmpty() && this != "null") {
            return this
        }
        return "-"
    }

    fun String?.nulltoZeroChecker(): String {
        if (this != null && !this.isNullOrBlank() && this != "null") {
            return this
        }
        return "0"
    }

    fun String?.notNullString(): String? {
        if (this != null && this != "null") {
            return this
        }
        return null
    }

    fun Int?.notNullInt(): Int? {
        if (this != null && !this.equals("null")) {
            return this
        }
        return null
    }

    fun String?.notNullBoolean(): Boolean {
        if (this != null && !this.isNullOrBlank() && this != "null") {
            return true
        }
        return false
    }

    fun logger(from: String, message: String, isError: Boolean = false) {
        if (isError)
            Log.e(from, message)
        else
            Log.d(from, message)
    }

    fun callIntent(context: Context, activity: Class<*>?) {
        val intent = Intent(context, activity)
        context.startActivity(intent)
    }

}