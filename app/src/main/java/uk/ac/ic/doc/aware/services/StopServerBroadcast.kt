package uk.ac.ic.doc.aware.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat.startActivity
import uk.ac.ic.doc.aware.MainActivity
import uk.ac.ic.doc.aware.clients.AwareApplication


class StopServerBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {


        val webSocketService = Intent(context.applicationContext, WebSocketService::class.java)
        val geofenceService = Intent(context.applicationContext, GeofenceService::class.java)
        context.applicationContext.stopService(webSocketService)
        context.applicationContext.stopService(geofenceService)
        val closeintent = Intent(context.applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("EXIT", true)
//        finishAffinity(context.applicationContext);
        System.exit(0)

    }
    }
