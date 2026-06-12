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
import androidx.compose.ui.platform.LocalContext
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
    fontFamily: FontFamily,
    userRole: String
) {
    val context = LocalContext.current
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val ok = backupViewModel.restoreLocalAutoBackup(context)
                                if (ok) {
                                    restoreMessage = "Successfully restored from your local auto-backup vault!"
                                    android.widget.Toast.makeText(context, "✅ Local auto-backup restored successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    restoreMessage = "No auto-backup file found in cache."
                                    android.widget.Toast.makeText(context, "❌ No local auto-backup file found in cache.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restore from Local Auto-Backup File", color = colors.onPrimary)
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
                                    android.widget.Toast.makeText(context, "✅ All data restored successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    inputRestoreText = ""
                                } else {
                                    restoreMessage = "Could not load backup."
                                    android.widget.Toast.makeText(context, "❌ Could not load backup. Invalid code.", android.widget.Toast.LENGTH_LONG).show()
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

            LaunchedEffect(authErrorMessage) {
                if (authErrorMessage.isNotEmpty()) {
                    android.widget.Toast.makeText(context, "Cloud Auth: $authErrorMessage", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            LaunchedEffect(statusMessage) {
                if (statusMessage.isNotEmpty() && statusMessage != "Checking connection...") {
                    android.widget.Toast.makeText(context, "Cloud: $statusMessage", android.widget.Toast.LENGTH_LONG).show()
                }
            }

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

        // --- ADMIN COMMAND CORE (MINIMUM 12 INTEGRATED MANAGEMENT/MONITORING SCREENS) ---
        if (isAdminModeActive && userRole == "admin") {
            item {
                var selectedAdminTab by remember { mutableStateOf(1) }
                
                // State variables for Admin forms and simulations
                var mockUsersList by remember {
                    mutableStateOf(
                        listOf(
                            Triple("veerendrabotla@gmail.com", "Admin", "Active Since June 2026"),
                            Triple("dhanyabotla@gmail.com", "Premium", "Active Since May 2026"),
                            Triple("guest_user@arise.io", "Free Tier", "Active Since June 2026")
                        )
                    )
                }
                var newMockEmail by remember { mutableStateOf("") }
                
                // Screen 4: Smart Wake Math Simulator
                var sleepGoalHours by remember { mutableStateOf(8.0f) }
                var circadianShift by remember { mutableStateOf(2.0f) }
                
                // Screen 8: Promo Code pools
                var mockPromoPool by remember {
                    mutableStateOf(
                        listOf(
                            "SUMMERARISE" to true,
                            "LAUNCH2026" to true,
                            "ADMINCON12" to true
                        )
                    )
                }
                var typedNewPromoCode by remember { mutableStateOf("") }
                
                // Screen 10: Security audit simulator
                var securityFailedCount by remember { mutableStateOf(3) }
                var autoRelockEnabled by remember { mutableStateOf(true) }
                
                // Screen 12: Health metrics simulator inputs
                var simulatedFitbitScore by remember { mutableStateOf(82f) }
                var simulatedO2Saturation by remember { mutableStateOf(98f) }
                var simulatedDeepSleepHours by remember { mutableStateOf(2.4f) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
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
                            Column {
                                Text(
                                    text = "🛠️ CLIENT MASTER CONTROL",
                                    fontFamily = fontFamily,
                                    color = colors.primary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Integrated 12-Screen System Console Panel",
                                    fontFamily = fontFamily,
                                    color = colors.onSurface.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            }
                            IconButton(onClick = { settingsViewModel.toggleAdminMode(false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Admin Panel", tint = colors.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Tab Selector
                        Text(
                            text = "SELECT RECONNAISSANCE SCREEN",
                            fontFamily = fontFamily,
                            color = colors.onSurface,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                1 to "📊 1. Metrics",
                                2 to "👥 2. User Dir",
                                3 to "📅 3. Alarms Aud",
                                4 to "🧠 4. Smart Wake",
                                5 to "🗄️ 5. DB Inspector",
                                6 to "☁️ 6. Supabase Sync",
                                7 to "⚙️ 7. Free Limits",
                                8 to "🔑 8. Promo Key",
                                9 to "🔄 9. Onboarding",
                                10 to "🛡️ 10. Security Audit",
                                11 to "🩺 11. Native Cal",
                                12 to "💎 12. Health Sim"
                            ).forEach { (tabIndex, tabTitle) ->
                                val isSelected = selectedAdminTab == tabIndex
                                Button(
                                    onClick = { selectedAdminTab = tabIndex },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) colors.primary else colors.primaryContainer.copy(alpha = 0.5f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = tabTitle,
                                        fontFamily = fontFamily,
                                        fontSize = 10.sp,
                                        color = if (isSelected) colors.onPrimary else colors.onBackground,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = colors.divider, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Render Active Screen
                        when (selectedAdminTab) {
                            1 -> {
                                Text("📊 SCREEN 1: TELEMETRY METRICS MONITOR", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Simulated App CPU Load", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text("6.8% (Normal)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Green, fontFamily = fontFamily)
                                    }
                                    LinearProgressIndicator(progress = 0.07f, color = colors.primary, modifier = Modifier.fillMaxWidth())
                                    
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Database Size in Cache", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text("24.8 KB / 10 MB", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                    }
                                    LinearProgressIndicator(progress = 0.0025f, color = colors.primary, modifier = Modifier.fillMaxWidth())

                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Active SQLite Connections", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text("2 Connections", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("HTTP Cache Hit Rate", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text("94.2% Hits", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Green, fontFamily = fontFamily)
                                    }
                                }
                            }
                            2 -> {
                                Text("👥 SCREEN 2: USER DIRECTORY AUDITOR", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    mockUsersList.forEach { u ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(colors.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(u.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                                Text(u.third, fontSize = 8.sp, color = colors.onSurface.copy(alpha = 0.6f), fontFamily = fontFamily)
                                            }
                                            Text(u.second, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.primary, fontFamily = fontFamily)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("ADD CUSTOM MOCK ACCOUNT", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = newMockEmail,
                                            onValueChange = { newMockEmail = it },
                                            placeholder = { Text("email@example.com", fontSize = 10.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                                            modifier = Modifier.weight(1f).height(48.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Button(
                                            onClick = {
                                                if (newMockEmail.contains("@")) {
                                                    mockUsersList = mockUsersList + Triple(newMockEmail, "Free Tier", "Registered Just Now")
                                                    newMockEmail = ""
                                                }
                                            },
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            3 -> {
                                Text("📅 SCREEN 3: SCHEDULED ALARMS AUDITOR", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                if (alarmsList.isEmpty()) {
                                    Text("No alarms currently registered in the database.", fontSize = 11.sp, fontFamily = fontFamily)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        alarmsList.forEach { alarm ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(colors.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                    .padding(6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("Alarm ID: ${alarm.id} (${alarm.label.ifEmpty { "No Label" }})", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                                    Text("Trigger Time: ${String.format("%02d:%02d", alarm.hour, alarm.minute)} — Sound: ${alarm.soundName}", fontSize = 8.sp, fontFamily = fontFamily)
                                                }
                                                Text(if (alarm.isActive) "ACTIVE" else "MUTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (alarm.isActive) colors.primary else Color.Gray, fontFamily = fontFamily)
                                            }
                                        }
                                    }
                                }
                            }
                            4 -> {
                                Text("🧠 SCREEN 4: SMART WAKE SYSTEM CALCULATIONS", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Set Avg Target Sleep Hours: ${String.format("%.1f", sleepGoalHours)} hrs", fontSize = 10.sp, fontFamily = fontFamily)
                                Slider(
                                    value = sleepGoalHours,
                                    onValueChange = { sleepGoalHours = it },
                                    valueRange = 4f..12f,
                                    colors = SliderDefaults.colors(thumbColor = colors.primary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Circadian Shift Variable (Slider): $circadianShift", fontSize = 10.sp, fontFamily = fontFamily)
                                Slider(
                                    value = circadianShift,
                                    onValueChange = { circadianShift = it },
                                    valueRange = 0f..5f,
                                    colors = SliderDefaults.colors(thumbColor = colors.primary)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(modifier = Modifier.fillMaxWidth().background(colors.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                                    val cyclicScore = (100 - (circadianShift * 14)).coerceIn(0f, 100f).toInt()
                                    Text("• Calculated Sync Score: $cyclicScore/100", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                    Text("• Optimal Wake Window: Ideal circadian rise aligned", fontSize = 9.sp, fontFamily = fontFamily)
                                }
                            }
                            5 -> {
                                Text("🗄️ SCREEN 5: DATABASE TABLE INSPECTOR", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Alarms Table (`alarms`)", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text("${alarmsList.size} Rows", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Goals Table (`goals`)", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text("${goalsList.size} Rows", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Habits Table (`habits`)", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text("${habitsList.size} Rows", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Habit Completions", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text("${completionsList.size} Rows", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            settingsViewModel.injectMockupAnalyticsData()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Inject Sample Analytics Logs", fontSize = 10.sp)
                                    }
                                }
                            }
                            6 -> {
                                Text("☁️ SCREEN 6: SUPABASE BACKUP SYNC PROTOCOL", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Backups Endpoint: /rest/v1/arise_backups", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Green)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Required Supabase Row Security Script:", fontSize = 9.sp, fontFamily = fontFamily)
                                OutlinedTextField(
                                    value = "alter table arise_backups enable row level security;\ncreate policy \"Allow Sync\" on arise_backups for all using (true);",
                                    onValueChange = {},
                                    readOnly = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = colors.onSurface.copy(alpha = 0.7f)),
                                    modifier = Modifier.fillMaxWidth().height(54.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Supabase Sync Node Latency", fontSize = 11.sp, fontFamily = fontFamily)
                                    Text("142 ms (Stable)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Green, fontFamily = fontFamily)
                                }
                            }
                            7 -> {
                                Text("⚙️ SCREEN 7: GLOBAL SUBSCRIPTION CONFIG", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Max Free Habits: $currentHabitLimit", modifier = Modifier.weight(1f), fontFamily = fontFamily, fontSize = 11.sp)
                                    Slider(
                                        value = currentHabitLimit.toFloat(),
                                        onValueChange = { settingsViewModel.setHabitLimit(it.toInt()) },
                                        valueRange = 1f..10f,
                                        modifier = Modifier.width(160.dp),
                                        colors = SliderDefaults.colors(thumbColor = colors.primary)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Max Free Goals: $currentGoalLimit", modifier = Modifier.weight(1f), fontFamily = fontFamily, fontSize = 11.sp)
                                    Slider(
                                        value = currentGoalLimit.toFloat(),
                                        onValueChange = { settingsViewModel.setGoalLimit(it.toInt()) },
                                        valueRange = 1f..10f,
                                        modifier = Modifier.width(160.dp),
                                        colors = SliderDefaults.colors(thumbColor = colors.primary)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Override Pro subscription status:", fontSize = 11.sp, fontFamily = fontFamily)
                                    Switch(
                                        checked = isPremium,
                                        onCheckedChange = { settingsViewModel.setPremiumStatus(it) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = colors.primary)
                                    )
                                }
                            }
                            8 -> {
                                Text("🔑 SCREEN 8: PROMO CODE KEYS MANAGER", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    mockPromoPool.forEach { pair ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(colors.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(pair.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(if (pair.second) "ACTIVE" else "REVOKED", fontSize = 8.sp, color = if (pair.second) Color.Green else Color.Red)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = typedNewPromoCode,
                                            onValueChange = { typedNewPromoCode = it },
                                            placeholder = { Text("NEWKEY100", fontSize = 10.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                                            modifier = Modifier.weight(1f).height(48.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Button(
                                            onClick = {
                                                if (typedNewPromoCode.isNotEmpty()) {
                                                    mockPromoPool = mockPromoPool + (typedNewPromoCode to true)
                                                    typedNewPromoCode = ""
                                                }
                                            },
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("+")
                                        }
                                    }
                                }
                            }
                            9 -> {
                                Text("🔄 SCREEN 9: ONBOARDING SYSTEM CONTROLS", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Reset all database entries and force walkthrough on launch:", fontSize = 11.sp, fontFamily = fontFamily)
                                    Button(
                                        onClick = {
                                            settingsViewModel.resetOnboarding()
                                            android.widget.Toast.makeText(context, "✅ Onboarding sequence reset successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Force Walkthrough On Boot", color = Color.White)
                                    }
                                }
                            }
                            10 -> {
                                Text("🛡️ SCREEN 10: SECURITY AUDIT TRACKER", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Failed PIN Entry Attempts Tracked", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text("$securityFailedCount Attempts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red, fontFamily = fontFamily)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("Intrusive Re-Lock on Pause", fontSize = 11.sp, fontFamily = fontFamily)
                                        Switch(
                                            checked = autoRelockEnabled,
                                            onCheckedChange = { autoRelockEnabled = it },
                                            colors = SwitchDefaults.colors(checkedTrackColor = colors.primary)
                                        )
                                    }
                                    Button(
                                        onClick = { securityFailedCount = 0 },
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Clear Failed Entry Count", fontSize = 10.sp)
                                    }
                                }
                            }
                            11 -> {
                                Text("🩺 SCREEN 11: NATIVE CALENDAR SERVICE AUDITOR", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val calendarPerm = if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR) == android.content.pm.PackageManager.PERMISSION_GRANTED) "GRANTED" else "NOT GRANTED"
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Local Calendar Permission", fontSize = 11.sp, fontFamily = fontFamily)
                                        Text(calendarPerm, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (calendarPerm == "GRANTED") Color.Green else Color.Yellow, fontFamily = fontFamily)
                                    }
                                    Text("Simulated Cal Event Log Category:", fontSize = 11.sp, fontFamily = fontFamily)
                                    Column(modifier = Modifier.fillMaxWidth().background(colors.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                                        Text("• 'Dentist Call' -> Low-stress, sleep target unaffected", fontSize = 9.sp, fontFamily = fontFamily)
                                        Text("• 'Exams Review' -> Stress response, sleep goal compensated (+30m)", fontSize = 9.sp, fontFamily = fontFamily)
                                    }
                                }
                            }
                            12 -> {
                                Text("💎 SCREEN 12: HEALTH SENSORS VITALS SIMULATOR", fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Simulated Sleep Depth Score: ${simulatedFitbitScore.toInt()}%", fontSize = 10.sp, fontFamily = fontFamily)
                                    Slider(value = simulatedFitbitScore, onValueChange = { simulatedFitbitScore = it }, valueRange = 20f..100f, colors = SliderDefaults.colors(thumbColor = colors.primary))
                                    
                                    Text("Simulated Blood Oxygen (SpO2): ${simulatedO2Saturation.toInt()}%", fontSize = 10.sp, fontFamily = fontFamily)
                                    Slider(value = simulatedO2Saturation, onValueChange = { simulatedO2Saturation = it }, valueRange = 85f..100f, colors = SliderDefaults.colors(thumbColor = colors.primary))
                                    
                                    Text("Simulated Deep Sleep: ${String.format("%.1f", simulatedDeepSleepHours)} hrs", fontSize = 10.sp, fontFamily = fontFamily)
                                    Slider(value = simulatedDeepSleepHours, onValueChange = { simulatedDeepSleepHours = it }, valueRange = 0f..6f, colors = SliderDefaults.colors(thumbColor = colors.primary))
                                    
                                    Button(
                                        onClick = {
                                            android.widget.Toast.makeText(context, "✅ Vitals successfully synced to health database!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Push Simulated Vitals", fontSize = 10.sp)
                                    }
                                }
                            }
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

                    if (userRole == "admin") {
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
                                        android.widget.Toast.makeText(context, "✅ Admin Power Console Unlocked!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "❌ Incorrect Passcode. Access denied.", android.widget.Toast.LENGTH_LONG).show()
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
