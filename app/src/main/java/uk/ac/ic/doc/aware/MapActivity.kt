package uk.ac.ic.doc.aware

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.ac.ic.doc.aware.api.ApiInterface
import uk.ac.ic.doc.aware.api.RetrofitClient
import uk.ac.ic.doc.aware.databinding.ActivityMapBinding
import uk.ac.ic.doc.aware.models.ClusterMarker
import uk.ac.ic.doc.aware.models.MarkerList

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager: ClusterManager<ClusterMarker>
    private lateinit var binding: ActivityMapBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpClusterer()
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpClusterer() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.487, -0.192), 12f))
        mClusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)
        getMarkers()
    }

    private fun getMarkers() {
        val retrofit = RetrofitClient.getInstance()
        val apiInterface = retrofit.create(ApiInterface::class.java)
        apiInterface.getAllMarkers().enqueue(object: Callback<MarkerList> {
            override fun onResponse(call: Call<MarkerList>, response: Response<MarkerList>) {
                val markersResp = response.body()?.markers!!
                for (marker in markersResp)
                    mClusterManager.addItem(ClusterMarker(marker.lat!!, marker.lng!!, marker.title!!, marker.description!!))
                mClusterManager.cluster()
            }
            override fun onFailure(call: Call<MarkerList>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

        })
    }

    override fun onInfoWindowClick(p0: Marker) {
        TODO("Not yet implemented")
    }
}