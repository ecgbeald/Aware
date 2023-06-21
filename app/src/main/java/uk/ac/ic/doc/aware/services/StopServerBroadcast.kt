package uk.ac.ic.doc.aware.services

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class StopServerBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val webSocketService = Intent(context, WebSocketService::class.java)
        val geofenceService = Intent(context, GeofenceService::class.java)
        context.applicationContext.stopService(webSocketService)
        context.applicationContext.stopService(geofenceService)
        System.exit(0)
        }
    }
