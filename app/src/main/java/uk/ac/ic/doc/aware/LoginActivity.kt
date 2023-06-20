package uk.ac.ic.doc.aware

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import uk.ac.ic.doc.aware.clients.AwareApplication
import uk.ac.ic.doc.aware.clients.WebSocketClient
import uk.ac.ic.doc.aware.models.Request
import uk.ac.ic.doc.aware.databinding.ActivityLoginBinding
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

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
        val request = Request("create","police1",password,salt)
        val requestJson = Gson().toJson(request)
        WebSocketClient.webSocketService.webSocket.send(requestJson)
    }

    private fun sendLoginRequest(username: String, password: String): Boolean {
        val latch = CountDownLatch(1)
        WebSocketClient.webSocketService.latch = latch
        val request = Request("getsalt",username)
        val requestJson = Gson().toJson(request)
        WebSocketClient.webSocketService.webSocket.send(requestJson)
        if (!latch.await(5, TimeUnit.SECONDS)) {
            WebSocketClient.webSocketService.isLoggedIn = false
            println("Timeout")
            val currentActivity = (applicationContext as? AwareApplication)?.getCurrentActivity()
            currentActivity?.runOnUiThread {
                val dialogBuilder = AlertDialog.Builder(currentActivity)
                dialogBuilder.setMessage("Connection failure. Retry?")
                    .setPositiveButton("Retry") { dialog, _ ->
                        // Retry logic
                        ActivityCompat.finishAffinity(currentActivity) // Close the app
                        val intent = Intent(currentActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        currentActivity.startActivity(intent) // Reopen MainActivity
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // Cancel logic
                        ActivityCompat.finishAffinity(currentActivity) // Close the app
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }
        return if (WebSocketClient.webSocketService.salt == "false") {
            WebSocketClient.webSocketService.isLoggedIn = false
            WebSocketClient.webSocketService.isLoggedIn
        } else {
            val mLatch = CountDownLatch(1)
            WebSocketClient.webSocketService.latch = mLatch
            val hashedPass = hashWithSalt(password,WebSocketClient.webSocketService.salt)
            val loginRequest = Request("login",username,hashedPass)
            val loginRequestJson = Gson().toJson(loginRequest)
            WebSocketClient.webSocketService.webSocket.send(loginRequestJson)
            if (!mLatch.await(5, TimeUnit.SECONDS)) {
                WebSocketClient.webSocketService.isLoggedIn = false
                println("Timeout")
                val currentActivity = (applicationContext as? AwareApplication)?.getCurrentActivity()
                currentActivity?.runOnUiThread {
                    val dialogBuilder = AlertDialog.Builder(currentActivity)
                    dialogBuilder.setMessage("Connection failure. Retry?")
                        .setPositiveButton("Retry") { dialog, _ ->
                            // Retry logic
                            ActivityCompat.finishAffinity(currentActivity) // Close the app
                            val intent = Intent(currentActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            currentActivity.startActivity(intent) // Reopen MainActivity
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            // Cancel logic
                            ActivityCompat.finishAffinity(currentActivity) // Close the app
                        }
                        .setCancelable(false)
                        .create()
                        .show()
                }
            }
            WebSocketClient.webSocketService.isLoggedIn
        }
    }

    private fun hashWithSalt(password: String, salt: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = password + salt

        val digest = messageDigest.digest(saltedPassword.toByteArray())

        return digest.joinToString("") { "%02x".format(it) }
    }
}