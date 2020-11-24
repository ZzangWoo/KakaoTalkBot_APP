package com.example.kakaotalknotification.Entity

import com.google.gson.annotations.SerializedName

data class NumberBaseballGameEntity (
    @SerializedName("NickName")
    val NickName: String,

    @SerializedName("RoomName")
    val RoomName: String
)