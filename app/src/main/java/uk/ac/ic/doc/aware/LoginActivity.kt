package uk.ac.ic.doc.aware

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import uk.ac.ic.doc.aware.api.Client
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