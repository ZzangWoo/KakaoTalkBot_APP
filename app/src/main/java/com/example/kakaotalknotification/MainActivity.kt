package com.example.kakaotalknotification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.NumberPicker
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.get
import com.example.kakaotalknotification.Adapter.LogList
import com.example.kakaotalknotification.Adapter.LogListAdapter
import com.example.kakaotalknotification.Receiver.AlarmReceiver
import com.example.kakaotalknotification.Util.AlarmUtil
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    companion object {
        var logItems = arrayListOf<LogList>()
        lateinit var logAdapter: LogListAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            if (!isNotificationPermissionAllowed()) {
                var intent: Intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                startActivity(intent)
            }

            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = packageName
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val powerManagerIntent = Intent()
                powerManagerIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                powerManagerIntent.setData(Uri.parse("package:"+packageName))
                startActivityForResult(powerManagerIntent, 0)
            }

            val setAlarmButton: Button = findViewById(R.id.SetAlarmButton)
            val cancelAlarmButton: Button = findViewById(R.id.CancelAlarmButton)

            val hourNumberPicker: NumberPicker = findViewById(R.id.HourNumberPicker)
            hourNumberPicker.minValue = 0
            hourNumberPicker.maxValue = 24

            val minuteNumberPicker: NumberPicker = findViewById(R.id.MinuteNumberPicker)
            minuteNumberPicker.minValue = 0
            minuteNumberPicker.maxValue = 60


            logAdapter = LogListAdapter(this, logItems)
            val logListView: ListView = findViewById(R.id.LogListView)
            logListView.adapter = logAdapter

            // 알람설정 버튼 클릭
            setAlarmButton.setOnClickListener{
                var calendar = Calendar.getInstance()

                if (calendar.get(Calendar.HOUR_OF_DAY) >= hourNumberPicker.value && calendar.get(Calendar.MINUTE) >= minuteNumberPicker.value) {
                    calendar.add(Calendar.DATE, 1)
                }

                calendar.set(Calendar.HOUR_OF_DAY, hourNumberPicker.value)
                calendar.set(Calendar.MINUTE, minuteNumberPicker.value)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // 알람 등록
                val alarmIntent = Intent(this, AlarmReceiver::class.java)
                alarmIntent.action = "android.com.intent.KAKAO_ALARM"
                alarmIntent.putExtra("trigger", calendar.timeInMillis)
                alarmIntent.putExtra("log", hourNumberPicker.value.toString() + "시 " + minuteNumberPicker.value.toString() + "분 알람설정 완료")
                AlarmUtil.setAlarm(this, alarmIntent, calendar.timeInMillis)
            }

            // 알람취소 버튼 클릭
            cancelAlarmButton.setOnClickListener {
                val alarmIntent = Intent(this, AlarmReceiver::class.java)
                alarmIntent.action = "android.com.intent.KAKAO_ALARM"
                AlarmUtil.cancelAlarm(this, alarmIntent)
            }
        } catch (e: Exception) {
            Log.e("Listener", e.toString())
        }



    }

    private fun isNotificationPermissionAllowed(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
            .any {enablePackageName -> enablePackageName == packageName}
    }
}