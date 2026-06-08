package com.example.features.sleep.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.features.sleep.presentation.viewmodel.SleepViewModel
import com.example.features.sleep.presentation.viewmodel.SleepTrackingViewModel
import com.example.core.database.SleepSession
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AriseSleepTab(
    viewModel: SleepViewModel,
    sleepTrackingViewModel: SleepTrackingViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val context = LocalContext.current
    val napTimerRemainingSeconds by viewModel.napTimerRemainingSeconds.collectAsState()
    val isNapTimerRunning by viewModel.isNapTimerRunning.collectAsState()
    val sleepLogsList by viewModel.sleepLogs.collectAsState()

    // Tracking states
    val isTracking by sleepTrackingViewModel.isTracking.collectAsState()
    val trackingStartTime by sleepTrackingViewModel.trackingStartTime.collectAsState()
    val sleepSessions by sleepTrackingViewModel.allSleepSessions.collectAsState()

    // Health Telemetry Core Flows
    val sleepTargetHours by sleepTrackingViewModel.sleepTargetHours.collectAsState()
    val insightWarning by sleepTrackingViewModel.insightWarning.collectAsState()
    var delayResultText by remember { mutableStateOf("") }

    // Live elapsed timer state
    var liveElapsedMillis by remember { mutableStateOf(0L) }

    // Dialog state for logging sleep
    var showLogDialog by remember { mutableStateOf(false) }
    var sleepRating by remember { mutableStateOf(4f) }
    var sleepNotes by remember { mutableStateOf("") }

    // Telemetry export and import states for offline sync checks
    val scope = rememberCoroutineScope()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var telemetryJsonText by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var telemetryFeedbackText by remember { mutableStateOf("") }

    // Coroutine effect for live tracking timer update
    LaunchedEffect(isTracking, trackingStartTime) {
        if (isTracking && trackingStartTime != null) {
            while (true) {
                liveElapsedMillis = System.currentTimeMillis() - trackingStartTime!!
                delay(1000)
            }
        } else {
            liveElapsedMillis = 0L
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("arise_sleep_tab")
    ) {
        // PILLAR 3: SLEEP GOAL CONFIGURATION CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("sleep_goal_card"),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BIO-RECOVERY TARGET DIRECTIVE",
                            fontFamily = fontFamily,
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "%.1fh Target".format(sleepTargetHours),
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Set your absolute target sleep duration to dynamically calculate deficits and suggest intelligent recovery workflows.",
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "5.0h",
                            fontFamily = fontFamily,
                            fontSize = 10.sp,
                            color = colors.onSurface.copy(alpha = 0.5f)
                        )
                        Slider(
                            value = sleepTargetHours,
                            onValueChange = { sleepTrackingViewModel.setSleepTargetHours(it) },
                            valueRange = 5.0f..10.0f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = colors.primary,
                                activeTrackColor = colors.primary,
                                inactiveTrackColor = colors.divider
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("sleep_goal_slider")
                        )
                        Text(
                            text = "10.0h",
                            fontFamily = fontFamily,
                            fontSize = 10.sp,
                            color = colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // PILLAR 3: AI INSIGHT WARNING & ALARM ADJUSTMENT CARD
        insightWarning?.let { warning ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("ai_insight_warning_card"),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.5.dp, colors.secondary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Recovery Debt Warning",
                                tint = colors.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "BIO-RECOVERY INSIGHT WARNING",
                                fontFamily = fontFamily,
                                color = colors.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val sleptHours = warning.actualHours
                        val target = warning.targetHours
                        val debt = warning.sleepDebtHours
                        
                        Text(
                            text = "System telemetry indicates a sleep session deficit. You slept only %.2fh, which is %.2fh below your biological directive of %.1fh.".format(sleptHours, debt, target),
                            fontFamily = fontFamily,
                            fontSize = 13.sp,
                            color = colors.onSurface,
                            lineHeight = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.secondary.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "🤖 SMART ALARM PROPOSAL",
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.secondary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Critical sleep debt increases cognitive decline risks. We recommend optimizing tomorrow's alarm sequence by delaying it to protect your recovery profile.",
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    color = colors.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { sleepTrackingViewModel.dismissInsight() },
                                border = BorderStroke(1.dp, colors.divider),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dismiss_insight_button")
                            ) {
                                Text(
                                    text = "Dismiss",
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    color = colors.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Button(
                                onClick = {
                                    sleepTrackingViewModel.delayAlarm(context, 30) { text ->
                                        delayResultText = text
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("delay_alarm_30s")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Snooze,
                                    contentDescription = null,
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+30 mins",
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onPrimary
                                )
                            }

                            Button(
                                onClick = {
                                    sleepTrackingViewModel.delayAlarm(context, 60) { text ->
                                        delayResultText = text
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("delay_alarm_60s")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Snooze,
                                    contentDescription = null,
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+60 mins",
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // CONFIRMATION STATUS FOR DELAYING ALARMS
        if (delayResultText.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("delay_alarm_result_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = delayResultText,
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = colors.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { delayResultText = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // PILLAR 3: ACTIVE TRACKER DASHBOARD CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("sleep_tracker_card"),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "COGNITIVE REST TRACKER",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isTracking) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = "Rest Mode Active",
                                tint = colors.secondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ready to sleep?",
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "Trigger real-time biorecovery telemetry logging. Keeps track of accurate sleep duration.",
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    color = colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { sleepTrackingViewModel.startTracking() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_sleep_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = colors.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Start Sleep Tracking",
                                color = colors.onPrimary,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Sleep session actively running
                        val hours = liveElapsedMillis / 3600000
                        val minutes = (liveElapsedMillis % 3600000) / 60000
                        val seconds = (liveElapsedMillis % 60000) / 1000

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "MONITORING REST PROFILE ACTIVE",
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "%02d:%02d:%02d".format(hours, minutes, seconds),
                                fontFamily = fontFamily,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primary
                            )

                            Text(
                                text = "Slept since: ${formatTime(trackingStartTime ?: System.currentTimeMillis())}",
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                color = colors.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { sleepTrackingViewModel.cancelTracking() },
                                    border = BorderStroke(1.dp, colors.divider),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(45.dp)
                                        .testTag("cancel_sleep_button")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = colors.onSurface)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Discard", fontFamily = fontFamily, color = colors.onSurface)
                                }

                                Button(
                                    onClick = {
                                        sleepRating = 4f
                                        sleepNotes = ""
                                        showLogDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(45.dp)
                                        .testTag("stop_sleep_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = colors.onPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Wake Up & Log", fontFamily = fontFamily, color = colors.onPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ORIGINAL: RECOVERY SYSTEM: COGNITIVE WAKE NAP TIMER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RECOVERY SYSTEM: COGNITIVE WAKE NAP TIMER",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (isNapTimerRunning) {
                        val mins = napTimerRemainingSeconds / 60
                        val secs = napTimerRemainingSeconds % 60
                        val displaySecs = if (secs < 10) "0$secs" else "$secs"
                        val displayMins = if (mins < 10) "0$mins" else "$mins"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$displayMins:$displaySecs",
                                fontFamily = fontFamily,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.cancelNapTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.testTag("cancel_nap_button")
                            ) {
                                Text("Recall Timer", color = Color.White)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val durations = listOf(20, 45, 90)
                            durations.forEach { mins ->
                                Button(
                                    onClick = { viewModel.startNapTimer(mins) },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .testTag("start_nap_${mins}")
                                ) {
                                    Text("${mins}m", color = colors.onPrimary, fontFamily = fontFamily, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ORIGINAL: SLEEP CYCLE OPTIMIZER (HEALTH RECOMMENDATIONS)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SLEEP CYCLE OPTIMIZER (CIRCADIAN PHASES)",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Suggested optimal sleep stage blocks based on standard 90-minute circadian cycles:",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("4.5h • Good", "6.0h • Great", "7.5h • Perfect").forEach { cycle ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primaryContainer)
                                    .padding(8.dp)
                             ) {
                                Text(cycle, fontFamily = fontFamily, fontSize = 10.sp, color = colors.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // PILLAR 3: HISTORICAL TRACKED SESSIONS LIST
        item {
            Text(
                text = "SLEEP BIO-TELEMETRY LOGS HISTORY",
                fontFamily = fontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
        }

        if (sleepSessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfied,
                                contentDescription = null,
                                tint = colors.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No sleep sessions registered in database yet. Try completing your first tracking cycle!",
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                color = colors.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            items(sleepSessions) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("sleep_session_item_${session.id}"),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Rest Session #${session.id}",
                                    fontFamily = fontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.primary
                                )
                                Text(
                                    text = "${formatTime(session.startTimeMillis)}  →  ${formatTime(session.endTimeMillis)}",
                                    fontFamily = fontFamily,
                                    fontSize = 10.sp,
                                    color = colors.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            // Interactive delete
                            IconButton(
                                onClick = { sleepTrackingViewModel.deleteSession(session) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_sleep_session_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Session",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val durationString = formatDuration(session.startTimeMillis, session.endTimeMillis)
                            Text(
                                text = "Duration: ",
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = durationString,
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                color = colors.secondary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Custom Golden Stars row represent rating!
                            Row {
                                val roundedRating = Math.round(session.sleepQualityRating)
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= roundedRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Star $i",
                                        tint = if (i <= roundedRating) Color(0xFFFFD700) else colors.onSurface.copy(alpha = 0.2f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        if (!session.notes.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = colors.background),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notes,
                                        contentDescription = null,
                                        tint = colors.primary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp).padding(top = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = session.notes,
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        color = colors.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // LOCAL TELEMETRY SYNC ENGINE (DATA INTEGRITY PANEL)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("telemetry_sync_panel_card"),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LOCAL TELEMETRY SYNAPSE PORT",
                            fontFamily = fontFamily,
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Disk Persistent",
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Verify database integrity, flush cached logs into master database via manual WAL Checkpointing, or extract/inject telemetry logs below.",
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (telemetryFeedbackText.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = telemetryFeedbackText,
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    color = colors.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { telemetryFeedbackText = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = colors.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Three core action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val json = sleepTrackingViewModel.exportSleepSessionsToJson()
                                    telemetryJsonText = json
                                    showExportDialog = true
                                    telemetryFeedbackText = "Telemetry logs exported successfully!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_telemetry_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Export",
                                color = colors.onPrimary,
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                importJsonText = ""
                                showImportDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("import_telemetry_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Import",
                                color = colors.onPrimary,
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        sleepTrackingViewModel.exportSleepSessionsToJson() // Runs checkpoint internally
                                        telemetryFeedbackText = "WAL Checkpoint fully executed. All volatile cache safely committed to SQLite disk!"
                                    } catch (e: Exception) {
                                        telemetryFeedbackText = "Checkpoint failed: ${e.message}"
                                    }
                                }
                            },
                            border = BorderStroke(1.dp, colors.divider),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("checkpoint_telemetry_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = colors.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Checkpoint",
                                color = colors.onSurface,
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ORIGINAL: RETENTION OF OTHER LOGS (SIMULATED ONES)
        item {
            Text(
                text = "SIMULATED DISMISS WAKE LOGS",
                fontFamily = fontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
        }

        if (sleepLogsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No simulated exit records registered yet. These log automatically when alarm challeges dismiss.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(sleepLogsList) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Wake Mood: ${log.wakeMood}",
                                fontFamily = fontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Text(
                                text = formatTime(log.wakeTime),
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val sleptHours = (log.wakeTime - log.bedTime) / 3600000f
                        Text(
                            text = "Slept: %.2f hours  •  Target: %.1f hours  •  Debt: %.2f hours".format(sleptHours, log.targetHours, log.sleepDebtHours),
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // DIA-LOG DIALOG BOX: RATING INTERACTION & SAVE SESSION
    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = {
                Text(
                    text = "BIOLOGICAL RECOVERY LOG",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.primary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Rate your subjective rest quality:",
                        fontFamily = fontFamily,
                        fontSize = 13.sp,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Stars row
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 1..5) {
                            val active = i <= sleepRating
                            Icon(
                                imageVector = if (active) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate $i",
                                tint = if (active) Color(0xFFFFD700) else colors.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { sleepRating = i.toFloat() }
                                    .testTag("rate_star_$i")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val subjectiveLabel = when {
                        sleepRating <= 1.5f -> "😓 Exhausted / Interrupted Sleep"
                        sleepRating <= 2.5f -> "🥱 Restless / Insufficient Rest"
                        sleepRating <= 3.5f -> "😐 Standard Adequate Sleep"
                        sleepRating <= 4.5f -> "🙂 High Recovery / Refreshing"
                        else -> "⚡ Exceptional Circus Awake Peak!"
                    }

                    Text(
                        text = subjectiveLabel,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes details
                    OutlinedTextField(
                        value = sleepNotes,
                        onValueChange = { sleepNotes = it },
                        label = { Text("Quality logs notes/comments...", fontFamily = fontFamily) },
                        placeholder = { Text("e.g. woke up once, drank warm milk, vivid dreams", fontFamily = fontFamily, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = colors.primary,
                            focusedBorderColor = colors.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("sleep_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sleepTrackingViewModel.stopAndSaveTracking(sleepRating, sleepNotes)
                        showLogDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier.testTag("save_sleep_session_confirm")
                ) {
                    Text("Save Rest Session", fontFamily = fontFamily, color = colors.onPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogDialog = false }
                ) {
                    Text("Go Back", fontFamily = fontFamily, color = colors.onSurface)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.surface
        )
    }

    // TELEMETRY EXPORT DIALOG
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(
                    text = "EXTRACT BIO-TELEMETRY MAP",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.primary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "This secure JSON string represents your raw sleep rest logs database. Copy and preserve it offline.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = telemetryJsonText,
                        onValueChange = {},
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("telemetry_json_output_text")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(telemetryJsonText))
                        showExportDialog = false
                        telemetryFeedbackText = "Telemetry JSON fully copied to clipboard!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier.testTag("copy_telemetry_json_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy to Clipboard", fontFamily = fontFamily, color = colors.onPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false }
                ) {
                    Text("Close", fontFamily = fontFamily, color = colors.onSurface)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.surface
        )
    }

    // TELEMETRY IMPORT DIALOG
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text(
                    text = "INJECT BIO-TELEMETRY LOGS",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.secondary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Paste a valid sleep logs JSON array string exported earlier to parse and restore sessions directly.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Paste JSON logs array here...", fontFamily = fontFamily, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.secondary,
                            focusedLabelColor = colors.secondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("telemetry_json_input_text")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = sleepTrackingViewModel.importSleepSessionsFromJson(importJsonText)
                            showImportDialog = false
                            telemetryFeedbackText = if (success) {
                                "Rest logs successfully parsed and injected into secure Room local database!"
                            } else {
                                "Failed to import logs: Invalid JSON telemetry mapping format."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                    modifier = Modifier.testTag("confirm_telemetry_import_btn")
                ) {
                    Text("Parse & Restore Logs", fontFamily = fontFamily, color = colors.onPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportDialog = false }
                ) {
                    Text("Cancel", fontFamily = fontFamily, color = colors.onSurface)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.surface
        )
    }
}

// Utility formatting methods
private fun formatTime(timeMillis: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timeMillis))
}

private fun formatDuration(startTimeMillis: Long, endTimeMillis: Long): String {
    val durationMs = endTimeMillis - startTimeMillis
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m ${seconds}s"
    } else {
        "${minutes}m ${seconds}s"
    }
}
