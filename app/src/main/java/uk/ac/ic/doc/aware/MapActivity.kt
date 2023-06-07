package uk.ac.ic.doc.aware

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.android.clustering.ClusterManager
import uk.ac.ic.doc.aware.api.Client
import uk.ac.ic.doc.aware.models.ClusterMarker
import uk.ac.ic.doc.aware.models.CustomClusterRenderer
import uk.ac.ic.doc.aware.models.CustomInfoWindow
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager: ClusterManager<ClusterMarker>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Client.mapActivity = this
        setContentView(R.layout.activity_map)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDvDlocXrkrA5O7iHdN2JCrd2ynwZQXz0Y")
        }
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setCountries("UK")
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Toast.makeText(this@MapActivity, "Search Bar error", Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng!!, 15f))
            }
        })
        val mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)
        findViewById<ImageButton>(R.id.refresh_button).setOnClickListener {
            Toast.makeText(this@MapActivity, "refreshing...", Toast.LENGTH_SHORT).show()
            refreshMarkers()
        }
    }

    fun refreshMarkers() {
        mClusterManager.clearItems()
        mClusterManager.cluster()
        getMarkers()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpClusterer()
        var lastMarker: com.google.android.gms.maps.model.Marker? = null
        mMap.setOnMapLongClickListener { location ->
            lastMarker?.remove()
            val newMarker = mMap.addMarker(MarkerOptions().position(location).title("New Marker"))
            lastMarker = newMarker
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            mMap.setOnInfoWindowClickListener {
                val alertDialogBuilder = AlertDialog.Builder(this)
                val layout = layoutInflater.inflate(R.layout.new_marker_layout, null)
                alertDialogBuilder.setView(layout)
                val timeTextBox = layout.findViewById<Button>(R.id.dateBox)
                val sdf = SimpleDateFormat("HH:mm", Locale.UK)
                timeTextBox.text = sdf.format(Date())
                timeTextBox.setOnClickListener { popUpTimePicker(timeTextBox) }
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
                mMap.setOnInfoWindowClickListener(null)
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
        // 2023-06-04T11:04:41+01:00
        // I HATE TIMEZONE LOCAL DATE SO MUCH PAIN
        val timeNow =
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).atZone(ZoneId.of("Europe/London"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val firstSplit = timeNow.split("T")
        val timeZone = firstSplit[1].split("+")[1]
        val finalTime = firstSplit[0] + "T$timeStamp:00+" + timeZone
        Client.webSocket.send("add<:>" + title + "<:>" + description + "<:>" + location.latitude.toString() + "<:>" + location.longitude.toString() + "<:>" + priority.toString() + "<:>" + finalTime)

    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpClusterer() {
        // set to earls court
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.487, -0.192), 12f))
        mClusterManager = ClusterManager(this, mMap)
        mClusterManager.renderer = CustomClusterRenderer(this, mMap, mClusterManager)
        mClusterManager.markerCollection
            .setInfoWindowAdapter(CustomInfoWindow(LayoutInflater.from(this)))
//        mClusterManager.setOnClusterClickListener {
//             click on a cluster of markers (not sure if it's useful
//            Toast.makeText(this@MapActivity, "Cluster click", Toast.LENGTH_SHORT).show()
//             if true, do not move camera
//            false
//        }
        mClusterManager.setOnClusterItemClickListener {
            Toast.makeText(
                this@MapActivity,
                "Cluster item ${it.getId()} clicked",
                Toast.LENGTH_SHORT
            ).show()
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

    @SuppressLint("CheckResult")
    private fun getMarkers() {
        val latch = CountDownLatch(1)
        Client.latch = latch
        Client.webSocket.send("get")
        if (!latch.await(5, TimeUnit.SECONDS)) {
            println("Timeout")
        }
        for (marker in Client.data) {
            val currentTime = LocalDateTime.now()
            val convertDate =
                LocalDateTime.parse(marker.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val minuteDifference = ChronoUnit.MINUTES.between(convertDate, currentTime)
            val description = marker.description + "\nAdded: $minuteDifference minutes ago."
            mClusterManager.addItem(
                ClusterMarker(
                    marker.id,
                    marker.lat,
                    marker.lng,
                    marker.title,
                    description,
                    marker.severity
                )
            )
        }
        mClusterManager.cluster()
    }

    private fun delete(id: Int) {
        Client.webSocket.send("delete<:>" + id.toString())
    }

    // for the cool clock widget thingy
    private fun popUpTimePicker(timeButton: Button) {
        val cal = Calendar.getInstance()
        var mHour = cal.get(Calendar.HOUR_OF_DAY)
        var mMinute = cal.get(Calendar.MINUTE)
        val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            mHour = hour
            mMinute = minute
            timeButton.text = String.format(Locale.UK, "%02d:%02d", mHour, mMinute)
        }
        val timePickerDialog =
            TimePickerDialog(this@MapActivity, onTimeSetListener, mHour, mMinute, true)
        timePickerDialog.show()
    }

//    private fun debugAddItems() {
//        // only call this after cluster manager is initialised!
//        mClusterManager.addItem(ClusterMarker(51.490, -0.196, "h1", "idk", 0))
//        mClusterManager.addItem(ClusterMarker(51.491, -0.196, "h1", "idk", 1))
//        mClusterManager.addItem(ClusterMarker(51.492, -0.196, "h1", "idk", 2))
//        mClusterManager.addItem(ClusterMarker(51.493, -0.196, "h1", "idk", 0))
//        mClusterManager.addItem(ClusterMarker(51.494, -0.196, "h1", "idk", 1))
//        mClusterManager.addItem(ClusterMarker(51.495, -0.196, "h1", "idk", 2))
//    }
}