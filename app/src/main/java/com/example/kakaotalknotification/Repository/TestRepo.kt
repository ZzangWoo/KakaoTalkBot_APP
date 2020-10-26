package com.example.kakaotalknotification.Repository

import com.example.kakaotalknotification.Entity.TestEntity
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TestRepo {

    @GET("/Test")
    fun getTest(): Call<List<TestEntity>>

    @POST("/Chat")
    fun postTest(@Body message: Map<String, String>): Call<TestEntity>

}