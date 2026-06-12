package com.example.features.goals.presentation.screens

import androidx.compose.animation.AnimatedVisibility

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
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var goalToEdit by remember { mutableStateOf<Goal?>(null) }

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
                val dailyObjectives = goalsList.filter { it.category == "Daily Objective" }
                val lifetimeMissions = goalsList.filter { it.category != "Daily Objective" }
                val context = androidx.compose.ui.platform.LocalContext.current

                // DYNAMIC DAILY OBJECTIVES DASHBOARD CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
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
                                text = "✨ TODAY'S DAILY CHALLENGES",
                                fontFamily = fontFamily,
                                color = colors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            IconButton(
                                onClick = { 
                                    goalToEdit = null
                                    showAddGoal = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Daily Objective",
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar calculation
                        val totalDailies = dailyObjectives.size
                        val completedDailies = dailyObjectives.count { it.currentProgress >= it.targetProgress }
                        val dailyPercent = if (totalDailies > 0) completedDailies.toFloat() / totalDailies.toFloat() else 0f

                        // Visual progress bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = { dailyPercent },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = colors.primary,
                                trackColor = colors.divider
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${(dailyPercent * 100).toInt()}%",
                                fontFamily = fontFamily,
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (totalDailies > 0) {
                                "$completedDailies of $totalDailies objectives achieved. Keep ascending! 🚀"
                            } else {
                                "No daily objectives defined yet. Tap '+' or below to make some!"
                            },
                            fontFamily = fontFamily,
                            color = colors.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (dailyObjectives.isEmpty()) {
                            Button(
                                onClick = {
                                    goalsViewModel.insertGoal(Goal(title = "Exercise and practice mindful breathing", category = "Daily Objective", targetProgress = 1, currentProgress = 0))
                                    goalsViewModel.insertGoal(Goal(title = "Complete primary learning milestone review", category = "Daily Objective", targetProgress = 1, currentProgress = 0))
                                    goalsViewModel.insertGoal(Goal(title = "Update sleep schedules and recovery logs", category = "Daily Objective", targetProgress = 1, currentProgress = 0))
                                    android.widget.Toast.makeText(context, "Seed objectives generated! 🌱", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Generate Seed Objectives 🌱", color = colors.primary, fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            dailyObjectives.forEach { goal ->
                                val isChecked = goal.currentProgress >= goal.targetProgress
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) {
                                                goalsViewModel.resetGoalProgress(goal)
                                            } else {
                                                goalsViewModel.incrementGoalDirectly(goal)
                                                android.widget.Toast.makeText(context, "Objective complete! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                goalsViewModel.incrementGoalDirectly(goal)
                                                android.widget.Toast.makeText(context, "Objective complete! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                goalsViewModel.resetGoalProgress(goal)
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                                    )
                                    Text(
                                        text = goal.title,
                                        fontFamily = fontFamily,
                                        fontSize = 12.sp,
                                        color = if (isChecked) colors.onSurface.copy(alpha = 0.5f) else colors.onSurface,
                                        style = if (isChecked) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle.Default,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = { goalToEdit = goal },
                                        modifier = Modifier.size(24.dp).testTag("edit_goal_${goal.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = colors.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { goalsViewModel.deleteGoal(goal) },
                                        modifier = Modifier.size(24.dp).testTag("delete_goal_${goal.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red.copy(alpha = 0.5f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (lifetimeMissions.isEmpty()) {
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
                        Text("No lifetime goals added yet", fontFamily = fontFamily, color = colors.onBackground, fontWeight = FontWeight.Bold)
                        Text("Link calendar alarms to complete lifetime streak milestones.", fontFamily = fontFamily, color = colors.onBackground.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        items(lifetimeMissions) { goal ->
                            AriseGoalCard(
                                viewModel = goalsViewModel,
                                goal = goal,
                                colors = colors,
                                fontFamily = fontFamily,
                                onEditClick = { goalToEdit = goal }
                            )
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
                            AriseHabitCard(
                                viewModel = habitsViewModel,
                                habit = habit,
                                completions = completions,
                                colors = colors,
                                fontFamily = fontFamily,
                                onEditClick = { habitToEdit = habit }
                            )
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

        if (habitToEdit != null) {
            AriseHabitDesignerDialog(
                viewModel = habitsViewModel,
                colors = colors,
                fontFamily = fontFamily,
                habit = habitToEdit,
                onDismiss = { habitToEdit = null }
            )
        }

        if (goalToEdit != null) {
            AriseGoalDesignerDialog(
                viewModel = goalsViewModel,
                colors = colors,
                fontFamily = fontFamily,
                goal = goalToEdit,
                onDismiss = { goalToEdit = null }
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
    fontFamily: FontFamily,
    onEditClick: () -> Unit
) {
    var showNoteLogger by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

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
                    
                    val milestoneText = remember(habit.currentStreak) {
                        when {
                            habit.currentStreak >= 30 -> "Ascent Master Legend! 🏆 (30+ Days)"
                            habit.currentStreak >= 14 -> "Relentless Climber! 🌟 (14+ Days)"
                            habit.currentStreak >= 7 -> "Perfect Weekly Ascent! 🏔️ (7+ Days)"
                            habit.currentStreak >= 3 -> "Ascent Trifecta Active! 🔥 (3+ Days)"
                            else -> null
                        }
                    }
                    if (milestoneText != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colors.primary.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colors.primary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = milestoneText,
                                    fontFamily = fontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                    }

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
                        onClick = { viewModel.completeHabit(habit.id, "SKIPPED_ON_VACATION") },
                        modifier = Modifier.testTag("skip_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = "Freeze Streak Today",
                            tint = colors.secondary
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.testTag("edit_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Habit Details",
                            tint = colors.onSurface.copy(alpha = 0.60f)
                        )
                    }

                    IconButton(
                        onClick = { showNoteLogger = true },
                        modifier = Modifier.testTag("note_habit_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Log with custom notes",
                            tint = colors.primary
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

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Historical Completion Logs (${completions.size})",
                    fontFamily = fontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Show Less" else "Show More",
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    if (completions.isEmpty()) {
                        Text(
                            text = "No completions logged yet.",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        completions.sortedByDescending { it.completionTimestamp }.forEach { completion ->
                            val dateStr = remember(completion.completionTimestamp) {
                                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                sdf.format(Date(completion.completionTimestamp))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(colors.background.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .border(1.dp, colors.divider, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dateStr,
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    if (completion.notes.isNotEmpty()) {
                                        Text(
                                            text = completion.notes,
                                            fontFamily = fontFamily,
                                            fontSize = 11.sp,
                                            color = colors.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteHabitCompletion(completion) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete Log Entry",
                                        tint = Color.Red.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
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
    habit: Habit? = null,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(habit?.title ?: "Morning Hydration") }
    var description by remember { mutableStateOf(habit?.description ?: "Drink 500ml pure water right on wake") }
    var frequency by remember { mutableStateOf(habit?.frequency ?: "Daily") }
    var targetCount by remember { mutableStateOf(habit?.targetCount ?: 1) }

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
                Text(if (habit != null) "MODIFY HABIT TRAIL" else "CULTIVATE HABIT TRAIL", fontFamily = fontFamily, color = colors.primary, fontWeight = FontWeight.Bold)
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
                                if (habit != null) {
                                    viewModel.insertHabit(
                                        habit.copy(
                                            title = title,
                                            description = description,
                                            frequency = frequency,
                                            targetCount = targetCount
                                        )
                                    )
                                } else {
                                    viewModel.insertHabit(
                                        Habit(
                                            title = title,
                                            description = description,
                                            frequency = frequency,
                                            targetCount = targetCount
                                        )
                                    )
                                }
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_habit_button")
                    ) {
                        Text(if (habit != null) "Update Trail" else "Initiate Trail", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
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
    fontFamily: FontFamily,
    onEditClick: () -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
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

                    if (goal.currentProgress > 0) {
                        IconButton(
                            onClick = { viewModel.decrementGoalProgress(goal) },
                            modifier = Modifier.testTag("decrement_goal_${goal.id}")
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = "Undo Goal Progress", tint = colors.onSurface.copy(alpha = 0.6f))
                        }
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.testTag("edit_goal_${goal.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Goal Details", tint = colors.onSurface.copy(alpha = 0.6f))
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
    goal: Goal? = null,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(goal?.title ?: "Complete Daily Walk") }
    var description by remember { mutableStateOf(goal?.description ?: "Study or complete personal task") }
    var category by remember { mutableStateOf(goal?.category ?: "Daily Objective") }
    var targetProgress by remember { mutableStateOf(if (category == "Daily Objective") 1 else (goal?.targetProgress ?: 10)) }
    var currentProgress by remember { mutableStateOf(goal?.currentProgress ?: 0) }

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
                Text(
                    text = if (category == "Daily Objective") {
                        if (goal != null) "MODIFY DAILY CHALLENGE" else "DEFINE DAILY CHALLENGE"
                    } else {
                        if (goal != null) "MODIFY LIFETIME MISSION" else "CREATE LIFETIME MISSION"
                    },
                    fontFamily = fontFamily,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details & Summary", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (category != "Daily Objective") {
                    Text("Milestone Target: $targetProgress", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp)
                    Slider(
                        value = targetProgress.toFloat(),
                        onValueChange = { 
                            targetProgress = it.toInt()
                        },
                        valueRange = 5f..100f,
                        colors = SliderDefaults.colors(thumbColor = colors.primary)
                    )

                    if (goal != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Current Progress: $currentProgress", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp)
                        Slider(
                            value = currentProgress.toFloat(),
                            onValueChange = { currentProgress = it.toInt().coerceAtMost(targetProgress) },
                            valueRange = 0f..targetProgress.toFloat(),
                            colors = SliderDefaults.colors(thumbColor = colors.primary)
                        )
                    }
                } else {
                    // For Daily Objective, force target targetProgress list to be 1
                    LaunchedEffect(category) {
                        targetProgress = 1
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Activity Category", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("Daily Objective", "Fitness", "Career", "Learning", "Health", "Finance").forEach { cat ->
                        Button(
                            onClick = { 
                                category = cat
                                if (cat == "Daily Objective") {
                                    targetProgress = 1
                                }
                            },
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
                            if (title.isNotEmpty()) {
                                if (goal != null) {
                                    viewModel.insertGoal(
                                        goal.copy(
                                            title = title,
                                            description = description,
                                            category = category,
                                            targetProgress = if (category == "Daily Objective") 1 else targetProgress,
                                            currentProgress = if (category == "Daily Objective") currentProgress.coerceIn(0, 1) else currentProgress
                                        )
                                    )
                                } else {
                                    viewModel.insertGoal(
                                        Goal(
                                            title = title,
                                            description = description,
                                            category = category,
                                            targetProgress = if (category == "Daily Objective") 1 else targetProgress,
                                            currentProgress = 0
                                        )
                                    )
                                }
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_goal_button")
                    ) {
                        Text(if (goal != null) "Update Objective" else "Launch Objective", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
