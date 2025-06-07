package logic

import com.google.android.gms.maps.model.LatLng

data class GameRecord(
    val time: Long,
    val score: Int,
    val lat: Double,
    val lng: Double
) {
    fun toLatLng(): LatLng = LatLng(lat, lng)
}