package com.example.features.onboarding.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CustomColorScheme
import com.example.features.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun AriseOnboardingScreen(
    viewModel: SettingsViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    var pagerState by remember { mutableStateOf(0) }
    var selectedSkin by remember { mutableStateOf("Futuristic") }
    var selectedAccent by remember { mutableStateOf("#00F5FF") }

    val isDark by viewModel.isDarkTheme.collectAsState()

    com.example.core.designsystem.AriseThemeWrapper(
        appSkin = selectedSkin,
        accentColorHex = selectedAccent,
        isDark = isDark
    ) { colors, fontFamily ->
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ARISE SYSTEM COEFFICIENT",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = colors.primary.copy(alpha = 0.8f),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "INITIAL CALIBRATION PROTOCOL",
                fontFamily = fontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = colors.onBackground,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (pagerState) {
                0 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🌟 YOUR COGNITIVE MORNING CO-PILOT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Welcome to Arise—a private, mindful wake-up supervisor designed to simplify your mornings. Arise gently guides your focus through interactive tasks, helping you start each day alert and fully refreshed.\n\n" +
                                    "No more lazy snoozing. Arise assists in transitioning your state of mind peacefully from rest to productive energy.",
                            fontFamily = fontFamily,
                            color = colors.onBackground.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.divider, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🔒 100% OFFLINE PRIVACY\nZero tracking. Zero external servers. All operations execute strictly on your device.",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = colors.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                1 -> {
                    Column {
                        Text(
                            text = "SELECT YOUR APP SKIN & THEME",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )

                        val skins = listOf(
                            "Futuristic" to "⚡ Futuristic (Neon Cyberpunk style)",
                            "Minimal" to "⬛ Minimal (Clean simplicity model)",
                            "Classic" to "⏳ Classic (Traditional serif style) [Premium] 👑",
                            "Nature" to "🌲 Nature (Calming foliage green accent) [Premium] 👑",
                            "Cosmic Void" to "🌌 Cosmic Void (Glow lavender theme) [Premium] 👑",
                            "Neon Synthwave" to "🎵 Neon Synthwave (Melt-Your-Face neon pink) [Premium] 👑"
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
                            text = "CHOOSE YOUR CUSTOM ACCENT COLOR",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val dynamicAccents = listOf(
                            "#00F5FF" to "COOL CYAN",
                            "#7C3AED" to "DEEP VIOLET",
                            "#DAA520" to "VINTAGE GOLD",
                            "#4CAF50" to "FRESH FOREST"
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
                            text = "ACCENT COLOR PREVIEW",
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
                                text = "Looking clean and polished!",
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
                        text = if (pagerState == 2) "GET STARTED" else "CONTINUE",
                        fontFamily = FontFamily.Monospace,
                        color = colors.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
}
