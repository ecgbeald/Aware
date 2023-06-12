package uk.ac.ic.doc.aware

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContextCompat
import uk.ac.ic.doc.aware.api.Client
import uk.ac.ic.doc.aware.api.NewClient
import uk.ac.ic.doc.aware.api.WebSocketService
import uk.ac.ic.doc.aware.databinding.ActivityMainBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.map.setOnClickListener {
            Client.isLoggedIn = false
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        binding.login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val serviceIntent = Intent(this, WebSocketService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        println(applicationContext.bindService(Intent(this, WebSocketService::class.java), serviceConnection, Context.BIND_AUTO_CREATE))
    }
}