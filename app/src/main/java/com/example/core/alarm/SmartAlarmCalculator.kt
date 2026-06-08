package com.example.core.alarm

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.core.database.Alarm
import com.example.core.database.AriseRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

object SmartAlarmCalculator {
    private const val TAG = "SmartAlarmCalculator"

    data class NextDayEvent(
        val title: String,
        val startTime: Long,
        val endTime: Long,
        val description: String,
        val source: String
    )

    data class SmartCalculationResult(
        val nextDayEvent: NextDayEvent?,
        val calculatedWakeTimeMs: Long?,
        val calculatedHour: Int?,
        val calculatedMinute: Int?,
        val bufferMinutes: Int,
        val originalEventTimeStr: String,
        val calculatedWakeTimeStr: String,
        val isFallback: Boolean = false
    )

    /**
     * Determines tomorrow's start and end timestamps.
     * Incorporates Midnight Boundary Protection: if called shortly after midnight (before 4:00 AM),
     * the time window is defined as the upcoming 24 hours rather than strictly the next calendar day.
     */
    fun getTomorrowTimeRange(): Pair<Long, Long> {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        
        if (currentHour < 4) {
            val startOfWindow = now.timeInMillis
            val endOfWindow = now.timeInMillis + (24 * 60 * 60 * 1000L)
            Log.d(TAG, "Midnight boundary query active. Scanning next 24-hour window: $startOfWindow to $endOfWindow")
            return Pair(startOfWindow, endOfWindow)
        }

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfTomorrow = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfTomorrow = calendar.timeInMillis
        return Pair(startOfTomorrow, endOfTomorrow)
    }

    /**
     * Queries both native CalendarProvider (if permission given) and the internal Room DB,
     * returning the chronologically first event starting tomorrow.
     */
    suspend fun findFirstEventOfNextDay(context: Context, repository: AriseRepository): NextDayEvent? {
        val (startOfTomorrow, endOfTomorrow) = getTomorrowTimeRange()
        val events = mutableListOf<NextDayEvent>()

        // 1. Query Internal Room Database Events
        try {
            val localEventsList = repository.allEvents.first()
            val localEventTomorrow = localEventsList
                .filter { it.startTime in startOfTomorrow..endOfTomorrow }
                .minByOrNull { it.startTime }

            if (localEventTomorrow != null) {
                events.add(
                    NextDayEvent(
                        title = localEventTomorrow.title,
                        startTime = localEventTomorrow.startTime,
                        endTime = localEventTomorrow.endTime,
                        description = localEventTomorrow.notes,
                        source = "Room DB"
                    )
                )
                Log.d(TAG, "Found local room event tomorrow: ${localEventTomorrow.title}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from room database", e)
        }

        // 2. Query Native Android CalendarProvider
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            try {
                val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
                ContentUris.appendId(builder, startOfTomorrow)
                ContentUris.appendId(builder, endOfTomorrow)
                val uri = builder.build()

                val projection = arrayOf(
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.DESCRIPTION
                )

                val sortOrder = "${CalendarContract.Instances.BEGIN} ASC"

                context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val titleIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                        val beginIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                        val endIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
                        val descIdx = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)

                        val title = cursor.getString(titleIdx) ?: "Unnamed Event"
                        val begin = cursor.getLong(beginIdx)
                        val end = cursor.getLong(endIdx)
                        val desc = cursor.getString(descIdx) ?: ""

                        events.add(
                            NextDayEvent(
                                title = title,
                                startTime = begin,
                                endTime = end,
                                description = desc,
                                source = "CalendarProvider"
                            )
                        )
                        Log.d(TAG, "Found native calendar provider event tomorrow: $title")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying system CalendarProvider", e)
            }
        } else {
            Log.d(TAG, "READ_CALENDAR permission not granted; skipping native provider query")
        }

        // Return the earliest of any detected events
        return events.minByOrNull { it.startTime }
    }

    /**
     * Calculates the smart alarm target time (bufferMinutes prior to the first event).
     */
    fun calculateSmartWakeTime(
        event: NextDayEvent?,
        bufferMinutes: Int,
        fallbackTimeStr: String = "07:00"
    ): SmartCalculationResult {
        if (event == null) {
            val parts = fallbackTimeStr.split(":")
            val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 7
            val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0

            val calendar = Calendar.getInstance().apply {
                val curHour = get(Calendar.HOUR_OF_DAY)
                // If it's shortly after midnight (before 4 AM), the fallback alarm should be set for today,
                // provided the target hour is later than current hour.
                val shouldBeToday = curHour < 4 && (hour > curHour || (hour == curHour && minute > get(Calendar.MINUTE)))
                if (!shouldBeToday) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val targetMs = calendar.timeInMillis
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

            return SmartCalculationResult(
                nextDayEvent = null,
                calculatedWakeTimeMs = targetMs,
                calculatedHour = hour,
                calculatedMinute = minute,
                bufferMinutes = bufferMinutes,
                originalEventTimeStr = "--:--",
                calculatedWakeTimeStr = dateFormat.format(Date(targetMs)),
                isFallback = true
            )
        }

        val targetMs = event.startTime - (bufferMinutes * 60 * 1000L)
        val eventCal = Calendar.getInstance().apply { timeInMillis = event.startTime }
        val wakeCal = Calendar.getInstance().apply { timeInMillis = targetMs }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

        return SmartCalculationResult(
            nextDayEvent = event,
            calculatedWakeTimeMs = targetMs,
            calculatedHour = wakeCal.get(Calendar.HOUR_OF_DAY),
            calculatedMinute = wakeCal.get(Calendar.MINUTE),
            bufferMinutes = bufferMinutes,
            originalEventTimeStr = timeFormat.format(Date(event.startTime)),
            calculatedWakeTimeStr = dateFormat.format(Date(targetMs))
        )
    }

    /**
     * Interfaces with the existing Alarm Engine to update the scheduled smart alarm broadcast.
     */
    suspend fun updateScheduledSmartAlarm(context: Context, repository: AriseRepository, result: SmartCalculationResult): Alarm? {
        val calculatedHour = result.calculatedHour ?: return null
        val calculatedMinute = result.calculatedMinute ?: return null

        // Find if a smart alarm already exists in the system
        val allAlarmsList = repository.allAlarms.first()
        val existingSmartAlarm = allAlarmsList.find { it.category == "Smart" || it.label == "Smart Wake Alarm" }

        val descriptionStr = if (result.isFallback) {
            "Master Default alarm time (no calendar events tomorrow)"
        } else {
            "Dynamically adjusted ${result.bufferMinutes}m before event: '${result.nextDayEvent?.title}'"
        }

        val finalAlarm = if (existingSmartAlarm != null) {
            existingSmartAlarm.copy(
                hour = calculatedHour,
                minute = calculatedMinute,
                isActive = true,
                description = descriptionStr
            )
        } else {
            Alarm(
                label = "Smart Wake Alarm",
                description = descriptionStr,
                hour = calculatedHour,
                minute = calculatedMinute,
                repeatDays = "Daily",
                isActive = true,
                category = "Smart",
                emoji = "⚡",
                gradualVolume = true,
                challengeType = "Math"
            )
        }

        // Save the updated/new smart alarm in DB
        val savedId = repository.insertAlarm(finalAlarm)
        val scheduledAlarm = finalAlarm.copy(id = if (existingSmartAlarm == null) savedId.toInt() else finalAlarm.id)

        // Reschedule via existing AlarmScheduler
        AlarmScheduler.scheduleAlarm(context.applicationContext, scheduledAlarm)
        Log.d(TAG, "Successfully updated and scheduled Smart Alarm for: ${String.format("%02d:%02d", calculatedHour, calculatedMinute)}")

        return scheduledAlarm
    }
}
