package com.example.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CustomColorScheme
import com.example.features.dashboard.presentation.viewmodel.AriseTab
import com.example.features.dashboard.presentation.viewmodel.DashboardViewModel

@Composable
fun AriseBottomBar(
    viewModel: DashboardViewModel,
    currentTab: AriseTab,
    colors: CustomColorScheme,
    fontFamily: androidx.compose.ui.text.font.FontFamily
) {
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
