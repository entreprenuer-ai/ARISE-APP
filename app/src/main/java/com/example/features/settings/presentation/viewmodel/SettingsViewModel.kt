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
                    "appSkin" -> _appSkin.value = setting.value
                    "accentColorHex" -> _accentColorHex.value = setting.value
                    "isDarkTheme" -> _isDarkTheme.value = setting.value.toBoolean()
                    "firstBootCompleted" -> _firstBootCompleted.value = setting.value.toBoolean()
                    "isPremium" -> _isPremium.value = setting.value.toBoolean()
                    "habitLimit" -> _habitLimit.value = setting.value.toIntOrNull() ?: 3
                    "goalLimit" -> _goalLimit.value = setting.value.toIntOrNull() ?: 3
                    "isAdminMode" -> _isAdminMode.value = setting.value.toBoolean()
                    "isTrialActive" -> _isTrialActive.value = setting.value.toBoolean()
                    "selectedAlarmSound" -> _selectedAlarmSound.value = setting.value
                }
            }
        }
    }

    fun setPremiumStatus(active: Boolean) = viewModelScope.launch {
        _isPremium.value = active
        repository.saveSetting("isPremium", active.toString())
        if (!active) {
            _isTrialActive.value = false
            repository.saveSetting("isTrialActive", "false")
        }
    }

    fun endFreeTrial() = viewModelScope.launch {
        _isPremium.value = false
        _isTrialActive.value = false
        repository.saveSetting("isPremium", "false")
        repository.saveSetting("isTrialActive", "false")
    }

    fun toggleAdminMode(active: Boolean) = viewModelScope.launch {
        _isAdminMode.value = active
        repository.saveSetting("isAdminMode", active.toString())
    }

    fun setHabitLimit(limit: Int) = viewModelScope.launch {
        _habitLimit.value = limit
        repository.saveSetting("habitLimit", limit.toString())
    }

    fun setGoalLimit(limit: Int) = viewModelScope.launch {
        _goalLimit.value = limit
        repository.saveSetting("goalLimit", limit.toString())
    }

    fun updateAppSkin(skinName: String) = viewModelScope.launch {
        _appSkin.value = skinName
        repository.saveSetting("appSkin", skinName)
    }

    fun updateAccentColor(hexColor: String) = viewModelScope.launch {
        _accentColorHex.value = hexColor
        repository.saveSetting("accentColorHex", hexColor)
    }

    fun toggleAppTheme() = viewModelScope.launch {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        repository.saveSetting("isDarkTheme", next.toString())
    }

    fun completeOnboarding(selectedSkin: String, selectedAccent: String) = viewModelScope.launch {
        _appSkin.value = selectedSkin
        _accentColorHex.value = selectedAccent
        _firstBootCompleted.value = true

        repository.saveSetting("appSkin", selectedSkin)
        repository.saveSetting("accentColorHex", selectedAccent)
        repository.saveSetting("firstBootCompleted", "true")
    }

    fun resetOnboarding() = viewModelScope.launch {
        _firstBootCompleted.value = false
        repository.saveSetting("firstBootCompleted", "false")
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

    fun updateAlarmSound(sound: String) = viewModelScope.launch {
        _selectedAlarmSound.value = sound
        repository.saveSetting("selectedAlarmSound", sound)
    }
}
