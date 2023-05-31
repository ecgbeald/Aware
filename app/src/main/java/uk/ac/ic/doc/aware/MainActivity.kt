package uk.ac.ic.doc.aware

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.ac.ic.doc.aware.api.ApiInterface
import uk.ac.ic.doc.aware.api.RetrofitClient
import uk.ac.ic.doc.aware.databinding.ActivityMainBinding
import uk.ac.ic.doc.aware.models.Marker
import uk.ac.ic.doc.aware.models.MarkerList

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.getButton.setOnClickListener {
            getMarkerList()
        }
        binding.addButton.setOnClickListener {
            addMarker()
        }
    }

    private fun addMarker() {
        val retrofit = RetrofitClient.getInstance()
        val apiInterface = retrofit.create(ApiInterface::class.java)
        apiInterface
            .addMarker(
                Marker(id = null, title = binding.title.text.toString(),
                    description = binding.description.text.toString(),
                    lat = binding.lat.text.toString().toDoubleOrNull(),
                    lng = binding.lng.text.toString().toDoubleOrNull(),
                    priority = binding.priority.text.toString().toIntOrNull()
                )
            )
            .enqueue(object: Callback<Marker> {
                override fun onResponse(call: Call<Marker>, response: Response<Marker>) {
                    binding.txtData.text = response.body().toString()
                }
                override fun onFailure(call: Call<Marker>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getMarkerList() {
        val retrofit = RetrofitClient.getInstance()
        val apiInterface = retrofit.create(ApiInterface::class.java)
        apiInterface.getAllMarkers().enqueue(object: Callback<MarkerList> {
            override fun onResponse(call: Call<MarkerList>, response: Response<MarkerList>) {
                val markers = response.body()?.markers
                if (markers == null || markers.size == 0)
                    binding.txtData.text = "no data"
                else
                    binding.txtData.text = markers.toString()
            }
            override fun onFailure(call: Call<MarkerList>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }
}