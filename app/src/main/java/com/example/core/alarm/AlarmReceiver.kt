package com.example.core.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.core.database.AriseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
        private const val CHANNEL_ID = "arise_alarm_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive invoked with action: $action")

        if (Intent.ACTION_BOOT_COMPLETED == action || "android.intent.action.QUICKBOOT_POWERON" == action) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AriseDatabase.getDatabase(context)
                    val activeAlarms = db.ariseDao().getActiveAlarmsSync()
                    Log.d(TAG, "Boot completed. Rescheduling ${activeAlarms.size} active alarms.")
                    for (alarm in activeAlarms) {
                        AlarmScheduler.scheduleAlarm(context, alarm)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling alarms on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        } else if ("com.example.ACTION_ALARM_TRIGGER" == action) {
            val alarmId = intent.getIntExtra("EXTRA_ALARM_ID", -1)
            Log.d(TAG, "Triggering alarm ID: $alarmId")
            if (alarmId != -1) {
                // Fetch the alarm to ensure it exists and is active before triggering
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AriseDatabase.getDatabase(context)
                        val alarm = db.ariseDao().getAlarmById(alarmId)
                        if (alarm != null && alarm.isActive) {
                            Log.d(TAG, "Alarm active. Starting AriseAlarmService, showing notification & bringing MainActivity to screen.")
                            
                            try {
                                val serviceIntent = Intent(context, AriseAlarmService::class.java).apply {
                                    setAction(AriseAlarmService.ACTION_START_ALARM)
                                    putExtra(AriseAlarmService.EXTRA_ALARM_ID, alarmId)
                                    putExtra(AriseAlarmService.EXTRA_SOUND_PATH, alarm.soundPath)
                                    putExtra(AriseAlarmService.EXTRA_SOUND_START_MS, alarm.soundStartMs)
                                    putExtra(AriseAlarmService.EXTRA_SOUND_END_MS, alarm.soundEndMs)
                                    putExtra(AriseAlarmService.EXTRA_GRADUAL_VOLUME, alarm.gradualVolume)
                                }
                                context.startService(serviceIntent)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to start AriseAlarmService", e)
                            }

                            showHeadsUpNotificationAndWake(context, alarmId, alarm.label, alarm.challengeType)
                        } else {
                            Log.d(TAG, "Alarm was deleted, inactive or not found: $alarmId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing alarm trigger", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun showHeadsUpNotificationAndWake(
        context: Context,
        alarmId: Int,
        label: String,
        challenge: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create high-importance Notification Channel (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ARISE Active Alarms"
            val descriptionText = "Heads up notifications for currently ringing alarms"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                setSound(
                    alarmSound,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Custom PendingIntent to open MainActivity
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_ALARM_ID", alarmId)
        }

        val activityPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        // Build elegant notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ ARISE: $label")
            .setContentText("Time to rise and shine! Solve the $challenge challenge to dismiss.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setFullScreenIntent(activityPendingIntent, true) // Turn on the screen
            .setContentIntent(activityPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        notificationManager.notify(alarmId, notification)

        // Force open MainActivity immediately
        try {
            context.startActivity(activityIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MainActivity directly, relying on full screen intent", e)
        }
    }
}
