package com.example.kakaotalknotification.Entity

import com.google.gson.annotations.SerializedName

data class SubscribeCommandEntity (

    @SerializedName("Function_Eng")
    val Function_Eng: String,

    @SerializedName("Function_Kor")
    val Function_Kor: String

)