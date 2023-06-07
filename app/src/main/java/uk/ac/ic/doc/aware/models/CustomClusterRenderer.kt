package uk.ac.ic.doc.aware.models

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import uk.ac.ic.doc.aware.R

class CustomClusterRenderer(
    context: Context,
    map: GoogleMap, clusterManager: ClusterManager<ClusterMarker>
) : DefaultClusterRenderer<ClusterMarker>(context, map, clusterManager) {
    // useful if redesign cluster
    private val mContext: Context = context

    override fun onBeforeClusterItemRendered(marker: ClusterMarker, markerOptions: MarkerOptions) {
        // this bit sets the marker orange
        val bdf: BitmapDescriptor = when (marker.getPriority()) {
            0 -> BitmapDescriptorFactory.fromResource(R.drawable.theft)
            1 -> BitmapDescriptorFactory.fromResource(R.drawable.anti_social)
            2 -> BitmapDescriptorFactory.fromResource(R.drawable.block)
            else -> {
                BitmapDescriptorFactory.fromResource(R.drawable.major)
            }
        }
        markerOptions.icon(bdf).title(marker.title).snippet(marker.snippet)
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