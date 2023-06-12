package uk.ac.ic.doc.aware

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContextCompat
import uk.ac.ic.doc.aware.api.Client
import uk.ac.ic.doc.aware.api.WebSocketService
import uk.ac.ic.doc.aware.databinding.ActivityLoginBinding
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var webSocketService: WebSocketService
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
            webSocketService = binder.getService()
            isServiceBound = true

            // You can now access the WebSocket service and use its methods or variables.
            // For example, you can call webSocketService.sendMessage("Hello") to send a message.
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        sendRegRequest()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener {
            val username = binding.username.text.toString()
            val result = sendLoginRequest(username, binding.txtPass.text.toString())
            if (result) {
                Toast.makeText(this@LoginActivity, "Welcome: $username", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this@LoginActivity, "Wrong username or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendRegRequest() {
        var password = "police1"
        val random = Random
        val salt = random.nextInt().toString()
        password = hashWithSalt(password, salt)
        Client.webSocket.send("create<:>police1<:>$password<:>$salt")
    }

    private fun sendLoginRequest(username: String, password: String): Boolean {
        val latch = CountDownLatch(1)
        Client.latch = latch
        Client.webSocket.send("getsalt<:>$username")
        if (!latch.await(5, TimeUnit.SECONDS)) {
            Client.isLoggedIn = false
            println("Timeout")
        }
        return if (Client.salt == "false") {
            Client.isLoggedIn = false
            Client.isLoggedIn
        } else {
            val mLatch = CountDownLatch(1)
            Client.latch = mLatch
            val hashedPass = hashWithSalt(password,Client.salt)
            Client.webSocket.send("login<:>$username<:>$hashedPass")
            if (!mLatch.await(5, TimeUnit.SECONDS)) {
                Client.isLoggedIn = false
                println("Timeout")
            }
            Client.isLoggedIn
        }
    }

    private fun hashWithSalt(password: String, salt: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = password + salt

        val digest = messageDigest.digest(saltedPassword.toByteArray())

        return digest.joinToString("") { "%02x".format(it) }
    }
}