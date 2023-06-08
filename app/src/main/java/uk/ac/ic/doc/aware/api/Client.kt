package uk.ac.ic.doc.aware.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okio.ByteString
import uk.ac.ic.doc.aware.MapActivity
import java.util.concurrent.CountDownLatch

object Client : WebSocketListener() {
    lateinit var latch: CountDownLatch
    lateinit var webSocket: WebSocket
    lateinit var data: List<MyData>
    lateinit var mapActivity: MapActivity

    fun isDataInitialized() = ::data.isInitialized

    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("WebSocket connection opened")
        // Send a message to the server
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Received message: $text")
        if (text.equals("refresh")) {
            mapActivity.runOnUiThread {
                mapActivity.refreshMarkers()
            }
        } else {
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
        latch.countDown()
    }

    fun startClient() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://drp-aware.herokuapp.com")
            .build()
        webSocket = client.newWebSocket(request, Client)


        // To gracefully close the WebSocket connection, uncomment the following line
        // webSocket.close(1000, "Goodbye, server!")}
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
        val typeToken = object : TypeToken<List<MyData>>() {}.type
        data = Gson().fromJson<List<MyData>>(s, typeToken)
    }

}