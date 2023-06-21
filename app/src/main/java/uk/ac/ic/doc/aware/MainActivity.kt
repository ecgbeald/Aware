package uk.ac.ic.doc.aware

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Html
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import uk.ac.ic.doc.aware.clients.GeofenceClient
import uk.ac.ic.doc.aware.services.GeofenceService
import uk.ac.ic.doc.aware.clients.WebSocketClient
import uk.ac.ic.doc.aware.services.WebSocketService
import uk.ac.ic.doc.aware.databinding.ActivityMainBinding
import uk.ac.ic.doc.aware.models.LoginStatus
import uk.ac.ic.doc.aware.models.RadiusList.radiusList
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isServiceBound = false
//    private val dialogsQueue = ArrayList<AlertDialog.Builder>()
    private val serviceConnection = object : ServiceConnection {


        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            println("NO BINDER RECEIVED :(")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            println("WebSocketService onServiceConnected called")
            val binder = service as WebSocketService.LocalBinder
            WebSocketClient.webSocketService = binder.getService()
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
            GeofenceClient.geofenceClient.geofencingClient =
                LocationServices.getGeofencingClient(this@MainActivity)
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

        if (intent.getBooleanExtra("EXIT", false)) {
            val webSocketService = Intent(this, WebSocketService::class.java)
            val geofenceService = Intent(this, GeofenceService::class.java)
            stopService(webSocketService)
            stopService(geofenceService)
            println("FINISHING")
            finishAffinity();
            System.exit(0)
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)
        binding.map.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            // no settings
//            binding.settings.visibility = View.GONE
        }
        binding.login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
//            binding.settings.visibility = View.GONE
        }
        val serviceIntent = Intent(this, WebSocketService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        applicationContext.bindService(
            Intent(this, WebSocketService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        println("service 1 started")
        val serviceIntent2 = Intent(this, GeofenceService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent2)
        applicationContext.bindService(
            Intent(this, GeofenceService::class.java),
            serviceConnection2,
            Context.BIND_AUTO_CREATE
        )
        println("service 2 started")

//        binding.settings.setOnClickListener {
//            val builder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//            val layout = layoutInflater.inflate(R.layout.settings_dialog, null)
//            builder.setView(layout)
//            builder.setTitle("Settings for Radius")
//            builder.setPositiveButton(android.R.string.yes) { _, _ ->
//                val theftField = layout.findViewById<EditText>(R.id.theft_radius)
//                val antiField = layout.findViewById<EditText>(R.id.anti_radius)
//                val roadField = layout.findViewById<EditText>(R.id.road_radius)
//                val majorField = layout.findViewById<EditText>(R.id.major_radius)
//                val theftRadiusStr = theftField.text.toString()
//                val antiRadiusStr = antiField.text.toString()
//                val roadRadiusStr = roadField.text.toString()
//                val majorRadiusStr = majorField.text.toString()
//                if (theftRadiusStr.isNotEmpty()) {
//                    var theftRadius = 100
//                    try {
//                        theftRadius = theftRadiusStr.toInt()
//                    } catch (nfe: NumberFormatException) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Thieving</b> alerts is too high, a radius of less than <b>2km</b> is recommended."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    }
//                    if (theftRadius < 100) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Thieving</b> alerts is too low, a radius of at least <b>100m</b> is needed."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton(android.R.string.no) { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    } else if (theftRadius >= 2000) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Thieving</b> alerts is too high, a radius of less than <b>2km</b> is recommended."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    } else {
//                        radiusList[0] = theftRadius
//                    }
//                } else {
//                    radiusList[0] = 100
//                }
//                if (antiRadiusStr.isNotEmpty()) {
//                    var antiRadius = 200
//                    try {
//                        antiRadius = antiRadiusStr.toInt()
//                    } catch (nfe: NumberFormatException) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Anti Social Behaviour</b> alerts too high, a radius of less than <b>2km</b> is recommended."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    }
//                    if (antiRadius < 100) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Anti Social Behaviour</b> alerts is too low, a radius of at least <b>100m</b> is needed."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    } else if (antiRadius >= 2000) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Anti Social Behaviour</b> alerts too high, a radius of less than <b>2km</b> is recommended."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    } else {
//                        radiusList[1] = antiRadius
//                    }
//                } else {
//                    radiusList[1] = 200
//                }
//                if (roadRadiusStr.isNotEmpty()) {
//                    var roadRadius = 300
//                    try {
//                        roadRadius = roadRadiusStr.toInt()
//                    } catch (nfe: NumberFormatException) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Travel Disruption</b> alerts too high, a radius of less than <b>2km</b> is recommended."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    }
//                    if (roadRadius < 100) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Travel Disruption</b> alerts is too low, a radius of at least <b>100m</b> is needed."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    } else if (roadRadius >= 2000) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Travel Disruption</b> alerts too high, a radius of less than <b>2km</b> is recommended."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    } else {
//                        radiusList[2] = roadRadius
//                    }
//                } else {
//                    radiusList[2] = 300
//                }
//                if (majorRadiusStr.isNotEmpty()) {
//                    var majorRadius = 400
//                    try {
//                        majorRadius = majorRadiusStr.toInt()
//                    } catch (nfe: NumberFormatException) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Major Disruption</b> alerts is too high, a radius of less than <b>2km</b> is recommended."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    }
//                    if (majorRadius < 100) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Major Disruption</b> alerts is too low, a radius of at least <b>100m</b> is needed."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    } else if (majorRadius >= 2000) {
//                        val builder1 = AlertDialog.Builder(this,R.style.CustomAlertDialog)
//                        builder1.setMessage(Html.fromHtml("Radius setting for <b>Major Disruption</b> alerts is too high, a radius of less than <b>2km</b> is recommended."))
//                        builder1.setCancelable(false)
//                        builder1.setNegativeButton("OK") { _, _ ->
//                        }
//                        dialogsQueue.add(builder1)
//                    } else {
//                        radiusList[3] = majorRadius
//                    }
//                } else {
//                    radiusList[3] = 400
//                }
//                val confirmationDialog = AlertDialog.Builder(this,R.style.CustomAlertDialog).setTitle("Confirmation")
//                    .setMessage(
//                        "Radius for Thieving Activity: ${radiusList[0]}m\nRadius for Anti Social Behaviour: ${radiusList[1]}m\n" +
//                                "Radius for Travel Disruptions: ${radiusList[2]}m\nRadius for Major Incidences: ${radiusList[3]}m"
//                    )
//                    .setPositiveButton("OK") { _, _ -> }
//                confirmationDialog.show()
//                for (dialog in dialogsQueue.reversed()) {
//                    dialog.show()
//                }
//                dialogsQueue.clear()
//                println(radiusList)
//            }
//            builder.setNegativeButton(android.R.string.no) { _, _ ->
//            }
//            builder.show()
//        }

    }
}