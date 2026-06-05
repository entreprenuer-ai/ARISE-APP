package com.example.features.security.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AriseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _appPin = MutableStateFlow("")
    val appPin: StateFlow<String> = _appPin.asStateFlow()

    init {
        loadPinFromDb()
    }

    private fun loadPinFromDb() = viewModelScope.launch {
        repository.allSettings.collect { list ->
            list.forEach { setting ->
                if (setting.key == "appPin") {
                    _appPin.value = setting.value
                    if (setting.value.isNotEmpty()) {
                        _isAppLocked.value = true
                    }
                }
            }
        }
    }

    fun setAppSecurityPin(pin: String) = viewModelScope.launch {
        _appPin.value = pin
        repository.saveSetting("appPin", pin)
        if (pin.isNotEmpty()) {
            _isAppLocked.value = true
        } else {
            _isAppLocked.value = false
        }
    }

    fun unlockAppWithPin(pin: String): Boolean {
        return if (_appPin.value == pin) {
            _isAppLocked.value = false
            true
        } else {
            false
        }
    }

    fun signOutLock() {
        if (_appPin.value.isNotEmpty()) {
            _isAppLocked.value = true
        }
    }
}
