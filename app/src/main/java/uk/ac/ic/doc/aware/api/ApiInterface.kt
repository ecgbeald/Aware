package uk.ac.ic.doc.aware.api

import retrofit2.Response
import retrofit2.http.GET
import uk.ac.ic.doc.aware.models.MarkerList

interface ApiInterface {
    @GET("/markers")
    suspend fun getAllMarkers(): Response<MarkerList>
}