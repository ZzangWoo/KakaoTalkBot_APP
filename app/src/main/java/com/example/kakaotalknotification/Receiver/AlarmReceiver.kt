package com.example.kakaotalknotification.Receiver

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import com.example.kakaotalknotification.Adapter.LogList
import com.example.kakaotalknotification.Entity.ResponseEntity
import com.example.kakaotalknotification.Entity.SubscribeEntity
import com.example.kakaotalknotification.Entity.SubscribeFunctionEntity
import com.example.kakaotalknotification.MainActivity
import com.example.kakaotalknotification.R
import com.example.kakaotalknotification.Repository.RequestRepo
import com.example.kakaotalknotification.Service.KakaoTalkNotificationListenerService
import com.example.kakaotalknotification.Util.AlarmUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "AlarmReceiver"
        const val NOTIFICATION_ID = 0
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmAction = intent.action

        if (alarmAction == "android.com.intent.KAKAO_ALARM") {
            val calendar = intent.getLongExtra("trigger", 0)
            val nowCalendar = Calendar.getInstance().timeInMillis

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("MM월 dd일 HH시 mm분 ss초")
            val date = current.format(formatter)
            var logList = LogList("테스트", "알람시간 : " + calendar + "\n알람울린시간" + nowCalendar, date)
            MainActivity.logItems.add(logList)
            MainActivity.logAdapter.notifyDataSetChanged()

            // 알람이 울리게 되면 다음 시간에 다시 알람 설정
            AlarmUtil.setNextAlarm(context, nowCalendar)

            var param = mutableMapOf<String, String>(
                "From" to ""
            )

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

                        if (userList.size != 0 && KakaoTalkNotificationListenerService.subscribeCommand.size != 0) {
                            for (j in 0.. userList.size - 1) {
                                if (KakaoTalkNotificationListenerService.members.containsKey(userList[j])) {
                                    param["From"] = userList[j]
                                    val subscribeCall = repo.requestSubscribeFunction(
                                        KakaoTalkNotificationListenerService.subscribeCommand[i], param)

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

                                            val actions = KakaoTalkNotificationListenerService.members[userList[j]]!!.notification.actions

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
                                                                context,
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


        }




    }
}
