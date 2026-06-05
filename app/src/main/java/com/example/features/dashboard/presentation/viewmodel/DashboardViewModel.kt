package com.example.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AriseTab {
    Home,
    Alarms,
    Calendar,
    Goals,
    Sleep,
    StatsCustomize
}

class DashboardViewModel : ViewModel() {
    private val _currentTab = MutableStateFlow(AriseTab.Home)
    val currentTab: StateFlow<AriseTab> = _currentTab.asStateFlow()

    fun setTab(tab: AriseTab) {
        _currentTab.value = tab
    }
}
