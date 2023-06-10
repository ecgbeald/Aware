package uk.ac.ic.doc.aware

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import uk.ac.ic.doc.aware.api.Client
import uk.ac.ic.doc.aware.databinding.ActivityLoginBinding
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
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
            }
        }
    }

//    private fun sendRegRequest() {
//        val password = "police1"
//        Client.webSocket.send("create<:>police1<:>$password")
//    }

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