package com.example.features.dashboard.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CustomColorScheme
import com.example.features.alarms.presentation.viewmodel.AlarmViewModel
import com.example.features.calendar.presentation.viewmodel.CalendarViewModel
import com.example.features.goals.presentation.viewmodel.GoalsViewModel
import com.example.features.habits.presentation.viewmodel.HabitsViewModel
import com.example.features.sleep.presentation.viewmodel.SleepViewModel
import com.example.features.sleep.presentation.viewmodel.SleepTrackingViewModel
import androidx.compose.material.icons.filled.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AriseHomeTab(
    alarmViewModel: AlarmViewModel,
    calendarViewModel: CalendarViewModel,
    goalsViewModel: GoalsViewModel,
    habitsViewModel: HabitsViewModel,
    sleepViewModel: SleepViewModel,
    sleepTrackingViewModel: SleepTrackingViewModel,
    onNavigateToSleep: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val alarmsList by alarmViewModel.alarms.collectAsState()
    val eventsList by calendarViewModel.events.collectAsState()
    val goalsList by goalsViewModel.goals.collectAsState()
    val sleepLogsList by sleepViewModel.sleepLogs.collectAsState()
    val habitsList by habitsViewModel.habits.collectAsState()

    val sleepTargetHours by sleepTrackingViewModel.sleepTargetHours.collectAsState()
    val sleepSessions by sleepTrackingViewModel.allSleepSessions.collectAsState()

    // Find smart alarm in alarmsList
    val smartAlarm = remember(alarmsList) {
        alarmsList.find { it.category == "Smart" || it.label == "Smart Wake Alarm" }
    }

    // Rolling last 3 sleep sessions
    val last3Sessions = remember(sleepSessions) {
        sleepSessions.sortedByDescending { it.endTimeMillis }.take(3)
    }

    // Latest sleep session duration vs target
    val latestSession = remember(sleepSessions) {
        sleepSessions.firstOrNull()
    }
    val targetProgressPercent = remember(latestSession, sleepTargetHours) {
        if (latestSession != null && sleepTargetHours > 0) {
            val sleptMs = latestSession.endTimeMillis - latestSession.startTimeMillis
            val sleptHours = sleptMs / 3600000.0f
            ((sleptHours / sleepTargetHours) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    // 1. Live ticking Clock State
    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }
    var blinkState by remember { mutableStateOf(true) }
    var showBatteryNotification by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdfDate = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
            currentTimeStr = sdfTime.format(now.time)
            currentDateStr = sdfDate.format(now.time)
            blinkState = !blinkState
            kotlinx.coroutines.delay(1000)
        }
    }

    // 2. Compute Next Alarm
    val nextAlarm = remember(alarmsList) {
        alarmsList.filter { it.isActive }.minByOrNull { (it.hour * 60) + it.minute }
    }

    // 3. Today's Events
    val todayEvents = remember(eventsList) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
        eventsList.filter { it.startTime in todayStart..todayEnd }
    }

    // 4. Sleep Score computation from latest sleep log
    val latestSleepScore = remember(sleepLogsList) {
        val lastLog = sleepLogsList.maxByOrNull { it.wakeTime }
        if (lastLog != null) {
            val actual = ((lastLog.wakeTime - lastLog.bedTime) / 3600000f).coerceAtLeast(0f)
            val score = (actual / lastLog.targetHours * 100).toInt().coerceIn(0, 100)
            score
        } else {
            82 // Simulated default
        }
    }

    // 5. Habits Streaks Summary
    val habitStreaks = remember(habitsList) {
        habitsList.filter { !it.isArchived }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- LIVE TICKING CLOCK ZONE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("home_clock_card"),
            colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.9f)),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COMMAND CENTER",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time with flashing colon separator
                val blinkColon = if (blinkState) ":" else " "
                val timeParts = currentTimeStr.split(":")
                val displayTime = if (timeParts.size == 2) {
                    "${timeParts[0]}$blinkColon${timeParts[1]}"
                } else {
                    currentTimeStr.ifEmpty { "07:00" }
                }

                Text(
                    text = displayTime,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = currentDateStr.ifEmpty { "Monday, June 01" },
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.divider, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // NEXT ALARM SUBSECTION
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Next Alarm Signal",
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (nextAlarm != null) {
                            val hourStr = if (nextAlarm.hour < 10) "0${nextAlarm.hour}" else "${nextAlarm.hour}"
                            val minStr = if (nextAlarm.minute < 10) "0${nextAlarm.minute}" else "${nextAlarm.minute}"
                            "NEXT ACTIVE ALARM: $hourStr:$minStr (${nextAlarm.label})"
                        } else {
                            "NO ACTIVE ALARM PATHS CONFIGURED"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }
            }
        }

        // --- DIRECT ACTION SHORTCUTS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToSleep,
                modifier = Modifier
                    .weight(1f)
                    .testTag("shortcut_to_sleep"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SLEEP HUB",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }

            Button(
                onClick = onNavigateToCalendar,
                modifier = Modifier
                    .weight(1f)
                    .testTag("shortcut_to_calendar"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.secondary.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, colors.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = colors.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BLUEPRINT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.secondary
                )
            }
        }

        // --- BATTERY OPTIMIZATION GUIDE ---
        if (showBatteryNotification) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("battery_optimization_card"),
                colors = CardDefaults.cardColors(containerColor = colors.secondary.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, colors.secondary.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = "Battery Alert",
                        tint = colors.secondary,
                        modifier = Modifier
                            .size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SYSTEM LATENCY MINIMIZATION REQUIRED",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.secondary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Set Arise to 'Unrestricted Battery Usage' in system settings. This prevents Android from halting the sleep tracking sub-routines and smart alarms overnight.",
                            fontFamily = fontFamily,
                            fontSize = 10.sp,
                            color = colors.onSurface.copy(alpha = 0.85f),
                            lineHeight = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showBatteryNotification = false },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("dismiss_battery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // --- CYBERPUNK TELEMETRY HUB ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("cyberpunk_telemetry_hub"),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.5.dp, colors.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Chrono Telemetry Logo",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CORE CYBER-TELEMETRY HARVESTER",
                        fontFamily = FontFamily.Monospace,
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Split metrics: Smart Wake & Biometric Target
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Smart Wake Glance Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("telemetry_smart_wake_card"),
                        colors = CardDefaults.cardColors(containerColor = colors.background.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, colors.divider),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "CHRONO-SYNAPSE WAKE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val isSmartActive = smartAlarm != null && smartAlarm.isActive
                            Text(
                                text = if (isSmartActive) "%02d:%02d".format(smartAlarm!!.hour, smartAlarm.minute) else "STANDBY / OFF",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSmartActive) colors.primary else colors.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isSmartActive) {
                                    if (smartAlarm!!.description.contains("Master Default", ignoreCase = true)) {
                                        "MASTER FALLBACK ROUTINE"
                                    } else {
                                        "EVENT-OPTIMIZED SYNC"
                                    }
                                } else {
                                    "No smart wake registered"
                                },
                                fontFamily = fontFamily,
                                fontSize = 9.sp,
                                color = colors.onSurface.copy(alpha = 0.5f),
                                lineHeight = 12.sp
                            )
                        }
                    }

                    // Biometric Goal Sync Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("telemetry_biometric_goal_card"),
                        colors = CardDefaults.cardColors(containerColor = colors.background.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, colors.divider),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "BIO-RECOVERY TARGET",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$targetProgressPercent%",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                color = if (targetProgressPercent >= 100) colors.primary else colors.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$targetProgressPercent% of your ${sleepTargetHours}h biological target.",
                                fontFamily = fontFamily,
                                fontSize = 9.sp,
                                color = colors.onSurface.copy(alpha = 0.5f),
                                lineHeight = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rolling last 3 days of sleep history summary list
                Text(
                    text = "HISTORIC BIOMETRIC TELEMETRY (ROLLING 3-DAY)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (last3Sessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.background.copy(alpha = 0.3f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Awaiting biometric telemetry logging...",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        last3Sessions.forEach { session ->
                            val sleptMs = session.endTimeMillis - session.startTimeMillis
                            val sleptHours = sleptMs / 3600000.0f
                            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                            val dateStr = sdf.format(Date(session.endTimeMillis))
                            val isDeficit = sleptHours < sleepTargetHours

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.background.copy(alpha = 0.4f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = null,
                                        tint = if (isDeficit) colors.secondary else colors.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dateStr,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "%.1fh Slept".format(sleptHours),
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        color = colors.onSurface.copy(alpha = 0.8f)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Quality: %.1f".format(session.sleepQualityRating),
                                        fontFamily = fontFamily,
                                        fontSize = 10.sp,
                                        color = colors.onSurface.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isDeficit) colors.secondary.copy(alpha = 0.15f)
                                                else colors.primary.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isDeficit) "DEFICIT" else "OPTIMAL",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDeficit) colors.secondary else colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- ASYMMETRIC STATS DOCK HEADER ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SLEEP SCORE GAUGE CARD (Compact Left Card)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SLEEP SCORE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { latestSleepScore.toFloat() / 100f },
                            modifier = Modifier.size(52.dp),
                            color = colors.primary,
                            strokeWidth = 5.dp,
                            trackColor = colors.divider
                        )
                        Text(
                            text = "$latestSleepScore%",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                }
            }

            // HABIT STREAK FLAME CARD (Compact Right Card)
            val maxStreakVal = remember(habitStreaks) {
                habitStreaks.maxOfOrNull { it.currentStreak } ?: 0
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "MAX STREAK",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Active Streaks Fire",
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$maxStreakVal DAYS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onSurface
                    )
                }
            }
        }

        // --- KEYSTONE TOP 3 DAILY MISSIONS PANEL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("home_missions_card"),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "TOP 3 CONSCIOUS MISSIONS TODAY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                val missionLabels = listOf(
                    "Drink 500ml water instantly after wake gate dismiss",
                    "Acknowledge daily learning milestones & update database",
                    "Verify sleep schedules & configure bedside modes"
                )

                missionLabels.forEachIndexed { index, missionText ->
                    var isChecked by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isChecked = !isChecked }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.divider)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = missionText,
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = if (isChecked) colors.onSurface.copy(alpha = 0.5f) else colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (index < 2) {
                        HorizontalDivider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        // --- CALENDAR EVENTS AGENDA ROW ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("home_agenda_card"),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "TODAY'S BLUEPRINT EVENTS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (todayEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO COMMITMENTS SCHEDULED FOR TODAY",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    todayEvents.forEachIndexed { index, event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(event.colorHex)))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = event.title,
                                        fontFamily = fontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    Text(
                                        text = "${sdf.format(Date(event.startTime))} - ${sdf.format(Date(event.endTime))}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Text(
                                text = event.priority,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = colors.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (index < todayEvents.size - 1) {
                            HorizontalDivider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // --- GOAL PROGRESS BLOCK LIST ---
        if (goalsList.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("home_goals_card"),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "LIFETIME STRATEGY PROGRESSIONS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    goalsList.take(3).forEachIndexed { index, goal ->
                        val percent = (goal.currentProgress.toFloat() / goal.targetProgress.toFloat()).coerceIn(0f, 1f)
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.title,
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "${goal.currentProgress}/${goal.targetProgress}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = colors.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { percent },
                                color = colors.primary,
                                trackColor = colors.divider,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                             )
                        }
                        if (index < goalsList.take(3).size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // --- HABIT TRAIL STREAKS ---
        if (habitStreaks.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("home_habits_card"),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ACTIVE HABIT ASCENT PATHS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    habitStreaks.take(4).forEachIndexed { index, habit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🔥 ${habit.title}",
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                color = colors.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "STREAK:",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = colors.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${habit.currentStreak} DAYS",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                        if (index < habitStreaks.take(4).size - 1) {
                            HorizontalDivider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(88.dp))
    }
}
