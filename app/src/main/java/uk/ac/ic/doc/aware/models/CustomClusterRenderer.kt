package uk.ac.ic.doc.aware.models

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CustomClusterRenderer(
    context: Context,
    map: GoogleMap, clusterManager: ClusterManager<ClusterMarker>
) : DefaultClusterRenderer<ClusterMarker>(context, map, clusterManager) {
    // useful if redesign cluster
    private val mContext: Context = context

    override fun onBeforeClusterItemRendered(marker: ClusterMarker, markerOptions: MarkerOptions) {
        // this bit sets the marker orange
        val bdf: Float = when (marker.getPriority()) {
            0 -> BitmapDescriptorFactory.HUE_ORANGE
            1 -> BitmapDescriptorFactory.HUE_AZURE
            2 -> BitmapDescriptorFactory.HUE_VIOLET
            else -> {
                BitmapDescriptorFactory.HUE_MAGENTA
            }
        }
        val markerDescriptor = BitmapDescriptorFactory.defaultMarker(bdf)
        markerOptions.icon(markerDescriptor).title(marker.title)
        markerOptions.icon(markerDescriptor).snippet(marker.snippet)
    }

//    this is for customise cluster design, not sure if useful
//    override fun onBeforeClusterRendered(
//        cluster: Cluster<ClusterMarker>,
//        markerOptions: MarkerOptions
//    ) {
//        val mClusterIconGenerator = IconGenerator(mContext.applicationContext)
//        mClusterIconGenerator.setBackground(ContextCompat.getDrawable(mContext, R.drawable.cluster))
//        mClusterIconGenerator.setTextAppearance(R.style.AppTheme_WhiteTextAppearance)
//        val icon: Bitmap = mClusterIconGenerator.makeIcon(cluster.size.toString())
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
//    }

}