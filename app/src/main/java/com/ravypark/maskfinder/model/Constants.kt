package com.ravypark.maskfinder.model

import com.google.android.gms.maps.model.LatLng

class Constants {
    companion object {
        const val REQ_GET_CURRENT_LOCATION = 1000
        const val DEFAULT_ZOOM = 15f
        val DEFAULT_LAT_LON = LatLng(37.5640907, 126.99794029999998)
    }
}