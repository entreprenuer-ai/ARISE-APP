package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.viewmodel.AriseTab
import com.example.ui.viewmodel.AriseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- THEME SUPPORT PER SKIN & ACCENT ---
@Composable
fun AriseThemeWrapper(
    appSkin: String,
    accentColorHex: String,
    isDark: Boolean,
    content: @Composable (colorScheme: CustomColorScheme, fontFamily: FontFamily) -> Unit
) {
    val primaryColor = remember(accentColorHex) {
        try {
            Color(android.graphics.Color.parseColor(accentColorHex))
        } catch (e: Exception) {
            Color(0xFF6200EE)
        }
    }

    val colorScheme = remember(appSkin, primaryColor, isDark) {
        when (appSkin) {
            "Futuristic" -> {
                CustomColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.Black,
                    primaryContainer = primaryColor.copy(alpha = 0.25f),
                    background = if (isDark) Color(0xFF070B19) else Color(0xFFECEFF1),
                    surface = if (isDark) Color(0xFF10172C) else Color(0xFFFFFFFF),
                    onBackground = if (isDark) Color(0xFF00E5FF) else Color(0xFF263238),
                    onSurface = if (isDark) Color(0xFF00E5FF) else Color(0xFF37474F),
                    secondary = Color(0xFF00E5FF),
                    divider = if (isDark) Color(0xFF1E294B) else Color(0xFFCFD8DC),
                    cardBorder = if (isDark) Color(0xFF00E5FF).copy(alpha = 0.5f) else Color(0xFFCFD8DC)
                )
            }
            "Minimal" -> {
                CustomColorScheme(
                    primary = Color.White,
                    onPrimary = Color.Black,
                    primaryContainer = Color.Gray.copy(alpha = 0.2f),
                    background = if (isDark) Color(0xFF121212) else Color(0xFFFAF6F0),
                    surface = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF),
                    onBackground = if (isDark) Color.White else Color.Black,
                    onSurface = if (isDark) Color.White else Color.Black,
                    secondary = Color.Gray,
                    divider = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE0E0E0),
                    cardBorder = if (isDark) Color(0xFF3E3E3E) else Color(0xFFE0E0E0)
                )
            }
            "Classic" -> {
                CustomColorScheme(
                    primary = Color(0xFFDAA520), // Goldenrod
                    onPrimary = Color.Black,
                    primaryContainer = Color(0xFFDAA520).copy(alpha = 0.2f),
                    background = if (isDark) Color(0xFF1C130B) else Color(0xFFFFFDF5),
                    surface = if (isDark) Color(0xFF281E13) else Color(0xFFFAECE1),
                    onBackground = if (isDark) Color(0xFFFAECE1) else Color(0xFF2E1C0C),
                    onSurface = if (isDark) Color(0xFFFAECE1) else Color(0xFF422813),
                    secondary = Color(0xFFCD7F32),
                    divider = if (isDark) Color(0xFF3E2D1B) else Color(0xFFEADBCE),
                    cardBorder = if (isDark) Color(0xFFDAA520).copy(alpha = 0.4f) else Color(0xFFEADBCE)
                )
            }
            "Nature" -> {
                CustomColorScheme(
                    primary = Color(0xFF4CAF50), // Forest green
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFF4CAF50).copy(alpha = 0.2f),
                    background = if (isDark) Color(0xFF112415) else Color(0xFFF3F7F4),
                    surface = if (isDark) Color(0xFF1A3520) else Color(0xFFFFFFFF),
                    onBackground = if (isDark) Color(0xFFD0E8D4) else Color(0xFF1E3522),
                    onSurface = if (isDark) Color(0xFFD0E8D4) else Color(0xFF2E4E34),
                    secondary = Color(0xFF8BC34A),
                    divider = if (isDark) Color(0xFF23442A) else Color(0xFFD5E3D8),
                    cardBorder = if (isDark) Color(0xFF4CAF50).copy(alpha = 0.4f) else Color(0xFFD5E3D8)
                )
            }
            else -> {
                CustomColorScheme(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    primaryContainer = primaryColor.copy(alpha = 0.15f),
                    background = if (isDark) Color(0xFF121212) else Color(0xFFF5F5F5),
                    surface = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF),
                    onBackground = if (isDark) Color.White else Color.Black,
                    onSurface = if (isDark) Color.White else Color.Black,
                    secondary = primaryColor.copy(alpha = 0.8f),
                    divider = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE0E0E0),
                    cardBorder = if (isDark) Color(0xFF444444) else Color(0xFFCCCCCC)
                )
            }
        }
    }

    val fontFamily = when (appSkin) {
        "Futuristic" -> FontFamily.Monospace
        "Minimal" -> FontFamily.SansSerif
        "Classic" -> FontFamily.Serif
        "Nature" -> FontFamily.Default
        else -> FontFamily.Default
    }

    content(colorScheme, fontFamily)
}

data class CustomColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val secondary: Color,
    val divider: Color,
    val cardBorder: Color
)

// --- MAIN ARISE ROOT CONTAINER LAYOUT ---
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AriseMainScreen(viewModel: AriseViewModel) {
    val appSkin by viewModel.appSkin.collectAsState()
    val customAccentColorHex by viewModel.customAccentColorHex.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val activeTriggeredAlarm by viewModel.activeTriggeredAlarm.collectAsState()

    AriseThemeWrapper(
        appSkin = appSkin,
        accentColorHex = customAccentColorHex,
        isDark = isDarkTheme
    ) { colors, fontFamily ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isAppLocked) {
                    AriseAppLockScreen(viewModel, colors, fontFamily)
                } else if (activeTriggeredAlarm != null) {
                    AriseAlarmTriggeredScreen(viewModel, activeTriggeredAlarm!!, colors, fontFamily)
                } else {
                    AriseDashboardLayout(viewModel, colors, fontFamily)
                }
            }
        }
    }
}

// --- APP SECURITY PIN LOCK SCREEN ---
@Composable
fun AriseAppLockScreen(
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    var passwordInput by remember { mutableStateOf("") }
    var showIncorrectError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "App Encrypted Lock",
            tint = colors.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ARISE PRIVATE ENCRYPTED SENSITIVE ZONE",
            fontFamily = fontFamily,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "100% On-Device Protection. Enter PIN to unlock.",
            fontFamily = fontFamily,
            color = colors.onBackground.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("App PIN Lock", color = colors.onBackground) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onBackground,
                unfocusedTextColor = colors.onBackground,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.divider
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .testTag("app_pin_input")
        )

        if (showIncorrectError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Incorrect PIN code. Access denied.",
                fontFamily = fontFamily,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val ok = viewModel.unlockAppWithPin(passwordInput)
                if (!ok) {
                    passwordInput = ""
                    showIncorrectError = true
                } else {
                    showIncorrectError = false
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
                .testTag("pin_unlock_button")
        ) {
            Text("Unlock Vault", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
        }
    }
}

// --- MAIN TABBED DASHBOARD CONTAINER ---
@Composable
fun AriseDashboardLayout(
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentTab == AriseTab.Alarms,
                    onClick = { viewModel.setTab(AriseTab.Alarms) },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = "Alarms Tracker") },
                    label = { Text("Alarms", fontFamily = fontFamily, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.onPrimary,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.onBackground.copy(alpha = 0.5f),
                        unselectedTextColor = colors.onBackground.copy(alpha = 0.5f),
                        indicatorColor = colors.primary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AriseTab.Calendar,
                    onClick = { viewModel.setTab(AriseTab.Calendar) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Off Calendar") },
                    label = { Text("Calendar", fontFamily = fontFamily, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.onPrimary,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.onBackground.copy(alpha = 0.5f),
                        unselectedTextColor = colors.onBackground.copy(alpha = 0.5f),
                        indicatorColor = colors.primary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AriseTab.Goals,
                    onClick = { viewModel.setTab(AriseTab.Goals) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Mission Goals") },
                    label = { Text("Goals", fontFamily = fontFamily, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.onPrimary,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.onBackground.copy(alpha = 0.5f),
                        unselectedTextColor = colors.onBackground.copy(alpha = 0.5f),
                        indicatorColor = colors.primary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AriseTab.Sleep,
                    onClick = { viewModel.setTab(AriseTab.Sleep) },
                    icon = { Icon(Icons.Default.Bedtime, contentDescription = "Sleep Debt Calculator") },
                    label = { Text("Sleep", fontFamily = fontFamily, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.onPrimary,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.onBackground.copy(alpha = 0.5f),
                        unselectedTextColor = colors.onBackground.copy(alpha = 0.5f),
                        indicatorColor = colors.primary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AriseTab.StatsCustomize,
                    onClick = { viewModel.setTab(AriseTab.StatsCustomize) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Style Custom") },
                    label = { Text("Controls", fontFamily = fontFamily, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.onPrimary,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.onBackground.copy(alpha = 0.5f),
                        unselectedTextColor = colors.onBackground.copy(alpha = 0.5f),
                        indicatorColor = colors.primary
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // Inspirational Quote dynamic banner header
            AriseInspirationalBannerHeader(viewModel, colors, fontFamily)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Crossfade(
                    targetState = currentTab,
                    animationSpec = tween(300)
                ) { tab ->
                    when (tab) {
                        AriseTab.Alarms -> AriseAlarmsTab(viewModel, colors, fontFamily)
                        AriseTab.Calendar -> AriseCalendarTab(viewModel, colors, fontFamily)
                        AriseTab.Goals -> AriseGoalsTab(viewModel, colors, fontFamily)
                        AriseTab.Sleep -> AriseSleepTab(viewModel, colors, fontFamily)
                        AriseTab.StatsCustomize -> AriseStatsCustomizeTab(viewModel, colors, fontFamily)
                    }
                }
            }
        }
    }
}

// --- INSPIRATIONAL OFFLINE HEADER BANNER ---
@Composable
fun AriseInspirationalBannerHeader(
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    var quoteIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        quoteIndex = Random().nextInt(viewModel.wakeQuotes.size)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.cardBorder),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ARISE WAKE ENGINE",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date()),
                        fontFamily = fontFamily,
                        color = colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                IconButton(
                    onClick = { quoteIndex = Random().nextInt(viewModel.wakeQuotes.size) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "New Quote",
                        tint = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = colors.divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "“",
                    fontFamily = fontFamily,
                    color = colors.primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = viewModel.wakeQuotes[quoteIndex],
                    fontFamily = fontFamily,
                    color = colors.onSurface.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ==========================================
// ========== 1. ALARMS TAB VIEW ============
// ==========================================
@Composable
fun AriseAlarmsTab(
    viewModel: AriseViewModel,
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
                    textAlign = TextAlign.Center
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
    viewModel: AriseViewModel,
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
                        Text(
                            text = "$displayHour:$displayMin",
                            fontFamily = fontFamily,
                            color = colors.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )
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
                Divider(color = colors.divider)
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

// Alarms Creator Dialouge
@Composable
fun AriseAlarmDesignerDialog(
    viewModel: AriseViewModel,
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

                // Time picker simple simulation sliders
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

                // Multi repeat string selector
                Text("Repeat Setting", fontFamily = fontFamily, color = colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                val repeats = listOf("One-time", "Daily", "Mon,Tue,Wed,Thu,Fri", "Weekend")
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    repeats.forEach { rep ->
                        Button(
                            onClick = { repeatDays = rep },
                            colors = ButtonDefaults.buttonColors(containerColor = if (repeatDays == rep) colors.primary else colors.primaryContainer),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(rep, color = if (repeatDays == rep) colors.onPrimary else colors.onBackground, fontSize = 11.sp, fontFamily = fontFamily)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dismiss challenge gate configuration
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

                // Difficulty selector
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

                // Custom vibration/vol toggles
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

// ===========================================
// ========== 2. CALENDAR TAB VIEW ===========
// ===========================================
@Composable
fun AriseCalendarTab(
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val eventsList by viewModel.events.collectAsState()
    var showAddEvent by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Interactive Day Navigation Simulator
            AriseMiniCalendarBanner(eventsList, colors, fontFamily)

            if (eventsList.isEmpty()) {
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
                        contentDescription = "No Events",
                        tint = colors.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your schedule is clear",
                        fontFamily = fontFamily,
                        color = colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Enable prep-time alarm suggestions linked seamlessly to events.",
                        fontFamily = fontFamily,
                        color = colors.onBackground.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(eventsList) { event ->
                        AriseEventCard(viewModel, event, colors, fontFamily)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddEvent = true },
            containerColor = colors.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_event_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Schedule New Event", tint = colors.onPrimary)
        }

        if (showAddEvent) {
            AriseEventDesignerDialog(
                viewModel = viewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showAddEvent = false }
            )
        }
    }
}

@Composable
fun AriseMiniCalendarBanner(
    events: List<CalendarEvent>,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.cardBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MINI CALENDAR FEED (OFFLINE)",
                fontFamily = fontFamily,
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Generates dynamic days count
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                var activeDay by remember { mutableStateOf("Mon") }
                days.forEachIndexed { idx, day ->
                    val hasEvent = true
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeDay == day) colors.primaryContainer else Color.Transparent)
                            .clickable { activeDay = day }
                            .padding(8.dp)
                    ) {
                        Text(day, fontFamily = fontFamily, fontSize = 11.sp, color = colors.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (hasEvent) colors.primary else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AriseEventCard(
    viewModel: AriseViewModel,
    event: CalendarEvent,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
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
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(event.colorHex)))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.title,
                        fontFamily = fontFamily,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.deleteEvent(event) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = Color.Red.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startStr = timeFormat.format(Date(event.startTime))
            val endStr = timeFormat.format(Date(event.endTime))
            Text(
                text = "🔔 $startStr - $endStr  • Priority: ${event.priority}",
                fontFamily = fontFamily,
                fontSize = 12.sp,
                color = colors.onSurface.copy(alpha = 0.75f)
            )

            if (event.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📝 Note: ${event.notes}",
                    fontFamily = fontFamily,
                    fontSize = 12.sp,
                    color = colors.onSurface.copy(alpha = 0.7f)
                )
            }

            // Alarms Fusion: auto suggestions display
            if (event.linkedAlarmId > 0 || event.prepTimeMinutes > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryContainer)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Fusion active",
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Alarm Link: prep/commute buffer waked early by ${event.prepTimeMinutes + event.travelTimeMinutes} mins!",
                        fontFamily = fontFamily,
                        fontSize = 11.sp,
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
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("Project Sync") }
    var notes by remember { mutableStateOf("Discuss milestone deadlines") }
    var startHour by remember { mutableStateOf(10) }
    var durationMins by remember { mutableStateOf(60) }
    var location by remember { mutableStateOf("Room 4B (Offline)") }
    var category by remember { mutableStateOf("Work") }
    var colorHex by remember { mutableStateOf("#4CAF50") }
    var priority by remember { mutableStateOf("Medium") }
    var linkAlarmToEvent by remember { mutableStateOf(true) }
    var prepTimeMinutes by remember { mutableStateOf(15) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
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
                    text = "SCHEDULE OFFLINE EVENT",
                    fontFamily = fontFamily,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Event Notes Checklist", color = colors.onSurface) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Start hour: $startHour:00", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp)
                Slider(
                    value = startHour.toFloat(),
                    onValueChange = { startHour = it.toInt() },
                    valueRange = 0f..23f,
                    colors = SliderDefaults.colors(thumbColor = colors.primary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Priority Level", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row {
                    listOf("High", "Medium", "Low").forEach { p ->
                        Button(
                            onClick = { priority = p },
                            colors = ButtonDefaults.buttonColors(containerColor = if (priority == p) colors.primary else colors.primaryContainer),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(p, color = if (priority == p) colors.onPrimary else colors.onBackground, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // FUSION TOGGLE: alarm buffer suggesting
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto suggest Prep-Alarm link", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, modifier = Modifier.weight(2f))
                    Switch(checked = linkAlarmToEvent, onCheckedChange = { linkAlarmToEvent = it }, colors = SwitchDefaults.colors(checkedTrackColor = colors.primary))
                }

                if (linkAlarmToEvent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Morning Prep Buffer Mins: $prepTimeMinutes", fontFamily = fontFamily, color = colors.onSurface, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Slider(
                            value = prepTimeMinutes.toFloat(),
                            onValueChange = { prepTimeMinutes = it.toInt() },
                            valueRange = 0f..60f,
                            colors = SliderDefaults.colors(thumbColor = colors.primary),
                            modifier = Modifier.weight(2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer), modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = colors.onBackground, fontFamily = fontFamily)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val calendarStart = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, startHour)
                                set(Calendar.MINUTE, 0)
                            }.timeInMillis

                            val calendarEnd = calendarStart + (durationMins * 60000)

                            viewModel.insertEvent(
                                CalendarEvent(
                                    title = title,
                                    notes = notes,
                                    startTime = calendarStart,
                                    endTime = calendarEnd,
                                    location = location,
                                    category = category,
                                    colorHex = colorHex,
                                    priority = priority,
                                    linkedAlarmId = if (linkAlarmToEvent) 999 else 0,
                                    prepTimeMinutes = prepTimeMinutes,
                                    travelTimeMinutes = 15 // commute default simulation
                                )
                            )

                            // SUGGEST FUSION ALARM automatically!
                            if (linkAlarmToEvent) {
                                val alarmHour = (startHour - 1).coerceAtLeast(0)
                                viewModel.insertAlarm(
                                    Alarm(
                                        label = "Wakeup for $title",
                                        description = "Fused alarm synced with commuter travel times.",
                                        hour = alarmHour,
                                        minute = 60 - prepTimeMinutes - 15,
                                        repeatDays = "One-time",
                                        emoji = "🔗",
                                        challengeType = "Math",
                                        challengeDifficulty = "Easy"
                                    )
                                )
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_event_button")
                    ) {
                        Text("Book Event", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ===========================================
// ========== 3. GOALS & MISSIONS ============
// ===========================================
@Composable
fun AriseGoalsTab(
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val goalsList by viewModel.goals.collectAsState()
    var showAddGoal by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Daily top 3 priorities checklist panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                            Checkbox(checked = checked, onCheckedChange = { checked = it }, colors = CheckboxDefaults.colors(checkedColor = colors.primary))
                            Text(text, fontFamily = fontFamily, fontSize = 12.sp, color = colors.onSurface)
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
                    Text("Link calendar alarms to complete lifetime streak milestones.", fontFamily = fontFamily, color = colors.onBackground.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(goalsList) { goal ->
                        AriseGoalCard(viewModel, goal, colors, fontFamily)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddGoal = true },
            containerColor = colors.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_goal_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Life Goal", tint = colors.onPrimary)
        }

        if (showAddGoal) {
            AriseGoalDesignerDialog(
                viewModel = viewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showAddGoal = false }
            )
        }
    }
}

@Composable
fun AriseGoalCard(
    viewModel: AriseViewModel,
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
    viewModel: AriseViewModel,
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

// ===========================================
// ========== 4. SLEEP SYSTEM AND NAPS =======
// ===========================================
@Composable
fun AriseSleepTab(
    viewModel: AriseViewModel,
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
                            // Preset Nap Durations
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
                    Text("No sleep records yet. Sleep simulation triggers automatically when alarms dismiss.", fontFamily = fontFamily, fontSize = 12.sp, color = colors.onBackground.copy(alpha = 0.6f), textAlign = TextAlign.Center)
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
        }
    }
}

// ===========================================
// ========== 5. STATS & PERSONALIZATION ====
// ===========================================
@Composable
fun AriseStatsCustomizeTab(
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val alarmsList by viewModel.alarms.collectAsState()
    val sleepLogsList by viewModel.sleepLogs.collectAsState()
    val goalsList by viewModel.goals.collectAsState()

    val currentSkin by viewModel.appSkin.collectAsState()
    val customAccentColorHex by viewModel.customAccentColorHex.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val currentBackupJson by viewModel.currentBackupJson.collectAsState()

    var customPinInput by remember { mutableStateOf("") }
    var inputRestoreText by remember { mutableStateOf("") }
    var restoreMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // --- Wake Statistics Subpanel ---
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
                        text = "ON-DEVICE DIAGNOSTIC HISTOGRAMS & SUCCESS",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val wakeSuccessRate = if (sleepLogsList.isEmpty()) 100 else {
                        val goodMoods = sleepLogsList.filter { it.wakeMood != "Tired" }.size
                        (goodMoods * 100) / sleepLogsList.size
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Wake Success Rate", fontFamily = fontFamily, fontSize = 12.sp, color = colors.onSurface)
                            Text("$wakeSuccessRate%", fontFamily = fontFamily, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary)
                        }
                        Column {
                            Text("Active Alarms", fontFamily = fontFamily, fontSize = 12.sp, color = colors.onSurface)
                            Text("${alarmsList.filter { it.isActive }.size}", fontFamily = fontFamily, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                        }
                        Column {
                            Text("Streaks Active", fontFamily = fontFamily, fontSize = 12.sp, color = colors.onSurface)
                            val totalStreaks = goalsList.sumOf { it.streakCount }
                            Text("$totalStreaks days", fontFamily = fontFamily, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                        }
                    }
                }
            }
        }

        // --- Skins & Theme Personalization Subpanel ---
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
                        text = "EXPERIENCE SKINS (ATHMOSPHERIC PROFILE)",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Skin Selectors List
                    val skinsList = listOf("Futuristic", "Minimal", "Classic", "Nature")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        skinsList.forEach { skin ->
                            Button(
                                onClick = { viewModel.updateAppSkin(skin) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentSkin == skin) colors.primary else colors.primaryContainer
                                ),
                                modifier = Modifier.testTag("skin_$skin")
                            ) {
                                Text(skin, color = if (currentSkin == skin) colors.onPrimary else colors.onBackground, fontFamily = fontFamily, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ACCENT CHIP (DYNAMIC BRAND PALETTE)",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val paletteHex = listOf("#3F51B5", "#009688", "#E91E63", "#FF9800", "#9C27B0", "#4CAF50")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        paletteHex.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (customAccentColorHex == hex) 3.dp else 1.dp,
                                        color = if (customAccentColorHex == hex) colors.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.updateAccentColor(hex) }
                                    .testTag("accent_${hex.substring(1)}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dark mode toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stark Eye-Protection Dark Theme", fontFamily = fontFamily, color = colors.onSurface, fontSize = 13.sp)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleAppTheme() },
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.primary),
                            modifier = Modifier.testTag("theme_toggle")
                        )
                    }
                }
            }
        }

        // --- PIN Vault Security Subpanel ---
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
                        text = "PRIVACY SECURITY PIN CODE LOCK",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customPinInput,
                        onValueChange = { customPinInput = it },
                        label = { Text("Define Security Lock PIN", color = colors.onSurface) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Button(
                            onClick = {
                                viewModel.setAppSecurityPin(customPinInput)
                                customPinInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_pin_button")
                        ) {
                            Text("Confirm PIN", color = colors.onPrimary)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { viewModel.signOutLock() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sign Out Guard", color = Color.White)
                        }
                    }
                }
            }
        }

        // --- JSON Backup Diagnostic Subpanel ---
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
                        text = "DATA PORTABILITY (SECURE JSON LOCAL DUMP)",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Compile full export backups copy-pasted as backups, or restore past backups.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.generateFullBackupJson() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Compile Backup JSON String", color = colors.onPrimary)
                    }

                    if (currentBackupJson.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = currentBackupJson,
                            onValueChange = {},
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = colors.divider)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Recover from Backup Code Input", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = inputRestoreText,
                        onValueChange = { inputRestoreText = it },
                        placeholder = { Text("Paste JSON code...") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val success = viewModel.restoreBackupJson(inputRestoreText)
                            if (success) {
                                restoreMessage = "All backup data loaded successfully!"
                                inputRestoreText = ""
                            } else {
                                restoreMessage = "Invalid configuration code format."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Load Recovery Configuration", color = colors.onPrimary)
                    }

                    if (restoreMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(restoreMessage, fontFamily = fontFamily, fontSize = 12.sp, color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================
// ========== ACTIVE ALARM SCREEN GATES =======
// ============================================
@Composable
fun AriseAlarmTriggeredScreen(
    viewModel: AriseViewModel,
    alarm: Alarm,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    var mathInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("") }

    val shakeCount by viewModel.shakeCount.collectAsState()
    val mathProblem by viewModel.mathProblem.collectAsState()
    val memoryPattern by viewModel.memoryPattern.collectAsState()
    val memorySelection by viewModel.memorySelection.collectAsState()
    val typingTarget by viewModel.typingTarget.collectAsState()
    val countBackwardVal by viewModel.currentCountBackward.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val strobeActive by viewModel.strobeActive.collectAsState()

    val shakeTarget = if (alarm.challengeDifficulty == "Hard") 40 else if (alarm.challengeDifficulty == "Easy") 15 else 25

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

    val stbackgroundColor = if (strobeActive && (System.currentTimeMillis() % 600 > 300)) Color.White else colors.background

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
            Text(alarm.emoji, fontSize = 60.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        val nowStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        Text(
            text = nowStr,
            fontFamily = fontFamily,
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.onBackground
        )

        Text(
            text = alarm.label,
            fontFamily = fontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            textAlign = TextAlign.Center
        )

        if (alarm.description.isNotEmpty()) {
            Text(
                text = alarm.description,
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedMood == key) colors.primaryContainer else Color.Transparent)
                        .border(1.dp, if (selectedMood == key) colors.primary else colors.divider, RoundedCornerShape(8.dp))
                        .clickable { viewModel.updateWakeMood(key) }
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
                    text = "DISMISS GATE: ${alarm.challengeType.uppercase()} GATE",
                    fontFamily = fontFamily,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                when (alarm.challengeType) {
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
                            placeholder = { Text("Type Answer") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .testTag("gate_math_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val parsed = mathInput.toIntOrNull() ?: 0
                                viewModel.submitMathAnswer(parsed)
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
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.submitTypingAnswer(typeInput) },
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

        if (alarm.snoozeEnabled) {
            Button(
                onClick = { viewModel.snoozeActiveAlarm() },
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.primary),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(44.dp)
                    .testTag("gate_snooze_button")
            ) {
                Text("Snooze (${alarm.snoozeDurationMinutes}m Limit: ${alarm.snoozeLimit}x)", color = colors.primary, fontFamily = fontFamily)
            }
        }
    }
}
