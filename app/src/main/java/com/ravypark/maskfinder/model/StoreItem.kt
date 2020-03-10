package com.ravypark.maskfinder.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class StoreItem(val store: Store) : ClusterItem {

    override fun getSnippet(): String {
        return "입고시간 : ${store.stockAt ?: "알 수 없음"}"
    }

    override fun getTitle(): String {
        return store.name
    }

    override fun getPosition(): LatLng {
        return LatLng(store.lat, store.lng)
    }
}