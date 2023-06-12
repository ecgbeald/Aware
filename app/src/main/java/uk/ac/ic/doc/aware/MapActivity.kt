package uk.ac.ic.doc.aware

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.maps.android.clustering.ClusterManager
import uk.ac.ic.doc.aware.api.Client
import uk.ac.ic.doc.aware.api.NewClient
import uk.ac.ic.doc.aware.api.WebSocketService
import uk.ac.ic.doc.aware.models.ClusterMarker
import uk.ac.ic.doc.aware.models.CustomClusterRenderer
import uk.ac.ic.doc.aware.models.CustomInfoWindow
import uk.ac.ic.doc.aware.models.PermissionUtils
import uk.ac.ic.doc.aware.models.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
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


class MapActivity : AppCompatActivity(), OnMapReadyCallback, OnRequestPermissionsResultCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager: ClusterManager<ClusterMarker>
    private var permissionDenied = false

    private val london = LatLngBounds(LatLng(51.463758, -0.237632), LatLng(51.5478144, -0.0527049))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NewClient.webSocketService.mapActivity = this
        setContentView(R.layout.activity_map)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDvDlocXrkrA5O7iHdN2JCrd2ynwZQXz0Y")
        }
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setCountries("UK")
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )
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
        findViewById<ImageButton>(R.id.filter_button).setOnClickListener {
            val listItems = arrayOf("Theft", "Anti Social", "Road Closure", "Major Incident")
            val checkedItems = BooleanArray(listItems.size)
            val selectedItems = mutableListOf<Int>()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Filter Events")
            builder.setMultiChoiceItems(listItems, checkedItems) { dialog, which, isChecked ->
                checkedItems[which] = isChecked
            }
            builder.setCancelable(false)
            builder.setPositiveButton("OK") { dialog, which ->
                for (i in checkedItems.indices) {
                    if (checkedItems[i]) {
                        selectedItems += i
                    }
                }
                println(selectedItems)
            }
            // use this to return to normal not filtered scenario
            builder.setNeutralButton("UNSET") { dialog, which ->

            }
            // abort action, not sending anything to backend
            builder.setNegativeButton("CANCEL") {_, _ -> }
            Toast.makeText(this@MapActivity, "clicked", Toast.LENGTH_SHORT).show()
            val alertDialog = builder.create()
            alertDialog.show()

        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ||
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(LOCATION_PERMISSION_REQUEST_CODE, true)
                .show(supportFragmentManager, "dialog")
            return
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (PermissionUtils.isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ||
            PermissionUtils.isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            enableMyLocation()
        } else {
            permissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
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
        enableMyLocation()
        mMap.setLatLngBoundsForCameraTarget(london)
        setUpClusterer()
        var lastMarker: Marker? = null
        if (NewClient.webSocketService.isLoggedIn) {
            mMap.setOnMapLongClickListener { location ->
                lastMarker?.remove()
                val newMarker =
                    mMap.addMarker(MarkerOptions().position(location).title("New Marker"))
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
                    val severitySpinner: Spinner = layout.findViewById(R.id.severity)
                    val timeSpinner: Spinner = layout.findViewById(R.id.timeSpinner)
                    var severity = 0
                    // based on minutes
                    var timeUnit = 1
                    severitySpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                severity = position
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                    timeSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                timeUnit = when (position) {
                                    0 -> 1
                                    1 -> 60
                                    2 -> 1440
                                    else -> {
                                        1
                                    }
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }

                        }
                    ArrayAdapter.createFromResource(
                        this,
                        R.array.event_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        severitySpinner.adapter = adapter
                    }
                    ArrayAdapter.createFromResource(
                        this,
                        R.array.time,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        timeSpinner.adapter = adapter
                    }
                    alertDialogBuilder.setTitle("New Marker")
                        .setNegativeButton("Cancel") { _, _ -> }
                        .setPositiveButton("Post") { _, _ ->
                            postMarker(
                                location,
                                layout.findViewById<TextView>(R.id.titleBox).text.toString(),
                                layout.findViewById<TextView>(R.id.descriptionBox).text.toString(),
                                severity,
                                timeTextBox.text.toString(),
                                layout.findViewById<TextView>(R.id.timeout).text.toString()
                                    .toInt() * timeUnit
                                //have unit and number textboxes for timeout, calculate number of minutes
                            )
                            refreshMarkers()
                        }
                        .create().show()
                    newMarker?.remove()
                }
            }
       }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postMarker(
        location: LatLng,
        title: String,
        description: String,
        priority: Int,
        timeStamp: String,
        timeOut: Int
    ) {
        // 2023-06-04T11:04:41+01:00
        // I HATE TIMEZONE LOCAL DATE SO MUCH PAIN
        val timeNow =
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).atZone(ZoneId.of("Europe/London"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val firstSplit = timeNow.split("T")
        val timeZone = firstSplit[1].split("+")[1]
        val finalTime = firstSplit[0] + "T$timeStamp:00+" + timeZone
        NewClient.webSocketService.webSocket.send("add<:>" + title + "<:>" + description + "<:>" + location.latitude.toString() + "<:>" + location.longitude.toString() + "<:>" + priority.toString() + "<:>" + finalTime + "<:>" + timeOut.toString())
        println(finalTime)
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpClusterer() {
        // set to earls court
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(51.487, -0.192), 12f))
        mClusterManager = ClusterManager(this, mMap)
        mClusterManager.renderer = CustomClusterRenderer(this, mMap, mClusterManager)
        mClusterManager.markerCollection
            .setInfoWindowAdapter(CustomInfoWindow(LayoutInflater.from(this)))
        mClusterManager.setOnClusterItemClickListener { item ->
            Toast.makeText(
                this@MapActivity,
                "Cluster item ${item.getId()} clicked",
                Toast.LENGTH_SHORT
            ).show()
            if (NewClient.webSocketService.isLoggedIn) {
                mMap.setOnInfoWindowClickListener {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    val layout = layoutInflater.inflate(R.layout.new_marker_layout, null)
                    alertDialogBuilder.setView(layout)
                    layout.findViewById<EditText>(R.id.titleBox).setText(item.title)
                    // remove "added ... ago" line and the timeout line
                    // ugly but it works eh
                    var snippet = item.snippet
                    if (snippet.lastIndexOf("\n") > 0) {
                        snippet = snippet.substring(0, snippet.lastIndexOf("\n"))
                    }
                    if (snippet.lastIndexOf("\n") > 0) {
                        snippet = snippet.substring(0, snippet.lastIndexOf("\n"))
                    }
                    layout.findViewById<EditText>(R.id.descriptionBox).setText(snippet)
                    layout.findViewById<EditText>(R.id.timeout)
                        .setText(item.getTimeout().toString())
                    alertDialogBuilder.setTitle("Change Marker")
                        .setNegativeButton("Delete") { _, _ -> delete(item.getId()) }
                        .setPositiveButton("Change") { _, _ ->
                            // TODO: PUT request to change content (maybe just delete is enough)
                        }
                        .create().show()
                }
            }
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

    private fun minuteDifferenceConverter(minute: Long): String {
        return if (minute in 60..1439) {
            (minute / 60).toString() + " hours"
        } else if (minute >= 1440) {
            (minute / 1440).toString() + " days"
        } else {
            "$minute minutes"
        }
    }

    @SuppressLint("CheckResult")
    private fun getMarkers() {
        val latch = CountDownLatch(1)
        NewClient.webSocketService.latch = latch
        NewClient.webSocketService.webSocket.send("get")
        if (!latch.await(5, TimeUnit.SECONDS)) {
            println("Timeout")
        }
        for (marker in NewClient.webSocketService.data) {
            val currentTime = LocalDateTime.now()
            val convertDate =
                LocalDateTime.parse(marker.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val minuteDifference = ChronoUnit.MINUTES.between(convertDate, currentTime)
            val description =
                marker.description + "\nAdded ${minuteDifferenceConverter(minuteDifference)} ago\nExpiring in ${
                    minuteDifferenceConverter(marker.timeout.toLong())
                }"
            mClusterManager.addItem(
                ClusterMarker(
                    marker.id,
                    marker.lat,
                    marker.lng,
                    marker.title,
                    description,
                    marker.severity,
                    marker.timeout
                )
            )
        }
        mClusterManager.cluster()
    }

    private fun delete(id: Int) {
        NewClient.webSocketService.webSocket.send("delete<:>$id")
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
}