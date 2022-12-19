package com.mytrack.service

import com.mytrack.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mytrack.ui.MainActivity
import com.mytrack.utils.Constants.TOKEN
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils

class FirebaseService: FirebaseMessagingService() {

    val TAG = "MyFirebaseMsgService"
    var SUBSCRIBE_TO: String? = null

    override fun onNewToken(token: String) {
        Utils.logger(TAG, "Refreshed token: $token",false)
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
        /*SUBSCRIBE_TO = "/topics/"+preferences.getString("Mobileno", "");
        logger(TAG,"message r"+SUBSCRIBE_TO);
        FirebaseMessaging.getInstance().subscribeToTopic(SUBSCRIBE_TO);*/
    }

    private fun sendRegistrationToServer(token: String) {
        //You can implement this method to store the token on your server
        SessionSave.saveSession(TOKEN, token, context = applicationContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Utils.logger(TAG, "message received $remoteMessage")
        sendNotification(remoteMessage.data["title"], remoteMessage.data["message"].toString())
    }

    private fun sendNotification(title: String?, messageBody: String) {
        Utils.logger(TAG, "message service$title $messageBody")
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channelId = "MapBroadcast"
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Mapbroadcast", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

}