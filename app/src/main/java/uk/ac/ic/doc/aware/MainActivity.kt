package uk.ac.ic.doc.aware

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import uk.ac.ic.doc.aware.api.ApiInterface
import uk.ac.ic.doc.aware.api.RetrofitClient
import uk.ac.ic.doc.aware.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.getButton.setOnClickListener {
            getMarkerList()
        }
    }

    private fun getMarkerList() {
        val retrofit = RetrofitClient.getInstance()
        val apiInterface = retrofit.create(ApiInterface::class.java)
        lifecycleScope.launchWhenCreated {
            try {
                val response = apiInterface.getAllMarkers()
                if (response.isSuccessful) {
                    val json = Gson().toJson(response.body())
                    if (response.body()?.markers?.size!! <= 0) {
                        Toast.makeText(
                            this@MainActivity,
                            "No Data ",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        binding.txtData.text = json
                    }

                    //new
                    /* if(response?.body()!!.support.text.contains("Harshita")){
                         Toast.makeText(
                             this@MainActivity,
                             "Hello Retrofit",
                             Toast.LENGTH_LONG
                         ).show()
                     }*/

                    // var getNEsteddata=response.body().data.get(0).suport.url

                } else {
                    Toast.makeText(
                        this@MainActivity,
                        response.errorBody().toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }catch (Ex: Exception){
                Ex.localizedMessage?.let { Log.e("Error", it) }
            }
        }

    }
}