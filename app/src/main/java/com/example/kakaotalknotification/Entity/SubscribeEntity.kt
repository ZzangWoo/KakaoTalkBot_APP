package com.example.kakaotalknotification.Entity

import com.google.gson.annotations.SerializedName

data class SubscribeEntity (

    @SerializedName("Function")
    val Function: String,

    @SerializedName("UserList")
    val UserList: String

)
