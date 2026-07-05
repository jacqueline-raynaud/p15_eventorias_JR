package fr.quinquenaire.p15_eventorias_jr.presentation.util

import android.net.Uri
import fr.quinquenaire.p15_eventorias_jr.BuildConfig

object MapUrlBuilder {

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/staticmap"

    fun build(
        latitude: Double,
        longitude: Double,
        zoom: Int = 15,
        widthPx: Int = 600,
        heightPx: Int = 300
    ): String {
        return Uri.parse(BASE_URL).buildUpon()
            .appendQueryParameter("center", "$latitude,$longitude")
            .appendQueryParameter("zoom", zoom.toString())
            .appendQueryParameter("size", "${widthPx}x$heightPx")
            .appendQueryParameter("markers", "color:red|$latitude,$longitude")
            .appendQueryParameter("key", BuildConfig.GOOGLE_MAPS_API_KEY)
            .build()
            .toString()
    }
}