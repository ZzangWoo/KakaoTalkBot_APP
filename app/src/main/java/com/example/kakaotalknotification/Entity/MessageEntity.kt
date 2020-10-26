package com.example.kakaotalknotification.Entity

import android.app.Notification
import com.example.kakaotalknotification.Repository.TestRepo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MessageEntity (
    var actions: Array<Notification.Action>,
    var from: String,
    var text: String,
    var type: String
) {}