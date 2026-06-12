package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.core.designsystem.CustomColorScheme
import androidx.compose.ui.text.font.FontFamily
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
import com.example.features.calendar.presentation.viewmodel.SmartAlarmViewModel
import com.example.features.onboarding.presentation.screens.AriseOnboardingScreen
import com.example.features.security.presentation.screens.AriseAppLockScreen
import com.example.features.dashboard.presentation.screens.AriseMainDashboardScreen
import com.example.features.alarms.presentation.screens.AriseAlarmTriggeredScreen

@Composable
fun RootNavGraph(
    navController: NavHostController,
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
    smartAlarmViewModel: SmartAlarmViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily,
    startDestination: Screen = Screen.MainDashboard,
    userRole: String = "user"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Onboarding> {
            AriseOnboardingScreen(
                viewModel = settingsViewModel,
                colors = colors,
                fontFamily = fontFamily
            )
        }

        composable<Screen.AppLock> {
            AriseAppLockScreen(
                viewModel = securityViewModel,
                colors = colors,
                fontFamily = fontFamily
            )
        }

        composable<Screen.MainDashboard> {
            AriseMainDashboardScreen(
                dashboardViewModel = dashboardViewModel,
                alarmViewModel = alarmViewModel,
                calendarViewModel = calendarViewModel,
                smartAlarmViewModel = smartAlarmViewModel,
                goalsViewModel = goalsViewModel,
                habitsViewModel = habitsViewModel,
                sleepViewModel = sleepViewModel,
                sleepTrackingViewModel = sleepTrackingViewModel,
                statisticsViewModel = statisticsViewModel,
                settingsViewModel = settingsViewModel,
                securityViewModel = securityViewModel,
                backupViewModel = backupViewModel,
                colors = colors,
                fontFamily = fontFamily,
                userRole = userRole
            )
        }

        composable<Screen.AlarmTriggered> {
            AriseAlarmTriggeredScreen(
                viewModel = alarmViewModel,
                colors = colors,
                fontFamily = fontFamily
            )
        }
    }
}
