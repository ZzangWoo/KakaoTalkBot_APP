package com.example.kakaotalknotification.Game

import android.service.notification.StatusBarNotification

abstract class Game {

    var kakaoRoom: String = "";
    var nickName: String = "";

    lateinit var noti: StatusBarNotification;

    var clientAnswer: String = "";
    var botAnswer: String = "";

}