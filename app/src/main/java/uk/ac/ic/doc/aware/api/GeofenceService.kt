package uk.ac.ic.doc.aware.api

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import uk.ac.ic.doc.aware.R
import uk.ac.ic.doc.aware.models.GeofenceBroadcastReceiver

class GeofenceService() : Service() {

    private val TAG = "GeofenceManager"
    lateinit var context : Context
    lateinit var geofencingClient: GeofencingClient

    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    val geofenceMap = mutableMapOf<String,Pair<Int,Float>>()


    val geofenceList: MutableList<Geofence> = mutableListOf()
    private  var geofencePendingIntent: PendingIntent? = null

    private val binder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): GeofenceService = this@GeofenceService
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        getLocationUpdates()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        val channelId = "Aware"
        val channelName = "Aware Background Service"
        val channelImportance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationManager.IMPORTANCE_LOW
        else
            NotificationManager.IMPORTANCE_NONE
        val channel = NotificationChannel(channelId, channelName, channelImportance)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Aware")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.notif)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        startForeground(1, notificationBuilder.build())
    }

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

    @SuppressLint("MissingPermission")
    private fun getLocationUpdates() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?.let {
                    for (location in locationResult.locations) {
                        // Handle the updated location here
                        val latitude = location.latitude
                        val longitude = location.longitude
                        // Do something with the location data
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    fun addSingleGeofence(geofenceId: String, latitude: Double, longitude: Double, radius: Float, timeout: Long) {
        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(timeout*60*1000 - 100)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        println("ADDED ID IS $geofenceId")

        val geofencingRequest = GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()

        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        geofencePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.addGeofences(geofencingRequest,geofencePendingIntent as PendingIntent)
        geofenceList.add(geofence)
    }

    fun removeGeofence(id : String) {
        geofencingClient.removeGeofences(listOf(id)) .addOnSuccessListener {
            val copy : MutableList<Geofence> = mutableListOf()
            copy.addAll(geofenceList)

            for (geofence in copy) {
                if (geofence.requestId == id) {
                    geofenceList.remove(geofence)
                }
            }
            Log.d(TAG, "Geofence removed: $id")
        }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error removing geofence: ${exception.localizedMessage}")
            }
    }

}