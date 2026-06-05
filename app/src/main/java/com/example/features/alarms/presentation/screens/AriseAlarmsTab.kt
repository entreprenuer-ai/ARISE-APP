package com.example.features.alarms.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.database.Alarm
import com.example.core.designsystem.CustomColorScheme
import com.example.features.alarms.presentation.viewmodel.AlarmViewModel

@Composable
fun AriseAlarmsTab(
    viewModel: AlarmViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val alarmsList by viewModel.alarms.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (alarmsList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Silent Alarms",
                    tint = colors.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(84.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No alarms configured",
                    fontFamily = fontFamily,
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Configure custom days, intervals, challenge gates, and gradual volumes.",
                    fontFamily = fontFamily,
                    color = colors.onBackground.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(alarmsList) { alarm ->
                    AriseAlarmCard(viewModel, alarm, colors, fontFamily)
                }
                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }

        // Add Floating Action Button for Unlimited Alarms
        FloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = colors.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_alarm_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Alarm Builder", tint = colors.onPrimary)
        }

        if (showAddSheet) {
            AriseAlarmDesignerDialog(
                viewModel = viewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showAddSheet = false }
            )
        }
    }
}

@Composable
fun AriseAlarmCard(
    viewModel: AlarmViewModel,
    alarm: Alarm,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { expanded = !expanded }
            .testTag("alarm_card_${alarm.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alarm.emoji,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        val displayMin = if (alarm.minute < 10) "0${alarm.minute}" else "${alarm.minute}"
                        val displayHour = if (alarm.hour < 10) "0${alarm.hour}" else "${alarm.hour}"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$displayHour:$displayMin",
                                fontFamily = fontFamily,
                                color = colors.onSurface,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            )
                            if (alarm.isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                val relativeStr = remember(alarm.hour, alarm.minute) {
                                    val now = java.util.Calendar.getInstance()
                                    val alarmCal = java.util.Calendar.getInstance().apply {
                                        set(java.util.Calendar.HOUR_OF_DAY, alarm.hour)
                                        set(java.util.Calendar.MINUTE, alarm.minute)
                                        set(java.util.Calendar.SECOND, 0)
                                    }
                                    if (alarmCal.before(now)) {
                                        alarmCal.add(java.util.Calendar.DATE, 1)
                                    }
                                    val diffMs = alarmCal.timeInMillis - now.timeInMillis
                                    val diffHours = diffMs / (1000 * 60 * 60)
                                    val diffMins = (diffMs / (1000 * 60)) % 60
                                    if (diffHours > 0) "${diffHours}h ${diffMins}m" else "${diffMins}m"
                                }
                                Text(
                                    text = "🔔 in $relativeStr",
                                    fontFamily = fontFamily,
                                    color = colors.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.primaryContainer.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = alarm.label,
                                fontFamily = fontFamily,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  ${alarm.repeatDays}",
                                fontFamily = fontFamily,
                                color = colors.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.simulateAlarmTrigger(alarm) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("simulate_alarm_${alarm.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Simulator",
                            tint = colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = alarm.isActive,
                        onCheckedChange = { viewModel.toggleAlarmActive(alarm) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onPrimary,
                            checkedTrackColor = colors.primary,
                            uncheckedThumbColor = colors.onBackground.copy(alpha = 0.5f),
                            uncheckedTrackColor = colors.divider
                        ),
                        modifier = Modifier.testTag("toggle_status_${alarm.id}")
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.divider)
                Spacer(modifier = Modifier.height(14.dp))

                if (alarm.description.isNotEmpty()) {
                    Text(
                        text = alarm.description,
                        fontFamily = fontFamily,
                        color = colors.onSurface.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category: ${alarm.category}", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Vibration: ${alarm.vibrationStyle}", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp)
                        Text("Challenge: ${alarm.challengeType} (${alarm.challengeDifficulty})", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gradual Wake: ${if (alarm.gradualVolume) "Yes" else "No"}", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp)
                        Text("Snooze: ${alarm.snoozeDurationMinutes}m, limit ${alarm.snoozeLimit}x", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp)
                        Text("Strobe Flashing: ${if (alarm.flashlightStrobe) "Enabled" else "Disabled"}", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.deleteAlarm(alarm) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
                        modifier = Modifier.testTag("delete_alarm_${alarm.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Alarm", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = Color.White, fontFamily = fontFamily, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AriseAlarmDesignerDialog(
    viewModel: AlarmViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf("Morning Awake") }
    var description by remember { mutableStateOf("Wake up and conquer") }
    var hour by remember { mutableStateOf(7) }
    var minute by remember { mutableStateOf(30) }
    var repeatDays by remember { mutableStateOf("Mon,Tue,Wed,Thu,Fri") }
    var challengeType by remember { mutableStateOf("Math") }
    var challengeDifficulty by remember { mutableStateOf("Medium") }
    var vibrationStyle by remember { mutableStateOf("Medium") }
    var gradualVolume by remember { mutableStateOf(true) }
    var flashlightStrobe by remember { mutableStateOf(false) }
    var snoozeLimit by remember { mutableStateOf(3) }
    var snoozeDuration by remember { mutableStateOf(5) }
    var emojiLabel by remember { mutableStateOf("⏰") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "ARISE CUSTOM ALARM DESIGNER",
                    fontFamily = fontFamily,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alarm Label", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface, unfocusedTextColor = colors.onSurface, focusedBorderColor = colors.primary, unfocusedBorderColor = colors.divider),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Alarm Description", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface, unfocusedTextColor = colors.onSurface, focusedBorderColor = colors.primary, unfocusedBorderColor = colors.divider),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select Time: $hour:$minute", fontFamily = fontFamily, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hour: $hour", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Slider(
                        value = hour.toFloat(),
                        onValueChange = { hour = it.toInt() },
                        valueRange = 0f..23f,
                        colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary),
                        modifier = Modifier.weight(3f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Minute: $minute", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Slider(
                        value = minute.toFloat(),
                        onValueChange = { minute = it.toInt() },
                        valueRange = 0f..59f,
                        colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary),
                        modifier = Modifier.weight(3f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Repeat Setting: $repeatDays", fontFamily = fontFamily, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                val presets = listOf("One-time", "Daily", "Weekend")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    presets.forEach { preset ->
                        val isPresetActive = when (preset) {
                            "One-time" -> repeatDays == "One-time"
                            "Daily" -> repeatDays.split(",").size == 7
                            "Weekend" -> repeatDays == "Sat,Sun"
                            else -> false
                        }
                        Button(
                            onClick = {
                                repeatDays = when (preset) {
                                    "One-time" -> "One-time"
                                    "Daily" -> "Mon,Tue,Wed,Thu,Fri,Sat,Sun"
                                    "Weekend" -> "Sat,Sun"
                                    else -> "One-time"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPresetActive) colors.primary else colors.primaryContainer),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(preset, color = if (isPresetActive) colors.onPrimary else colors.onBackground, fontSize = 11.sp, fontFamily = fontFamily)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val weekdays = listOf(
                        "Mon" to "M",
                        "Tue" to "T",
                        "Wed" to "W",
                        "Thu" to "T",
                        "Fri" to "F",
                        "Sat" to "S",
                        "Sun" to "S"
                    )
                    
                    val currentDays = if (repeatDays == "One-time") emptyList() else repeatDays.split(",").filter { it.isNotEmpty() }
                    
                    weekdays.forEach { (day, initial) ->
                        val isSelected = currentDays.contains(day)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.primary else colors.primaryContainer)
                                .clickable {
                                    val newList = currentDays.toMutableList()
                                    if (isSelected) {
                                        newList.remove(day)
                                    } else {
                                        newList.add(day)
                                    }
                                    repeatDays = if (newList.isEmpty()) {
                                        "One-time"
                                    } else {
                                        val sortingOrder = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                        newList.sortedBy { sortingOrder.indexOf(it) }.joinToString(",")
                                    }
                                }
                        ) {
                            Text(
                                text = initial,
                                color = if (isSelected) colors.onPrimary else colors.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = fontFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Dismiss Challenge Type", fontFamily = fontFamily, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                val challenges = listOf("None", "Math", "Memory", "Shake", "Type", "Counting", "Rhythm")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    challenges.forEach { type ->
                        Button(
                            onClick = { challengeType = type },
                            colors = ButtonDefaults.buttonColors(containerColor = if (challengeType == type) colors.primary else colors.primaryContainer),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(type, color = if (challengeType == type) colors.onPrimary else colors.onBackground, fontSize = 11.sp, fontFamily = fontFamily)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Difficulty: $challengeDifficulty", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    listOf("Easy", "Medium", "Hard").forEach { d ->
                        Button(
                            onClick = { challengeDifficulty = d },
                            colors = ButtonDefaults.buttonColors(containerColor = if (challengeDifficulty == d) colors.primary else colors.primaryContainer),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(d, color = if (challengeDifficulty == d) colors.onPrimary else colors.onBackground, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Audio & Vibration Profile", fontFamily = fontFamily, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Crescendo Wake (Gradual Volume)", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(2f))
                    Switch(checked = gradualVolume, onCheckedChange = { gradualVolume = it }, colors = SwitchDefaults.colors(checkedTrackColor = colors.primary))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Strobe Light (Hearing Impaired)", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(2f))
                    Switch(checked = flashlightStrobe, onCheckedChange = { flashlightStrobe = it }, colors = SwitchDefaults.colors(checkedTrackColor = colors.primary))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = colors.onBackground, fontFamily = fontFamily)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.insertAlarm(
                                Alarm(
                                    label = label,
                                    description = description,
                                    hour = hour,
                                    minute = minute,
                                    repeatDays = repeatDays,
                                    challengeType = challengeType,
                                    challengeDifficulty = challengeDifficulty,
                                    vibrationStyle = vibrationStyle,
                                    gradualVolume = gradualVolume,
                                    flashlightStrobe = flashlightStrobe,
                                    snoozeLimit = snoozeLimit,
                                    snoozeDurationMinutes = snoozeDuration,
                                    emoji = emojiLabel
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_alarm_button")
                    ) {
                        Text("Save Alarm", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
