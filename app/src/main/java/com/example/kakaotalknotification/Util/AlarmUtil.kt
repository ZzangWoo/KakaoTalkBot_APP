package com.example.kakaotalknotification.Util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.kakaotalknotification.Receiver.AlarmReceiver

class AlarmUtil {

    companion object {

        fun setAlarm(context: Context, intent: Intent, calendar: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar, pendingIntent)
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar, pendingIntent)
            }
            else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar, pendingIntent)
            }
        }

        fun setNextAlarm(context: Context, trigger: Long) {
            // 24시간 interval
            val calendar = trigger + 1000 * 60 * 60 * 24

            val intent = Intent(context, AlarmReceiver::class.java)
            intent.action = "android.com.intent.KAKAO_ALARM"
            intent.putExtra("trigger", calendar)
            setAlarm(context, intent, calendar)
        }

    }

}