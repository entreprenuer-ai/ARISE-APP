package com.example.features.dashboard.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.example.core.designsystem.CustomColorScheme
import com.example.features.dashboard.presentation.viewmodel.AriseTab
import com.example.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.example.features.alarms.presentation.viewmodel.AlarmViewModel
import com.example.features.calendar.presentation.viewmodel.CalendarViewModel
import com.example.features.goals.presentation.viewmodel.GoalsViewModel
import com.example.features.habits.presentation.viewmodel.HabitsViewModel
import com.example.features.sleep.presentation.viewmodel.SleepViewModel
import com.example.features.sleep.presentation.viewmodel.SleepTrackingViewModel
import com.example.features.statistics.presentation.viewmodel.StatisticsViewModel
import com.example.features.settings.presentation.viewmodel.SettingsViewModel
import com.example.features.security.presentation.viewmodel.SecurityViewModel
import com.example.features.backup.presentation.viewmodel.BackupViewModel
import com.example.navigation.AriseBottomBar

import com.example.features.alarms.presentation.screens.AriseAlarmsTab
import com.example.features.calendar.presentation.screens.AriseCalendarTab
import com.example.features.goals.presentation.screens.AriseGoalsTab
import com.example.features.sleep.presentation.screens.AriseSleepTab
import com.example.features.settings.presentation.screens.AriseSettingsTab

@Composable
fun AriseMainDashboardScreen(
    dashboardViewModel: DashboardViewModel,
    alarmViewModel: AlarmViewModel,
    calendarViewModel: CalendarViewModel,
    goalsViewModel: GoalsViewModel,
    habitsViewModel: HabitsViewModel,
    sleepViewModel: SleepViewModel,
    sleepTrackingViewModel: SleepTrackingViewModel,
    statisticsViewModel: StatisticsViewModel,
    settingsViewModel: SettingsViewModel,
    securityViewModel: SecurityViewModel,
    backupViewModel: BackupViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    val currentTab by dashboardViewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = colors.background,
        bottomBar = {
            AriseBottomBar(
                viewModel = dashboardViewModel,
                currentTab = currentTab,
                colors = colors,
                fontFamily = fontFamily
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AriseTab.Home -> {
                    AriseHomeTab(
                        alarmViewModel = alarmViewModel,
                        calendarViewModel = calendarViewModel,
                        goalsViewModel = goalsViewModel,
                        habitsViewModel = habitsViewModel,
                        sleepViewModel = sleepViewModel,
                        sleepTrackingViewModel = sleepTrackingViewModel,
                        onNavigateToSleep = { dashboardViewModel.setTab(AriseTab.Sleep) },
                        onNavigateToCalendar = { dashboardViewModel.setTab(AriseTab.Calendar) },
                        colors = colors,
                        fontFamily = fontFamily
                    )
                }
                AriseTab.Alarms -> {
                    AriseAlarmsTab(
                        viewModel = alarmViewModel,
                        colors = colors,
                        fontFamily = fontFamily
                    )
                }
                AriseTab.Calendar -> {
                    AriseCalendarTab(
                        viewModel = calendarViewModel,
                        alarmViewModel = alarmViewModel,
                        colors = colors,
                        fontFamily = fontFamily
                    )
                }
                AriseTab.Goals -> {
                    AriseGoalsTab(
                        goalsViewModel = goalsViewModel,
                        habitsViewModel = habitsViewModel,
                        colors = colors,
                        fontFamily = fontFamily
                    )
                }
                AriseTab.Sleep -> {
                    AriseSleepTab(
                        viewModel = sleepViewModel,
                        sleepTrackingViewModel = sleepTrackingViewModel,
                        colors = colors,
                        fontFamily = fontFamily
                    )
                }
                AriseTab.StatsCustomize -> {
                    AriseSettingsTab(
                        settingsViewModel = settingsViewModel,
                        alarmsViewModel = alarmViewModel,
                        goalsViewModel = goalsViewModel,
                        habitsViewModel = habitsViewModel,
                        securityViewModel = securityViewModel,
                        backupViewModel = backupViewModel,
                        colors = colors,
                        fontFamily = fontFamily
                    )
                }
            }
        }
    }
}
