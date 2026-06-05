package com.example.features.goals.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.database.Goal
import com.example.core.database.Habit
import com.example.core.database.HabitCompletion
import com.example.core.designsystem.CustomColorScheme
import com.example.features.goals.presentation.viewmodel.GoalsViewModel
import com.example.features.habits.presentation.viewmodel.HabitsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AriseGoalsTab(
    goalsViewModel: GoalsViewModel,
    habitsViewModel: HabitsViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val goalsList by goalsViewModel.goals.collectAsState()
    val habitsList by habitsViewModel.habits.collectAsState()
    val completionsList by habitsViewModel.habitCompletions.collectAsState()

    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Goals, 1 = Habits
    var showAddGoal by remember { mutableStateOf(false) }
    var showAddHabit by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sleek sub-tab switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.divider, RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedSubTab == 0) colors.primaryContainer else Color.Transparent)
                        .clickable { selectedSubTab = 0 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "COSMIC GOALS",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (selectedSubTab == 0) colors.primary else colors.onBackground.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedSubTab == 1) colors.primaryContainer else Color.Transparent)
                        .clickable { selectedSubTab = 1 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HABIT ASCENTS",
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (selectedSubTab == 1) colors.primary else colors.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            if (selectedSubTab == 0) {
                // DAILY PRIORITIES CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DAILY MISSIONS: MORNING 3 PRIORITIES",
                            fontFamily = fontFamily,
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val priorities = listOf(
                            "1. Exercise and practice mindful breathing",
                            "2. Complete primary learning milestone review",
                            "3. Update sleep schedules and recovery logs"
                        )
                        priorities.forEach { text ->
                            var checked by remember { mutableStateOf(false) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { checked = !checked }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { checked = it },
                                    colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                                )
                                Text(
                                    text = text,
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }

                if (goalsList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Achievements tracker",
                            tint = colors.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No goals added yet", fontFamily = fontFamily, color = colors.onBackground, fontWeight = FontWeight.Bold)
                        Text("Link calendar alarms to complete lifetime streak milestones.", fontFamily = fontFamily, color = colors.onBackground.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        items(goalsList) { goal ->
                            AriseGoalCard(goalsViewModel, goal, colors, fontFamily)
                        }
                        item {
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
            } else {
                if (habitsList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            drawCircle(
                                color = colors.primary.copy(alpha = 0.1f),
                                radius = size.minDimension / 1.8f
                            )
                            val stemPath = Path().apply {
                                moveTo(size.width / 2, size.height * 0.9f)
                                cubicTo(
                                    size.width / 2, size.height * 0.6f,
                                    size.width * 0.4f, size.height * 0.4f,
                                    size.width * 0.45f, size.height * 0.2f
                                )
                            }
                            drawPath(
                                path = stemPath,
                                color = colors.primary.copy(alpha = 0.6f),
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawCircle(
                                color = colors.primary,
                                radius = 8.dp.toPx(),
                                center = Offset(size.width * 0.45f, size.height * 0.2f)
                            )
                            drawCircle(
                                color = colors.secondary,
                                radius = 6.dp.toPx(),
                                center = Offset(size.width * 0.38f, size.height * 0.42f)
                            )
                            drawCircle(
                                color = colors.primary,
                                radius = 6.dp.toPx(),
                                center = Offset(size.width * 0.52f, size.height * 0.55f)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No active habit trails yet",
                            fontFamily = fontFamily,
                            color = colors.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Habits define our paths. Cultivate your initial habit now.",
                            fontFamily = fontFamily,
                            color = colors.onBackground.copy(alpha = 0.6f),
                            fontSize = 12.sp,
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
                        items(habitsList) { habit ->
                            val completions = completionsList.filter { it.habitId == habit.id }
                            AriseHabitCard(habitsViewModel, habit, completions, colors, fontFamily)
                        }
                        item {
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (selectedSubTab == 0) {
                    showAddGoal = true
                } else {
                    showAddHabit = true
                }
            },
            containerColor = colors.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_growth_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Life Element", tint = colors.onPrimary)
        }

        if (showAddGoal) {
            AriseGoalDesignerDialog(
                viewModel = goalsViewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showAddGoal = false }
            )
        }

        if (showAddHabit) {
            AriseHabitDesignerDialog(
                viewModel = habitsViewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showAddHabit = false }
            )
        }
    }
}

@Composable
fun AriseHabitCard(
    viewModel: HabitsViewModel,
    habit: Habit,
    completions: List<HabitCompletion>,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    var showNoteLogger by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("habit_card_${habit.id}"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = habit.frequency.uppercase(),
                            fontFamily = fontFamily,
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔥 Streak: ${habit.currentStreak} DAYS",
                            fontFamily = fontFamily,
                            color = colors.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = habit.title,
                        fontFamily = fontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    if (habit.description.isNotEmpty()) {
                        Text(
                            text = habit.description,
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.completeHabit(habit.id, "") },
                        modifier = Modifier.testTag("complete_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Log Habit Completion",
                            tint = colors.primary
                        )
                    }

                    IconButton(
                        onClick = { showNoteLogger = true },
                        modifier = Modifier.testTag("note_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Log with custom notes",
                            tint = colors.onSurface.copy(alpha = 0.60f)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.deleteHabit(habit) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Habit",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "LAST 7 DAYS TRACK",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = colors.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(6.dp))

            val last7Days = remember {
                (0..6).map { offset ->
                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
                }.reversed()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                last7Days.forEach { dayCal ->
                    val df = remember { SimpleDateFormat("EE", Locale.getDefault()) }
                    val dayLabel = df.format(dayCal.time).take(1).uppercase()
                    val isCompleted = completions.any { comp ->
                        val compCal = Calendar.getInstance().apply { timeInMillis = comp.completionTimestamp }
                        compCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                        compCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCompleted) colors.primary.copy(alpha = 0.25f) else Color.Transparent
                            )
                            .border(
                                width = if (isCompleted) 2.dp else 1.dp,
                                color = if (isCompleted) colors.primary else colors.divider,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayLabel,
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) colors.primary else colors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }

    if (showNoteLogger) {
        AlertDialog(
            onDismissRequest = { showNoteLogger = false },
            title = {
                Text("Log Habit Completion Notes", fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = colors.onSurface)
            },
            text = {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Performance description (e.g. 10km run done!)", color = colors.onSurface) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeHabit(habit.id, notesText)
                        notesText = ""
                        showNoteLogger = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Log", color = colors.onPrimary)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showNoteLogger = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer)
                ) {
                    Text("Cancel", color = colors.onBackground)
                }
            },
            containerColor = colors.surface
        )
    }
}

@Composable
fun AriseHabitDesignerDialog(
    viewModel: HabitsViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("Morning Hydration") }
    var description by remember { mutableStateOf("Drink 500ml pure water right on wake") }
    var frequency by remember { mutableStateOf("Daily") }
    var targetCount by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("CULTIVATE HABIT TRAIL", fontFamily = fontFamily, color = colors.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Intention Summary", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Frequency", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Daily", "Weekly", "Custom").forEach { freq ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (frequency == freq) colors.primaryContainer else colors.surface)
                                .border(1.dp, if (frequency == freq) colors.primary else colors.divider, RoundedCornerShape(8.dp))
                                .clickable { frequency = freq }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(freq, fontFamily = fontFamily, color = if (frequency == freq) colors.primary else colors.onBackground.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Target Repetitions: $targetCount per cycle", fontFamily = fontFamily, color = colors.onSurface, fontSize = 11.sp)
                Slider(
                    value = targetCount.toFloat(),
                    onValueChange = { targetCount = it.toInt() },
                    valueRange = 1f..5f,
                    colors = SliderDefaults.colors(thumbColor = colors.primary)
                )

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
                            if (title.isNotEmpty()) {
                                viewModel.insertHabit(
                                    Habit(
                                        title = title,
                                        description = description,
                                        frequency = frequency,
                                        targetCount = targetCount
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_habit_button")
                    ) {
                        Text("Initiate Trail", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AriseGoalCard(
    viewModel: GoalsViewModel,
    goal: Goal,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("goal_card_${goal.id}"),
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
                Column {
                    Text(goal.category.uppercase(), fontFamily = fontFamily, color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(goal.title, fontFamily = fontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.incrementGoalProgress(goal) },
                        modifier = Modifier.testTag("increment_goal_${goal.id}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Mark Goal Progress", tint = colors.primary)
                    }

                    IconButton(
                        onClick = { viewModel.deleteGoal(goal) }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            val percent = (goal.currentProgress.toFloat() / goal.targetProgress.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.primary,
                trackColor = colors.divider
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Goal Milestone: ${goal.currentProgress}/${goal.targetProgress}",
                    fontFamily = fontFamily,
                    fontSize = 12.sp,
                    color = colors.onSurface
                )

                Text(
                    text = "🔥 Streak: ${goal.streakCount} days",
                    fontFamily = fontFamily,
                    fontSize = 12.sp,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AriseGoalDesignerDialog(
    viewModel: GoalsViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("Learn Kotlin Core") }
    var description by remember { mutableStateOf("Study daily and implement local apps") }
    var targetProgress by remember { mutableStateOf(10) }
    var category by remember { mutableStateOf("Learning") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("CREATE LIFETIME MISSION", fontFamily = fontFamily, color = colors.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Mission Title", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mission Description Summary", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Milestone Milestones Target: $targetProgress", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp)
                Slider(
                    value = targetProgress.toFloat(),
                    onValueChange = { targetProgress = it.toInt() },
                    valueRange = 5f..50f,
                    colors = SliderDefaults.colors(thumbColor = colors.primary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Mission Type", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("Fitness", "Career", "Learning", "Health", "Finance").forEach { cat ->
                        Button(
                            onClick = { category = cat },
                            colors = ButtonDefaults.buttonColors(containerColor = if (category == cat) colors.primary else colors.primaryContainer),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(cat, color = if (category == cat) colors.onPrimary else colors.onBackground, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer), modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = colors.onBackground, fontFamily = fontFamily)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.insertGoal(
                                Goal(
                                    title = title,
                                    description = description,
                                    category = category,
                                    targetProgress = targetProgress,
                                    currentProgress = 0
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_goal_button")
                    ) {
                        Text("Start Mission", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
