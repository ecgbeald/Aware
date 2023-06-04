package uk.ac.ic.doc.aware

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.ac.ic.doc.aware.api.ApiInterface
import uk.ac.ic.doc.aware.api.RetrofitClient
import uk.ac.ic.doc.aware.models.ClusterMarker
import uk.ac.ic.doc.aware.models.CustomClusterRenderer
import uk.ac.ic.doc.aware.models.CustomInfoWindow
import uk.ac.ic.doc.aware.models.Marker
import uk.ac.ic.doc.aware.models.MarkerList
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale


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
            refreshMarkers()
        }
    }

    private fun refreshMarkers() {
        mClusterManager.clearItems()
        mClusterManager.cluster()
        getMarkers()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpClusterer()
        mMap.setOnMapLongClickListener { location ->
            val newMarker = mMap.addMarker(MarkerOptions().position(location).title("New Marker"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            mMap.setOnInfoWindowClickListener {
                val alertDialogBuilder = AlertDialog.Builder(this)
                val layout = layoutInflater.inflate(R.layout.new_marker_layout, null)
                alertDialogBuilder.setView(layout)
                val timeTextBox = layout.findViewById<TextView>(R.id.dateBox)
                val sdf = SimpleDateFormat("HH:mm", Locale.UK)
                timeTextBox.text = sdf.format(Date())
                alertDialogBuilder.setTitle("New Marker")
                    .setNegativeButton("Cancel") { _, _ -> newMarker?.remove() }
                    .setPositiveButton("Post") { _, _ ->
                        val priorityString =
                            layout.findViewById<TextView>(R.id.priority).text.toString()
                        if (priorityString.isEmpty()) {
                            Toast.makeText(
                                this@MapActivity,
                                "Priority Level Empty!",
                                Toast.LENGTH_SHORT
                            ).show()
                            newMarker?.remove()
                        } else {
                            postMarker(
                                location,
                                layout.findViewById<TextView>(R.id.titleBox).text.toString(),
                                layout.findViewById<TextView>(R.id.descriptionBox).text.toString(),
                                priorityString.toInt(),
                                timeTextBox.text.toString()
                            )
                            newMarker?.remove()
                            refreshMarkers()
                        }
                    }
                    .create().show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postMarker(
        location: LatLng,
        title: String,
        description: String,
        priority: Int,
        timeStamp: String
    ) {
        val retrofit = RetrofitClient.getInstance()
        val apiInterface = retrofit.create(ApiInterface::class.java)
        // 2023-06-04T11:04:41+01:00
        // I HATE TIMEZONE LOCAL DATE SO MUCH PAIN
        val timeNow =
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).atZone(ZoneId.of("Europe/London"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val firstSplit = timeNow.split("T")
        val timeZone = firstSplit[1].split("+")[1]
        val finalTime = firstSplit[0] + "T$timeStamp:00+" + timeZone

        apiInterface.addMarker(
            Marker(
                id = null,
                title = title,
                description = description,
                priority = priority,
                date = finalTime,
                lat = location.latitude,
                lng = location.longitude
            )
        )
            .enqueue(object : Callback<Marker> {
                override fun onResponse(call: Call<Marker>, response: Response<Marker>) {
                    if (response.code() == 500) {
                        Toast.makeText(this@MapActivity, "POST request failed", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@MapActivity,
                            "Success",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }

                override fun onFailure(call: Call<Marker>, t: Throwable) {
                    Toast.makeText(this@MapActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
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