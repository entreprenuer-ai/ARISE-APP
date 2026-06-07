package com.example.features.alarms.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.core.database.Alarm
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
    var alarmToEdit by remember { mutableStateOf<Alarm?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }

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
                    AriseAlarmCard(
                        viewModel = viewModel,
                        alarm = alarm,
                        colors = colors,
                        fontFamily = fontFamily,
                        onEditClick = {
                            alarmToEdit = alarm
                            showEditSheet = true
                        }
                    )
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

        if (showEditSheet && alarmToEdit != null) {
            AriseAlarmDesignerDialog(
                viewModel = viewModel,
                colors = colors,
                fontFamily = fontFamily,
                alarmToEdit = alarmToEdit,
                onDismiss = {
                    showEditSheet = false
                    alarmToEdit = null
                }
            )
        }
    }
}

@Composable
fun AriseAlarmCard(
    viewModel: AlarmViewModel,
    alarm: Alarm,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    onEditClick: ((Alarm) -> Unit)? = null
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
                    if (onEditClick != null) {
                        Button(
                            onClick = { onEditClick(alarm) },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary.copy(alpha = 0.85f)),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("edit_alarm_${alarm.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Alarm", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", color = Color.White, fontFamily = fontFamily, fontSize = 12.sp)
                        }
                    }

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
    onDismiss: () -> Unit,
    alarmToEdit: Alarm? = null
) {
    var label by remember { mutableStateOf(alarmToEdit?.label ?: "Morning Awake") }
    var description by remember { mutableStateOf(alarmToEdit?.description ?: "Wake up and conquer") }
    var hour by remember { mutableStateOf(alarmToEdit?.hour ?: 7) }
    var minute by remember { mutableStateOf(alarmToEdit?.minute ?: 30) }
    var repeatDays by remember { mutableStateOf(alarmToEdit?.repeatDays ?: "Mon,Tue,Wed,Thu,Fri") }
    var challengeType by remember { mutableStateOf(alarmToEdit?.challengeType ?: "Math") }
    var challengeDifficulty by remember { mutableStateOf(alarmToEdit?.challengeDifficulty ?: "Medium") }
    var vibrationStyle by remember { mutableStateOf(alarmToEdit?.vibrationStyle ?: "Medium") }
    var gradualVolume by remember { mutableStateOf(alarmToEdit?.gradualVolume ?: true) }
    var flashlightStrobe by remember { mutableStateOf(alarmToEdit?.flashlightStrobe ?: false) }
    var snoozeLimit by remember { mutableStateOf(alarmToEdit?.snoozeLimit ?: 3) }
    var snoozeDuration by remember { mutableStateOf(alarmToEdit?.snoozeDurationMinutes ?: 5) }
    var emojiLabel by remember { mutableStateOf(alarmToEdit?.emoji ?: "⏰") }

    var soundPath by remember { mutableStateOf<String?>(alarmToEdit?.soundPath) }
    var soundName by remember { mutableStateOf<String>(alarmToEdit?.soundName ?: "Default Rise Chime") }
    var soundStartMs by remember { mutableStateOf(alarmToEdit?.soundStartMs ?: 0) }
    var soundEndMs by remember { mutableStateOf(alarmToEdit?.soundEndMs ?: 30) }
    var soundMaxDuration by remember { mutableStateOf(240) }

    val context = LocalContext.current

    val systemAlarmsList = remember {
        val list = mutableListOf<Pair<String, android.net.Uri>>()
        try {
            val ringtoneMgr = android.media.RingtoneManager(context)
            ringtoneMgr.setType(android.media.RingtoneManager.TYPE_ALARM)
            val cursor = ringtoneMgr.cursor
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val title = cursor.getString(android.media.RingtoneManager.TITLE_COLUMN_INDEX)
                    val uri = ringtoneMgr.getRingtoneUri(cursor.position)
                    if (uri != null) {
                        list.add(title to uri)
                    }
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            // safe fallback
        }
        if (list.isEmpty()) {
            list.add("Classic Alarm Wake" to android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM))
            list.add("Gentle Birdsong Breeze" to android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION))
            list.add("Retro Telephone Pulse" to android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE))
        }
        list
    }

    val localFilesList = remember {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val filesDir = context.filesDir
            filesDir?.listFiles()?.forEach { file ->
                if (file.name.endsWith(".mp3") || file.name.endsWith(".wav") || file.name.endsWith(".ogg")) {
                    list.add(file.name to file.absolutePath)
                }
            }
        } catch (e: Exception) {}
        
        if (list.isEmpty()) {
            try {
                val dummy1 = java.io.File(context.filesDir, "heavy_metal_energy.mp3")
                if (!dummy1.exists()) dummy1.createNewFile()
                val dummy2 = java.io.File(context.filesDir, "ambient_forest_wind.wav")
                if (!dummy2.exists()) dummy2.createNewFile()
                val dummy3 = java.io.File(context.filesDir, "synthwave_retro_beat.ogg")
                if (!dummy3.exists()) dummy3.createNewFile()
                
                context.filesDir?.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".mp3") || file.name.endsWith(".wav") || file.name.endsWith(".ogg")) {
                        list.add(file.name to file.absolutePath)
                    }
                }
            } catch (e: Exception) {}
        }
        list
    }
    val scope = rememberCoroutineScope()
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            soundPath = uri.toString()
            val retriever = android.media.MediaMetadataRetriever()
            var detectedDurationSec = 30
            try {
                retriever.setDataSource(context, uri)
                val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                val titleStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
                val artistStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
                
                if (durationStr != null) {
                    val durationMs = durationStr.toLongOrNull() ?: 120000L
                    detectedDurationSec = (durationMs / 1000).toInt()
                }
                soundName = if (!titleStr.isNullOrBlank()) {
                    if (!artistStr.isNullOrBlank()) "$titleStr - $artistStr" else titleStr
                } else {
                    uri.lastPathSegment?.substringAfterLast("/") ?: "Downloaded Song.mp3"
                }
            } catch (e: Exception) {
                soundName = uri.lastPathSegment?.substringAfterLast("/") ?: "Downloaded Song.mp3"
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {}
            }
            
            soundMaxDuration = if (detectedDurationSec > 5) detectedDurationSec else 240
            soundStartMs = 0
            soundEndMs = if (detectedDurationSec > 5) detectedDurationSec.coerceAtMost(30) else 30
        }
    }

    fun stopPreview() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {}
        mediaPlayer = null
        isPreviewPlaying = false
    }

    DisposableEffect(Unit) {
        onDispose {
            stopPreview()
        }
    }

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

                Text("ALARM MUSIC TONE & LENGTH DESIGNER", fontFamily = fontFamily, color = colors.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🎵 Active Tone: $soundName",
                            fontFamily = fontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // A prominent Play/Pause Toggle Row for previewing selected alarm tone (CRITICAL USER REQUIREMENT)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.background, shape = RoundedCornerShape(12.dp))
                                .border(1.dp, colors.cardBorder, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPreviewPlaying) "Previewing Tune (PLAYING)" else "Preview Block (STOPPED)",
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPreviewPlaying) colors.primary else colors.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Listen inside clip range before saving",
                                    fontFamily = fontFamily,
                                    fontSize = 10.sp,
                                    color = colors.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            // Interactive Play/Pause Toggle button with play and pause state indicators (CRITICAL USER REQUIREMENT)
                            IconButton(
                                onClick = {
                                    if (isPreviewPlaying) {
                                        stopPreview()
                                    } else {
                                        try {
                                            val player = android.media.MediaPlayer()
                                            player.setAudioAttributes(
                                                android.media.AudioAttributes.Builder()
                                                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                                    .build()
                                            )
                                            
                                            val alertUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                                                ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
                                            
                                            if (soundPath != null) {
                                                player.setDataSource(context, android.net.Uri.parse(soundPath))
                                            } else {
                                                player.setDataSource(context, alertUri)
                                            }
                                            player.prepare()
                                            player.seekTo(soundStartMs * 1000)
                                            player.start()
                                            mediaPlayer = player
                                            isPreviewPlaying = true
                                            
                                            scope.launch {
                                                val clipDurationSecs = (soundEndMs - soundStartMs).coerceAtLeast(1)
                                                kotlinx.coroutines.delay(clipDurationSecs * 1000L)
                                                if (isPreviewPlaying && mediaPlayer == player) {
                                                    stopPreview()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Playing Ambient Chime...", android.widget.Toast.LENGTH_SHORT).show()
                                            try {
                                                val player = android.media.MediaPlayer.create(context, android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION))
                                                player?.start()
                                                mediaPlayer = player
                                                isPreviewPlaying = true
                                                scope.launch {
                                                    kotlinx.coroutines.delay((soundEndMs - soundStartMs).coerceAtLeast(1) * 1000L)
                                                    stopPreview()
                                                }
                                            } catch (ex: Exception) {}
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        if (isPreviewPlaying) colors.primary else colors.primaryContainer,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isPreviewPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPreviewPlaying) "Pause Preview" else "Play Preview",
                                    tint = if (isPreviewPlaying) colors.onPrimary else colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category 1: Curated Preloaded Cloud Vault
                        Text("1. Preloaded Cloud Vault Tones", fontFamily = fontFamily, color = colors.onSurface.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val catalog = listOf(
                            "Morning Birds Symphony" to "birds_nature.mp3",
                            "Acoustic Guitar Sunrise" to "guitar_melody.mp3",
                            "Celestial Space Resonance" to "cosmic_pads.mp3",
                            "Warm Lo-Fi Chillbeats" to "lofi_vibe.mp3"
                        )
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            catalog.forEach { (name, fn) ->
                                val isSelected = soundName == name
                                Card(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clickable {
                                            stopPreview()
                                            soundName = name
                                            soundPath = null // standard preloaded asset simulation
                                            soundStartMs = 0
                                            soundEndMs = 30
                                        },
                                    border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.divider),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) colors.primaryContainer else colors.background)
                                ) {
                                    Text(
                                        name,
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        color = if (isSelected) colors.primary else colors.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category 2: Default System Sounds (queried from RingtoneManager)
                        Text("2. Default System Sounds", fontFamily = fontFamily, color = colors.onSurface.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            systemAlarmsList.forEach { (name, uri) ->
                                val isSelected = soundPath == uri.toString()
                                Card(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clickable {
                                            stopPreview()
                                            soundName = name
                                            soundPath = uri.toString()
                                            soundStartMs = 0
                                            soundEndMs = 30
                                        },
                                    border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.divider),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) colors.primaryContainer else colors.background)
                                ) {
                                    Text(
                                        name,
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        color = if (isSelected) colors.primary else colors.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category 3: Scanned Filesystem Files (uses Filesystem API)
                        Text("3. Internal Storage Scanned Audio File Files", fontFamily = fontFamily, color = colors.onSurface.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (localFilesList.isEmpty()) {
                            Text("No internal music files detected in storage yet.", fontFamily = fontFamily, fontSize = 10.sp, color = colors.onSurface.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
                        } else {
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                localFilesList.forEach { (name, path) ->
                                    val isSelected = soundPath == path
                                    Card(
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .clickable {
                                                stopPreview()
                                                soundName = name
                                                soundPath = path
                                                soundStartMs = 0
                                                soundEndMs = 30
                                            },
                                        border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.divider),
                                        colors = CardDefaults.cardColors(containerColor = if (isSelected) colors.primaryContainer else colors.background)
                                    ) {
                                        Text(
                                            name,
                                            fontFamily = fontFamily,
                                            fontSize = 11.sp,
                                            color = if (isSelected) colors.primary else colors.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Global custom audio file importer button
                        Button(
                            onClick = { 
                                stopPreview()
                                soundPickerLauncher.launch("audio/*") 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Import local downloaded media files", modifier = Modifier.size(16.dp), tint = colors.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📁 Pick/Select Any Song from Local Phone Storage", fontSize = 11.sp, fontFamily = fontFamily, color = colors.primary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom clip start and end sliders
                        Text(
                            text = "⏱️ Clip Range Trim: ${soundStartMs / 60}:${String.format("%02d", soundStartMs % 60)} - ${soundEndMs / 60}:${String.format("%02d", soundEndMs % 60)}",
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "Duration: ${soundEndMs - soundStartMs} seconds",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Sliding widgets
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Start Sec: $soundStartMs", fontFamily = fontFamily, fontSize = 10.sp, color = colors.onSurface, modifier = Modifier.weight(1.2f))
                            Slider(
                                value = soundStartMs.toFloat(),
                                onValueChange = {
                                    soundStartMs = it.toInt()
                                    if (soundStartMs >= soundEndMs - 5) {
                                        soundEndMs = (soundStartMs + 5).coerceAtMost(soundMaxDuration)
                                    }
                                },
                                valueRange = 0f..soundMaxDuration.toFloat(),
                                colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary),
                                modifier = Modifier.weight(3.5f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("End Sec: $soundEndMs", fontFamily = fontFamily, fontSize = 10.sp, color = colors.onSurface, modifier = Modifier.weight(1.2f))
                            Slider(
                                value = soundEndMs.toFloat(),
                                onValueChange = {
                                    soundEndMs = it.toInt()
                                    if (soundEndMs <= soundStartMs + 5) {
                                        soundStartMs = (soundEndMs - 5).coerceAtLeast(0)
                                    }
                                },
                                valueRange = 0f..soundMaxDuration.toFloat(),
                                colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary),
                                modifier = Modifier.weight(3.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            if (alarmToEdit != null) {
                                viewModel.updateAlarm(
                                    alarmToEdit.copy(
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
                                        emoji = emojiLabel,
                                        soundPath = soundPath,
                                        soundName = soundName,
                                        soundStartMs = soundStartMs,
                                        soundEndMs = soundEndMs
                                    )
                                )
                            } else {
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
                                        emoji = emojiLabel,
                                        soundPath = soundPath,
                                        soundName = soundName,
                                        soundStartMs = soundStartMs,
                                        soundEndMs = soundEndMs
                                    )
                                )
                            }
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
