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
import androidx.core.app.NotificationManagerCompat
import com.example.kakaotalknotification.Receiver.AlarmReceiver
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isNotificationPermissionAllowed()) {
            var intent: Intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }

        // 알람 등록
        val alarmManager = getSystemService((Context.ALARM_SERVICE)) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, AlarmReceiver.NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = packageName
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val powerManagerIntent = Intent()
            powerManagerIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            powerManagerIntent.setData(Uri.parse("package:"+packageName))
            startActivityForResult(powerManagerIntent, 0)
        }

//        val alarmTime = (SystemClock.elapsedRealtime() + 60 * 1000)

        var calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 6)
        calendar.set(Calendar.MINUTE, 40)
        calendar.set(Calendar.SECOND, 0)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.e("Listener", "알람설정 완료")
    }

    private fun isNotificationPermissionAllowed(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
            .any {enablePackageName -> enablePackageName == packageName}
    }
}