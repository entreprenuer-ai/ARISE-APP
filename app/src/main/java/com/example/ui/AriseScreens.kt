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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
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
            "Cosmic Void" -> {
                CustomColorScheme(
                    primary = Color(0xFF9E7BFF), // Glowing cosmic lavender
                    onPrimary = Color.Black,
                    primaryContainer = Color(0xFF9E7BFF).copy(alpha = 0.25f),
                    background = if (isDark) Color(0xFF020208) else Color(0xFFF9F9FC),
                    surface = if (isDark) Color(0xFF0B0A11) else Color(0xFFFFFFFF),
                    onBackground = if (isDark) Color(0xFFE5E0FF) else Color(0xFF0F0B1E),
                    onSurface = if (isDark) Color(0xFFD6CFFF) else Color(0xFF231C3C),
                    secondary = Color(0xFFFF7BB5),
                    divider = if (isDark) Color(0xFF1B182E) else Color(0xFFE5E0FF),
                    cardBorder = if (isDark) Color(0xFF9E7BFF).copy(alpha = 0.5f) else Color(0xFFE5E0FF)
                )
            }
            "Neon Synthwave" -> {
                CustomColorScheme(
                    primary = Color(0xFFFF007F), // Neon hot pink
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFF007F).copy(alpha = 0.25f),
                    background = if (isDark) Color(0xFF14051D) else Color(0xFFFFF2FA),
                    surface = if (isDark) Color(0xFF260D35) else Color(0xFFFFFFFF),
                    onBackground = if (isDark) Color(0xFF00FFFF) else Color(0xFF330033),
                    onSurface = if (isDark) Color(0xFF00FFFF) else Color(0xFF4A004A),
                    secondary = Color(0xFF00E5FF),
                    divider = if (isDark) Color(0xFF3B0B54) else Color(0xFFFFCCEC),
                    cardBorder = if (isDark) Color(0xFFFF007F).copy(alpha = 0.5f) else Color(0xFFFFCCEC)
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
        "Cosmic Void" -> FontFamily.Serif
        "Neon Synthwave" -> FontFamily.Monospace
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
    val firstBootCompleted by viewModel.firstBootCompleted.collectAsState()
    val activeTriggeredAlarm by viewModel.activeTriggeredAlarm.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

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
                if (!isLoggedIn) {
                    AriseAuthScreen(viewModel, colors, fontFamily)
                } else if (isAppLocked) {
                    AriseAppLockScreen(viewModel, colors, fontFamily)
                } else if (activeTriggeredAlarm != null) {
                    AriseAlarmTriggeredScreen(viewModel, activeTriggeredAlarm!!, colors, fontFamily)
                } else if (!firstBootCompleted) {
                    AriseOnboardingScreen(viewModel, colors, fontFamily)
                } else {
                    AriseDashboardLayout(viewModel, colors, fontFamily)
                }
            }
        }
    }
}

@Composable
fun AriseAuthScreen(
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authErrorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Icon(
            imageVector = Icons.Default.AllInclusive,
            contentDescription = "ARISE Logo",
            tint = colors.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ARISE",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = colors.onBackground,
            fontSize = 24.sp,
            letterSpacing = 4.sp
        )
        Text(
            text = "Secure Cloud Synchronization Gateway",
            fontFamily = fontFamily,
            color = colors.onBackground.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (isSignUpMode) "CREATE ACCOUNT" else "SECURE SIGN IN",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", fontFamily = fontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedLabelColor = colors.primary,
                        unfocusedLabelColor = colors.onSurface.copy(alpha = 0.6f),
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.onSurface.copy(alpha = 0.3f),
                        cursorColor = colors.primary,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", fontFamily = fontFamily) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedLabelColor = colors.primary,
                        unfocusedLabelColor = colors.onSurface.copy(alpha = 0.6f),
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.onSurface.copy(alpha = 0.3f),
                        cursorColor = colors.primary,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (authError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = authError,
                        fontFamily = fontFamily,
                        color = if (authError.contains("successful", ignoreCase = true)) colors.primary else Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (authLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(28.dp))
                    }
                } else {
                    Button(
                        onClick = {
                            if (isSignUpMode) {
                                viewModel.registerUser(email, password, "user")
                            } else {
                                viewModel.loginUser(email, password)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (isSignUpMode) "REGISTER" else "SIGN IN",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { isSignUpMode = !isSignUpMode },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (isSignUpMode) "Already have an account? Sign In" else "New to Arise? Create an account",
                        fontFamily = fontFamily,
                        color = colors.onSurface.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.enableGuestMode() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                ) {
                    Text(
                        text = "EXPLORE OFFLINE GUEST MODE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ============================================
// ========== ARISE ONBOARDING SYSTEM =========
// ============================================
@Composable
fun AriseOnboardingScreen(
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    var pagerState by remember { mutableStateOf(0) }
    var selectedSkin by remember { mutableStateOf("Futuristic") }
    var selectedAccent by remember { mutableStateOf("#00F5FF") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AllInclusive,
                contentDescription = "ARISE LIFE ENGINE",
                tint = colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ARISE : LIFE OS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onBackground,
                fontSize = 16.sp,
                letterSpacing = 2.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (pagerState) {
                0 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🚀",
                            fontSize = 64.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "WELCOME TO THE FUTURE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = colors.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ARISE is an offline-first private tactical Life OS application. Reclaim your morning focus, manage events, track lifetime goals, habits, and configure bulletproof custom distress gates designed to wake you up.",
                            fontFamily = fontFamily,
                            fontSize = 13.sp,
                            color = colors.onBackground.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "SELECT APP SKIN SYSTEM",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val skins = listOf(
                            "Futuristic" to "⚡ Neon Cyberpunk Futuristic Layout",
                            "Minimal" to "⬛ Monochromatic Spacious Architecture",
                            "Classic" to "⏳ Warm Traditional Elegant Serif styling",
                            "Nature" to "🌲 Forest Organic Green and Calming Balance"
                        )

                        skins.forEach { (skinKey, desc) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedSkin = skinKey },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedSkin == skinKey) colors.primaryContainer else colors.surface
                                ),
                                border = BorderStroke(
                                    width = if (selectedSkin == skinKey) 2.dp else 1.dp,
                                    color = if (selectedSkin == skinKey) colors.primary else colors.divider
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedSkin == skinKey,
                                        onClick = { selectedSkin = skinKey },
                                        colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = skinKey.uppercase(),
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onSurface,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = desc,
                                            fontFamily = fontFamily,
                                            color = colors.onSurface.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CHOOSE YOUR ACCENT ACCELERATOR",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val dynamicAccents = listOf(
                            "#00F5FF" to "CYAN BEAM",
                            "#7C3AED" to "VIOLET ENGINE",
                            "#DAA520" to "GOLDEN BULLET",
                            "#4CAF50" to "FOREST PULSE"
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            dynamicAccents.forEach { (hex, label) ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = selectedAccent.equals(hex, ignoreCase = true)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { selectedAccent = hex }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) colors.onBackground else Color.Transparent,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = label.split(" ")[0],
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) colors.primary else colors.onBackground.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "PREVIEW STYLE ACCENT MATRIX",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(android.graphics.Color.parseColor(selectedAccent))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                                .border(1.dp, Color(android.graphics.Color.parseColor(selectedAccent)), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "CORE CALIBRATION SUCCESS",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = colors.onSurface
                            )
                        }
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                listOf(0, 1, 2).forEach { idx ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState == idx) 20.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState == idx) colors.primary
                                else colors.divider
                            )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState > 0) {
                    OutlinedButton(
                        onClick = { pagerState-- },
                        border = BorderStroke(1.dp, colors.divider),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .padding(end = 6.dp)
                    ) {
                        Text("BACK", fontFamily = FontFamily.Monospace, color = colors.onBackground, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (pagerState < 2) {
                            pagerState++
                        } else {
                            viewModel.completeOnboarding(selectedSkin, selectedAccent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(start = if (pagerState > 0) 6.dp else 0.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (pagerState == 2) "INITIALIZE SYSTEM" else "CONTINUE",
                        fontFamily = FontFamily.Monospace,
                        color = colors.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (pagerState < 2) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = { viewModel.completeOnboarding(selectedSkin, selectedAccent) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Skip Customization & Proceed",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
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
                    selected = currentTab == AriseTab.Home,
                    onClick = { viewModel.setTab(AriseTab.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home Command Center") },
                    label = { Text("Home", fontFamily = fontFamily, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.onPrimary,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.onBackground.copy(alpha = 0.5f),
                        unselectedTextColor = colors.onBackground.copy(alpha = 0.5f),
                        indicatorColor = colors.primary
                    )
                )

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
                    icon = { Icon(Icons.Default.Star, contentDescription = "Growth Operations") },
                    label = { Text("Growth", fontFamily = fontFamily, fontSize = 11.sp) },
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
                        AriseTab.Home -> AriseHomeTab(viewModel, colors, fontFamily)
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
    val customAffsList by viewModel.customAffirmations.collectAsState()
    val allQuotes = remember(customAffsList) {
        viewModel.wakeQuotes + customAffsList
    }
    var quoteIndex by remember { mutableStateOf(0) }
    var titleTapCount by remember { mutableStateOf(0) }
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    LaunchedEffect(allQuotes) {
        if (allQuotes.isNotEmpty() && quoteIndex >= allQuotes.size) {
            quoteIndex = 0
        }
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
                Column(
                    modifier = Modifier.clickable {
                        titleTapCount++
                        if (titleTapCount >= 5) {
                            if (userRole == "admin") {
                                viewModel.toggleAdminMode(!isAdminMode)
                                titleTapCount = 0
                                android.widget.Toast.makeText(
                                    viewModel.getApplication(),
                                    if (!isAdminMode) "🛠️ ADMIN CONSOLE UNLOCKED!" else "🛠️ ADMIN CONSOLE LOCKED.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                titleTapCount = 0
                                android.widget.Toast.makeText(
                                    viewModel.getApplication(),
                                    "Administrator permissions required.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ARISE WAKE ENGINE",
                            fontFamily = fontFamily,
                            color = colors.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        if (isPremium) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("👑", fontSize = 11.sp)
                        }
                        if (isAdminMode) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🛠️ [ADMIN]", fontSize = 10.sp, color = colors.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date()),
                        fontFamily = fontFamily,
                        color = colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                IconButton(
                    onClick = {
                        if (allQuotes.isNotEmpty()) {
                            quoteIndex = Random().nextInt(allQuotes.size)
                        }
                    },
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
                    text = if (allQuotes.isNotEmpty()) allQuotes[quoteIndex] else "Every day is a fresh beginning.",
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
    var alarmToEdit by remember { mutableStateOf<Alarm?>(null) }

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
                    AriseAlarmCard(viewModel, alarm, colors, fontFamily, onEditAlarm = { alarmToEdit = it })
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

        if (showAddSheet || alarmToEdit != null) {
            AriseAlarmDesignerDialog(
                viewModel = viewModel,
                colors = colors,
                fontFamily = fontFamily,
                alarmToEdit = alarmToEdit,
                onDismiss = {
                    showAddSheet = false
                    alarmToEdit = null
                }
            )
        }
    }
}

@Composable
fun AriseAlarmCard(
    viewModel: AriseViewModel,
    alarm: Alarm,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    onEditAlarm: (Alarm) -> Unit
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
                        onClick = { onEditAlarm(alarm) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.testTag("edit_alarm_btn_${alarm.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Alarm", tint = colors.onPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = colors.onPrimary, fontFamily = fontFamily, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

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
    alarmToEdit: Alarm? = null,
    onDismiss: () -> Unit
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

                Spacer(modifier = Modifier.height(6.dp))
                val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val selectedDaysList = remember(repeatDays) {
                    repeatDays.split(",")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdays.forEach { day ->
                        val isSelected = selectedDaysList.contains(day) || (repeatDays == "Daily") || (repeatDays == "Mon,Tue,Wed,Thu,Fri" && day != "Sat" && day != "Sun") || (repeatDays == "Weekend" && (day == "Sat" || day == "Sun"))
                        val dayColor = if (isSelected) colors.primary else colors.divider
                        val textColor = if (isSelected) colors.onPrimary else colors.onBackground
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(dayColor)
                                .clickable {
                                    if (repeatDays == "One-time" || repeatDays == "Daily" || repeatDays == "Mon,Tue,Wed,Thu,Fri" || repeatDays == "Weekend") {
                                        repeatDays = day
                                    } else {
                                        val currentMutable = selectedDaysList.toMutableList()
                                        if (currentMutable.contains(day)) {
                                            currentMutable.remove(day)
                                        } else {
                                            currentMutable.add(day)
                                        }
                                        if (currentMutable.isEmpty()) {
                                            repeatDays = "One-time"
                                        } else {
                                            repeatDays = currentMutable.joinToString(",")
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(day.take(2), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor, fontFamily = fontFamily)
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
                Text("ALARM MUSIC TONE & LENGTH DESIGNER", fontFamily = fontFamily, color = colors.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🎵 Active Tone: $soundName",
                            fontFamily = fontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom downloaded song vault
                        Text("Preloaded Cloud Vault", fontFamily = fontFamily, color = colors.onSurface.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                                            soundName = name
                                            soundPath = null // standard preloaded asset simulation
                                            soundStartMs = 10
                                            soundEndMs = 50
                                        },
                                    border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.divider),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) colors.primaryContainer else colors.background)
                                ) {
                                    Text(
                                        name,
                                        fontFamily = fontFamily,
                                        fontSize = 11.sp,
                                        color = if (isSelected) colors.primary else colors.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Standard file picker button
                        Button(
                            onClick = { soundPickerLauncher.launch("audio/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Import downloaded audio", modifier = Modifier.size(16.dp), tint = colors.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📥 Choose Local Downloaded Song (.mp3)", fontSize = 11.sp, fontFamily = fontFamily, color = colors.primary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

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
                            Text("Start Sec: $soundStartMs", fontFamily = fontFamily, fontSize = 10.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
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
                            Text("End Sec: $soundEndMs", fontFamily = fontFamily, fontSize = 10.sp, color = colors.onSurface, modifier = Modifier.weight(1f))
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Play Trim Preview Button
                        Button(
                            onClick = {
                                if (isPreviewPlaying) {
                                    stopPreview()
                                } else {
                                    try {
                                        val player = android.media.MediaPlayer()
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
                            colors = ButtonDefaults.buttonColors(containerColor = if (isPreviewPlaying) Color.Red else colors.primary),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPreviewPlaying) "Stop Preview" else "Play Preview",
                                modifier = Modifier.size(16.dp),
                                tint = if (isPreviewPlaying) Color.White else colors.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isPreviewPlaying) "Stop Trim Preview" else "Preview Range Selection 🎵",
                                fontSize = 11.sp,
                                fontFamily = fontFamily,
                                color = if (isPreviewPlaying) Color.White else colors.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                                    id = alarmToEdit?.id ?: 0,
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
                                    soundEndMs = soundEndMs,
                                    isActive = alarmToEdit?.isActive ?: true
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
    val alarmsList by viewModel.alarms.collectAsState()
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
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onDragEnd = {
                                if (totalDrag > 100f) {
                                    // Swipe Right -> Prev Month
                                    if (currentMonth == 0) {
                                        currentMonth = 11
                                        currentYear -= 1
                                    } else {
                                        currentMonth -= 1
                                    }
                                    selectedDay = 1
                                } else if (totalDrag < -100f) {
                                    // Swipe Left -> Next Month
                                    if (currentMonth == 11) {
                                        currentMonth = 0
                                        currentYear += 1
                                    } else {
                                        currentMonth += 1
                                    }
                                    selectedDay = 1
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            }
                        )
                    },
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

                                            // Determine day of week to check recurring alarms
                                            val dayOfWeekNum = dayCal.get(Calendar.DAY_OF_WEEK)
                                            val dayOfWeekStr = when (dayOfWeekNum) {
                                                Calendar.SUNDAY -> "Sun"
                                                Calendar.MONDAY -> "Mon"
                                                Calendar.TUESDAY -> "Tue"
                                                Calendar.WEDNESDAY -> "Wed"
                                                Calendar.THURSDAY -> "Thu"
                                                Calendar.FRIDAY -> "Fri"
                                                else -> "Sat"
                                            }
                                            val hasMatchingAlarm = alarmsList.any { alarm ->
                                                alarm.isActive && (
                                                    alarm.repeatDays.contains("Daily") ||
                                                    alarm.repeatDays.contains(dayOfWeekStr)
                                                )
                                            }

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

                                                if (hasMatchingEvent || hasMatchingAlarm) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Max 2 dots for events
                                                        eventsOnCell.take(2).forEach { ev ->
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
                                                        // Max 1 dot for dynamic scheduled alarms
                                                        if (hasMatchingAlarm) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(horizontal = 1.dp)
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFFF9800))
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
    viewModel: AriseViewModel,
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
    viewModel: AriseViewModel,
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

                // Priority rating
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

                // Category & Theme selector
                Spacer(modifier = Modifier.height(12.dp))
                Text("Category & Theme Color", fontFamily = fontFamily, color = colors.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                val customCategories = listOf(
                    Triple("Work", "#2196F3", "💼"),
                    Triple("Health", "#E91E63", "❤️"),
                    Triple("Study", "#FF9800", "🎓"),
                    Triple("Personal", "#9C27B0", "👤"),
                    Triple("Leisure", "#4CAF50", "🍹")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    customCategories.forEach { (cat, col, emoji) ->
                        val isSelected = category == cat
                        val parsedColor = try { Color(android.graphics.Color.parseColor(col)) } catch(e: Exception) { colors.primary }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) parsedColor.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (isSelected) parsedColor else colors.divider, RoundedCornerShape(8.dp))
                                .clickable {
                                    category = cat
                                    colorHex = col
                                }
                                .padding(8.dp)
                                .weight(1f)
                        ) {
                            Text(emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) parsedColor else colors.onSurface)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
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
    val habitsList by viewModel.habits.collectAsState()
    val completionsList by viewModel.habitCompletions.collectAsState()

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
                        Text("Link calendar alarms to complete lifetime streak milestones.", fontFamily = fontFamily, color = colors.onBackground.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
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
            } else {
                // HABITS SUB-TAB CORES
                if (habitsList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Drawing custom modern plant vector canvas diagram
                        Canvas(modifier = Modifier.size(120.dp)) {
                            // Circular background glow
                            drawCircle(
                                color = colors.primary.copy(alpha = 0.1f),
                                radius = size.minDimension / 1.8f
                            )
                            // Stem
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
                            // Leaves
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
                        items(habitsList) { habit ->
                            val completions = completionsList.filter { it.habitId == habit.id }
                            AriseHabitCard(viewModel, habit, completions, colors, fontFamily)
                        }
                    }
                }
            }
        }

        // Action FAB overlay
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
                viewModel = viewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showAddGoal = false }
            )
        }

        if (showAddHabit) {
            AriseHabitDesignerDialog(
                viewModel = viewModel,
                colors = colors,
                fontFamily = fontFamily,
                onDismiss = { showAddHabit = false }
            )
        }
    }
}

@Composable
fun AriseHabitCard(
    viewModel: AriseViewModel,
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

            // --- 7 DAY VISUAL TIMELINE GRID STRIP ---
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

                    // Grid Item
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
    viewModel: AriseViewModel,
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

                val isPremium by viewModel.isPremium.collectAsState()
                val habitsList by viewModel.habits.collectAsState()
                val habitLimitVal by viewModel.habitLimit.collectAsState()
                val limitReached = habitsList.size >= habitLimitVal && !isPremium

                if (limitReached) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(1.dp, colors.primary, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "FREE LIMIT REACHED (${habitsList.size}/$habitLimitVal)",
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Cultivating new habit trails requires upgrading to Cosmic Premium. Unlimited trails, dynamic styling, and cloud hypersync.",
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                color = colors.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Explore Cosmic Premium Level", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                    }
                } else {
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

                val isPremium by viewModel.isPremium.collectAsState()
                val goalsList by viewModel.goals.collectAsState()
                val goalLimitVal by viewModel.goalLimit.collectAsState()
                val limitReached = goalsList.size >= goalLimitVal && !isPremium

                if (limitReached) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(1.dp, colors.primary, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "FREE LIMIT REACHED (${goalsList.size}/$goalLimitVal)",
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Starting new growth missions requires upgrading to Cosmic Premium. Unlimited missions, dynamic styling, and cloud hypersync.",
                                fontFamily = fontFamily,
                                fontSize = 11.sp,
                                color = colors.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Explore Cosmic Premium Level", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                    }
                } else {
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
                        Column {
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

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "CUSTOM SLIDER ASSISTED NAP DURATION",
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            var customSliderMins by remember { mutableStateOf(15f) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Slider(
                                    value = customSliderMins,
                                    onValueChange = { customSliderMins = it },
                                    valueRange = 5f..120f,
                                    steps = 22,
                                    colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Button(
                                    onClick = { viewModel.startNapTimer(customSliderMins.toInt()) },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("start_custom_nap")
                                ) {
                                    Text("${customSliderMins.toInt()}m", color = colors.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
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
            var showManualLogDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showManualLogDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("log_sleep_manually_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Sleep", tint = colors.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOG SLEEP MANUALLY", color = colors.onPrimary, fontWeight = FontWeight.Bold, fontFamily = fontFamily, fontSize = 11.sp)
            }

            if (showManualLogDialog) {
                var hoursSlept by remember { mutableStateOf(8f) }
                var selectedMood by remember { mutableStateOf("Neutral") }
                var notes by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showManualLogDialog = false },
                    title = {
                        Text(
                            "LOG PHYSICAL SLEEP CYCLE",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column {
                            Text(
                                "Enter the duration of sleep as well as your morning feel state level.",
                                fontSize = 12.sp,
                                fontFamily = fontFamily,
                                color = colors.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Duration Slept: ${"%.1f".format(hoursSlept)} hours",
                                fontFamily = fontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Slider(
                                value = hoursSlept,
                                onValueChange = { hoursSlept = it },
                                valueRange = 2f..14f,
                                steps = 24,
                                colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Wake Mind/Mood Style",
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("Awesome", "Neutral", "Tired", "Restless").forEach { mood ->
                                    val isSelected = selectedMood == mood
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) colors.primary else colors.divider)
                                            .clickable { selectedMood = mood }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = mood,
                                            fontFamily = fontFamily,
                                            fontSize = 10.sp,
                                            color = if (isSelected) colors.onPrimary else colors.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Quality sleep notes", color = colors.onSurface) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val wake = System.currentTimeMillis()
                                val bed = wake - (hoursSlept * 3600000L).toLong()
                                viewModel.addManualSleepLog(bed, wake, selectedMood, notes)
                                showManualLogDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                        ) {
                            Text("SAVE CYCLE", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showManualLogDialog = false }) {
                            Text("CANCEL", color = colors.primary)
                        }
                    },
                    containerColor = colors.surface,
                    shape = RoundedCornerShape(16.dp)
                )
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

    val isPremium by viewModel.isPremium.collectAsState()
    var showPremiumUnlockDialog by remember { mutableStateOf(false) }

    val habitsList by viewModel.habits.collectAsState()
    val completionsList by viewModel.habitCompletions.collectAsState()
    val currentHabitLimit by viewModel.habitLimit.collectAsState()
    val currentGoalLimit by viewModel.goalLimit.collectAsState()
    val promoInput by viewModel.promoCodeInput.collectAsState()
    val promoStatus by viewModel.promoCodeStatus.collectAsState()
    val isAdminModeActive by viewModel.isAdminMode.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

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
                        text = "APP SKIN & VISUAL THEMES",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Skin Selectors List
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
                                        viewModel.updateAppSkin(skin)
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
                                            viewModel.updateAccentColor(hex)
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

                    // Dark mode toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Mode (Eye-Friendly Style)", fontFamily = fontFamily, color = colors.onSurface, fontSize = 13.sp)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleAppTheme() },
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.primary),
                            modifier = Modifier.testTag("theme_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Navigation Sound effects toggle
                    val navSoundsEnabled by viewModel.navigationSoundsEnabled.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("UI Navigation Sound Effects", fontFamily = fontFamily, color = colors.onSurface, fontSize = 13.sp)
                        Switch(
                            checked = navSoundsEnabled,
                            onCheckedChange = { viewModel.toggleNavigationSounds() },
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reset Onboarding button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reset App Onboarding", fontFamily = fontFamily, color = colors.onSurface, fontSize = 13.sp)
                        Button(
                            onClick = { viewModel.resetOnboarding() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("RUN TUTORIAL AGAIN", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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

        // --- CUSTOM AFFIRMATIONS AND MORNING QUOTES CARD ---
        item {
            var newAffirmationText by remember { mutableStateOf("") }
            val customAffsList by viewModel.customAffirmations.collectAsState()

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
                        text = "AFFIRMATION SYSTEM: BUILD CUSTOM MANTRAS",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Type in custom affirmations to rotate alongside standard wake Quotes on your Command Center banner.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newAffirmationText,
                        onValueChange = { newAffirmationText = it },
                        placeholder = { Text("e.g. Every day is a fresh opportunity...", color = colors.onSurface.copy(alpha = 0.5f)) },
                        label = { Text("New Custom Quote", color = colors.onSurface) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (newAffirmationText.isNotBlank()) {
                                viewModel.addCustomAffirmation(newAffirmationText)
                                newAffirmationText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Custom Affirmation", color = colors.onPrimary)
                    }

                    if (customAffsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ACTIVE MANTRAS",
                            fontFamily = fontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            customAffsList.forEach { aff ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colors.background, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = aff,
                                        fontFamily = fontFamily,
                                        fontSize = 12.sp,
                                        color = colors.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteCustomAffirmation(aff) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete Quote",
                                            tint = Color.Red,
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

        // --- SUPABASE CLOUD SYNC VAULT ---
        item {
            val dbUrl by viewModel.supabaseUrl.collectAsState()
            val dbKey by viewModel.supabaseAnonKey.collectAsState()
            val userEmailByM by viewModel.userEmail.collectAsState()
            val statusMessage by viewModel.supabaseStatus.collectAsState()
            val showSqlAlert by viewModel.showSqlSuggestion.collectAsState()

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
                        text = "COSMIC CLOUD SYNC (SUPABASE HYPERSYNC)",
                        fontFamily = fontFamily,
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Establish robust, safe sync links automatically with the central cloud storage.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.70f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userEmailByM,
                        onValueChange = { viewModel.updateSupabaseUserEmail(it) },
                        label = { Text("Backup Email Address", color = colors.onSurface) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status display block
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
                                    viewModel.uploadBackupToSupabase()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.weight(1f)
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
                                    viewModel.restoreBackupFromSupabase()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                            modifier = Modifier.weight(1f)
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
                            viewModel.logoutUser()
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

                    if (showSqlAlert) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "REQUIRED TABLE STRUCTURE (SQL SCRIPT)",
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

        // --- BLACK CARDS FOR FREEMIUM & ADMINS ---
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
                                text = "🛠️ ADMIN COMMAND CENTER CORE",
                                fontFamily = fontFamily,
                                color = colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.toggleAdminMode(false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Admin", tint = colors.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Manage business rules, mock database, metrics simulation, and system telemetry.",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface.copy(alpha = 0.70f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("USER PREMIUM SIMULATION", fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Force Override Premium Status: ${if (isPremium) "ACTIVE" else "DISABLED"}", fontFamily = fontFamily, fontSize = 12.sp)
                            Switch(
                                checked = isPremium,
                                onCheckedChange = { viewModel.setPremiumStatus(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = colors.primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("FREE USER RESOURCE LIMITS", fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Max Free Habits: $currentHabitLimit", modifier = Modifier.width(140.dp), fontFamily = fontFamily, fontSize = 12.sp)
                            Slider(
                                value = currentHabitLimit.toFloat(),
                                onValueChange = { viewModel.setHabitLimit(it.toInt()) },
                                valueRange = 1f..10f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = colors.primary)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Max Free Goals: $currentGoalLimit", modifier = Modifier.width(140.dp), fontFamily = fontFamily, fontSize = 12.sp)
                            Slider(
                                value = currentGoalLimit.toFloat(),
                                onValueChange = { viewModel.setGoalLimit(it.toInt()) },
                                valueRange = 1f..10f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = colors.primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("OFFLINE TELEMETRY & DB ROWS COUNTS", fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Registered Active Alarms", fontSize = 11.sp, fontFamily = fontFamily)
                                Text("${alarmsList.size} items", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Simulated Sleep Metric Logs", fontSize = 11.sp, fontFamily = fontFamily)
                                Text("${sleepLogsList.size} logs", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Tracked Growth Goals", fontSize = 11.sp, fontFamily = fontFamily)
                                Text("${goalsList.size} items", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Custom Habits Reg", fontSize = 11.sp, fontFamily = fontFamily)
                                Text("${habitsList.size} items", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Logged Habit Completions", fontSize = 11.sp, fontFamily = fontFamily)
                                Text("${completionsList.size} completions", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("SYSTEM MAINTENANCE EXECUTION", fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.injectMockupAnalyticsData() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Inject Mock Logs", color = colors.onPrimary, fontFamily = fontFamily, fontSize = 9.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.resetOnboarding()
                                    android.widget.Toast.makeText(viewModel.getApplication(), "Onboarding reset completed!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset Onboarding", color = Color.White, fontFamily = fontFamily, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            var adminInputPasscode by remember { mutableStateOf("") }

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
                            text = "COSMIC PLATINUM LICENSARDS",
                            fontFamily = fontFamily,
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isPremium) {
                            Text(
                                "👑 PREMIUM",
                                color = colors.primary,
                                fontFamily = fontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPremium)
                            "You are currently enjoying unhindered, unlimited Cosmic Arise operations. Thank you for supporting offline-first developers!"
                        else
                            "Unlock limitless habits, Supabase backups, personalized layouts, and infinite statistics tracker.",
                        fontFamily = fontFamily,
                        fontSize = 12.sp,
                        color = colors.onSurface.copy(alpha = 0.70f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isPremium) {
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { viewModel.updatePromoCodeInput(it) },
                            label = { Text("Redeem Premium Key (e.g. COSMIC99)", color = colors.onSurface) },
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.redeemPromoCode() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("Redeem Upgrade Key", color = colors.onPrimary, fontFamily = fontFamily, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.setPremiumStatus(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Mock Buy ($2.99)", color = Color.White, fontFamily = fontFamily, fontSize = 9.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.setPremiumStatus(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Revert to Free Version (Test Limits)", color = colors.onBackground, fontFamily = fontFamily, fontSize = 11.sp)
                        }
                    }

                    if (userRole == "admin") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = colors.divider)
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
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.onSurface),
                                modifier = Modifier.weight(1.5f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (adminInputPasscode == "admin123") {
                                        viewModel.toggleAdminMode(true)
                                        adminInputPasscode = ""
                                        android.widget.Toast.makeText(viewModel.getApplication(), "Admin Core Activated!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(viewModel.getApplication(), "Incorrect Passcode.", android.widget.Toast.LENGTH_SHORT).show()
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
    }

    if (showPremiumUnlockDialog) {
        ArisePremiumPromoCodeDialog(
            viewModel = viewModel,
            colors = colors,
            fontFamily = fontFamily,
            onDismiss = { showPremiumUnlockDialog = false }
        )
    }
}

@Composable
fun ArisePremiumPromoCodeDialog(
    viewModel: AriseViewModel,
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
    val context = LocalContext.current
    var isRinging by remember { mutableStateOf(false) }
    var mediaPlayerInstance by remember { mutableStateOf<android.media.MediaPlayer?>(null) }

    DisposableEffect(alarm.id) {
        val player = android.media.MediaPlayer()
        try {
            val alertUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)

            if (alarm.soundPath != null) {
                player.setDataSource(context, android.net.Uri.parse(alarm.soundPath))
            } else {
                player.setDataSource(context, alertUri)
            }
            player.setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            player.prepare()
            
            val startPosMs = (alarm.soundStartMs * 1000).coerceAtLeast(0)
            player.seekTo(startPosMs)
            player.start()
            mediaPlayerInstance = player
            isRinging = true
        } catch (e: Exception) {
            try {
                val fallbackUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val fallbackPlayer = android.media.MediaPlayer.create(context, fallbackUri)
                fallbackPlayer?.isLooping = true
                fallbackPlayer?.start()
                mediaPlayerInstance = fallbackPlayer
                isRinging = true
            } catch (ex: Exception) {}
        }

        onDispose {
            isRinging = false
            try {
                mediaPlayerInstance?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                }
            } catch (e: Exception) {}
            mediaPlayerInstance = null
        }
    }

    LaunchedEffect(alarm.id, isRinging) {
        if (isRinging) {
            val startPosMs = (alarm.soundStartMs * 1000).coerceAtLeast(0)
            val endPosMs = (alarm.soundEndMs * 1000).coerceAtLeast(startPosMs + 2000)
            while (isRinging) {
                try {
                    mediaPlayerInstance?.let { player ->
                        if (player.isPlaying) {
                            val currentPos = player.currentPosition
                            if (currentPos >= endPosMs || currentPos < startPosMs) {
                                player.seekTo(startPosMs)
                            }
                        }
                    }
                } catch (e: Exception) {}
                kotlinx.coroutines.delay(200)
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

// ============================================
// ========== ARISE HOME COMMAND CENTER =======
// ============================================
@Composable
fun AriseHomeTab(
    viewModel: AriseViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val alarmsList by viewModel.alarms.collectAsState()
    val eventsList by viewModel.events.collectAsState()
    val goalsList by viewModel.goals.collectAsState()
    val sleepLogsList by viewModel.sleepLogs.collectAsState()
    val habitsList by viewModel.habits.collectAsState()

    // 1. Live ticking Clock State
    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }
    var blinkState by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdfDate = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
            currentTimeStr = sdfTime.format(now.time)
            currentDateStr = sdfDate.format(now.time)
            blinkState = !blinkState
            kotlinx.coroutines.delay(1000)
        }
    }

    // 2. Compute Next Alarm
    val nextAlarm = remember(alarmsList) {
        alarmsList.filter { it.isActive }.minByOrNull { (it.hour * 60) + it.minute }
    }

    // 3. Today's Events
    val todayEvents = remember(eventsList) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
        eventsList.filter { it.startTime in todayStart..todayEnd }
    }

    // 4. Sleep Score computation from latest sleep log
    val latestSleepScore = remember(sleepLogsList) {
        val lastLog = sleepLogsList.maxByOrNull { it.wakeTime }
        if (lastLog != null) {
            val actual = ((lastLog.wakeTime - lastLog.bedTime) / 3600000f).coerceAtLeast(0f)
            val score = (actual / lastLog.targetHours * 100).toInt().coerceIn(0, 100)
            score
        } else {
            82 // Simulated default
        }
    }

    // 5. Habits Streaks Summary
    val habitStreaks = remember(habitsList) {
        habitsList.filter { !it.isArchived }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- LIVE TICKING CLOCK ZONE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("home_clock_card"),
            colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.9f)),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COMMAND CENTER",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Time with flashing colon separator
                val blinkColon = if (blinkState) ":" else " "
                val timeParts = currentTimeStr.split(":")
                val displayTime = if (timeParts.size == 2) {
                    "${timeParts[0]}$blinkColon${timeParts[1]}"
                } else {
                    currentTimeStr.ifEmpty { "07:00" }
                }

                Text(
                    text = displayTime,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = currentDateStr.ifEmpty { "Monday, June 01" },
                    fontFamily = fontFamily,
                    fontSize = 13.sp,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = colors.divider, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // NEXT ALARM SUBSECTION
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Next Alarm Signal",
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (nextAlarm != null) {
                            val hourStr = if (nextAlarm.hour < 10) "0${nextAlarm.hour}" else "${nextAlarm.hour}"
                            val minStr = if (nextAlarm.minute < 10) "0${nextAlarm.minute}" else "${nextAlarm.minute}"
                            "NEXT ACTIVE ALARM: $hourStr:$minStr (${nextAlarm.label})"
                        } else {
                            "NO ACTIVE ALARM PATHS CONFIGURED"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }
            }
        }

        // --- ASYMMETRIC STATS DOCK HEADER ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SLEEP SCORE GAUGE CARD (Compact Left Card)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "SLEEP SCORE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { latestSleepScore.toFloat() / 100f },
                            modifier = Modifier.size(52.dp),
                            color = colors.primary,
                            strokeWidth = 5.dp,
                            trackColor = colors.divider
                        )
                        Text(
                            text = "$latestSleepScore%",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                }
            }

            // HABIT STREAK FLAME CARD (Compact Right Card)
            val maxStreakVal = remember(habitStreaks) {
                habitStreaks.maxOfOrNull { it.currentStreak } ?: 0
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "MAX STREAK",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Active Streaks Fire",
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$maxStreakVal DAYS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onSurface
                    )
                }
            }
        }

        // --- REALTIME PHYSICAL HYDRATION TRACKER ---
        val dailyWaterCups by viewModel.dailyWaterCups.collectAsState()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("hydration_card"),
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
                        Text(
                            text = "DAILY COGNITIVE HYDRATION BALANCE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$dailyWaterCups / 8 Cups (2.0L Goal)",
                            fontFamily = fontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.adjustWaterCups(-1) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.divider)
                        ) {
                            Text("-", color = colors.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.adjustWaterCups(1) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                        ) {
                            Text("+", color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Render dynamic visual grids of cups representing the cups drank!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 1..8) {
                        val isFilled = dailyWaterCups >= i
                        Icon(
                            imageVector = if (isFilled) Icons.Default.LocalDrink else Icons.Default.WaterDrop,
                            contentDescription = "Water Cup $i",
                            tint = if (isFilled) Color(0xFF00B2FF) else colors.divider,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // --- KEYSTONE TOP 3 DAILY MISSIONS PANEL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("home_missions_card"),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "TOP 3 CONSCIOUS MISSIONS TODAY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                val missionLabels = listOf(
                    "Drink 500ml water instantly after wake gate dismiss",
                    "Acknowledge daily learning milestones & update database",
                    "Verify sleep schedules & configure bedside modes"
                )

                missionLabels.forEachIndexed { index, missionText ->
                    var isChecked by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isChecked = !isChecked }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.divider)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = missionText,
                            fontFamily = fontFamily,
                            fontSize = 12.sp,
                            color = if (isChecked) colors.onSurface.copy(alpha = 0.5f) else colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (index < 2) {
                        Divider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        // --- CALENDAR EVENTS AGENDA ROW ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("home_agenda_card"),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "TODAY'S BLUEPRINT EVENTS",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (todayEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO COMMITMENTS SCHEDULED FOR TODAY",
                            fontFamily = fontFamily,
                            fontSize = 11.sp,
                            color = colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    todayEvents.forEachIndexed { index, event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(event.colorHex)))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = event.title,
                                        fontFamily = fontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    Text(
                                        text = "${sdf.format(Date(event.startTime))} - ${sdf.format(Date(event.endTime))}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Text(
                                text = event.priority,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = colors.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.primaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (index < todayEvents.size - 1) {
                            Divider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // --- GOAL PROGRESS BLOCK LIST ---
        if (goalsList.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("home_goals_card"),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "LIFETIME STRATEGY PROGRESSIONS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    goalsList.take(3).forEachIndexed { index, goal ->
                        val percent = (goal.currentProgress.toFloat() / goal.targetProgress.toFloat()).coerceIn(0f, 1f)
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.title,
                                    fontFamily = fontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "${goal.currentProgress}/${goal.targetProgress}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = colors.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { percent },
                                color = colors.primary,
                                trackColor = colors.divider,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                        if (index < goalsList.take(3).size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // --- HABIT TRAIL STREAKS ---
        if (habitStreaks.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("home_habits_card"),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ACTIVE HABIT ASCENT PATHS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    habitStreaks.take(4).forEachIndexed { index, habit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🔥 ${habit.title}",
                                fontFamily = fontFamily,
                                fontSize = 12.sp,
                                color = colors.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "STREAK:",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = colors.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${habit.currentStreak} DAYS",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                        if (index < habitStreaks.take(4).size - 1) {
                            Divider(color = colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
