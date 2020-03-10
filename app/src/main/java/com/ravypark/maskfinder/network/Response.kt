package com.ravypark.maskfinder.network

import com.google.gson.annotations.SerializedName
import com.ravypark.maskfinder.model.Store

data class Response(
    @SerializedName("count")
    val count: Int,
    @SerializedName("stores")
    val stores: List<Store>
)