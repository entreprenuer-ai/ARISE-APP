package com.example.features.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AriseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    private val _appSkin = MutableStateFlow("Futuristic")
    val appSkin: StateFlow<String> = _appSkin.asStateFlow()

    private val _accentColorHex = MutableStateFlow("#00F5FF")
    val accentColorHex: StateFlow<String> = _accentColorHex.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _firstBootCompleted = MutableStateFlow(false)
    val firstBootCompleted: StateFlow<Boolean> = _firstBootCompleted.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _isPremiumLocal = MutableStateFlow(false)
    private val _isPremiumInitiallyFree = MutableStateFlow(true)
    private val _premiumExpiryTime = MutableStateFlow(0L)
    private val _forcePremiumOverride = MutableStateFlow(false)

    fun updateCalculatedPremiumStatus() {
        val initiallyFree = _isPremiumInitiallyFree.value
        val override = _forcePremiumOverride.value
        val local = _isPremiumLocal.value
        val expiry = _premiumExpiryTime.value
        val currentTime = System.currentTimeMillis()
        _isPremium.value = initiallyFree || override || local || (expiry > 0L && currentTime < expiry)
    }

    private val _isTrialActive = MutableStateFlow(false)
    val isTrialActive: StateFlow<Boolean> = _isTrialActive.asStateFlow()

    private val _selectedAlarmSound = MutableStateFlow("Default Arise Chime")
    val selectedAlarmSound: StateFlow<String> = _selectedAlarmSound.asStateFlow()

    private val _habitLimit = MutableStateFlow(3)
    val habitLimit: StateFlow<Int> = _habitLimit.asStateFlow()

    private val _goalLimit = MutableStateFlow(3)
    val goalLimit: StateFlow<Int> = _goalLimit.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _promoCodeInput = MutableStateFlow("")
    val promoCodeInput: StateFlow<String> = _promoCodeInput.asStateFlow()

    private val _promoCodeStatus = MutableStateFlow("")
    val promoCodeStatus: StateFlow<String> = _promoCodeStatus.asStateFlow()

    init {
        loadSettingsFromDb()
    }

    private fun loadSettingsFromDb() = viewModelScope.launch {
        repository.allSettings.collect { list ->
            list.forEach { setting ->
                when (setting.key) {
                    "appSkin", "app_skin" -> _appSkin.value = setting.value
                    "accentColorHex", "accent_color" -> _accentColorHex.value = setting.value
                    "isDarkTheme", "is_dark_theme" -> _isDarkTheme.value = setting.value.toBoolean()
                    "firstBootCompleted", "first_boot_completed" -> _firstBootCompleted.value = setting.value.toBoolean()
                    "isPremium", "is_premium" -> {
                        _isPremiumLocal.value = setting.value.toBoolean()
                        updateCalculatedPremiumStatus()
                    }
                    "is_premium_initially_free" -> {
                        _isPremiumInitiallyFree.value = setting.value.toBoolean()
                        updateCalculatedPremiumStatus()
                    }
                    "premium_expiry_time" -> {
                        _premiumExpiryTime.value = setting.value.toLongOrNull() ?: 0L
                        updateCalculatedPremiumStatus()
                    }
                    "force_premium_override" -> {
                        _forcePremiumOverride.value = setting.value.toBoolean()
                        updateCalculatedPremiumStatus()
                    }
                    "habitLimit", "habit_limit" -> _habitLimit.value = setting.value.toIntOrNull() ?: 3
                    "goalLimit", "goal_limit" -> _goalLimit.value = setting.value.toIntOrNull() ?: 3
                    "isAdminMode" -> _isAdminMode.value = setting.value.toBoolean()
                    "isTrialActive" -> _isTrialActive.value = setting.value.toBoolean()
                    "selectedAlarmSound" -> _selectedAlarmSound.value = setting.value
                }
            }
        }
    }

    fun setPremiumStatus(active: Boolean) = viewModelScope.launch {
        _isPremiumLocal.value = active
        updateCalculatedPremiumStatus()
        repository.saveSetting("isPremium", active.toString())
        repository.saveSetting("is_premium", active.toString())
        if (!active) {
            _isTrialActive.value = false
            repository.saveSetting("isTrialActive", "false")
        }
    }

    fun endFreeTrial() = viewModelScope.launch {
        _isPremium.value = false
        _isTrialActive.value = false
        repository.saveSetting("isPremium", "false")
        repository.saveSetting("is_premium", "false")
        repository.saveSetting("isTrialActive", "false")
    }

    fun toggleAdminMode(active: Boolean) = viewModelScope.launch {
        _isAdminMode.value = active
        repository.saveSetting("isAdminMode", active.toString())
    }

    fun setHabitLimit(limit: Int) = viewModelScope.launch {
        _habitLimit.value = limit
        repository.saveSetting("habitLimit", limit.toString())
        repository.saveSetting("habit_limit", limit.toString())
    }

    fun setGoalLimit(limit: Int) = viewModelScope.launch {
        _goalLimit.value = limit
        repository.saveSetting("goalLimit", limit.toString())
        repository.saveSetting("goal_limit", limit.toString())
    }

    fun updateAppSkin(skinName: String) = viewModelScope.launch {
        _appSkin.value = skinName
        repository.saveSetting("appSkin", skinName)
        repository.saveSetting("app_skin", skinName)
    }

    fun updateAccentColor(hexColor: String) = viewModelScope.launch {
        _accentColorHex.value = hexColor
        repository.saveSetting("accentColorHex", hexColor)
        repository.saveSetting("accent_color", hexColor)
    }

    fun toggleAppTheme() = viewModelScope.launch {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        repository.saveSetting("isDarkTheme", next.toString())
        repository.saveSetting("is_dark_theme", next.toString())
    }

    fun completeOnboarding(selectedSkin: String, selectedAccent: String) = viewModelScope.launch {
        _appSkin.value = selectedSkin
        _accentColorHex.value = selectedAccent
        _firstBootCompleted.value = true

        repository.saveSetting("appSkin", selectedSkin)
        repository.saveSetting("app_skin", selectedSkin)
        repository.saveSetting("accentColorHex", selectedAccent)
        repository.saveSetting("accent_color", selectedAccent)
        repository.saveSetting("firstBootCompleted", "true")
        repository.saveSetting("first_boot_completed", "true")
    }

    fun resetOnboarding() = viewModelScope.launch {
        _firstBootCompleted.value = false
        repository.saveSetting("firstBootCompleted", "false")
        repository.saveSetting("first_boot_completed", "false")
    }

    fun updatePromoCodeInput(code: String) {
        _promoCodeInput.value = code
    }

    fun redeemPromoCode() = viewModelScope.launch {
        val code = _promoCodeInput.value
        redeemPromoCode(code)
    }

    fun redeemPromoCode(code: String) = viewModelScope.launch {
        _promoCodeInput.value = code
        if (code.trim().uppercase() == "ARISEPRO" || code.trim().uppercase() == "COSMIC99") {
            _isPremium.value = true
            _promoCodeStatus.value = "Premium Unlocked! Spark of Cosmic Power."
            repository.saveSetting("isPremium", "true")
        } else {
            _promoCodeStatus.value = "Invalid configuration code."
        }
    }

    fun resetPromoCodeStatus() {
        _promoCodeStatus.value = ""
        _promoCodeInput.value = ""
    }

    fun startFreeTrial() = viewModelScope.launch {
        _isTrialActive.value = true
        _isPremium.value = true
        repository.saveSetting("isTrialActive", "true")
        repository.saveSetting("isPremium", "true")
    }

    fun injectMockupAnalyticsData() = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val dayInMs = 24 * 3600 * 1000L
        val list = listOf(
            com.example.core.database.AlarmHistoryItem(alarmId = 1, alarmLabel = "Morning Strobe Check", triggeredTime = now - 5 * dayInMs, dismissedTime = now - 5 * dayInMs + 23000, responseTimeSeconds = 23, challengeCompleted = "Math", wakeMood = "Good"),
            com.example.core.database.AlarmHistoryItem(alarmId = 2, alarmLabel = "Evening Habit Spark", triggeredTime = now - 4 * dayInMs, dismissedTime = now - 4 * dayInMs + 45000, responseTimeSeconds = 45, challengeCompleted = "Memory", wakeMood = "Tired"),
            com.example.core.database.AlarmHistoryItem(alarmId = 3, alarmLabel = "Rise & Shine", triggeredTime = now - 3 * dayInMs, dismissedTime = now - 3 * dayInMs + 12000, responseTimeSeconds = 12, challengeCompleted = "Shake", wakeMood = "Good"),
            com.example.core.database.AlarmHistoryItem(alarmId = 4, alarmLabel = "Workout Blitz", triggeredTime = now - 2 * dayInMs, dismissedTime = now - 2 * dayInMs + 18000, responseTimeSeconds = 18, challengeCompleted = "Math", wakeMood = "Neutral"),
            com.example.core.database.AlarmHistoryItem(alarmId = 5, alarmLabel = "Meditation Hour", triggeredTime = now - 1 * dayInMs, dismissedTime = now - 1 * dayInMs + 5000, responseTimeSeconds = 5, challengeCompleted = "None", wakeMood = "Good")
        )
        list.forEach { item ->
            repository.insertAlarmHistoryItem(item)
        }
    }

    fun updateAlarmSound(sound: String) = viewModelScope.launch {
        _selectedAlarmSound.value = sound
        repository.saveSetting("selectedAlarmSound", sound)
    }
}
