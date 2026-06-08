package com.example.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.core.database.AriseDatabase
import com.example.core.database.AriseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AriseBootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AriseBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive invoked with action: $action")

        if (Intent.ACTION_BOOT_COMPLETED == action || 
            "android.intent.action.QUICKBOOT_POWERON" == action ||
            "com.htc.intent.action.QUICKBOOT_POWERON" == action
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AriseDatabase.getDatabase(context)
                    val repository = AriseRepository(db.ariseDao(), db.sleepSessionDao(), db)

                    // 1. Reschedule regular active alarms
                    val activeAlarms = repository.getActiveAlarmsSync()
                    Log.d(TAG, "Rescheduling ${activeAlarms.size} active alarms on boot.")
                    for (alarm in activeAlarms) {
                        // Skip registering a duplicate active Smart Alarm if we are going to recalculate it
                        if (alarm.category != "Smart") {
                            AlarmScheduler.scheduleAlarm(context, alarm)
                        }
                    }

                    // 2. Reschedule Smart Wake Alarm by recalculating
                    val savedBuffer = repository.getSetting("smart_alarm_buffer_minutes")?.toIntOrNull() ?: 90
                    val savedMasterDefault = repository.getSetting("master_default_alarm_time") ?: "07:00"

                    val event = SmartAlarmCalculator.findFirstEventOfNextDay(context, repository)
                    val result = SmartAlarmCalculator.calculateSmartWakeTime(
                        event = event,
                        bufferMinutes = savedBuffer,
                        fallbackTimeStr = savedMasterDefault
                    )

                    SmartAlarmCalculator.updateScheduledSmartAlarm(context, repository, result)
                    Log.d(TAG, "Successfully recalculated and rescheduled Smart Wake Alarm on boot.")

                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling alarms on boot completed", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
