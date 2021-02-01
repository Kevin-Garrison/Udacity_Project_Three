package com.udacity.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.R

private val NOTIFICATION_ID = 0

fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context, status: String) {

    val completedIntent = Intent(applicationContext, DetailActivity::class.java)
    completedIntent.apply {
        putExtra("fileName", messageBody)
        putExtra("status", status)
    }

    val completedPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        completedIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val action = NotificationCompat.Action.Builder(0,"Show Details",completedPendingIntent).build()

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.download_notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_download_complete)
        .setContentTitle(messageBody)
        .setContentText(status)
        .setContentIntent(completedPendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(action)

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}