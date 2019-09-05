package com.pertamina.digitalcontract.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.GROUP_ALERT_SUMMARY
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pertamina.digitalcontract.R
import com.pertamina.digitalcontract.activity.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        remoteMessage?.data?.let {
            createNotification(remoteMessage?.data?.get("title"),remoteMessage?.data?.get("message"), it)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
        /*val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
        val myJob = dispatcher.newJobBuilder()
            .setService(MyJobService::class.java)
            .setTag("my-job-tag")
            .build()
        dispatcher.schedule(myJob)*/
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun createNotification(title: String?,body: String?,data : MutableMap<String,String>) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP )
        intent.action = System.currentTimeMillis().toString()

        var resultIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        var notificationSoundURI : Uri
        val GROUP_KEY = "group"
        val CHANNEL_ID = resources.getString(R.string.channel_id)// The id of the channel.
        val groupBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        groupBuilder
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(title)
            .setContentText(body)
            .setGroupSummary(true)
            .setGroup(GROUP_KEY)
            .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)

        notificationBuilder
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(title)
            .setContentText(body)
            .setGroup(GROUP_KEY)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
            .setContentIntent(resultIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            groupBuilder.color = resources.getColor(R.color.redSoft,null)
            notificationBuilder.color = resources.getColor(R.color.redSoft,null)
        }
        else {
            groupBuilder.color = resources.getColor(R.color.redSoft)
            notificationBuilder.color = resources.getColor(R.color.redSoft)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)// The user-visible name of the channel

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
            mChannel.enableLights(true)
            mChannel.lightColor = Color.BLUE
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mChannel.setSound(notificationSoundURI,attributes)

            notificationManager.createNotificationChannel(mChannel)
        }
        else {
            groupBuilder
                .setSound(notificationSoundURI)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))

            notificationBuilder
                .setSound(notificationSoundURI)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                notificationBuilder.priority = NotificationManager.IMPORTANCE_HIGH
            }
        }

        notificationManager.notify(0, groupBuilder.build())
        notificationManager.notify(NotificationID.id, notificationBuilder.build())
//        notificationManager.notify(NotificationID.id, notificationBuilder.build())
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}