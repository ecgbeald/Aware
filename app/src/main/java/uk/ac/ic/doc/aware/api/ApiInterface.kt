package uk.ac.ic.doc.aware.api

import retrofit2.Call
import retrofit2.http.GET
import uk.ac.ic.doc.aware.models.MarkerList

interface ApiInterface {
    @GET("/markers")
    fun getAllMarkers(): Call<MarkerList>
}