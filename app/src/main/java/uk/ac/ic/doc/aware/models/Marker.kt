package uk.ac.ic.doc.aware.models

import com.google.gson.annotations.SerializedName

data class Marker(
    @SerializedName("id"          ) var id          : Int?    = null,
    @SerializedName("title"       ) var title       : String? = null,
    @SerializedName("description" ) var description : String? = null,
    @SerializedName("lat"         ) var lat         : Double? = null,
    @SerializedName("lng"         ) var lng         : Double? = null,
    @SerializedName("priority"    ) var priority    : Int?    = null,
    @SerializedName("date"    )     var date        : String? = null
)
