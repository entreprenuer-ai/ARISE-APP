package com.example.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.core.database.Alarm
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleAlarm(context: Context, alarm: Alarm) {
        if (!alarm.isActive) {
            cancelAlarm(context, alarm)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ACTION_ALARM_TRIGGER"
            putExtra("EXTRA_ALARM_ID", alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextTriggerTime(alarm)

        try {
            var scheduledExact = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    scheduledExact = true
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                scheduledExact = true
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                scheduledExact = true
            }

            if (!scheduledExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Scheduled non-exact alarm (due to missing exact alarm permission) for ID: ${alarm.id}")
            } else {
                Log.d(TAG, "Scheduled exact alarm for ID: ${alarm.id} at $triggerTime")
            }
        } catch (e: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                Log.w(TAG, "SecurityException thrown: scheduled non-exact alarm fallback for ID: ${alarm.id}", e)
            } catch (ex: Exception) {
                Log.e(TAG, "Critical error scheduling fallback alarm for ID: ${alarm.id}", ex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error scheduling alarm for ID: ${alarm.id}", e)
        }
    }

    fun cancelAlarm(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ACTION_ALARM_TRIGGER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled Alarm ID: ${alarm.id}")
        }
    }

    fun calculateNextTriggerTime(alarm: Alarm): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Handle one-time and daily simply
        if (alarm.repeatDays == "One-time" || alarm.repeatDays == "Daily" || alarm.repeatDays.isBlank()) {
            if (target.before(now)) {
                target.add(Calendar.DATE, 1)
            }
            return target.timeInMillis
        }

        // Handle specific repeat days e.g. "Mon,Tue"
        val dayMapping = mapOf(
            "Sun" to Calendar.SUNDAY,
            "Mon" to Calendar.MONDAY,
            "Tue" to Calendar.TUESDAY,
            "Wed" to Calendar.WEDNESDAY,
            "Thu" to Calendar.THURSDAY,
            "Fri" to Calendar.FRIDAY,
            "Sat" to Calendar.SATURDAY
        )

        val activeCalDays = alarm.repeatDays.split(",")
            .map { it.trim() }
            .mapNotNull { dayMapping[it] }
            .sorted()

        if (activeCalDays.isEmpty()) {
            if (target.before(now)) {
                target.add(Calendar.DATE, 1)
            }
            return target.timeInMillis
        }

        // Find the next active day starting from today
        val todayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        var daysToAdding = -1

        // Check if today is one of the active days and the alarm time is still in the future
        if (activeCalDays.contains(todayOfWeek) && target.after(now)) {
            daysToAdding = 0
        } else {
            // Find the next day in the cycle
            for (i in 1..7) {
                val nextDay = (todayOfWeek + i - 1) % 7 + 1
                if (activeCalDays.contains(nextDay)) {
                    daysToAdding = i
                    break
                }
            }
        }

        if (daysToAdding == -1) {
            daysToAdding = 1 // Fallback
        }

        target.add(Calendar.DATE, daysToAdding)
        return target.timeInMillis
    }
}
