package uk.ac.ic.doc.aware

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import uk.ac.ic.doc.aware.api.GeofenceClient
import uk.ac.ic.doc.aware.api.GeofenceService
import uk.ac.ic.doc.aware.api.NewClient
import uk.ac.ic.doc.aware.api.WebSocketService
import uk.ac.ic.doc.aware.databinding.ActivityMainBinding
import uk.ac.ic.doc.aware.models.RadiusList.radiusList


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isServiceBound = false
    private val serviceConnection = object : ServiceConnection {

        lateinit var mapActivity: MapActivity

        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            println("NO BINDER RECEIVED :(")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            println("WebSocketService onServiceConnected called")
            val binder = service as WebSocketService.LocalBinder
            NewClient.webSocketService = binder.getService()
            isServiceBound = true

            // You can now access the WebSocket service and use its methods or variables.
            // For example, you can call webSocketService.sendMessage("Hello") to send a message.
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }
    private val serviceConnection2 = object : ServiceConnection {

        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            println("NO BINDER RECEIVED :(")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            println("GeoFence onServiceConnected called")
            val binder = service as GeofenceService.LocalBinder
            GeofenceClient.geofenceClient = binder.getService()
            GeofenceClient.geofenceClient.context = this@MainActivity
            GeofenceClient.geofenceClient.geofencingClient = LocationServices.getGeofencingClient(this@MainActivity)
            isServiceBound = true

            // You can now access the WebSocket service and use its methods or variables.
            // For example, you can call webSocketService.sendMessage("Hello") to send a message.
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)
        binding.map.setOnClickListener {
            NewClient.webSocketService.isLoggedIn = false
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            // no settings
            binding.settings.visibility = View.GONE
        }
        binding.login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.settings.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val layout = layoutInflater.inflate(R.layout.settings_dialog, null)
            builder.setView(layout)
            builder.setTitle("Settings for Radius")
            builder.setPositiveButton(android.R.string.yes) { _, _ ->
                val theftRadiusStr = layout.findViewById<EditText>(R.id.theft_radius).text.toString()
                val antiRadiusStr = layout.findViewById<EditText>(R.id.anti_radius).text.toString()
                val roadRadiusStr = layout.findViewById<EditText>(R.id.road_radius).text.toString()
                val majorRadiusStr = layout.findViewById<EditText>(R.id.major_radius).text.toString()
                if (theftRadiusStr.isNotEmpty()) {
                    radiusList[0] = theftRadiusStr.toInt()
                } else {
                    radiusList[0] = 100
                }
                if (antiRadiusStr.isNotEmpty()) {
                    radiusList[1] = antiRadiusStr.toInt()
                } else {
                    radiusList[1] = 200
                }
                if (roadRadiusStr.isNotEmpty()) {
                    radiusList[2] = roadRadiusStr.toInt()
                } else {
                    radiusList[2] = 300
                }
                if (majorRadiusStr.isNotEmpty()) {
                    radiusList[3] = majorRadiusStr.toInt()
                } else {
                    radiusList[3] = 400
                }
                println(radiusList)
            }
            builder.setNegativeButton(android.R.string.no) { _, _ ->
            }
            builder.show()
        }
        val serviceIntent = Intent(this, WebSocketService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        applicationContext.bindService(Intent(this, WebSocketService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        val serviceIntent2 = Intent(this, GeofenceService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent2)
        applicationContext.bindService(Intent(this, GeofenceService::class.java), serviceConnection2, Context.BIND_AUTO_CREATE)

    }
}