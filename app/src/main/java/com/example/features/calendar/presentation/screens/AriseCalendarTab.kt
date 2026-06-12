package com.example.features.calendar.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.database.Alarm
import com.example.core.database.CalendarEvent
import com.example.core.designsystem.CustomColorScheme
import com.example.features.calendar.presentation.viewmodel.CalendarViewModel
import com.example.features.alarms.presentation.viewmodel.AlarmViewModel
import com.example.features.calendar.presentation.viewmodel.SmartAlarmViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.features.alarms.presentation.screens.AriseAlarmCard
import com.example.features.alarms.presentation.screens.AriseAlarmDesignerDialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AriseCalendarTab(
    viewModel: CalendarViewModel,
    alarmViewModel: AlarmViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    smartAlarmViewModel: SmartAlarmViewModel = viewModel()
) {
    val eventsList by viewModel.events.collectAsState()
    val alarmsList by viewModel.alarms.collectAsState()
    var showAddEvent by remember { mutableStateOf(false) }
    var showAddAlarmDirect by remember { mutableStateOf(false) }
    var showAddChoiceDialog by remember { mutableStateOf(false) }
    var showConflictFreeSlotsDialog by remember { mutableStateOf(false) }

    var eventToEdit by remember { mutableStateOf<CalendarEvent?>(null) }
    var showEditEvent by remember { mutableStateOf(false) }

    var alarmToEdit by remember { mutableStateOf<Alarm?>(null) }
    var showEditAlarm by remember { mutableStateOf(false) }
    var showYearNavigator by remember { mutableStateOf(false) }

    // Google Calendar Sync States
    val googleSyncStatus by viewModel.googleSyncStatus.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    var showGoogleSyncDialog by remember { mutableStateOf(false) }
    var tokenInputText by remember { mutableStateOf("") }

    // Smart Alarm State Collections
    val context = LocalContext.current
    val smartBufferMinutes by smartAlarmViewModel.bufferMinutes.collectAsState()
    val smartAutoAdjust by smartAlarmViewModel.autoAdjustEnabled.collectAsState()
    val smartMasterDefaultTime by smartAlarmViewModel.masterDefaultAlarmTime.collectAsState()
    val smartCalculationResult by smartAlarmViewModel.calculationResult.collectAsState()
    val smartIsCalculating by smartAlarmViewModel.isCalculating.collectAsState()
    val smartIsSaving by smartAlarmViewModel.isSaving.collectAsState()
    val smartStatusText by smartAlarmViewModel.smartAlarmStatusText.collectAsState()

    LaunchedEffect(googleSyncStatus) {
        if (googleSyncStatus.isNotEmpty()) {
            android.widget.Toast.makeText(context, googleSyncStatus, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Calendar navigation states
    val calendarInstance = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(calendarInstance.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(calendarInstance.get(Calendar.MONTH)) } // 0-indexed
    var selectedDay by remember { mutableStateOf(calendarInstance.get(Calendar.DAY_OF_MONTH)) }

    // Currently selected date representation
    val selectedDateCalendar = remember(currentYear, currentMonth, selectedDay) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    // Number of days in the currently selecting month
    val daysInMonth = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // Week day offset of the 1st of the month (0 = Sunday, 1 = Monday, ...)
    val firstDayOfWeekOffset = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed offset (0=Sun, 1=Mon, ...)
    }

    // Events booked on the selected calendar date
    val activeEventsOnDay = remember(eventsList, selectedDateCalendar, currentYear, currentMonth, selectedDay) {
        val startMs = selectedDateCalendar.timeInMillis
        val endMs = startMs + 24 * 60 * 60 * 1000L
        eventsList.filter { it.startTime >= startMs && it.startTime < endMs }
    }

    val selectedDayOfWeek = remember(selectedDateCalendar) {
        val dayOfWeekNames = listOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val idx = selectedDateCalendar.get(Calendar.DAY_OF_WEEK)
        if (idx in 1..7) dayOfWeekNames[idx] else ""
    }

    val activeAlarmsOnDay = remember(alarmsList, selectedDayOfWeek) {
        alarmsList.filter { alarm ->
            !alarm.label.contains("Event:") &&
            (alarm.repeatDays.contains("Daily", ignoreCase = true) ||
             alarm.repeatDays.contains(selectedDayOfWeek, ignoreCase = true) ||
             alarm.repeatDays.contains("One-time", ignoreCase = true))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Google Calendar Synchronization Strip Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("google_sync_banner_card"),
                colors = CardDefaults.cardColors(containerColor = colors.primaryContainer.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GOOGLE CALENDAR",
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                        if (googleSyncStatus.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = googleSyncStatus,
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                color = colors.onBackground,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Synchronize upcoming meetings with alarm triggers.",
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                color = colors.onBackground.copy(alpha = 0.65f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        val currentTz = remember { java.util.TimeZone.getDefault() }
                        val tzId = currentTz.id
                        val dName = currentTz.getDisplayName(currentTz.inDaylightTime(java.util.Date()), java.util.TimeZone.SHORT)
                        val rawOffset = currentTz.rawOffset
                        val dstOffset = if (currentTz.inDaylightTime(java.util.Date())) currentTz.dstSavings else 0
                        val totalOffsetHours = (rawOffset + dstOffset) / (3600 * 1000f)
                        Text(
                            text = "🌐 Zone: $tzId ($dName, UTC%+g)".format(totalOffsetHours),
                            fontFamily = fontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.secondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { showGoogleSyncDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.primary.copy(alpha = 0.2f), CircleShape)
                                .testTag("btn_manage_google_conn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Manage Token",
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.syncWithGoogleCalendar(null) },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_sync_google_now")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    color = colors.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sync",
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

            var isSmartExpanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("smart_alarm_calculator_card"),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSmartExpanded = !isSmartExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SMART WAKE CALCULATOR",
                                fontFamily = fontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                        IconButton(
                            onClick = { isSmartExpanded = !isSmartExpanded },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isSmartExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isSmartExpanded) "Collapse" else "Expand",
                                tint = colors.primary
                            )
                        }
                    }

                    if (isSmartExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Arise links your Calendar and Alarm engines to calculate your optimal sleep window.",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onBackground.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch & Settings Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Schedule Smart Alarm",
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "Auto adjusts the smart alarm when calendar changes are detected.",
                                    fontFamily = fontFamily,
                                    fontSize = 10.sp,
                                    color = colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = smartAutoAdjust,
                                onCheckedChange = { smartAlarmViewModel.toggleAutoAdjust(it) },
                                modifier = Modifier.testTag("switch_auto_adjust_smart_alarm")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Master Default Alarm Setup Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Master Default Alarm",
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "Fallback wake time if no calendar events are found.",
                                    fontFamily = fontFamily,
                                    fontSize = 10.sp,
                                    color = colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            
                            var isEditingTime by remember { mutableStateOf(false) }
                            var editingTimeText by remember { mutableStateOf(smartMasterDefaultTime) }
                            
                            if (isEditingTime) {
                                OutlinedTextField(
                                    value = editingTimeText,
                                    onValueChange = { editingTimeText = it },
                                    modifier = Modifier
                                        .width(95.dp)
                                        .height(48.dp)
                                        .testTag("input_master_default_alarm"),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    ),
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                if (editingTimeText.matches(Regex("^[0-2][0-9]:[0-5][0-9]$"))) {
                                                    smartAlarmViewModel.setMasterDefaultAlarmTime(editingTimeText)
                                                    isEditingTime = false
                                                }
                                            },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Save time",
                                                tint = colors.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                )
                            } else {
                                FilterChip(
                                    selected = true,
                                    onClick = {
                                        editingTimeText = smartMasterDefaultTime
                                        isEditingTime = true
                                    },
                                    label = {
                                        Text(
                                            text = smartMasterDefaultTime,
                                            fontFamily = fontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primaryContainer,
                                        selectedLabelColor = colors.onSurface
                                    ),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit default time",
                                            modifier = Modifier.size(12.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("btn_edit_master_default_alarm")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Buffer Time Set
                        Text(
                            text = "Wake Buffer: $smartBufferMinutes minutes before first event",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(45, 60, 90, 120).forEach { mins ->
                                val isSelected = (smartBufferMinutes == mins)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { smartAlarmViewModel.setBufferMinutes(mins) },
                                    label = { Text("$mins m", fontFamily = fontFamily, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary,
                                        selectedLabelColor = colors.onPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chip_buffer_$mins")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Real-Time Dynamic Wake-up Status Badge
                        if (smartCalculationResult != null) {
                            val result = smartCalculationResult!!
                            val badgeText = if (result.isFallback) {
                                "Wake up at ${result.calculatedWakeTimeStr.substringAfter(", ")} (Default Alarm - No events tomorrow)"
                            } else {
                                "Wake up at ${result.calculatedWakeTimeStr.substringAfter(", ")} based on your '${result.nextDayEvent?.title}' event"
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (result.isFallback) colors.surface else colors.primaryContainer
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (result.isFallback) colors.onSurface.copy(alpha = 0.15f) else colors.secondary.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("smart_alarm_status_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (result.isFallback) Icons.Default.Info else Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (result.isFallback) colors.primary else colors.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = badgeText,
                                        fontFamily = fontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Render Calculation Status Panel
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.primaryContainer.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (smartCalculationResult?.nextDayEvent != null) {
                                    val result = smartCalculationResult!!
                                    val event = result.nextDayEvent!!
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Tomorrow's First Event (via ${event.source})",
                                            fontFamily = fontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = event.title,
                                        fontFamily = fontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "Starts At: ${result.originalEventTimeStr}",
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        color = colors.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = colors.primary.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = colors.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Dynamic Wake-Up Time",
                                            fontFamily = fontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = result.calculatedWakeTimeStr,
                                        fontFamily = fontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.secondary
                                    )
                                } else if (smartCalculationResult?.isFallback == true) {
                                    val result = smartCalculationResult!!
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "No Events Tomorrow (Fallback Active)",
                                            fontFamily = fontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Using Master Default Alarm",
                                        fontFamily = fontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "Configured fallback: $smartMasterDefaultTime",
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        color = colors.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = colors.primary.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = colors.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Default Wake-Up Time",
                                            fontFamily = fontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = result.calculatedWakeTimeStr,
                                        fontFamily = fontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors.secondary
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsOff,
                                            contentDescription = null,
                                            tint = colors.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "No Smart Calculator analysis performed yet.",
                                            fontFamily = fontFamily,
                                            fontSize = 11.sp,
                                            color = colors.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }

                        if (smartStatusText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = smartStatusText,
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { smartAlarmViewModel.calculateSmartAlarm(context) },
                                enabled = !smartIsCalculating,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_analyse_smart_shedule")
                            ) {
                                if (smartIsCalculating) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scan Event", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { smartAlarmViewModel.saveAndScheduleSmartAlarm(context) },
                                enabled = !smartIsSaving && smartCalculationResult?.calculatedHour != null,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(44.dp)
                                    .testTag("btn_save_smart_alarm")
                            ) {
                                if (smartIsSaving) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Apply Alarm", color = Color.White, fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Calendar Month Navigation Panel Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Month & Year Swapper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val monthsArray = listOf(
                            "January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December"
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = " << ",
                                fontFamily = fontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.primary.copy(alpha = 0.61f),
                                modifier = Modifier
                                    .clickable {
                                        currentYear -= 1
                                        selectedDay = 1
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("prev_year_btn")
                            )
                            IconButton(
                                onClick = {
                                    if (currentMonth == 0) {
                                        currentMonth = 11
                                        currentYear -= 1
                                    } else {
                                        currentMonth -= 1
                                    }
                                    selectedDay = 1 // reset to first of month
                                }
                            ) {
                                Icon(Icons.Default.ArrowLeft, contentDescription = "Prev Month", tint = colors.primary)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showYearNavigator = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("btn_open_year_navigator")
                        ) {
                            Text(
                                text = "${monthsArray[currentMonth]} $currentYear",
                                fontFamily = fontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Open Year Navigator",
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (currentMonth == 11) {
                                        currentMonth = 0
                                        currentYear += 1
                                    } else {
                                        currentMonth += 1
                                    }
                                    selectedDay = 1 // reset to first of month
                                }
                            ) {
                                Icon(Icons.Default.ArrowRight, contentDescription = "Next Month", tint = colors.primary)
                            }
                            Text(
                                text = " >> ",
                                fontFamily = fontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.primary.copy(alpha = 0.61f),
                                modifier = Modifier
                                    .clickable {
                                        currentYear += 1
                                        selectedDay = 1
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("next_year_btn")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days of Week Header Indicators
                    val weekDays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekDays.forEach { dayName ->
                            Text(
                                text = dayName,
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic Grid Renderer for Month days
                    val totalTiles = daysInMonth + firstDayOfWeekOffset
                    val rowCount = (totalTiles + 6) / 7

                    Column {
                        for (row in 0 until rowCount) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (col in 0 until 7) {
                                    val index = row * 7 + col
                                    val cellDay = index - firstDayOfWeekOffset + 1

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.1f)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (cellDay in 1..daysInMonth) {
                                            val isSelected = (selectedDay == cellDay)

                                            // Count events on this day
                                            val dayCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, currentYear)
                                                set(Calendar.MONTH, currentMonth)
                                                set(Calendar.DAY_OF_MONTH, cellDay)
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            val dayStart = dayCal.timeInMillis
                                            val dayEnd = dayStart + 24 * 60 * 60 * 1000L
                                            val eventsOnCell = eventsList.filter { it.startTime >= dayStart && it.startTime < dayEnd }
                                            val hasMatchingEvent = eventsOnCell.isNotEmpty()

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) colors.primaryContainer else Color.Transparent)
                                                    .border(
                                                        width = if (isSelected) 1.dp else 0.dp,
                                                        color = if (isSelected) colors.primary else Color.Transparent,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { selectedDay = cellDay }
                                            ) {
                                                Text(
                                                    text = cellDay.toString(),
                                                    fontFamily = fontFamily,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isSelected) colors.primary else colors.onSurface
                                                )

                                                if (hasMatchingEvent) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Max 3 dots for visual beauty
                                                        eventsOnCell.take(3).forEach { ev ->
                                                            val dotColor = try {
                                                                Color(android.graphics.Color.parseColor(ev.colorHex))
                                                            } catch (e: Exception) {
                                                                colors.primary
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(horizontal = 1.dp)
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(dotColor)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Agenda List Header with Date Selection Tracker
            val dateLabelStr = remember(selectedDateCalendar) {
                SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(selectedDateCalendar.time)
            }
            Text(
                text = "📅 Agenda: $dateLabelStr",
                fontFamily = fontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Dynamic list showing selected day events & alarms or a cute empty slate
            val dayHasAgenda = activeEventsOnDay.isNotEmpty() || activeAlarmsOnDay.isNotEmpty()
            if (!dayHasAgenda) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "No Events Today",
                        tint = colors.onBackground.copy(alpha = 0.25f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your slate is clear for today",
                        fontFamily = fontFamily,
                        color = colors.onBackground.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    if (activeEventsOnDay.isNotEmpty()) {
                        val sortedTodayEvents = activeEventsOnDay.sortedBy { it.startTime }
                        val periodsOverlapList = mutableListOf<Pair<CalendarEvent, CalendarEvent>>()
                        for (i in 0 until sortedTodayEvents.size - 1) {
                            val evA = sortedTodayEvents[i]
                            val evB = sortedTodayEvents[i+1]
                            if (evB.startTime < evA.endTime) {
                                periodsOverlapList.add(evA to evB)
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📅 EVENTS & PLANNER",
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                                Button(
                                    onClick = { showConflictFreeSlotsDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, colors.secondary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Free Slots 💡", fontFamily = fontFamily, fontSize = 10.sp, color = colors.secondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (periodsOverlapList.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.secondary.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, colors.secondary.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "⚠️ OVERLAP CONFLICT DETECTED!",
                                                fontFamily = fontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.secondary
                                            )
                                            val conflictNames = periodsOverlapList.map { "${it.first.title} & ${it.second.title}" }.joinToString(", ")
                                            Text(
                                                "Overlaps: $conflictNames",
                                                fontFamily = fontFamily,
                                                fontSize = 11.sp,
                                                color = colors.onSurface.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        items(activeEventsOnDay) { event ->
                            AriseEventCard(
                                viewModel = viewModel,
                                event = event,
                                colors = colors,
                                fontFamily = fontFamily,
                                onEditClick = {
                                    eventToEdit = event
                                    showEditEvent = true
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(10.dp)) }
                    }

                    if (activeAlarmsOnDay.isNotEmpty()) {
                        item {
                            Text(
                                text = "⏰ ACTIVE ALARMS FOR THIS DAY",
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(activeAlarmsOnDay) { alarm ->
                            AriseAlarmCard(
                                viewModel = alarmViewModel,
                                alarm = alarm,
                                colors = colors,
                                fontFamily = fontFamily,
                                onEditClick = {
                                    alarmToEdit = alarm
                                    showEditAlarm = true
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(110.dp))
                    }
                }
            }
        }

        // Floating Action Button to book directly on the selected date
        FloatingActionButton(
            onClick = { showAddChoiceDialog = true },
            containerColor = colors.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_event_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Book event for selected day", tint = colors.onPrimary)
        }

        if (showAddChoiceDialog) {
            Dialog(onDismissRequest = { showAddChoiceDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CHOOSE PLANNER TYPE",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                showAddChoiceDialog = false
                                showAddEvent = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = colors.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Book Calendar Event", color = colors.onPrimary, fontFamily = fontFamily)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                showAddChoiceDialog = false
                                showAddAlarmDirect = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Specific Alarm", color = Color.White, fontFamily = fontFamily)
                        }
                    }
                }
            }
        }

        if (showAddEvent) {
            AriseEventDesignerDialog(
                viewModel = viewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showAddEvent = false },
                prefillDay = selectedDay,
                prefillMonth = currentMonth,
                prefillYear = currentYear
            )
        }

        if (showConflictFreeSlotsDialog) {
            val sortedTodayEvents = activeEventsOnDay.sortedBy { it.startTime }
            val freeSlotsList = mutableListOf<String>()
            var lastEndMin = 8 * 60 // 8:00 AM
            val targetEndMin = 22 * 60 // 10:00 PM
            sortedTodayEvents.forEach { ev ->
                val evCal = Calendar.getInstance().apply { timeInMillis = ev.startTime }
                val startH = evCal.get(Calendar.HOUR_OF_DAY)
                val startM = evCal.get(Calendar.MINUTE)
                val startDayMin = startH * 60 + startM

                val durCal = Calendar.getInstance().apply { timeInMillis = ev.endTime }
                val endH = durCal.get(Calendar.HOUR_OF_DAY)
                val endM = durCal.get(Calendar.MINUTE)
                val endDayMin = endH * 60 + endM

                if (startDayMin > lastEndMin + 15) {
                    val freeFrom = String.format("%02d:%02d", lastEndMin / 60, lastEndMin % 60)
                    val freeTo = String.format("%02d:%02d", startDayMin / 60, startDayMin % 60)
                    freeSlotsList.add("$freeFrom - $freeTo")
                }
                lastEndMin = maxOf(lastEndMin, endDayMin)
            }
            if (lastEndMin < targetEndMin - 15) {
                val freeFrom = String.format("%02d:%02d", lastEndMin / 60, lastEndMin % 60)
                val freeTo = String.format("%02d:%02d", targetEndMin / 60, targetEndMin % 60)
                freeSlotsList.add("$freeFrom - $freeTo")
            }
            if (freeSlotsList.isEmpty() && sortedTodayEvents.isEmpty()) {
                freeSlotsList.add("08:00 - 22:00 (All Day Open)")
            } else if (freeSlotsList.isEmpty()) {
                freeSlotsList.add("No major block between 08:00 and 22:00")
            }

            AlertDialog(
                onDismissRequest = { showConflictFreeSlotsDialog = false },
                title = {
                    Text(
                        "💡 SMART AGENDA SUGGESTER",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.primary
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Based on your daily planner, here are your optimal available schedules (8:00 AM to 10:00 PM):",
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = colors.onBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        freeSlotsList.forEach { slot ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = slot,
                                        fontFamily = fontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.onBackground
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showConflictFreeSlotsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Awesome!", color = colors.onPrimary, fontFamily = fontFamily)
                    }
                }
            )
        }

        if (showAddAlarmDirect) {
            AriseAlarmDesignerDialog(
                viewModel = alarmViewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showAddAlarmDirect = false }
            )
        }

        if (showEditEvent && eventToEdit != null) {
            AriseEventDesignerDialog(
                viewModel = viewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showEditEvent = false; eventToEdit = null },
                prefillDay = selectedDay,
                prefillMonth = currentMonth,
                prefillYear = currentYear,
                eventToEdit = eventToEdit
            )
        }

        if (showEditAlarm && alarmToEdit != null) {
            AriseAlarmDesignerDialog(
                viewModel = alarmViewModel,
                colors = colors,
                fontFamily = fontFamily,
                alarmToEdit = alarmToEdit,
                onDismiss = { showEditAlarm = false; alarmToEdit = null }
            )
        }

        if (showYearNavigator) {
            AriseYearNavigatorDialog(
                colors = colors,
                fontFamily = fontFamily,
                initialYear = currentYear,
                initialMonth = currentMonth,
                eventsList = eventsList,
                onMonthYearSelected = { month, year ->
                    currentMonth = month
                    currentYear = year
                    selectedDay = 1 // reset to first of month on jump
                    showYearNavigator = false
                },
                onDismiss = { showYearNavigator = false }
            )
        }

        // Google Sync Settings Credential Dialog
        if (showGoogleSyncDialog) {
            Dialog(onDismissRequest = { showGoogleSyncDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Connect Google Calendar",
                                fontFamily = fontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            IconButton(onClick = { showGoogleSyncDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "To synchronize live events, paste your Google OAuth Access Token below.",
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = tokenInputText,
                            onValueChange = { tokenInputText = it },
                            label = { Text("Paste Google OAuth Token", color = colors.onSurface.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_token_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Don't have a token? Tap below to pre-populate with the high-fidelity Sandbox Simulation Feed.",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    tokenInputText = "demo_mode"
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_use_demo_token")
                            ) {
                                Text("Use Sandbox", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    viewModel.syncWithGoogleCalendar(tokenInputText)
                                    showGoogleSyncDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_save_google_token")
                        ) {
                            Text("Save & Sync", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.onPrimary)
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun AriseEventCard(
    viewModel: CalendarViewModel,
    event: CalendarEvent,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    onEditClick: ((CalendarEvent) -> Unit)? = null
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("event_card_${event.id}"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.cardBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val labelColor = remember(event.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(event.colorHex))
                        } catch (e: Exception) {
                            colors.primary
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(labelColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.title,
                        fontFamily = fontFamily,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onEditClick != null) {
                        IconButton(
                            onClick = { onEditClick(event) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit event", tint = colors.primary.copy(alpha = 0.8f))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = { viewModel.deleteEvent(event) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete event", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startStr = timeFormat.format(Date(event.startTime))
            val lenMin = (event.endTime - event.startTime) / 60000
            val sublineText = "⏰ $startStr (${lenMin}m) • Priority: ${event.priority}"

            Text(
                text = sublineText,
                fontFamily = fontFamily,
                fontSize = 11.sp,
                color = colors.onSurface.copy(alpha = 0.75f)
            )

            if (event.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📝 notes: ${event.notes}",
                    fontFamily = fontFamily,
                    fontSize = 11.sp,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }

            // Linked alarms indication
            if ((event.linkedAlarmId ?: 0) > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Ringing alarm sync",
                        tint = colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real Ringing Alarm Reminder Active for this Event",
                        fontFamily = fontFamily,
                        fontSize = 10.sp,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Column {
                    Text(
                        text = "Create Alarm Reminder:",
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0 to "At Start",
                            15 to "15m Before",
                            30 to "30m Before"
                        ).forEach { (offsetMinutes, label) ->
                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = event.startTime
                                        add(Calendar.MINUTE, -offsetMinutes)
                                    }
                                    val alarmHour = cal.get(Calendar.HOUR_OF_DAY)
                                    val alarmMinute = cal.get(Calendar.MINUTE)

                                    val targetLabel = if (offsetMinutes == 0) {
                                        "⏰ Start: ${event.title.take(12)}"
                                    } else {
                                        "⏰ -${offsetMinutes}m: ${event.title.take(12)}"
                                    }

                                    viewModel.linkAlarmToEvent(
                                        event,
                                        Alarm(
                                            label = targetLabel,
                                            description = "Alarm reminder for ${event.title} starting soon",
                                            hour = alarmHour,
                                            minute = alarmMinute,
                                            repeatDays = "One-time",
                                            emoji = "⏰",
                                            snoozeEnabled = true,
                                            snoozeDurationMinutes = 5,
                                            snoozeLimit = 3
                                        )
                                    )
                                    android.widget.Toast.makeText(context, "Alarm reminder configured at " + String.format("%02d:%02d", alarmHour, alarmMinute), android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .testTag("link_alarm_${event.id}_$offsetMinutes")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontFamily = fontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AriseEventDesignerDialog(
    viewModel: CalendarViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    onDismiss: () -> Unit,
    prefillDay: Int,
    prefillMonth: Int,
    prefillYear: Int,
    eventToEdit: CalendarEvent? = null
) {
    var title by remember { mutableStateOf(eventToEdit?.title ?: "") }
    var notes by remember { mutableStateOf(eventToEdit?.notes ?: "") }
    
    val initialStartHour = remember(eventToEdit) {
        if (eventToEdit != null) {
            val cal = Calendar.getInstance().apply { timeInMillis = eventToEdit.startTime }
            cal.get(Calendar.HOUR_OF_DAY)
        } else {
            9
        }
    }
    var startHour by remember { mutableStateOf(initialStartHour) }
    
    val initialDuration = remember(eventToEdit) {
        if (eventToEdit != null) {
            ((eventToEdit.endTime - eventToEdit.startTime) / 60000L).toInt()
        } else {
            60
        }
    }
    var durationMins by remember { mutableStateOf(initialDuration) }
    
    var category by remember { mutableStateOf(eventToEdit?.category ?: "Work") }
    var colorHex by remember { mutableStateOf(eventToEdit?.colorHex ?: "#4CAF50") }
    var priority by remember { mutableStateOf(eventToEdit?.priority ?: "High") }
    
    // Custom Alarm options
    val reminderOptions = listOf(
        "No Alarm" to "None",
        "Ringing Alarm (Exact Start)" to "Exact",
        "Ringing Alarm (15m before)" to "15m",
        "Ringing Alarm (30m before)" to "30m"
    )
    
    val initialReminderStyle = remember(eventToEdit) {
        if (eventToEdit != null) {
            if ((eventToEdit.linkedAlarmId ?: 0) > 0) {
                if (eventToEdit.prepTimeMinutes == 15) "15m"
                else if (eventToEdit.prepTimeMinutes == 30) "30m"
                else "Exact"
            } else {
                "None"
            }
        } else {
            "Exact"
        }
    }
    var selectedReminderStyle by remember { mutableStateOf(initialReminderStyle) }

    val monthsLabels = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "BOOK CALENDAR EVENT & ALARM",
                    fontFamily = fontFamily,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Target Date: $prefillDay ${monthsLabels[prefillMonth]} $prefillYear",
                    fontFamily = fontFamily,
                    color = colors.onSurface.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title", fontFamily = fontFamily) },
                    placeholder = { Text("e.g. Sales Standup Meeting", fontFamily = fontFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedLabelColor = colors.primary,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background,
                        unfocusedBorderColor = colors.onSurface.copy(alpha = 0.3f),
                        focusedBorderColor = colors.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Checked notes", fontFamily = fontFamily) },
                    placeholder = { Text("Include agendas, links...", fontFamily = fontFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedLabelColor = colors.primary,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background,
                        unfocusedBorderColor = colors.onSurface.copy(alpha = 0.3f),
                        focusedBorderColor = colors.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Start Clock Time: ${String.format("%02d:00", startHour)}", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = startHour.toFloat(),
                    onValueChange = { startHour = it.toInt() },
                    valueRange = 0f..23f,
                    colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary)
                )

                Text("Duration Length: $durationMins minutes", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = durationMins.toFloat(),
                    onValueChange = { durationMins = it.toInt() },
                    valueRange = 15f..180f,
                    colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Priority tag swapper
                Text("Priority Rating", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    listOf("High", "Medium", "Low").forEach { p ->
                        val active = priority == p
                        Button(
                            onClick = { priority = p },
                            colors = ButtonDefaults.buttonColors(containerColor = if (active) colors.primary else colors.primaryContainer),
                            modifier = Modifier.padding(end = 6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(p, color = if (active) colors.onPrimary else colors.onBackground, fontSize = 11.sp, fontFamily = fontFamily)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ringing Alarm Trigger preferences (Unified integration!)
                Text("⏰ Linked Alarm Action Option", fontFamily = fontFamily, color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                reminderOptions.forEach { (label, key) ->
                    val isSelected = selectedReminderStyle == key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReminderStyle = key }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedReminderStyle = key },
                            colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(label, fontFamily = fontFamily, fontSize = 12.sp, color = colors.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
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
                            if (title.isBlank()) {
                                title = "Unnamed Event"
                            }
                            
                            val startCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, prefillYear)
                                set(Calendar.MONTH, prefillMonth)
                                set(Calendar.DAY_OF_MONTH, prefillDay)
                                set(Calendar.HOUR_OF_DAY, startHour)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val startMs = startCal.timeInMillis
                            val endMs = startMs + (durationMins * 60000L)

                            val autoAlarmLinkedId = if (selectedReminderStyle != "None") {
                                eventToEdit?.linkedAlarmId ?: (1000 + startHour + (System.currentTimeMillis().toInt() % 10000))
                            } else {
                                null
                            }

                            // If reminder type changed to "None" and they had an alarm, let's delete it
                            if (selectedReminderStyle == "None" && eventToEdit?.linkedAlarmId != null) {
                                viewModel.deleteAlarm(
                                    Alarm(
                                        id = eventToEdit.linkedAlarmId,
                                        label = "",
                                        description = "",
                                        hour = 0,
                                        minute = 0,
                                        repeatDays = ""
                                    )
                                )
                            }

                            val eventToSave = if (eventToEdit != null) {
                                eventToEdit.copy(
                                    title = title,
                                    notes = notes,
                                    startTime = startMs,
                                    endTime = endMs,
                                    category = category,
                                    colorHex = colorHex,
                                    priority = priority,
                                    linkedAlarmId = autoAlarmLinkedId,
                                    prepTimeMinutes = if (selectedReminderStyle == "15m") 15 else if (selectedReminderStyle == "30m") 30 else 0
                                )
                            } else {
                                CalendarEvent(
                                    title = title,
                                    notes = notes,
                                    startTime = startMs,
                                    endTime = endMs,
                                    location = "Room A (Fused)",
                                    category = category,
                                    colorHex = colorHex,
                                    priority = priority,
                                    linkedAlarmId = autoAlarmLinkedId,
                                    prepTimeMinutes = if (selectedReminderStyle == "15m") 15 else if (selectedReminderStyle == "30m") 30 else 0
                                )
                            }

                            viewModel.insertEvent(eventToSave)

                            // Programmatically add or update the real single-time alarm into Room database!
                            if (selectedReminderStyle != "None") {
                                val alarmHour: Int
                                val alarmMinute: Int
                                when (selectedReminderStyle) {
                                    "15m" -> {
                                        alarmHour = if (startHour == 0) 23 else startHour - 1
                                        alarmMinute = 45
                                    }
                                    "30m" -> {
                                        alarmHour = if (startHour == 0) 23 else startHour - 1
                                        alarmMinute = 30
                                    }
                                    else -> {
                                        alarmHour = startHour
                                        alarmMinute = 0
                                    }
                                }

                                viewModel.updateAlarm(
                                    Alarm(
                                        id = autoAlarmLinkedId ?: 0,
                                        label = "📅 Event: $title",
                                        description = "Created automatically from calendar agenda.",
                                        hour = alarmHour,
                                        minute = alarmMinute,
                                        repeatDays = "One-time",
                                        emoji = "📅",
                                        challengeType = "None",
                                        challengeDifficulty = "Easy",
                                        soundPath = null,
                                        soundName = "Calendar Chime Accent",
                                        soundStartMs = 0,
                                        soundEndMs = 30
                                    )
                                )
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_event_button")
                    ) {
                        Text("Book & Sync", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AriseYearNavigatorDialog(
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    initialYear: Int,
    initialMonth: Int,
    eventsList: List<CalendarEvent>,
    onMonthYearSelected: (month: Int, year: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tempYear by remember { mutableStateOf(initialYear) }
    val monthsLabels = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("year_navigator_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YEAR NAVIGATION DECK",
                    fontFamily = fontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary.copy(alpha = 0.8f),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Year Switcher Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { tempYear -= 5 },
                        modifier = Modifier.size(36.dp).testTag("btn_prev_5_years")
                    ) {
                        Text(
                            text = "«",
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = fontFamily
                        )
                    }

                    IconButton(
                        onClick = { tempYear -= 1 },
                        modifier = Modifier.size(36.dp).testTag("btn_prev_year")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Year",
                            tint = colors.primary
                        )
                    }

                    Text(
                        text = "$tempYear",
                        fontFamily = fontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primary,
                        modifier = Modifier.testTag("navigator_current_year")
                    )

                    IconButton(
                        onClick = { tempYear += 1 },
                        modifier = Modifier.size(36.dp).testTag("btn_next_year")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Year",
                            tint = colors.primary
                        )
                    }

                    IconButton(
                        onClick = { tempYear += 5 },
                        modifier = Modifier.size(36.dp).testTag("btn_next_5_years")
                    ) {
                        Text(
                            text = "»",
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            fontFamily = fontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3x4 Grid of months
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (col in 0 until 3) {
                                val monthIdx = row * 3 + col
                                val isSelectedMonth = monthIdx == initialMonth && tempYear == initialYear

                                // Calculate events in this specific month & year
                                val eventCount = remember(eventsList, tempYear, monthIdx) {
                                    val startCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, tempYear)
                                        set(Calendar.MONTH, monthIdx)
                                        set(Calendar.DAY_OF_MONTH, 1)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    val startMs = startCal.timeInMillis
                                    val endCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, tempYear)
                                        set(Calendar.MONTH, monthIdx)
                                        val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                                        set(Calendar.DAY_OF_MONTH, maxDay)
                                        set(Calendar.HOUR_OF_DAY, 23)
                                        set(Calendar.MINUTE, 59)
                                        set(Calendar.SECOND, 59)
                                        set(Calendar.MILLISECOND, 999)
                                    }
                                    val endMs = endCal.timeInMillis
                                    eventsList.filter { it.startTime >= startMs && it.startTime <= endMs }.size
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .clickable {
                                            onMonthYearSelected(monthIdx, tempYear)
                                        }
                                        .testTag("month_btn_$monthIdx"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = if (isSelectedMonth) 2.dp else 1.dp,
                                        color = if (isSelectedMonth) colors.primary else colors.cardBorder
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelectedMonth) colors.primary.copy(alpha = 0.12f) else colors.surface.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = monthsLabels[monthIdx],
                                            fontFamily = fontFamily,
                                            fontWeight = if (isSelectedMonth) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelectedMonth) colors.primary else colors.onSurface
                                        )
                                        if (eventCount > 0) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(colors.primary, CircleShape)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "$eventCount",
                                                    fontFamily = fontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    color = colors.onPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_year_navigator")
                ) {
                    Text(
                        text = "Close",
                        color = colors.primary,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
