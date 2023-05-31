package uk.ac.ic.doc.aware.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import uk.ac.ic.doc.aware.models.Marker
import uk.ac.ic.doc.aware.models.MarkerList

interface ApiInterface {
    @GET("/markers")
    fun getAllMarkers(): Call<MarkerList>

    @POST("/markers/")
    fun addMarker(@Body markerData: Marker): Call<Marker>
}