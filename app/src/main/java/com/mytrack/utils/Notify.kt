package com.mytrack.utils

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.mytrack.R
import com.mytrack.utils.Utils.logger
import java.io.File
import java.util.*

object Notify : AppCompatActivity() {

    val TAG  = "Notify"
    private var alertmDialog: Dialog? = null
    private var loadingDialog: Dialog? = null
    var mobileArr = ArrayList<String>()
    var DeviceToken: String? = null
    var Appid = "fede8c6f8a61cfd5c731ec3865d0294c"
    var recognitionListener: RecognitionListener? = null
    var speechRecognizer: SpeechRecognizer? = null
    var speechIntent: Intent? = null
    var permission = false
    const val MY_PERMISSIONS_REQUEST_LOCATION = 99

    /** Create notification  */
    fun createNotificationChannel(Title: String?, Message: String?, context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val CHANNEL_ID = "ID_1"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifyID = 1
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, Title, importance)
            channel.description = Message
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notification: Notification = Notification.Builder(context)
                .setContentTitle(Title)
                .setContentText(Message)
                .setSmallIcon(R.drawable.app_icon)
                .setChannelId(CHANNEL_ID)
                .build()
            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel)
                mNotificationManager.notify(notifyID, notification)
            }
        } else {
//               Intent intent = new Intent(this, forgotpwdActivity.class);
//               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//               PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(Title)
                .setContentText(Message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1, mBuilder.build())
        }
    }

    /** Common alert view  */
    fun alertView(
        title: String,
        mcontext: Context,
        imagepath: String,
        image: Int,
        message: String?,
        success: String?,
        failed: String?
    ) {
        try {
            if (alertmDialog != null && alertmDialog!!.isShowing) {
                alertmDialog!!.dismiss()
            }
            val view = View.inflate(mcontext, R.layout.alert_view, null)
            alertmDialog = Dialog(mcontext, R.style.DialogFragmentTheme)
            alertmDialog!!.setContentView(view)
            alertmDialog!!.setCancelable(true)
            val window = alertmDialog!!.window
            val wlp = window!!.attributes
            wlp.gravity = Gravity.CENTER
            wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
            window.attributes = wlp
            alertmDialog!!.window!!
                .setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            alertmDialog!!.show()

//            final AdView mAdView = alertmDialog.findViewById(R.id.adView);
            val message_text = alertmDialog!!.findViewById<AppCompatTextView>(R.id.alertMessage)
            val txtSuccess = alertmDialog!!.findViewById<AppCompatTextView>(R.id.success_btn)
            val txtCancel = alertmDialog!!.findViewById<AppCompatTextView>(R.id.failure_btn)
            val txtTitle = alertmDialog!!.findViewById<AppCompatTextView>(R.id.alertTitle)
            val alertImage = alertmDialog!!.findViewById<AppCompatImageView>(R.id.alertImage)
            val alertIcon = alertmDialog!!.findViewById<AppCompatImageView>(R.id.alertIcon)

//               try{
//                   AdRequest adRequest = new AdRequest.Builder().build();
//                   mAdView.loadAd(adRequest);
//               } catch (Exception e){
//                   e.printStackTrace();
//               }
            if (title.equals("Screenshot", ignoreCase = true)) {
                message_text.visibility = View.GONE
                alertImage.visibility = View.VISIBLE
                txtTitle.text = title
                txtSuccess.text = success
                txtCancel.text = failed
                Glide.with(mcontext).load(imagepath).into(alertImage)
            } else if (title.equals("Enable GPS", ignoreCase = true)) {
                message_text.visibility = View.VISIBLE
                alertIcon.visibility = View.VISIBLE
                alertIcon.setImageResource(image)
                txtTitle.text = title
                message_text.text = message
                txtSuccess.text = success
                txtCancel.visibility = View.GONE
            } else if (title.equals("Network Connection", ignoreCase = true)) {
                message_text.visibility = View.VISIBLE
                txtTitle.text = title
                message_text.text = message
                txtSuccess.text = success
                txtCancel.visibility = View.GONE
            } else if (title.equals("Find Friend", ignoreCase = true)) {
                message_text.visibility = View.VISIBLE
                txtTitle.text = title
                message_text.text = message
                txtSuccess.text = success
                txtCancel.visibility = View.GONE
            }
            txtSuccess.setOnClickListener { v: View? ->
                if (title.equals("Screenshot", ignoreCase = true)) {
                    Toast.makeText(mcontext, "Image saved", Toast.LENGTH_SHORT).show()
                } else if (title.equals("Enable GPS", ignoreCase = true)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val mIntent =
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        mcontext.startActivity(mIntent)
                    }
                }
                alertmDialog!!.dismiss()
            }
            txtCancel.setOnClickListener { v: View? ->
                if (title.equals("Screenshot", ignoreCase = true)) {
                    val fdelete = File(imagepath)
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            Toast.makeText(mcontext, "Image not saved ", Toast.LENGTH_SHORT).show()
                            logger(TAG, "file Deleted :$imagepath")
                        } else {
                            Toast.makeText(mcontext, "Image saved", Toast.LENGTH_SHORT).show()
                            logger(TAG,"file not Deleted :$imagepath")
                        }
                    }
                }
                alertmDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Dismiss if alert dialog is present  */
    fun dismissalert() {
        if (alertmDialog != null && alertmDialog!!.isShowing) {
            alertmDialog!!.dismiss()
        }
    }

    fun voicerec(context: Context?) {
        recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {
                logger(TAG,"SpeechRecognizer : onReadyForSpeech $params")
            }

            override fun onBeginningOfSpeech() {
                logger(TAG,"SpeechRecognizer : onBeginningOfSpeech ")
            }

            override fun onRmsChanged(rmsdB: Float) {
                logger(TAG,"SpeechRecognizer : onRmsChanged $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray) {
                logger(TAG,"SpeechRecognizer : onBufferReceived $buffer")
            }

            override fun onEndOfSpeech() {
                logger(TAG,"SpeechRecognizer : onEndOfSpeech ")
            }

            override fun onError(error: Int) {
                logger(TAG,"SpeechRecognizer : onError $error")
            }

            override fun onResults(results: Bundle) {
                logger(TAG,
                    StringBuilder().append("SpeechRecognizer : onResults ").append(results)
                        .toString()
                )
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                /*if (matches != null && matches == "Hello") {
                    MapsActivity.searchvoice_btn.performClick()
                }*/
            }

            override fun onPartialResults(partialResults: Bundle) {
                logger(TAG,"SpeechRecognizer : onPartialResults $partialResults")
            }

            override fun onEvent(eventType: Int, params: Bundle) {
                logger(TAG,"SpeechRecognizer : onEvent $eventType params $params")
            }
        }
        initSpeechRecognizer(context)
    }


    fun initSpeechRecognizer(mcontext: Context?) {
        logger(TAG,"SpeechRecognizer : ")
        // Create the speech recognizer and set the listener
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(mcontext)
        speechRecognizer!!.setRecognitionListener(recognitionListener)

        // Create the intent with ACTION_RECOGNIZE_SPEECH
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
        listen()
    }

    fun listen() {
        // startListening should be called on Main thread
        val mainHandler = Handler(Looper.getMainLooper())
        val myRunnable = Runnable {
            speechRecognizer!!.startListening(
                speechIntent
            )
        }
        mainHandler.post(myRunnable)
    }

    fun checkLocationPermission(activity: Activity?) {
        permission =
            if (ContextCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
                true
            } else true
    }
}