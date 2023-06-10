package uk.ac.ic.doc.aware

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import uk.ac.ic.doc.aware.api.Client
import uk.ac.ic.doc.aware.databinding.ActivityLoginBinding
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnLogin.setOnClickListener {
            val result = sendLoginRequest(binding.username.text.toString(), hash(binding.txtPass.text.toString()))
            Toast.makeText(this@LoginActivity, result.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun hash(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "02x".format(it) }
    }

    private fun sendRegRequest() {
        val password = hash("amongus")
        Client.webSocket.send("create<:>amongus<:>$password")
    }

    private fun sendLoginRequest(username: String, password: String): Boolean {
        val latch = CountDownLatch(1)
        Client.latch = latch
        Client.webSocket.send("login<:>$username<:>$password")
        if (!latch.await(5, TimeUnit.SECONDS)) {
            println("Timeout")
        }
        return Client.isLoggedIn
    }
}