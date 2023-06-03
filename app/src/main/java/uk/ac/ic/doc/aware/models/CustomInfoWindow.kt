package uk.ac.ic.doc.aware.models

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import uk.ac.ic.doc.aware.R

class CustomInfoWindow(inflater: LayoutInflater) : GoogleMap.InfoWindowAdapter {
    private val mInflater = inflater

    @SuppressLint("InflateParams")
    override fun getInfoContents(marker: Marker): View? {
        val popup = mInflater.inflate(R.layout.info_window, null)
        popup.findViewById<TextView>(R.id.title).text = marker.title
        popup.findViewById<TextView>(R.id.snippet).text = marker.snippet
        return popup
    }

    @SuppressLint("InflateParams")
    override fun getInfoWindow(marker: Marker): View {
        val popup = mInflater.inflate(R.layout.info_window, null)
        popup.findViewById<TextView>(R.id.title).text = marker.title
        popup.findViewById<TextView>(R.id.snippet).text = marker.snippet
        return popup
    }

}