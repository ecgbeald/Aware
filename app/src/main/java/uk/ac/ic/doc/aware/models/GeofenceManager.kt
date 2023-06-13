import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import uk.ac.ic.doc.aware.models.GeofenceBroadcastReceiver

class GeofenceManager(private val context: Context) {

    private val TAG = "GeofenceManager"

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val geofenceList: MutableList<Geofence> = mutableListOf()
    private  var geofencePendingIntent: PendingIntent? = null

    fun addGeofence(geofenceId: String, latitude: Double, longitude: Double, radius: Float, timeout: Long) {
        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(timeout*60*1000 - 100)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        geofenceList.add(geofence)
    }

    @SuppressLint("MissingPermission")
    fun startGeofencing() {
        if (geofenceList.isEmpty()) {
            Log.e(TAG, "Geofence list is empty")
            return
        }

        val geofencingRequest = GeofencingRequest.Builder()
            .addGeofences(geofenceList)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        geofencePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent as PendingIntent)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Geofences added successfully")
                } else {
                    Log.e(TAG, "Failed to add geofences: " + task.exception)
                }
            }
    }

    fun stopGeofencing() {
        if (geofencePendingIntent == null) {
            return
        }
        geofencePendingIntent.let {
            geofencingClient.removeGeofences(it as PendingIntent)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Geofences removed successfully")
                    } else {
                        Log.e(TAG, "Failed to remove geofences: " + task.exception)
                    }
                }
        }
    }

    fun clear() {
        geofenceList.clear()
    }
}