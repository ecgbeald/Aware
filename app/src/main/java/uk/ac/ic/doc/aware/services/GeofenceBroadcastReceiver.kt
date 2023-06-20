package uk.ac.ic.doc.aware.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import uk.ac.ic.doc.aware.R
import uk.ac.ic.doc.aware.clients.GeofenceClient

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "GeofenceBroadcastReceiver"
    private val CHANNEL_ID = "GeofenceChannel"
    private val CRIMES = listOf("THEFT","ANTI-SOCIAL BEHAVIOUR","TRAVEL DISRUPTION","MAJOR INCIDENT")
    private val ICONS = listOf(R.drawable.theft_hires, R.drawable.anti_social_hires, R.drawable.block_hires, R.drawable.major_hires)

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
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "Entered geofence")
            if (geofenceList != null) {
                for (geofence in geofenceList) {
                    println(geofence.requestId)
                    val severity = GeofenceClient.geofenceClient.geofenceMap[geofence.requestId]!!.first
                    val message = CRIMES[severity]
                    val dist = GeofenceClient.geofenceClient.geofenceMap[geofence.requestId]!!.second
                    sendNotification(context, "Within " + dist.toInt() + "m of $message!", severity, geofence.requestId.toInt())
                }
            }

            println("ENTERED REEEE")

            // Handle geofence enter event
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d(TAG, "Exited geofence")
            println("EXITED REEEE")
            // Handle geofence exit event
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(context: Context, message: String, severity: Int, id: Int) {
        createNotificationChannel(context)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notif)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, ICONS[severity]))
            .setContentTitle("Aware Alert")
            .setContentText(message)
            .setColor(Color.BLUE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(id, notificationBuilder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alert Channel"
            val descriptionText = "Channel for geo-location alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}