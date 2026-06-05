package com.example.features.settings.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.designsystem.CustomColorScheme
import com.example.features.alarms.presentation.viewmodel.AlarmViewModel
import com.example.features.backup.presentation.viewmodel.BackupViewModel
import com.example.features.goals.presentation.viewmodel.GoalsViewModel
import com.example.features.habits.presentation.viewmodel.HabitsViewModel
import com.example.features.security.presentation.viewmodel.SecurityViewModel
import com.example.features.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun AriseSettingsTab(
    settingsViewModel: SettingsViewModel,
    alarmsViewModel: AlarmViewModel,
    goalsViewModel: GoalsViewModel,
    habitsViewModel: HabitsViewModel,
    securityViewModel: SecurityViewModel,
    backupViewModel: BackupViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val alarmsList by alarmsViewModel.alarms.collectAsState()
    val goalsList by goalsViewModel.goals.collectAsState()
    val habitsList by habitsViewModel.habits.collectAsState()
    val completionsList by habitsViewModel.habitCompletions.collectAsState()

    val currentSkin by settingsViewModel.appSkin.collectAsState()
    val customAccentColorHex by settingsViewModel.accentColorHex.collectAsState()
    val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
    val isPremium by settingsViewModel.isPremium.collectAsState()
    var currentBackupJson by remember { mutableStateOf("") }

    val currentHabitLimit by settingsViewModel.habitLimit.collectAsState()
    val currentGoalLimit by settingsViewModel.goalLimit.collectAsState()
    val promoInput by settingsViewModel.promoCodeInput.collectAsState()
    val promoStatus by settingsViewModel.promoCodeStatus.collectAsState()
    val isAdminModeActive by settingsViewModel.isAdminMode.collectAsState()
    val isTrialActive by settingsViewModel.isTrialActive.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var customPinInput by remember { mutableStateOf("") }
    var inputRestoreText by remember { mutableStateOf("") }
    var restoreMessage by remember { mutableStateOf("") }
    var adminInputPasscode by remember { mutableStateOf("") }
    var showPremiumUnlockDialog by remember { mutableStateOf(false) }

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
                        text = "YOUR LIVE ACTIVITY & STATS",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Alarms", fontFamily = fontFamily, fontSize = 12.sp, color = colors.onSurface)
                            Text("${alarmsList.filter { it.isActive }.size}", fontFamily = fontFamily, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary)
                        }
                        Column {
                            Text("Total Habits", fontFamily = fontFamily, fontSize = 12.sp, color = colors.onSurface)
                            Text("${habitsList.size}", fontFamily = fontFamily, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
                        }
                        Column {
                            Text("Completions", fontFamily = fontFamily, fontSize = 12.sp, color = colors.onSurface)
                            Text("${completionsList.size}", fontFamily = fontFamily, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.onSurface)
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
                        text = "APP SKIN & VISUAL THEMES",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val skinsList = listOf("Futuristic", "Minimal", "Classic", "Nature", "Cosmic Void", "Neon Synthwave")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        skinsList.forEach { skin ->
                            val isSkinPremium = skin == "Classic" || skin == "Nature" || skin == "Cosmic Void" || skin == "Neon Synthwave"
                            Button(
                                onClick = {
                                    if (isSkinPremium && !isPremium) {
                                        showPremiumUnlockDialog = true
                                    } else {
                                        settingsViewModel.updateAppSkin(skin)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentSkin == skin) colors.primary else colors.primaryContainer
                                ),
                                modifier = Modifier.testTag("skin_$skin")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(skin, color = if (currentSkin == skin) colors.onPrimary else colors.onBackground, fontFamily = fontFamily, fontSize = 11.sp)
                                    if (isSkinPremium && !isPremium) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("👑", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CUSTOM ACCENT COLORS",
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
                            val isColorPremium = listOf("#FF9800", "#9C27B0", "#4CAF50").contains(hex)
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
                                    .clickable {
                                        if (isColorPremium && !isPremium) {
                                            showPremiumUnlockDialog = true
                                        } else {
                                            settingsViewModel.updateAccentColor(hex)
                                        }
                                    }
                                    .testTag("accent_${hex.substring(1)}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isColorPremium && !isPremium) {
                                    Text("👑", fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Mode (Eye-Friendly Style)", fontFamily = fontFamily, color = colors.onSurface, fontSize = 13.sp)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { settingsViewModel.toggleAppTheme() },
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.primary),
                            modifier = Modifier.testTag("theme_toggle")
                        )
                    }
                }
            }
        }

        // --- Premium Alarm Sounds subpanel ---
        item {
            val selectedSound by settingsViewModel.selectedAlarmSound.collectAsState()
            var previewPlayingSound by remember { mutableStateOf<String?>(null) }

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
                        text = "PREMIUM ALARM RINGTONE SOUNDS",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Customize your morning vibes. Choose from our premium therapeutic ambient collection.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.70f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val soundsList = listOf(
                        Pair("Default Arise Bell", false),
                        Pair("Zen Forest Stream & Flute", true),
                        Pair("Ethereal Cosmic Awakening", true),
                        Pair("Retro Future Neon Pulse", true),
                        Pair("Soothing Sunrise Vibraphone", true)
                    )

                    soundsList.forEach { (sound, isSoundPremium) ->
                        val isSoundSelected = selectedSound == sound
                        val isSoundPremiumLocked = isSoundPremium && !isPremium

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSoundSelected) colors.primaryContainer else Color.Transparent)
                                .border(1.dp, if (isSoundSelected) colors.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isSoundPremiumLocked) {
                                        showPremiumUnlockDialog = true
                                    } else {
                                        settingsViewModel.updateAlarmSound(sound)
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSoundSelected) Icons.Default.Check else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isSoundSelected) colors.primary else colors.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = sound,
                                        fontFamily = fontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSoundSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = colors.onSurface
                                    )
                                    if (isSoundPremium && !isPremium) {
                                        Text(
                                            text = "Premium Only 👑",
                                            fontFamily = fontFamily,
                                            fontSize = 9.sp,
                                            color = colors.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (isSoundPremiumLocked) {
                                        showPremiumUnlockDialog = true
                                    } else {
                                        if (previewPlayingSound == sound) {
                                            previewPlayingSound = null
                                        } else {
                                            previewPlayingSound = sound
                                        }
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (previewPlayingSound == sound) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Preview Sound",
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (previewPlayingSound != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = colors.primary, strokeWidth = 2.dp, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Playing: ${previewPlayingSound}... (Sounds lovely!)",
                                    fontFamily = fontFamily,
                                    fontSize = 11.sp,
                                    color = colors.primary
                                )
                            }
                        }
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
                        text = "PRIVACY SECURITY SCREEN LOCK",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customPinInput,
                        onValueChange = { customPinInput = it },
                        label = { Text("Create App Access PIN", color = colors.onSurface) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        Button(
                            onClick = {
                                securityViewModel.setAppSecurityPin(customPinInput)
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
                            onClick = { securityViewModel.signOutLock() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sign Out Guard", color = Color.White)
                        }
                    }
                }
            }
        }

        // --- DATA PORTABILITY JSON backup ---
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
                        text = "LOCAL DATA BACKUP & RESTORE",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create a direct text backup of all your habits and alarms, or restore from code.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                currentBackupJson = backupViewModel.generateFullBackupJson()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Text Backup Code", color = colors.onPrimary)
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
                    HorizontalDivider(color = colors.divider)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Restore from Backup Code", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = inputRestoreText,
                        onValueChange = { inputRestoreText = it },
                        placeholder = { Text("Paste your backup code here...") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val success = backupViewModel.restoreBackupJson(inputRestoreText)
                                if (success) {
                                    restoreMessage = "All data restored successfully!"
                                    inputRestoreText = ""
                                } else {
                                    restoreMessage = "Could not load backup. Please double check the text code."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restore Backup Data", color = colors.onPrimary)
                    }

                    if (restoreMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(restoreMessage, fontFamily = fontFamily, fontSize = 12.sp, color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- SUPABASE CLOUD SYNC VAULT ---
        item {
            val dbUrl by backupViewModel.supabaseUrl.collectAsState()
            val dbKey by backupViewModel.supabaseAnonKey.collectAsState()
            val userEmailByM by backupViewModel.userEmail.collectAsState()
            val isLoggedIn by backupViewModel.isLoggedIn.collectAsState()
            val authLoading by backupViewModel.authLoading.collectAsState()
            val authErrorMessage by backupViewModel.authErrorMessage.collectAsState()
            val statusMessage by backupViewModel.supabaseStatus.collectAsState()
            val showSqlAlert by backupViewModel.showSqlSuggestion.collectAsState()

            var authEmail by remember { mutableStateOf("") }
            var authPassword by remember { mutableStateOf("") }
            var isSignUpMode by remember { mutableStateOf(false) }

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
                        text = "CLOUD SECURE SYNC & STORAGE",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Synchronize your app data safely with the central secure cloud storage.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.70f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isLoggedIn) {
                        // --- OFFLINE / LOGGED OUT: SHOW AUTH GATEWAY ---
                        Text(
                            text = if (isSignUpMode) "CREATE CLOUD ACCOUNT" else "LOG IN TO CLOUD SYNC",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = authEmail,
                            onValueChange = { authEmail = it },
                            label = { Text("Email Address", color = colors.onSurface.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = authPassword,
                            onValueChange = { authPassword = it },
                            label = { Text("Password", color = colors.onSurface.copy(alpha = 0.6f)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (authErrorMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = authErrorMessage,
                                fontFamily = fontFamily,
                                color = Color(0xFFD32F2F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (authLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (isSignUpMode) {
                                        backupViewModel.registerUser(authEmail, authPassword)
                                    } else {
                                        backupViewModel.loginUser(authEmail, authPassword)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isSignUpMode) "Sign Up" else "Sign In",
                                    color = colors.onPrimary,
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                                    fontFamily = fontFamily,
                                    color = colors.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { isSignUpMode = !isSignUpMode }
                                )
                            }
                        }
                    } else {
                        // --- ONLINE / LOGGED IN: SHOW CLOUD ACTIONS ---
                        OutlinedTextField(
                            value = userEmailByM,
                            onValueChange = { },
                            readOnly = true,
                            enabled = false,
                            label = { Text("Active Cloud User Account", color = colors.onSurface) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Secure Session", tint = colors.primary, modifier = Modifier.size(16.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = colors.onSurface,
                                disabledLabelColor = colors.onSurface.copy(alpha = 0.6f),
                                disabledBorderColor = colors.primary.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.primaryContainer, RoundedCornerShape(8.dp))
                                .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    "CONNECTION STATUS",
                                    fontFamily = fontFamily,
                                    fontSize = 9.sp,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    statusMessage,
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    color = colors.onBackground,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isPremium) {
                                        showPremiumUnlockDialog = true
                                    } else {
                                        backupViewModel.uploadBackupToSupabase()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Cloud Backup", color = colors.onPrimary, fontFamily = fontFamily, fontSize = 11.sp)
                                    if (!isPremium) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("👑", fontSize = 10.sp)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (!isPremium) {
                                        showPremiumUnlockDialog = true
                                    } else {
                                        backupViewModel.restoreBackupFromSupabase()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Cloud Restore", color = Color.White, fontFamily = fontFamily, fontSize = 11.sp)
                                    if (!isPremium) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("👑", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                backupViewModel.logoutUser()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Out Current Session", color = Color.White, fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (showSqlAlert) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "RECOMMENDED CLOUD TABLE SETUP (SQL)",
                            fontFamily = fontFamily,
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = "create table if not exists arise_backups (\n  user_email text primary key,\n  backup_data jsonb not null,\n  updated_at timestamp with time zone default timezone('utc'::text, now()) not null\n);",
                            onValueChange = {},
                            readOnly = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Green),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Green,
                                unfocusedTextColor = Color.Green,
                                focusedBorderColor = colors.divider,
                                unfocusedBorderColor = colors.divider
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // --- ADMIN COMMAND CORE ---
        if (isAdminModeActive) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(2.dp, colors.primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛠️ ADMIN POWER CONSOLE",
                                fontFamily = fontFamily,
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { settingsViewModel.toggleAdminMode(false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Admin", tint = colors.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Test local limits, control premium simulation, and manage configuration defaults.",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface.copy(alpha = 0.70f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("ADMIN TESTING TOGGLES", fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Override Premium Active: ${if (isPremium) "ON" else "OFF"}", fontFamily = fontFamily, fontSize = 12.sp)
                            Switch(
                                checked = isPremium,
                                onCheckedChange = { settingsViewModel.setPremiumStatus(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = colors.primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("LIMITS FOR FREE USERS", fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Max Free Habits: $currentHabitLimit", modifier = Modifier.width(140.dp), fontFamily = fontFamily, fontSize = 12.sp)
                            Slider(
                                value = currentHabitLimit.toFloat(),
                                onValueChange = { settingsViewModel.setHabitLimit(it.toInt()) },
                                valueRange = 1f..10f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = colors.primary)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Max Free Goals: $currentGoalLimit", modifier = Modifier.width(140.dp), fontFamily = fontFamily, fontSize = 12.sp)
                            Slider(
                                value = currentGoalLimit.toFloat(),
                                onValueChange = { settingsViewModel.setGoalLimit(it.toInt()) },
                                valueRange = 1f..10f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = colors.primary)
                            )
                        }
                    }
                }
            }
        }

        // --- MOCK BUY & PROMO REDEMPTIONS CHIP ---
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🌟 ARISE PREMIUM SERVICES",
                            fontFamily = fontFamily,
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isPremium) {
                            Text(
                                text = if (isTrialActive) "👑 FREE TRIAL ACTIVE" else "👑 PREMIUM ACTIVE",
                                color = colors.primary,
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPremium) {
                            if (isTrialActive) {
                                "You are currently enjoying your 3-Day Free Trial! Explore premium skins (nature, cosmic, synthwave), sound collections, and secure cloud backups."
                            } else {
                                "Thank you for supporting Arise! Your Lifetime Premium access is fully unlocked. Complete habits, save clouds, and wake beautifully."
                            }
                        } else {
                            "Get unlimited habits and alarms, premium visual skins, therapeutic waking sound collections, and secure cloud storage backups."
                        },
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.70f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isPremium) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { settingsViewModel.startFreeTrial() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text("Start 3-Day Free Trial", color = colors.onPrimary, fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { settingsViewModel.setPremiumStatus(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                modifier = Modifier.weight(0.8f)
                            ) {
                                Text("Mock Buy ($2.99)", color = Color.White, fontFamily = fontFamily, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = colors.divider.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { settingsViewModel.updatePromoCodeInput(it) },
                            label = { Text("Redeem Upgrade Promo Code (e.g. ARISEPRO)", color = colors.onSurface) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (promoStatus.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                promoStatus,
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                color = if (promoStatus.contains("Unlocked")) Color(0xFF4CAF50) else Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { settingsViewModel.redeemPromoCode() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Redeem Promotion Key", color = colors.onBackground, fontFamily = fontFamily, fontSize = 11.sp)
                        }
                    } else {
                        val revertText = if (isTrialActive) "End Free Trial (Test limits)" else "Revert to Free Version (Test limits)"
                        Button(
                            onClick = {
                                if (isTrialActive) {
                                    settingsViewModel.endFreeTrial()
                                } else {
                                    settingsViewModel.setPremiumStatus(false)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(revertText, color = colors.onBackground, fontFamily = fontFamily, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.divider)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("ACCESS ADMIN POWER CONSOLE", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = adminInputPasscode,
                            onValueChange = { adminInputPasscode = it },
                            placeholder = { Text("Enter Passcode...", color = colors.onSurface.copy(alpha = 0.5f)) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                            modifier = Modifier.weight(1.5f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (adminInputPasscode == "admin123") {
                                    settingsViewModel.toggleAdminMode(true)
                                    adminInputPasscode = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Launch Admin", color = colors.onPrimary, fontFamily = fontFamily, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    if (showPremiumUnlockDialog) {
        ArisePremiumPromoCodeDialog(
            viewModel = settingsViewModel,
            colors = colors,
            fontFamily = fontFamily,
            onDismiss = { showPremiumUnlockDialog = false }
        )
    }
}

@Composable
fun ArisePremiumPromoCodeDialog(
    viewModel: SettingsViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    onDismiss: () -> Unit
) {
    val promoInput by viewModel.promoCodeInput.collectAsState()
    val promoStatus by viewModel.promoCodeStatus.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    Dialog(onDismissRequest = {
        viewModel.resetPromoCodeStatus()
        onDismiss()
    }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(colors.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Cosmic link",
                        tint = colors.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "COSMIC ARISE PREMIUM",
                    fontFamily = fontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Unlock elite features including Supabase Hypersync, advanced experience skins, customizable dynamic brand palettes, and unlimited habit or goal tracking.",
                    fontFamily = fontFamily,
                    fontSize = 12.sp,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isPremium) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Premium Status: ACTIVE! Enjoy Elite Arise.",
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = promoInput,
                        onValueChange = { viewModel.updatePromoCodeInput(it) },
                        label = { Text("Enter Promo Key (e.g. COSMIC99)", color = colors.onSurface) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (promoStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = promoStatus,
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = if (promoStatus.contains("Unlocked")) Color(0xFF4CAF50) else Color.Red,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.redeemPromoCode() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("Unlock Now", color = colors.onPrimary, fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.setPremiumStatus(true)
                                viewModel.updatePromoCodeInput("")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mock Buy ($2.99)", color = Color.White, fontFamily = fontFamily, fontSize = 9.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = {
                    viewModel.resetPromoCodeStatus()
                    onDismiss()
                }) {
                    Text("Close", color = colors.onSurface.copy(alpha = 0.6f), fontFamily = fontFamily, fontSize = 12.sp)
                }
            }
        }
    }
}
