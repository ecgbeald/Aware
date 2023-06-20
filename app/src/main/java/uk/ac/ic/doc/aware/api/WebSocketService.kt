package uk.ac.ic.doc.aware.api

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import uk.ac.ic.doc.aware.MainActivity
import uk.ac.ic.doc.aware.MapActivity
import uk.ac.ic.doc.aware.R
import java.util.concurrent.CountDownLatch


class WebSocketService: Service() {
    lateinit var webSocket: WebSocket
    lateinit var latch: CountDownLatch
    lateinit var data: List<MyData>
    lateinit var mapActivity: MapActivity
    var isLoggedIn = false
    lateinit var salt: String
    private var awareApplication: AwareApplication? = null

    fun isDataInitialized() = ::data.isInitialized
    private val binder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): WebSocketService = this@WebSocketService
    }

    override fun onCreate() {
        super.onCreate()
        awareApplication = applicationContext as? AwareApplication
    }

    override fun onBind(intent: Intent?): IBinder {
        println("RETURNING BINDER")
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startWebSocket()
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

    private val handler = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("WebSocket connection opened")
            // Send a message to the server
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            println("Received message: $text")
            if (text == "refresh") {
                mapActivity.runOnUiThread {
                    mapActivity.refreshMarkers()
                }
            } else if (text == "true") {
                isLoggedIn = true
                latch.countDown()
            } else if (text == "false") {
                isLoggedIn = false
                latch.countDown()
            } else if (text.contains("salt ")) {
                val res = text.split(" ")
                salt = res[1]
                latch.countDown()
            }
            else {
                toArrayList(text)
                latch.countDown()
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            println("Received bytes: $bytes")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            println("WebSocket connection closing: $code $reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            println("WebSocket connection closed: $code $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("WebSocket connection failure: ${t.message}")
            if (::latch.isInitialized) {
                latch.countDown()
            }

            val currentActivity = (applicationContext as? AwareApplication)?.getCurrentActivity()
            currentActivity?.runOnUiThread {
                val dialogBuilder = AlertDialog.Builder(currentActivity)
                dialogBuilder.setMessage("Connection failure. Retry?")
                    .setPositiveButton("Retry") { dialog, _ ->
                        // Retry logic
                        finishAffinity(currentActivity) // Close the app
                        val intent = Intent(currentActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        currentActivity.startActivity(intent) // Reopen MainActivity
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // Cancel logic
                        finishAffinity(currentActivity) // Close the app
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }

    }

    private fun startWebSocket() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://drp-aware.herokuapp.com")
            .build()
        this.webSocket = client.newWebSocket(request, handler)
        println("SOCKET CREATED")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::webSocket.isInitialized) {
            webSocket.close(1000, "Service destroyed")
        }
    }

    data class MyData(
        val id: Int,
        val title: String,
        val description: String,
        val lat: Double,
        val lng: Double, // Use 'Any' if 'lng' can be either Double or String
        val severity: Int,
        val date: String,
        val timeout: Int
    )

    fun toArrayList(s: String) {
        println(s)
        val typeToken = object : TypeToken<List<MyData>>() {}.type
        this.data = Gson().fromJson<List<MyData>>(s, typeToken)
    }
}