package com.example.kakaotalknotification.Repository

import com.example.kakaotalknotification.Entity.ResponseEntity
import com.example.kakaotalknotification.Entity.SubscribeCommandEntity
import com.example.kakaotalknotification.Entity.SubscribeEntity
import com.example.kakaotalknotification.Entity.SubscribeFunctionEntity
import retrofit2.Call
import retrofit2.http.*

interface RequestRepo {
    @GET("/getRequest")
    fun getRequest(
        @QueryMap param: Map<String, String>
    ): Call<ResponseEntity>

    @POST("/subscribe/{path}")
    fun subscribeTest(
        @Path("path") path: String,
        @Body message: Map<String, String>
    ): Call<List<SubscribeEntity>>

    @POST("/subscribe/{path}")
    fun requestSubscribeCommand(
        @Path("path") path: String
    ): Call<List<SubscribeCommandEntity>>

    @POST("/subscribe/{path}")
    fun requestSubscribeFunction(
        @Path("path") path: String,
        @Body message: Map<String, String>
    ): Call<SubscribeFunctionEntity>
}