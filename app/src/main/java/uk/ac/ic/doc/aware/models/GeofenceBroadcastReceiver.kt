package uk.ac.ic.doc.aware.models

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import uk.ac.ic.doc.aware.R

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "GeofenceBroadcastReceiver"
    private val CHANNEL_ID = "GeofenceChannel"
    private val NOTIFICATION_ID = 3

    override fun onReceive(context: Context, intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = geofencingEvent.errorCode
                Log.e(TAG, "Geofence error: $errorMessage")
                return
            }
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition
        val geofenceList = geofencingEvent?.triggeringGeofences
        println(geofenceTransition)
        println(geofenceTransition)
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "Entered geofence")
            sendNotification(context, "WITHIN 500m of ALERT")
            println("ENTERED REEEE")

            // Handle geofence enter event
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d(TAG, "Exited geofence")
            println("EXITED REEEE")
            // Handle geofence exit event
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(context: Context, message: String) {
        createNotificationChannel(context)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.major)
            .setContentTitle("Geofence Notification")
            .setContentText(message)
            .setColor(Color.BLUE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Geofence Channel"
            val descriptionText = "Channel for geofence notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}