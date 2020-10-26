package com.example.kakaotalknotification.Entity

import com.google.gson.annotations.SerializedName

data class TestEntity (

    @SerializedName("No")
    val No: Int,

    @SerializedName("Test")
    val Test: String
)
