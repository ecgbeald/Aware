package uk.ac.ic.doc.aware

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import uk.ac.ic.doc.aware.api.Client
import uk.ac.ic.doc.aware.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.map.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        binding.login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        Client.startClient()
    }
}