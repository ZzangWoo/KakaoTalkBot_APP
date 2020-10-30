package com.example.kakaotalknotification.Util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.kakaotalknotification.Adapter.LogList
import com.example.kakaotalknotification.MainActivity
import com.example.kakaotalknotification.Receiver.AlarmReceiver
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.hours

class AlarmUtil {

    companion object {

        fun setAlarm(context: Context, intent: Intent, calendar: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar, pendingIntent)
                val log = intent.getStringExtra("log")
                Log.e("Listener", if(log == null) "예약됐을걸?" else log)

                // 예약설정된 시간 로그에 남기기 위해 현재시간 추출
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("MM월 dd일 HH시 mm분 ss초")
                val date = current.format(formatter)
                val logList = LogList("알람설정", if(log == null) "예약됐을걸?" else log, date)

                // 로그 갱신
                MainActivity.logItems.add(logList)
                MainActivity.logAdapter.notifyDataSetChanged()
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar, pendingIntent)
            }
            else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar, pendingIntent)
            }
        }

        fun setNextAlarm(context: Context, trigger: Long) {
            // 알람시간 24시간 후로 설정
            val calendar = trigger + 1000 * 60 * 60 * 24

            // 24시간 후에 알람 설정
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.action = "android.com.intent.KAKAO_ALARM"
            intent.putExtra("trigger", calendar)
            intent.putExtra("log", "24시간 후에 다시 알람")
            setAlarm(context, intent, calendar)
        }

        fun cancelAlarm(context: Context, intent: Intent) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            alarmManager.cancel(pendingIntent)
            Log.e("Listener", "알람취소 완료")

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("MM월 dd일 HH시 mm분 ss초")
            val date = current.format(formatter)
            val logList = LogList("알람취소", "알람취소 완료", date)

            MainActivity.logItems.add(logList)
            MainActivity.logAdapter.notifyDataSetChanged()
        }

    }

}