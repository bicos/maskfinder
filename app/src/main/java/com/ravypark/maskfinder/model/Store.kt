package com.ravypark.maskfinder.model

import com.google.gson.annotations.SerializedName

/**
 * description: "재고 상태[100개 이상(녹색): 'plenty' / 30개 이상 100개미만(노랑색): 'some' / 2개 이상 30개 미만(빨강색): 'few' / 1개 이하(회색): 'empty']"
 */
data class Store(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("addr")
    val addr: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double,
    @SerializedName("stock_at")
    val stockAt: String?,
    @SerializedName("remain_stat")
    val remainStat: Stat?,
    @SerializedName("created_at")
    val createdAt: String?
)

enum class Stat {
    plenty, some, few, empty
}