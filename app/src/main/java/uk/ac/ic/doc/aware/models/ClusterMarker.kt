package uk.ac.ic.doc.aware.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterMarker(lat: Double, lng: Double, title: String, snippet: String, priority: Int) :
    ClusterItem {
    private val mPosition: LatLng
    private val mTitle: String = title
    private val mSnippet: String = snippet
    private val mPriority: Int = priority

    init {
        mPosition = LatLng(lat, lng)
    }

    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun getTitle(): String {
        return mTitle
    }

    override fun getSnippet(): String {
        return mSnippet
    }

    fun getPriority(): Int {
        return mPriority
    }

    override fun getZIndex(): Float {
        return 0f
    }

}