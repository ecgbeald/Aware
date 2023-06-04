package uk.ac.ic.doc.aware

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.ac.ic.doc.aware.api.ApiInterface
import uk.ac.ic.doc.aware.api.RetrofitClient
import uk.ac.ic.doc.aware.models.ClusterMarker
import uk.ac.ic.doc.aware.models.CustomClusterRenderer
import uk.ac.ic.doc.aware.models.CustomInfoWindow
import uk.ac.ic.doc.aware.models.MarkerList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager: ClusterManager<ClusterMarker>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)
        findViewById<ImageButton>(R.id.refresh_button).setOnClickListener {
            Toast.makeText(this@MapActivity, "refreshing...", Toast.LENGTH_SHORT).show()
            mClusterManager.clearItems()
            mClusterManager.cluster()
            getMarkers()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpClusterer()
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpClusterer() {
        // set to earls court
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.487, -0.192), 12f))
        mClusterManager = ClusterManager(this, mMap)
        mClusterManager.renderer = CustomClusterRenderer(this, mMap, mClusterManager)
        mClusterManager.markerCollection
            .setInfoWindowAdapter(CustomInfoWindow(LayoutInflater.from(this)))
        mClusterManager.setOnClusterClickListener {
            // click on a cluster of markers (not sure if it's useful
            Toast.makeText(this@MapActivity, "Cluster click", Toast.LENGTH_SHORT).show()
            // if true, do not move camera
            false
        }
        mClusterManager.setOnClusterItemClickListener {
            Toast.makeText(this@MapActivity, "Cluster item click", Toast.LENGTH_SHORT).show()
            // if true, click handling stops here and do not show info view, do not move camera
            // you can avoid this by calling:
            // renderer.getMarker(clusterItem).showInfoWindow();
            false
        }
        mClusterManager.setOnClusterItemInfoWindowClickListener { stringClusterItem ->
            // TODO: maybe add a new popup for details
            Toast.makeText(
                this@MapActivity, "Clicked info window: " + stringClusterItem.title,
                Toast.LENGTH_SHORT
            ).show()
        }
        mMap.setInfoWindowAdapter(mClusterManager.markerManager)
        mMap.setOnInfoWindowClickListener(mClusterManager)
        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)
        getMarkers()
    }

    private fun getMarkers() {
        val retrofit = RetrofitClient.getInstance()
        val apiInterface = retrofit.create(ApiInterface::class.java)
        apiInterface.getAllMarkers().enqueue(object : Callback<MarkerList> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<MarkerList>, response: Response<MarkerList>) {
                val markersResp = response.body()?.markers!!
                for (marker in markersResp) {
                    val current = LocalDateTime.now()
                    val convertDate =
                        LocalDateTime.parse(marker.date!!, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    val minutes = ChronoUnit.MINUTES.between(convertDate, current)
                    // TODO: need to redesign info window
                    val description = marker.description!! + "\nAdded: " + minutes + " minutes ago"
                    println(description)
                    mClusterManager.addItem(
                        ClusterMarker(
                            marker.lat!!,
                            marker.lng!!,
                            marker.title!!,
                            description
                        )
                    )
                }
                mClusterManager.cluster()
            }

            override fun onFailure(call: Call<MarkerList>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun debugAddItems() {
        // only call this after cluster manager is initialised!
        mClusterManager.addItem(ClusterMarker(51.490, -0.196, "h1", "idk"))
        mClusterManager.addItem(ClusterMarker(51.491, -0.196, "h1", "idk"))
        mClusterManager.addItem(ClusterMarker(51.492, -0.196, "h1", "idk"))
        mClusterManager.addItem(ClusterMarker(51.493, -0.196, "h1", "idk"))
        mClusterManager.addItem(ClusterMarker(51.494, -0.196, "h1", "idk"))
        mClusterManager.addItem(ClusterMarker(51.495, -0.196, "h1", "idk"))
    }
}