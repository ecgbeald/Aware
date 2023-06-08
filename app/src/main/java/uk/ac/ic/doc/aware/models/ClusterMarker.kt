package uk.ac.ic.doc.aware.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterMarker(id: Int, lat: Double, lng: Double, title: String, snippet: String, priority: Int, timeout: Int) :
    ClusterItem {
    private val mPosition: LatLng
    private val mTitle: String = title
    private val mSnippet: String = snippet
    private val mPriority: Int = priority
    private val mId: Int = id
    private val mTimeout: Int = timeout

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

    fun getId(): Int {
        return mId
    }

    fun getTimeout(): Int {
        return mTimeout
    }

    override fun getZIndex(): Float {
        return 0f
    }

}