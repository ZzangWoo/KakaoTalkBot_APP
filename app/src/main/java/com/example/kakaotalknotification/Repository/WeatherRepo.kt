package com.example.kakaotalknotification.Repository

import com.example.kakaotalknotification.Entity.WeatherEntity
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherRepo {
    @GET("/weather")
    fun requestWeather(
        @Query("city") city: String,
        @Query("gu") gu: String
    ): Call<WeatherEntity>
}