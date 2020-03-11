package com.ravypark.maskfinder.model

import com.google.gson.annotations.SerializedName

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

enum class Stat(val msg: String) {
    @SerializedName("plenty")
    Plenty("충분"),

    @SerializedName("some")
    Some("보통"),

    @SerializedName("few")
    Few("부족"),

    @SerializedName("empty")
    Empty("없음")
}