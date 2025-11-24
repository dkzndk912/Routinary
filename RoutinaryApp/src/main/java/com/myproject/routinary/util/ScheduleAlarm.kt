package com.myproject.routinary.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.myproject.routinary.R
import java.util.Calendar

class ScheduleAlarm() : BroadcastReceiver() {
    @RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context?, intent: Intent?) {
        var title: String = ""
        var alarmId: Int = -1

        intent?.let {
            // ⭐ getStringExtra()를 사용하여 문자열을 꺼냅니다.
            title = it.getStringExtra("title") ?: "일정 이름"
            alarmId = it.getIntExtra("alarmId", -1)
        }
        context?.let  {
            // 이전에 구현했던 알림 표시 로직을 호출합니다.
            val helper = ScheduleAlarm()
            helper.createNotificationChannel(it) // 채널은 이미 생성되었겠지만, 안전하게 다시 호출
            helper.showAlarmNotification(it, title, alarmId)
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun setAlarm(context: Context, alarmId: Int, targetTimeMillis: Long, title: String) : Boolean {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. 알람이 울릴 때 실행될 Intent 정의 (ScheduleAlarm를 타겟)
        val intent = Intent(context, ScheduleAlarm::class.java).apply {
            // 알람마다 다른 데이터를 넣을 수 있습니다. (예: 알림 제목, 내용)
            putExtra("title", title)
            putExtra("alarmId", alarmId)
        }

        // 2. PendingIntent 생성 (미래의 Intent)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId, // 요청 코드 (알람 ID와 동일하게 사용하여 구분)
            intent,
            // FLAG_IMMUTABLE은 필수. 이전 알림을 업데이트하려면 FLAG_UPDATE_CURRENT 사용 가능.
            PendingIntent.FLAG_IMMUTABLE
        )

        // 3. 알람 예약
        // Doze 모드(저전력 모드)에서도 정확하게 알람을 울리도록 설정 (권장)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 이상
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, // 디바이스가 잠자고 있어도 깨워서 알람 실행
                    targetTimeMillis,
                    pendingIntent
                )
                return true
            } else {
                Toast.makeText(context, "권한이 없어 알림 설정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            // Android 12 미만: setExact() 호출 가능 (권한 확인 불필요)
            // ...
            return true
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleAlarmAt(context: Context, hour: Int, minute: Int, title: String, alarmId: Int) {
        val alarmId = alarmId // 고유 알람 ID

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // ⭐ 만약 설정된 시간이 현재 시간보다 이전이라면, 다음 날로 설정
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val targetTimeMillis = calendar.timeInMillis

        // 알람 설정 함수 호출
        if (setAlarm(context, alarmId, targetTimeMillis, title)) {
            Toast.makeText(context, "${hour}시 ${minute}분에 알람이 예약되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 채널 생성 로직
    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "scheduleAlarm"
            val channelName = "일정 알림"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 알림 표시 로직
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showAlarmNotification(context: Context, title: String, alarmId: Int) {
        val channelId = "scheduleAlarm"
        val notificationId = alarmId // 알림을 구분하는 고유 ID

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 알림 아이콘
            .setContentTitle("일정 알림 : $title")
            .setContentText("일정을 확인하세요")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}

