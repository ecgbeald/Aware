package uk.ac.ic.doc.aware.models

import com.google.gson.annotations.SerializedName

data class MarkerList(
    @SerializedName("markers")
    var markers: ArrayList<Marker> = arrayListOf()
)
