package com.example.kakaotalknotification.Entity

import com.google.gson.annotations.SerializedName

data class ResponseEntity (
    @SerializedName("Message")
    val Message: String
)