package com.example.kakaotalknotification.Service

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.kakaotalknotification.Entity.*
import com.example.kakaotalknotification.R
import com.example.kakaotalknotification.Repository.RequestRepo
import com.example.kakaotalknotification.Repository.TestRepo
import com.example.kakaotalknotification.Repository.WeatherRepo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class KakaoTalkNotificationListenerService: NotificationListenerService() {

    companion object {
        // 둥봇 구독자 정보 저장 변수
        var members: MutableMap<String, StatusBarNotification> = mutableMapOf<String, StatusBarNotification>()

        // 둥봇 구독 (단톡방)
        var subscribeRooms: MutableMap<CharSequence, StatusBarNotification> = mutableMapOf()

        // 둥봇 admin 정보 저장 변수
        var admin: MutableMap<String, StatusBarNotification> = mutableMapOf()

        // 구독기능 매칭 ( <"영어", "한글"> )
        var subscribeCommand: MutableList<String> = mutableListOf()
    }

    /************************* 게임 카운트를 위한 변수 ****************************/
    private var gameTime1 = 0
    private var isGameRunning1 = false
    private var gameTimerTask1: Timer?=null
    private var gamePlayNoti1: MutableMap<String, StatusBarNotification> = mutableMapOf()

    private var gameTime2 = 0
    private var isGameRunning2 = false
    private var gameTimerTask2: Timer?=null
    private var gamePlayNoti2: MutableMap<String, StatusBarNotification> = mutableMapOf()
    /****************************************************************************/

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.e("Listen", "KakaoTalkNotificationListener is Connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.e("Listen", "KakaoTalkNotificationListener is Disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) {
            return
        }

        val mNotification: Notification = sbn.notification
        val extras: Bundle = mNotification.extras

        if (sbn != null && sbn.packageName.equals("com.kakao.talk")) {
            val from: String? = extras.getString(Notification.EXTRA_TITLE)
            val text: String? = extras.getString(Notification.EXTRA_TEXT)
            val kakaoRoom = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)


            if (from != null && text != null) {
                Log.e("Listener", "Who : ${from} | Contents : ${text}\nRoom : ${kakaoRoom}")

                 val actions = sbn.notification.actions

                // POST 방식 테스트
                if (text!!.startsWith("둥봇")) {
//                    val messageEntity = MessageEntity(actions, from, text, "Test")
//                    sendMessage(messageEntity)
                }
                // GET 방식 테스트
                else if (text!!.startsWith("@게시글")) {

                    for (act in actions) {
                        if (act != null && act.allowGeneratedReplies && act.actionIntent != null) {
                            val pendingIntent = act.actionIntent
                            val replyIntent = Intent()
                            val replyBundle = Bundle()

                            try {
                                if (act.remoteInputs != null) {
                                    val remoteInputs = act.remoteInputs

                                    var botMessage: String = "DB 정보 : \n"

                                    // Chat API 호출
                                    val builder = Retrofit.Builder()
                                        .baseUrl("http://doonge.synology.me:2697")
                                        .addConverterFactory(GsonConverterFactory.create())

                                    val retrofit: Retrofit = builder.build()

                                    val repo: TestRepo = retrofit.create(TestRepo::class.java)

                                    val call: Call<List<TestEntity>> = repo.getTest()
                                    call.enqueue(object: Callback<List<TestEntity>> {
                                        override fun onFailure(
                                            call: Call<List<TestEntity>>,
                                            t: Throwable
                                        ) {
                                            Log.e("Listener", "GET 방식 실패 : " + t)
                                        }

                                        override fun onResponse(
                                            call: Call<List<TestEntity>>,
                                            response: Response<List<TestEntity>>
                                        ) {
                                            Log.e("Listener", "[GET] API Server 통신 성공")

                                            val apiResult: List<TestEntity>? = response.body()

                                            apiResult?.forEach{it ->
                                                botMessage += "${it.No} : ${it.Test}\n"
                                            }

                                            Log.e("Listener", "받은 메세지 : " + botMessage)

                                            for (inputs in remoteInputs) {
                                                replyBundle.putCharSequence(inputs.resultKey, botMessage)
                                            }
                                            RemoteInput.addResultsToIntent(remoteInputs, replyIntent, replyBundle)

                                            act.actionIntent.send(applicationContext, 0, replyIntent)
                                        }

                                    })
                                }
                            } catch (e: Exception) {
                                Log.e("Listener", "오류발생 오류발생!!\n" + e)
                            }

                        }
                    }
                }

                else if (text!!.startsWith("/테스트")) {
                        if (from != "조창우") {
                            return
                        }

                        var param = mutableMapOf<String, String>(
                            "From" to from
                        )
//                        var param = mutableMapOf<String, String>(
//                            "command" to "테스트",
//                            "from" to from,
//                            "param1" to "",
//                            "param2" to "",
//                            "param3" to ""
//                        )

                        val builder = Retrofit.Builder()
                            .baseUrl("http://doonge.synology.me:2697")
                            .addConverterFactory(GsonConverterFactory.create())

                        val retrofit: Retrofit = builder.build()

                        val repo = retrofit.create(RequestRepo::class.java)
                        val call = repo.subscribeTest("list", param)

                        call.enqueue(object: Callback<List<SubscribeEntity>> {
                            override fun onFailure(
                                call: Call<List<SubscribeEntity>>,
                                t: Throwable
                            ) {
                                Log.e("Listener", "API GET 방식 통신 실패 : " + t)
                            }

                            override fun onResponse(
                                call: Call<List<SubscribeEntity>>,
                                response: Response<List<SubscribeEntity>>
                            ) {
                                val apiResult: List<SubscribeEntity>? = response.body()

                                for (i in 0..apiResult!!.size - 1) {
                                    val function = apiResult!![i].Function
                                    val userList = apiResult!![i].UserList.split(',')

                                    if (userList.size != 0 && subscribeCommand.size != 0) {
                                        for (j in 0.. userList.size - 1) {
                                            if (members.containsKey(userList[j])) {
                                                param["From"] = userList[j]
                                                val subscribeCall = repo.requestSubscribeFunction(subscribeCommand[i], param)

                                                subscribeCall.enqueue(object: Callback<SubscribeFunctionEntity> {
                                                    override fun onFailure(
                                                        call: Call<SubscribeFunctionEntity>,
                                                        t: Throwable
                                                    ) {
                                                        Log.e("Listener", "API GET 방식 통신 실패 : " + t)
                                                    }

                                                    override fun onResponse(
                                                        call: Call<SubscribeFunctionEntity>,
                                                        response: Response<SubscribeFunctionEntity>
                                                    ) {
                                                        val apiResult = response.body()
                                                        val message = apiResult?.Message

                                                        val actions = members[userList[j]]!!.notification.actions

                                                        for (act in actions) {
                                                            if (act != null && act.allowGeneratedReplies && act.actionIntent != null) {
                                                                val replyIntent = Intent()
                                                                val replyBundle = Bundle()

                                                                try {
                                                                    if (act.remoteInputs != null) {
                                                                        var remoteInputs = act.remoteInputs

                                                                        for (inputs in remoteInputs) {
                                                                            replyBundle.putCharSequence(
                                                                                inputs.resultKey,
                                                                                message
                                                                            )
                                                                        }
                                                                        RemoteInput.addResultsToIntent(
                                                                            remoteInputs,
                                                                            replyIntent,
                                                                            replyBundle
                                                                        )

                                                                        act.actionIntent.send(
                                                                            applicationContext,
                                                                            0,
                                                                            replyIntent
                                                                        )
                                                                    }
                                                                } catch (e: Exception) {
                                                                    Log.e("Listener", "오류발생 오류발생!!\n" + e)
                                                                }
                                                            }
                                                        }
                                                    }

                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        })
//                    }
                }
                else if (text!!.startsWith("/구독리스트갱신")) {
                    val builder = Retrofit.Builder()
                        .baseUrl("http://doonge.synology.me:2697")
                        .addConverterFactory(GsonConverterFactory.create())

                    val retrofit: Retrofit = builder.build()

                    val repo = retrofit.create(RequestRepo::class.java)
                    val call = repo.requestSubscribeCommand("subscribeList")

                    call.enqueue(object: Callback<List<SubscribeCommandEntity>> {
                        override fun onFailure(
                            call: Call<List<SubscribeCommandEntity>>,
                            t: Throwable
                        ) {
                            Log.e("Listener", "API GET 방식 통신 실패 : " + t)
                        }

                        override fun onResponse(
                            call: Call<List<SubscribeCommandEntity>>,
                            response: Response<List<SubscribeCommandEntity>>
                        ) {
                            val apiResult: List<SubscribeCommandEntity>? = response.body()

                            for (i in 0..apiResult!!.size - 1) {
                                subscribeCommand.add(apiResult[i].Function_Eng)
                            }

                            var message = "구독 리스트 갱신 완료!!"

                            val actions = sbn.notification.actions

                            for (act in actions) {
                                if (act != null && act.allowGeneratedReplies && act.actionIntent != null) {
                                    val replyIntent = Intent()
                                    val replyBundle = Bundle()

                                    try {
                                        if (act.remoteInputs != null) {
                                            var remoteInputs = act.remoteInputs

                                            for (inputs in remoteInputs) {
                                                replyBundle.putCharSequence(
                                                    inputs.resultKey,
                                                    message
                                                )
                                            }
                                            RemoteInput.addResultsToIntent(
                                                remoteInputs,
                                                replyIntent,
                                                replyBundle
                                            )

                                            act.actionIntent.send(
                                                applicationContext,
                                                0,
                                                replyIntent
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Listener", "오류발생 오류발생!!\n" + e)
                                    }
                                }
                            }
                        }


                    })
                }
                else if (text!!.startsWith("/콜")) {
                    for (testSbn in members) {
                        val userName = testSbn.key
                        val actions = testSbn.value.notification.actions

                        for (act in actions) {
                            if (act != null && act.allowGeneratedReplies && act.actionIntent != null) {
                                val replyIntent = Intent()
                                val replyBundle = Bundle()

                                try {
                                    if (act.remoteInputs != null) {
                                        var remoteInputs = act.remoteInputs

                                        var message: String = "구독자에게 메세지 보내기 테스트\n"
                                        message += "${userName}님 반가워요"

                                        for (inputs in remoteInputs) {
                                            replyBundle.putCharSequence(inputs.resultKey, message)
                                        }
                                        RemoteInput.addResultsToIntent(remoteInputs, replyIntent, replyBundle)

                                        act.actionIntent.send(applicationContext, 0, replyIntent)
                                    }
                                } catch (e: Exception) {
                                    Log.e("Listener", "오류발생 오류발생!!\n" + e)
                                }
                            }
                        }
                    }
                }
                else if (text!!.startsWith("/로또추첨")) {
                    try {
                        val random = Random()
                        var lotto: MutableList<Int> = mutableListOf()
                        var num = 0

                        while (true) {
                            if (lotto.size == 6) {
                                break;
                            }

                            num = random.nextInt(45) + 1

                            if (!lotto.contains(num)) {
                                lotto.add(num)
                            }
                        }

                        lotto.sort()

                        var splitCommand = text.split(' ')

                        var param = mutableMapOf<String, String>(
                            "command" to splitCommand[0].substringAfter('/'),
                            "from" to from,
                            "room" to if(kakaoRoom == null) "null" else kakaoRoom.toString(),
                            "param1" to "",
                            "param2" to "",
                            "param3" to ""
                        )

                        for (i in 1..splitCommand.size - 1) {
                            param["param${i}"] = splitCommand[i]
                        }

                        param["param1"] = lotto.toString()

                        for (act in actions) {
                            if (act != null && act.allowGeneratedReplies && act.actionIntent != null) {
                                val replyIntent = Intent()
                                val replyBundle = Bundle()

                                try {
                                    if (act.remoteInputs != null) {
                                        val remoteInputs = act.remoteInputs

                                        // GET 방식 API 호출
                                        val builder = Retrofit.Builder()
                                            .baseUrl(getString(R.string.API_Server_URL))
                                            .addConverterFactory(GsonConverterFactory.create())

                                        val retrofit: Retrofit = builder.build()

                                        val repo = retrofit.create(RequestRepo::class.java)
                                        val call = repo.getRequest(param)

                                        call.enqueue(object: Callback<ResponseEntity> {
                                            override fun onFailure(
                                                call: Call<ResponseEntity>,
                                                t: Throwable
                                            ) {
                                                Log.e("Listener", "API GET 방식 통신 실패 : " + t)
                                            }

                                            override fun onResponse(
                                                call: Call<ResponseEntity>,
                                                response: Response<ResponseEntity>
                                            ) {
                                                val apiResult: ResponseEntity? = response.body()
                                                val message = apiResult?.Message

                                                for (inputs in remoteInputs) {
                                                    replyBundle.putCharSequence(inputs.resultKey, message)
                                                }
                                                RemoteInput.addResultsToIntent(remoteInputs, replyIntent, replyBundle)

                                                act.actionIntent.send(applicationContext, 0, replyIntent)
                                            }

                                        })
                                    }
                                } catch (e: Exception) {
                                    Log.e("Listener", "오류발생 오류발생!!\n" + e)
                                }
                            }
                        }

                    } catch (ex:Exception) {
                        Log.e("Listener", "오류발생 오류발생!!\n" + ex)
                    }
                }
                else if (text!!.startsWith("/우리만난지")) {
                    try {
                        // 기념일 계산
                        val startMeetDate = "2016-11-18 00:00:00"
                        val sf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val date = sf.parse(startMeetDate)

                        var today = Calendar.getInstance()
                        var dateDifference = (today.time.time - date.time) / (60 * 60 * 24 * 1000) + 1

                        var splitCommand = text.split(' ')

                        var param = mutableMapOf<String, String>(
                            "command" to splitCommand[0].substringAfter('/'),
                            "from" to from,
                            "room" to if(kakaoRoom == null) "null" else kakaoRoom.toString(),
                            "param1" to "",
                            "param2" to "",
                            "param3" to ""
                        )

                        for (i in 1..splitCommand.size - 1) {
                            param["param${i}"] = splitCommand[i]
                        }

                        param["param1"] = dateDifference.toString()

                        for (act in actions) {
                            if (act != null && act.allowGeneratedReplies && act.actionIntent != null) {
                                val replyIntent = Intent()
                                val replyBundle = Bundle()

                                try {
                                    if (act.remoteInputs != null) {
                                        val remoteInputs = act.remoteInputs
                                        2
                                        // GET 방식 API 호출
                                        val builder = Retrofit.Builder()
                                            .baseUrl(getString(R.string.API_Server_URL))
                                            .addConverterFactory(GsonConverterFactory.create())

                                        val retrofit: Retrofit = builder.build()

                                        val repo = retrofit.create(RequestRepo::class.java)
                                        val call = repo.getRequest(param)

                                        call.enqueue(object: Callback<ResponseEntity> {
                                            override fun onFailure(
                                                call: Call<ResponseEntity>,
                                                t: Throwable
                                            ) {
                                                Log.e("Listener", "API GET 방식 통신 실패 : " + t)
                                            }

                                            override fun onResponse(
                                                call: Call<ResponseEntity>,
                                                response: Response<ResponseEntity>
                                            ) {
                                                val apiResult: ResponseEntity? = response.body()
                                                val message = apiResult?.Message

                                                for (inputs in remoteInputs) {
                                                    replyBundle.putCharSequence(inputs.resultKey, message)
                                                }
                                                RemoteInput.addResultsToIntent(remoteInputs, replyIntent, replyBundle)

                                                act.actionIntent.send(applicationContext, 0, replyIntent)
                                            }

                                        })
                                    }
                                } catch (e: Exception) {
                                    Log.e("Listener", "오류발생 오류발생!!\n" + e)
                                }
                            }
                        }

                    } catch (ex:Exception) {
                        Log.e("Listener", "오류발생 오류발생!!\n" + ex)
                    }
                }
                else if (text!!.startsWith("/숫자야구")) {
                    // 게임이 실행중인지 확인
                    if (isGameRunning1) {
                        if (gamePlayNoti1.keys.contains(kakaoRoom) || gamePlayNoti1.keys.contains(from)) {
                            var gameMessage = "[경고경고]\n"
                            gameMessage += "현재 게임이 진행되고 있어요.\n"
                            gameMessage += "******** 게임상황 ********\n"
                            sendMessage(sbn, gameMessage)
                        } else if (!gamePlayNoti1.keys.contains(kakaoRoom) && !gamePlayNoti1.keys.contains(from)
                            && gamePlayNoti1.size > 0) {
                            var gameMessage = "[경고경고]\n"
                            gameMessage += "다른 방에서 게임이 진행되고 있어요\n"
                            gameMessage += "잠시 후에 다시 시도해주세용\n"
                            sendMessage(sbn, gameMessage)
                        }
                    } else {
                        isGameRunning1 = true

                        gamePlayNoti1.put(
                            if (kakaoRoom == null) from else kakaoRoom.toString(),
                            sbn
                        )
                        var gameMessage = "[둥봇 안내메세지]\n"
                        gameMessage += "게임시작!!!"
                        sendMessage(gamePlayNoti1.values.first(), gameMessage)
                        gameStart()
                    }
                }
                else if (text!!.startsWith(('/'))) {
                    var splitCommand = text.split(' ')

                    var param = mutableMapOf<String, String>(
                        "command" to splitCommand[0].substringAfter('/'),
                        "from" to from,
                        "room" to if(kakaoRoom == null) "null" else kakaoRoom.toString(),
                        "param1" to "",
                        "param2" to "",
                        "param3" to ""
                    )

                    for (i in 1..splitCommand.size - 1) {
                        param["param${i}"] = splitCommand[i]
                    }

                    /**************** 구독관련 명령어 분기처리 **********************/
                    if (param["command"] == "구독") {
                        // admin 저장
                        if (param["param1"] == "admin") {
                            if (from == "둥이" || from == "조창우") {
                                admin.put("admin", sbn)

                                param["param3"] = "admin"
                            }
                            else {
                                param["param3"] = "notadmin"
                            }
                        }
                        else if (param["param1"] == "단톡") {
                            if (from == "둥이" || from == "조창우") {
                                if (kakaoRoom != null) {
                                    subscribeRooms.put(kakaoRoom, sbn)

                                    param["param3"] = "admin"
                                }
                                else {
                                    param["param3"] = "notroom"
                                }
                            }
                            else {
                                param["param3"] = "notadmin"
                            }
                        }
                        else if (param["param1"] == "") {
                            // 단톡방인 경우
                            if (kakaoRoom != null) {
                                param["param3"] = "room"
                            }
                            else {
                                // 이미 존재
                                if (members.containsKey(from)) {
                                    param["param3"] = "using"
                                }
                                // 구독자 추가
                                else {
                                    members.put(from, sbn)
                                    param["param3"] = "normal"
                                }
                            }
                        }
                    }
                    else if (param["command"] == "구독취소") {
                        if (from == "둥이" || from == "조창우") {
                            param["param3"] = "admin"

                            if (members.containsKey(param["param1"])) {
                                members.remove(param["param1"])
                            }
                        }
                        else {
                            // members에 취소할 아이디 있으면
                            if (members.containsKey(param["param1"])) {
                                // members에 취소할 아이디와 닉네임 일치하면
                                if (param["param1"] == from) {
                                    param["param3"] = "exist"

                                    members.remove(from)
                                }
                                else {
                                    param["param3"] = "notcorrespond"
                                }
                            }
                            else {
                                param["param3"] = "none"
                            }
                        }
0                    }
                    else if (param["command"] == "구독확인" && (from == "둥이" || from == "조창우")) {
                        for (member in members) {
                            param["param3"] += member.key + "\n"
                        }
                    }
                    /************************************************************/

                    for (act in actions) {
                        if (act != null && act.allowGeneratedReplies && act.actionIntent != null) {
                            val replyIntent = Intent()
                            val replyBundle = Bundle()

                            try {
                                if (act.remoteInputs != null) {
                                    val remoteInputs = act.remoteInputs
2
                                    // GET 방식 API 호출
                                    val builder = Retrofit.Builder()
                                        .baseUrl(getString(R.string.API_Server_URL))
                                        .addConverterFactory(GsonConverterFactory.create())

                                    val retrofit: Retrofit = builder.build()

                                    val repo = retrofit.create(RequestRepo::class.java)
                                    val call = repo.getRequest(param)

                                    call.enqueue(object: Callback<ResponseEntity> {
                                        override fun onFailure(
                                            call: Call<ResponseEntity>,
                                            t: Throwable
                                        ) {
                                            Log.e("Listener", "API GET 방식 통신 실패 : " + t)
                                        }

                                        override fun onResponse(
                                            call: Call<ResponseEntity>,
                                            response: Response<ResponseEntity>
                                        ) {
                                            val apiResult: ResponseEntity? = response.body()
                                            val message = apiResult?.Message

                                            for (inputs in remoteInputs) {
                                                replyBundle.putCharSequence(inputs.resultKey, message)
                                            }
                                            RemoteInput.addResultsToIntent(remoteInputs, replyIntent, replyBundle)

                                            act.actionIntent.send(applicationContext, 0, replyIntent)
                                        }

                                    })
                                }
                            } catch (e: Exception) {
                                Log.e("Listener", "오류발생 오류발생!!\n" + e)
                            }
                        }
                    }
                }

                /*
                **** 봉인 *****
                else if (text!!.startsWith("@날씨")) {
                    var weatherMessage: String = "${from}님 께서 요청하신 날씨정보입니다.\n";

                    val temp = text.split(' ')

                    for (act in actions) {
                        if (act != null && act.allowGeneratedReplies && act.actionIntent != null) {
                            val pendingIntent = act.actionIntent
                            val replyIntent = Intent()
                            val replyBundle = Bundle()

                            try {
                                if (act.remoteInputs != null) {
                                    val remoteInputs = act.remoteInputs

                                    // Weather API 호출
                                    val builder = Retrofit.Builder()
                                        .baseUrl("http://doonge.synology.me:2697")
                                        .addConverterFactory(GsonConverterFactory.create())

                                    val retrofit: Retrofit = builder.build()

                                    val repo: WeatherRepo = retrofit.create(WeatherRepo::class.java)

                                    val call: Call<WeatherEntity> = repo.requestWeather(temp[1], temp[2])
                                    call.enqueue(object: Callback<WeatherEntity> {
                                        override fun onFailure(
                                            call: Call<WeatherEntity>,
                                            t: Throwable
                                        ) {
                                            Log.e("Listener", "날씨 가져오기 실패 : " + t)
                                        }

                                        override fun onResponse(
                                            call: Call<WeatherEntity>,
                                            response: Response<WeatherEntity>
                                        ) {
                                            Log.e("Listener", "날씨 가져오기 성공")
                                            Log.e("Listener", "결과 메세지 : " + response.body()?.Message)

                                            val apiResult: WeatherEntity? = response.body()
                                            weatherMessage += apiResult?.Message

                                            for (inputs in remoteInputs) {
                                                replyBundle.putCharSequence(inputs.resultKey, weatherMessage)
                                            }
                                            RemoteInput.addResultsToIntent(remoteInputs, replyIntent, replyBundle)

                                            act.actionIntent.send(applicationContext, 0, replyIntent)
                                        }

                                    })

                                }
                            } catch (e: Exception) {
                                Log.e("Listener", "오류발생 오류발생!!\n" + e)
                            }

                        }
                    }
                }
                 */
            }
        }
    }

    fun sendMessage(sbn: StatusBarNotification, msg: String) {
        val mNotification = sbn.notification
        val extras: Bundle = mNotification.extras

        if (sbn != null && sbn.packageName.equals("com.kakao.talk")) {
            val actions = sbn.notification.actions

            for (act in actions) {
                if (act != null && act.allowGeneratedReplies && act.actionIntent != null) {
                    val replyIntent = Intent()
                    val replyBundle = Bundle()

                    try {
                        if (act.remoteInputs != null) {
                            val remoteInputs = act.remoteInputs

                            for (inputs in remoteInputs) {
                                replyBundle.putCharSequence(inputs.resultKey, msg)
                            }
                            RemoteInput.addResultsToIntent(remoteInputs, replyIntent, replyBundle)

                            act.actionIntent.send(applicationContext, 0, replyIntent)
                        }
                    } catch (e: Exception) {
                        Log.e("Listener", "오류발생 오류발생!!\n" + e)
                    }
                }
            }
        }
    }

    private fun gameStart() {
        gameTimerTask1 = timer(period=1000) {
            gameTime1++;
            if (gameTime1 == 15) {
                var gameMessage = "[둥봇의 안내메세지]\n"
                gameMessage += "게임 시작 15초가 지났습니다. 남은 15초 안에 정답을 외쳐주세요."
                sendMessage(gamePlayNoti1.values.first(), gameMessage)
            } else if (gameTime1 == 30) {
                var gameMessage = "[둥봇의 안내메세지]\n"
                gameMessage += "시간초과로 게임이 종료되었어요\n"
                gameMessage += "정답은 [XXX] 였어요"
                sendMessage(gamePlayNoti1.values.first(), gameMessage)

                gameTime1 = 0
                isGameRunning1 = false
                gamePlayNoti1.clear()
                cancel()
            }
        }
    }
}