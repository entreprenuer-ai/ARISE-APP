package com.example.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    object Onboarding : Screen()

    @Serializable
    object AppLock : Screen()

    @Serializable
    object MainDashboard : Screen()

    @Serializable
    object AlarmTriggered : Screen()
}
