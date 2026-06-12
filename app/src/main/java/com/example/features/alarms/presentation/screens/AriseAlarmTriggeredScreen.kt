package com.example.features.alarms.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.Alarm
import com.example.core.designsystem.CustomColorScheme
import com.example.features.alarms.presentation.viewmodel.AlarmViewModel
import androidx.compose.ui.platform.LocalContext
import com.example.core.alarm.AriseAlarmService
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AriseAlarmTriggeredScreen(
    viewModel: AlarmViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val alarm by viewModel.activeTriggeredAlarm.collectAsState()
    val localAlarm = alarm ?: return

    val context = LocalContext.current

    DisposableEffect(localAlarm.id) {
        val startServiceIntent = Intent(context, AriseAlarmService::class.java).apply {
            action = AriseAlarmService.ACTION_START_ALARM
            putExtra(AriseAlarmService.EXTRA_ALARM_ID, localAlarm.id)
            putExtra(AriseAlarmService.EXTRA_SOUND_PATH, localAlarm.soundPath)
            putExtra(AriseAlarmService.EXTRA_SOUND_START_MS, localAlarm.soundStartMs)
            putExtra(AriseAlarmService.EXTRA_SOUND_END_MS, localAlarm.soundEndMs)
            putExtra(AriseAlarmService.EXTRA_GRADUAL_VOLUME, localAlarm.gradualVolume)
        }
        try {
            context.startService(startServiceIntent)
        } catch (e: Exception) {
            android.util.Log.e("TriggeredScreen", "Fail starting sound service", e)
        }

        onDispose {
            val stopServiceIntent = Intent(context, AriseAlarmService::class.java).apply {
                action = AriseAlarmService.ACTION_STOP_ALARM
            }
            try {
                context.startService(stopServiceIntent)
            } catch (e: Exception) {
                android.util.Log.e("TriggeredScreen", "Fail stopping sound service", e)
            }
        }
    }

    var mathInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("") }

    val shakeCount by viewModel.shakeCount.collectAsState()
    val mathProblem by viewModel.mathProblem.collectAsState()
    val memoryPattern by viewModel.memoryPattern.collectAsState()
    val memorySelection by viewModel.memorySelection.collectAsState()
    val typingTarget by viewModel.typingTarget.collectAsState()
    val countBackwardVal by viewModel.currentCountBackward.collectAsState()
    val strobeActive by viewModel.strobeActive.collectAsState()

    val shakeTarget = if (localAlarm.challengeDifficulty == "Hard") 40 else if (localAlarm.challengeDifficulty == "Easy") 15 else 25

    // Infinite pulsing visual animation
    val infiniteTransition = rememberInfiniteTransition()
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val stbackgroundColor = if (strobeActive && (System.currentTimeMillis() % 600 > 300)) colors.primaryContainer else colors.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stbackgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing flashing alarm bell banner representation
        IconButton(
            onClick = {},
            modifier = Modifier
                .size(100.dp)
                .drawBehind {
                    drawCircle(
                        color = colors.primary.copy(alpha = 0.2f),
                        radius = size.minDimension * 0.75f * scaleFactor
                    )
                }
        ) {
            Text(localAlarm.emoji, fontSize = 60.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WAKE UP MOTIVATIONAL QUOTE WIDGET (Gap #4)
        val quotes = remember {
            listOf(
                "Arise, awake, and stop not until the goal is reached.",
                "The best way to predict your future is to create it.",
                "Your attitude determines your direction.",
                "Every day is a fresh start. Take a deep breath and begin again.",
                "Today is another opportunity to rise higher and shine brighter!"
            )
        }
        val todaysQuote = remember {
            quotes[(System.currentTimeMillis() / (1000 * 3600)).toInt() % quotes.size]
        }
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = colors.primary.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✨ WAKING INSPIRATION",
                    fontFamily = fontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"$todaysQuote\"",
                    fontFamily = fontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = colors.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val nowStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        Text(
            text = nowStr,
            fontFamily = fontFamily,
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.onBackground
        )

        Text(
            text = localAlarm.label,
            fontFamily = fontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            textAlign = TextAlign.Center
        )

        if (localAlarm.description.isNotEmpty()) {
            Text(
                text = localAlarm.description,
                fontFamily = fontFamily,
                fontSize = 14.sp,
                color = colors.onBackground.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // MOOD SELECTOR ON WAKING (Feature Bible requirement)
        Text(
            text = "HOW IS YOUR WAKEUP MOOD?",
            fontFamily = fontFamily,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        val selectedMood by viewModel.selectedWakeMood.collectAsState()

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val moods = listOf(
                "Good" to "☀️ Good",
                "Neutral" to "😐 Neutral",
                "Tired" to "💤 Tired"
            )
            moods.forEach { (key, display) ->
                val isSelected = selectedMood == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) colors.primaryContainer else Color.Transparent)
                        .border(1.dp, if (isSelected) colors.primary else colors.divider, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setSelectedWakeMood(key) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(display, fontFamily = fontFamily, fontSize = 11.sp, color = colors.onBackground)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // CHALLENGE MODULE GATES DISPLAY
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(2.dp, colors.primary)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DISMISS GATE: ${localAlarm.challengeType.uppercase()} GATE",
                    fontFamily = fontFamily,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                when (localAlarm.challengeType) {
                    "Math" -> {
                        Text(
                            text = "Solve Math Challenge: $mathProblem",
                            fontFamily = fontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = mathInput,
                            onValueChange = { mathInput = it },
                            placeholder = { Text("Type Answer", color = colors.onSurface.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.onSurface,
                                unfocusedTextColor = colors.onSurface,
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.divider
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .testTag("gate_math_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val parsed = mathInput.toIntOrNull() ?: 0
                                val isCorrect = viewModel.submitMathAnswer(parsed)
                                if (!isCorrect) {
                                    android.widget.Toast.makeText(context, "❌ Incorrect answer! A new problem has been generated.", android.widget.Toast.LENGTH_SHORT).show()
                                    mathInput = ""
                                } else {
                                    android.widget.Toast.makeText(context, "✅ Challenge solved! Alarm dismissed.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.testTag("gate_math_submit")
                        ) {
                            Text("Submit Key", color = colors.onPrimary)
                        }
                    }

                    "Memory" -> {
                        Text(
                            text = "Memorize & Tap Active Tiles Pattern",
                            fontFamily = fontFamily,
                            fontSize = 13.sp,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val count = memoryPattern.size
                        val columns = if (count == 16) 4 else 3
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            items(count) { idx ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (memorySelection[idx]) colors.primary
                                            else colors.primaryContainer.copy(alpha = 0.5f)
                                        )
                                        .clickable { viewModel.toggleMemoryTile(idx) }
                                )
                            }
                        }
                    }

                    "Shake" -> {
                        Text(
                            text = "Shake Phone to Inc: $shakeCount / $shakeTarget",
                            fontFamily = fontFamily,
                            fontSize = 16.sp,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { shakeCount.toFloat() / shakeTarget.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = colors.primary,
                            trackColor = colors.divider
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.recordShake() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .testTag("gate_shake_simulator")
                        ) {
                            Text("SHAKE", color = colors.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    "Type" -> {
                        Text(
                            text = "Type this inspirational quote exactly:",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface
                        )
                        Text(
                            text = "“$typingTarget”",
                            fontFamily = fontFamily,
                            fontSize = 13.sp,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = typeInput,
                            onValueChange = { typeInput = it },
                            placeholder = { Text("Type quote here...", color = colors.onSurface.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.onSurface,
                                unfocusedTextColor = colors.onSurface,
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.divider
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val isCorrect = viewModel.submitTypingAnswer(typeInput)
                                if (!isCorrect) {
                                    android.widget.Toast.makeText(context, "❌ Mismatch! Please copy the exact quote.", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "✅ Quote verified! Alarm dismissed.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("Submit Match", color = colors.onPrimary)
                        }
                    }

                    "Counting" -> {
                        Text(
                            text = "Solve spelling counting: Countdown backwards!",
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = colors.onSurface
                        )
                        Text(
                            text = "$countBackwardVal",
                            fontFamily = fontFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.countBackwardMinusOne() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("Count Down (-1)", color = colors.onPrimary)
                        }
                    }

                    "Rhythm" -> {
                        Text(
                            text = "Tap the button consistently in synchronization",
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.triggerRhythmTap() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Text("TAP", color = colors.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        // Standard basic dismiss
                        Button(
                            onClick = { viewModel.dismissActiveAlarm() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                                .testTag("gate_standard_dismiss")
                        ) {
                            Text("Dismiss Wakeup Alert", color = colors.onPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (localAlarm.snoozeEnabled) {
            val snoozeLimitReached = localAlarm.snoozeLimit > 0 && localAlarm.snoozeCount >= localAlarm.snoozeLimit
            Button(
                onClick = { if (!snoozeLimitReached) viewModel.snoozeActiveAlarm() },
                enabled = !snoozeLimitReached,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.surface,
                    disabledContainerColor = colors.surface.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, if (snoozeLimitReached) colors.divider else colors.primary),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(44.dp)
                    .testTag("gate_snooze_button")
            ) {
                val snoozeText = if (snoozeLimitReached) {
                    "Snooze Limit Reached! Solve Challenge"
                } else {
                    "Snooze (${localAlarm.snoozeDurationMinutes}m Limit: ${localAlarm.snoozeCount}/${localAlarm.snoozeLimit}x)"
                }
                Text(snoozeText, color = if (snoozeLimitReached) colors.onSurface.copy(alpha = 0.4f) else colors.primary, fontFamily = fontFamily)
            }
        }
    }
}
