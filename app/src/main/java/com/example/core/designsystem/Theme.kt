package com.example.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

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
            Color(0xFF3F51B5)
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
