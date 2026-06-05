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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AriseCalendarTab(
    viewModel: CalendarViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val eventsList by viewModel.events.collectAsState()
    var showAddEvent by remember { mutableStateOf(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
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

                        Text(
                            text = "${monthsArray[currentMonth]} $currentYear",
                            fontFamily = fontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.primary
                        )

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

            // Dynamic list showing selected day events or a cute empty slate
            if (activeEventsOnDay.isEmpty()) {
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
                    items(activeEventsOnDay) { event ->
                        AriseEventCard(viewModel, event, colors, fontFamily)
                    }
                }
            }
        }

        // Floating Action Button to book directly on the selected date
        FloatingActionButton(
            onClick = { showAddEvent = true },
            containerColor = colors.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_event_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Book event for selected day", tint = colors.onPrimary)
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
    }
}

@Composable
fun AriseEventCard(
    viewModel: CalendarViewModel,
    event: CalendarEvent,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        fontSize = 15.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.deleteEvent(event) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete event", tint = Color.Red.copy(alpha = 0.7f))
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
    prefillYear: Int
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf(9) }
    var durationMins by remember { mutableStateOf(60) }
    var category by remember { mutableStateOf("Work") }
    var colorHex by remember { mutableStateOf("#4CAF50") }
    var priority by remember { mutableStateOf("High") }
    
    // Custom Alarm options
    val reminderOptions = listOf(
        "No Alarm" to "None",
        "Ringing Alarm (Exact Start)" to "Exact",
        "Ringing Alarm (15m before)" to "15m",
        "Ringing Alarm (30m before)" to "30m"
    )
    var selectedReminderStyle by remember { mutableStateOf("Exact") }

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

                            val autoAlarmLinkedId = if (selectedReminderStyle != "None") 1000 + startHour else 0

                            viewModel.insertEvent(
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
                            )

                            // Programmatically add a real single-time alarm into Room database!
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

                                viewModel.insertAlarm(
                                    Alarm(
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
