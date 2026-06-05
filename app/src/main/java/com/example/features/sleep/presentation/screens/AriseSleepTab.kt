package com.example.features.sleep.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CustomColorScheme
import com.example.features.sleep.presentation.viewmodel.SleepViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AriseSleepTab(
    viewModel: SleepViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val napTimerRemainingSeconds by viewModel.napTimerRemainingSeconds.collectAsState()
    val isNapTimerRunning by viewModel.isNapTimerRunning.collectAsState()
    val sleepLogsList by viewModel.sleepLogs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
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
                        text = "SLEEP CYCLE OPTIMIZER (HEALTH RECOMMENDATIONS)",
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

        item {
            Text(
                text = "SLEEP LOG HISTORY & DEBT TARGETS",
                fontFamily = fontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground,
                modifier = Modifier.padding(vertical = 12.dp)
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
                    Text("No sleep records yet. Sleep simulation triggers automatically when alarms dismiss.", fontFamily = fontFamily, fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                            val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            Text(
                                text = "Wake Mood: ${log.wakeMood}",
                                fontFamily = fontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Text(
                                text = df.format(Date(log.wakeTime)),
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                color = colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val sleptHours = (log.wakeTime - log.bedTime) / 3600000f
                        Text(
                            text = "Time slept: %.2f hours  •  Target: %.1f hours  •  Sleep Debt: %.2f hours".format(sleptHours, log.targetHours, log.sleepDebtHours),
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = colors.onSurface
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}
