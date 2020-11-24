package com.example.kakaotalknotification.Repository

import com.example.kakaotalknotification.Entity.NumberBaseballGameEntity
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GameRepo {
    @POST("/numberBaseball")
    fun setPoint(
        @Body message: Map<String, String>
    ): Call<NumberBaseballGameEntity>
}