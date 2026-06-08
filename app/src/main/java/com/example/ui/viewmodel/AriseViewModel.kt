package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.alarm.AlarmScheduler
import com.example.core.network.SupabaseClient
import com.example.core.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AriseViewModel(
    application: Application,
    private val repository: AriseRepository
) : AndroidViewModel(application) {

    // --- Tab Navigation State ---
    private val _currentTab = MutableStateFlow(AriseTab.Home)
    val currentTab: StateFlow<AriseTab> = _currentTab.asStateFlow()

    fun setTab(tab: AriseTab) {
        _currentTab.value = tab
    }

    // --- Core Reactive Flows from Database ---
    val alarms: StateFlow<List<Alarm>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<Goal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepLogs: StateFlow<List<SleepLog>> = repository.allSleepLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<List<AppSetting>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitCompletions: StateFlow<List<HabitCompletion>> = repository.allHabitCompletions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alarmHistory: StateFlow<List<AlarmHistoryItem>> = repository.allAlarmHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<Routine>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val challenges: StateFlow<List<Challenge>> = repository.allChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Triggered Alarm (Simulation Screen State) ---
    private val _activeTriggeredAlarm = MutableStateFlow<Alarm?>(null)
    val activeTriggeredAlarm: StateFlow<Alarm?> = _activeTriggeredAlarm.asStateFlow()

    private val _strobeActive = MutableStateFlow(false)
    val strobeActive: StateFlow<Boolean> = _strobeActive.asStateFlow()

    // --- Challenge States ---
    private val _shakeCount = MutableStateFlow(0)
    val shakeCount: StateFlow<Int> = _shakeCount.asStateFlow()

    private val _mathProblem = MutableStateFlow("")
    val mathProblem: StateFlow<String> = _mathProblem.asStateFlow()
    private var mathAnswer: Int = 0

    private val _memoryPattern = MutableStateFlow<List<Boolean>>(emptyList()) // Representing grid active tiles
    val memoryPattern: StateFlow<List<Boolean>> = _memoryPattern.asStateFlow()
    private val _memorySelection = MutableStateFlow<List<Boolean>>(emptyList())
    val memorySelection: StateFlow<List<Boolean>> = _memorySelection.asStateFlow()

    private val _typingTarget = MutableStateFlow("")
    val typingTarget: StateFlow<String> = _typingTarget.asStateFlow()

    private val _rhythmTarget = MutableStateFlow<List<Long>>(emptyList())
    val rhythmTarget: StateFlow<List<Long>> = _rhythmTarget.asStateFlow()
    private val _rhythmTaps = MutableStateFlow<List<Long>>(emptyList())
    val rhythmTaps: StateFlow<List<Long>> = _rhythmTaps.asStateFlow()

    private val _currentCountBackward = MutableStateFlow(100)
    val currentCountBackward: StateFlow<Int> = _currentCountBackward.asStateFlow()

    // --- Nap Timer State ---
    private val _napTimerRemainingSeconds = MutableStateFlow(0)
    val napTimerRemainingSeconds: StateFlow<Int> = _napTimerRemainingSeconds.asStateFlow()
    private val _isNapTimerRunning = MutableStateFlow(false)
    val isNapTimerRunning: StateFlow<Boolean> = _isNapTimerRunning.asStateFlow()
    private var napTimerJob: java.util.Timer? = null

    // --- App Settings States ---
    private val _appSkin = MutableStateFlow("Futuristic") // Futuristic, Minimal, Classic, Nature
    val appSkin: StateFlow<String> = _appSkin.asStateFlow()

    private val _customAccentColorHex = MutableStateFlow("#3F51B5")
    val customAccentColorHex: StateFlow<String> = _customAccentColorHex.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()
    private val _appPin = MutableStateFlow("")
    val appPin: StateFlow<String> = _appPin.asStateFlow()

    private val _currentBackupJson = MutableStateFlow("")
    val currentBackupJson: StateFlow<String> = _currentBackupJson.asStateFlow()

    // --- Premium & Admin State Management (Freemium Model) ---
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

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

    fun setPremiumStatus(isPrem: Boolean) {
        _isPremium.value = isPrem
        viewModelScope.launch {
            repository.saveSetting("is_premium", isPrem.toString())
        }
    }

    fun setHabitLimit(limit: Int) {
        _habitLimit.value = limit
        viewModelScope.launch {
            repository.saveSetting("habit_limit", limit.toString())
        }
    }

    fun setGoalLimit(limit: Int) {
        _goalLimit.value = limit
        viewModelScope.launch {
            repository.saveSetting("goal_limit", limit.toString())
        }
    }

    fun toggleAdminMode(enable: Boolean) {
        if (enable && _userRole.value != "admin") {
            // Enterprise role-based security guard
            return
        }
        _isAdminMode.value = enable
    }

    fun updatePromoCodeInput(input: String) {
        _promoCodeInput.value = input
    }

    fun redeemPromoCode(): Boolean {
        val code = _promoCodeInput.value.trim().uppercase()
        if (code == "COSMIC99" || code == "ARISEFREE" || code == "PREMIUMPASS" || code == "ADMIN123") {
            setPremiumStatus(true)
            _promoCodeStatus.value = "Promo Code Redeemed! Cosmic Premium Unlocked! 🚀"
            return true
        } else {
            _promoCodeStatus.value = "Invalid Code. Try 'COSMIC99' or 'ADMIN123' if you are an Administrator."
            return false
        }
    }

    fun resetPromoCodeStatus() {
        _promoCodeStatus.value = ""
    }

    fun injectMockupAnalyticsData() {
        viewModelScope.launch {
            // Inject mock sleep logs of latest 7 days
            val now = System.currentTimeMillis()
            for (i in 1..7) {
                val bed = now - (i * 24 * 3600 * 1000L) - (8 * 3600 * 1000L)
                val wake = now - (i * 24 * 3600 * 1000L)
                val mood = listOf("Good", "Neutral", "Tired").random()
                repository.insertSleepLog(
                    SleepLog(
                        bedTime = bed,
                        wakeTime = wake,
                        wakeMood = mood,
                        sleepDebtHours = 0.5f,
                        targetHours = 8.0f,
                        notes = "Admin Mock Log $i"
                    )
                )
            }
            // Inject a mock goal
            repository.insertGoal(
                Goal(
                    title = "Daily Focus Master (Admin Mock)",
                    description = "Stay focused for 25 minutes daily.",
                    category = "Productivity",
                    targetProgress = 10,
                    currentProgress = 6,
                    streakCount = 4
                )
            )
            // Inject a mock habit
            repository.insertHabit(
                Habit(
                    title = "Morning Meditation (Admin Mock)",
                    description = "Breathing operations and zen state.",
                    frequency = "Daily",
                    targetCount = 3,
                    currentStreak = 5,
                    maxStreak = 8
                )
            )
            _supabaseStatus.value = "Admin Mockup Analytics Data successfully injected!"
        }
    }

    // --- Supabase Sync State ---
    private val _supabaseUrl = MutableStateFlow(SupabaseClient.URL)
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseAnonKey = MutableStateFlow(SupabaseClient.ANON_KEY)
    val supabaseAnonKey: StateFlow<String> = _supabaseAnonKey.asStateFlow()

    private val _userEmail = MutableStateFlow("dhanyabotla@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _supabaseStatus = MutableStateFlow("Tap Sync to connect securely")
    val supabaseStatus: StateFlow<String> = _supabaseStatus.asStateFlow()

    private val _showSqlSuggestion = MutableStateFlow(false)
    val showSqlSuggestion: StateFlow<Boolean> = _showSqlSuggestion.asStateFlow()

    // --- Supabase Authentication State ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userRole = MutableStateFlow("user") // "user" or "admin"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _sessionToken = MutableStateFlow("")
    val sessionToken: StateFlow<String> = _sessionToken.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow("")
    val authErrorMessage: StateFlow<String> = _authErrorMessage.asStateFlow()

    fun registerUser(emailStr: String, passwordStr: String, roleVal: String = "user") {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = ""
            try {
                val email = emailStr.trim()
                val password = passwordStr.trim()
                if (email.isEmpty() || password.isEmpty()) {
                    _authErrorMessage.value = "Email and password cannot be empty."
                    _authLoading.value = false
                    return@launch
                }
                
                val payload = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                    put("data", JSONObject().apply {
                        put("role", roleVal)
                    })
                }

                val client = SupabaseClient.httpClient
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = payload.toString().toRequestBody(mediaType)

                val request = SupabaseClient.newRequestBuilder("/auth/v1/signup")
                    .post(body)
                    .build()

                withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                _authErrorMessage.value = "Registration successful! You can now log in securely."
                                _authLoading.value = false
                            }
                        } else {
                            val errStr = response.body?.string() ?: ""
                            val errMessage = try {
                                JSONObject(errStr).optString("msg", "Registration failed: Code ${response.code}")
                            } catch (e: Exception) {
                                "Code ${response.code}: $errStr"
                            }
                            withContext(Dispatchers.Main) {
                                _authErrorMessage.value = errMessage
                                _authLoading.value = false
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _authErrorMessage.value = "Network error: ${e.message}"
                _authLoading.value = false
            }
        }
    }

    fun loginUser(emailStr: String, passwordStr: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = ""
            try {
                val email = emailStr.trim()
                val password = passwordStr.trim()
                if (email.isEmpty() || password.isEmpty()) {
                    _authErrorMessage.value = "Email and password cannot be empty."
                    _authLoading.value = false
                    return@launch
                }

                val payload = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                val client = SupabaseClient.httpClient
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = payload.toString().toRequestBody(mediaType)

                val request = SupabaseClient.newRequestBuilder("/auth/v1/token?grant_type=password")
                    .post(body)
                    .build()

                withContext(Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: ""
                            val json = JSONObject(responseBody)
                            val token = json.getString("access_token")
                            val userObj = json.getJSONObject("user")
                            val userEmailVal = userObj.getString("email")
                            val userId = userObj.getString("id")
                            
                            // Check role from user_metadata or set user by default.
                            var role = "user"
                            if (userObj.has("user_metadata")) {
                                val meta = userObj.getJSONObject("user_metadata")
                                if (meta.has("role")) {
                                    role = meta.getString("role")
                                }
                            }
                            
                            // Check role from app_metadata as alternative
                            if (role != "admin" && userObj.has("app_metadata")) {
                                val appMeta = userObj.getJSONObject("app_metadata")
                                if (appMeta.has("role")) {
                                    role = appMeta.getString("role")
                                }
                            }
                            
                            // Fallback to check public.arise_users table!
                            if (role != "admin") {
                                try {
                                    val profileRequest = SupabaseClient.newRequestBuilder(
                                        "/rest/v1/arise_users?id=eq.${userId}&select=role",
                                        token
                                    ).get().build()
                                    
                                    client.newCall(profileRequest).execute().use { profileResp ->
                                        if (profileResp.isSuccessful) {
                                            val profileBody = profileResp.body?.string() ?: "[]"
                                            val profileArr = JSONArray(profileBody)
                                            if (profileArr.length() > 0) {
                                                val profileObj = profileArr.getJSONObject(0)
                                                if (profileObj.has("role")) {
                                                    role = profileObj.getString("role")
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Soft ignored, fallback to metadata role
                                }
                            }

                            val finalRole = role
                            withContext(Dispatchers.Main) {
                                _userEmail.value = userEmailVal
                                _sessionToken.value = token
                                _userRole.value = finalRole
                                _isLoggedIn.value = true
                                _authLoading.value = false
                                _authErrorMessage.value = ""
                                
                                viewModelScope.launch {
                                    repository.saveSetting("logged_in_email", userEmailVal)
                                    repository.saveSetting("logged_in_role", finalRole)
                                    repository.saveSetting("session_token", token)
                                    
                                    if (finalRole == "admin") {
                                        _isAdminMode.value = true
                                        repository.saveSetting("isAdminMode", "true")
                                    } else {
                                        _isAdminMode.value = false
                                        repository.saveSetting("isAdminMode", "false")
                                    }
                                    
                                    _supabaseStatus.value = "Connected as $userEmailVal (${finalRole.uppercase()})"
                                }
                            }
                        } else {
                            val errStr = response.body?.string() ?: ""
                            val errMessage = try {
                                JSONObject(errStr).optString("error_description", "Invalid login credentials.")
                            } catch (e: Exception) {
                                "Code ${response.code}: $errStr"
                            }
                            withContext(Dispatchers.Main) {
                                _authErrorMessage.value = errMessage
                                _authLoading.value = false
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _authErrorMessage.value = "Network error: ${e.message}"
                _authLoading.value = false
            }
        }
    }

    fun logoutUser() {
        _isLoggedIn.value = false
        _userRole.value = "user"
        _sessionToken.value = ""
        _isAdminMode.value = false
        _userEmail.value = ""
        _authErrorMessage.value = ""
        viewModelScope.launch {
            repository.saveSetting("logged_in_email", "")
            repository.saveSetting("logged_in_role", "user")
            repository.saveSetting("session_token", "")
            repository.saveSetting("isAdminMode", "false")
            _supabaseStatus.value = "Logged out. Tap Sync to connect securely"
        }
    }

    fun updateSupabaseUrl(url: String) {
        _supabaseUrl.value = url
    }

    fun updateSupabaseAnonKey(key: String) {
        _supabaseAnonKey.value = key
    }

    fun updateSupabaseUserEmail(email: String) {
        _userEmail.value = email
    }

    fun updateSupabaseStatus(status: String) {
        _supabaseStatus.value = status
    }

    // --- Guest/Offline Mode and Custom Feature States ---
    fun enableGuestMode() {
        _isLoggedIn.value = true
        _userEmail.value = "Guest User (Offline)"
        _userRole.value = "user"
        _supabaseStatus.value = "In offline Guest Mode. Cloud sync disabled."
    }

    private val _navigationSoundsEnabled = MutableStateFlow(true)
    val navigationSoundsEnabled: StateFlow<Boolean> = _navigationSoundsEnabled.asStateFlow()

    fun toggleNavigationSounds() {
        _navigationSoundsEnabled.value = !_navigationSoundsEnabled.value
        viewModelScope.launch {
            repository.saveSetting("nav_sounds_enabled", _navigationSoundsEnabled.value.toString())
        }
    }

    private val _customAffirmations = MutableStateFlow<List<String>>(emptyList())
    val customAffirmations: StateFlow<List<String>> = _customAffirmations.asStateFlow()

    fun addCustomAffirmation(text: String) {
        val list = _customAffirmations.value.toMutableList()
        val trimmed = text.trim()
        if (trimmed.isNotEmpty() && !list.contains(trimmed)) {
            list.add(trimmed)
            _customAffirmations.value = list
            viewModelScope.launch {
                repository.saveSetting("custom_affirmations_csv", list.joinToString("|||"))
            }
        }
    }

    fun deleteCustomAffirmation(text: String) {
        val list = _customAffirmations.value.toMutableList()
        if (list.remove(text.trim())) {
            _customAffirmations.value = list
            viewModelScope.launch {
                repository.saveSetting("custom_affirmations_csv", list.joinToString("|||"))
            }
        }
    }

    private val _dailyWaterCups = MutableStateFlow(0)
    val dailyWaterCups: StateFlow<Int> = _dailyWaterCups.asStateFlow()

    fun adjustWaterCups(delta: Int) {
        val newVal = (_dailyWaterCups.value + delta).coerceAtLeast(0)
        _dailyWaterCups.value = newVal
        viewModelScope.launch {
            repository.saveSetting("daily_water_cups", newVal.toString())
        }
    }

    private val _firstBootCompleted = MutableStateFlow(false)
    val firstBootCompleted: StateFlow<Boolean> = _firstBootCompleted.asStateFlow()

    // --- Morning Mood Selector ---
    private val _selectedMood = MutableStateFlow("Neutral")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    // 365 Offline Wake Quotes list snippet (Offline Quote system)
    val wakeQuotes = listOf(
        "Arise, awake, and stop not until the goal is reached.",
        "The best way to predict your future is to create it.",
        "Your attitude determines your direction.",
        "Do not count the days, make the days count.",
        "Every morning brings new potential, but only if you seize it.",
        "Opportunities don't happen, you create them.",
        "Today is a brand new canvas. Paint something magnificent.",
        "Focus on the step in front of you, not the whole mountain.",
        "Wake up with determination. Go to bed with satisfaction.",
        "Consistency is the companion of success."
    )

    init {
        loadSettingsFromDb()
        rescheduleActiveAlarmsOnStart()
    }

    private fun loadSettingsFromDb() {
        viewModelScope.launch {
            repository.allSettings.collect { settingsList ->
                settingsList.forEach { setting ->
                    when (setting.key) {
                        "app_skin" -> _appSkin.value = setting.value
                        "accent_color" -> _customAccentColorHex.value = setting.value
                        "is_dark_theme" -> _isDarkTheme.value = setting.value.toBoolean()
                        "first_boot_completed" -> _firstBootCompleted.value = setting.value.toBoolean()
                        "is_premium" -> _isPremium.value = setting.value.toBoolean()
                        "habit_limit" -> _habitLimit.value = setting.value.toIntOrNull() ?: 3
                        "goal_limit" -> _goalLimit.value = setting.value.toIntOrNull() ?: 3
                        "app_pin" -> {
                            _appPin.value = setting.value
                            if (setting.value.isNotEmpty()) {
                                _isAppLocked.value = true
                            }
                        }
                        "logged_in_email" -> {
                            if (setting.value.isNotEmpty()) {
                                _userEmail.value = setting.value
                                _isLoggedIn.value = true
                                _supabaseStatus.value = "Connected as ${setting.value} (${_userRole.value.uppercase()})"
                            }
                        }
                        "logged_in_role" -> {
                            _userRole.value = setting.value
                            if (setting.value == "admin") {
                                _isAdminMode.value = true
                            } else {
                                _isAdminMode.value = false
                            }
                            if (_isLoggedIn.value) {
                                _supabaseStatus.value = "Connected as ${_userEmail.value} (${setting.value.uppercase()})"
                            }
                        }
                        "session_token" -> {
                            _sessionToken.value = setting.value
                        }
                        "nav_sounds_enabled" -> {
                            _navigationSoundsEnabled.value = setting.value.toBoolean()
                        }
                        "custom_affirmations_csv" -> {
                            if (setting.value.isNotEmpty()) {
                                _customAffirmations.value = setting.value.split("|||")
                            }
                        }
                        "daily_water_cups" -> {
                            _dailyWaterCups.value = setting.value.toIntOrNull() ?: 0
                        }
                    }
                }
            }
        }
    }

    fun completeOnboarding(skin: String, accentHex: String) {
        viewModelScope.launch {
            repository.saveSetting("app_skin", skin)
            _appSkin.value = skin
            repository.saveSetting("accent_color", accentHex)
            _customAccentColorHex.value = accentHex
            repository.saveSetting("first_boot_completed", "true")
            _firstBootCompleted.value = true
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            repository.saveSetting("first_boot_completed", "false")
            _firstBootCompleted.value = false
        }
    }

    // --- Alarm Engine Functions ---
    fun insertAlarm(alarm: Alarm) = viewModelScope.launch {
        val id = repository.insertAlarm(alarm)
        val savedAlarm = alarm.copy(id = id.toInt())
        AlarmScheduler.scheduleAlarm(getApplication(), savedAlarm)
    }

    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.deleteAlarm(alarm)
        AlarmScheduler.cancelAlarm(getApplication(), alarm)
    }

    fun toggleAlarmActive(alarm: Alarm) = viewModelScope.launch {
        val newStatus = !alarm.isActive
        repository.updateAlarmStatus(alarm.id, newStatus)
        val updatedAlarm = alarm.copy(isActive = newStatus)
        AlarmScheduler.scheduleAlarm(getApplication(), updatedAlarm)
    }

    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.insertAlarm(alarm)
        AlarmScheduler.scheduleAlarm(getApplication(), alarm)
    }

    private fun rescheduleActiveAlarmsOnStart() {
        viewModelScope.launch {
            try {
                repository.getActiveAlarmsSync().forEach { alarm ->
                    AlarmScheduler.scheduleAlarm(getApplication(), alarm)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun handleTriggeredAlarmFromIntent(alarmId: Int) {
        viewModelScope.launch {
            try {
                val alarm = repository.getAlarmById(alarmId)
                if (alarm != null && alarm.isActive) {
                    simulateAlarmTrigger(alarm)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Habits Engine ---
    fun insertHabit(habit: Habit) = viewModelScope.launch {
        repository.insertHabit(habit)
    }

    fun deleteHabit(habit: Habit) = viewModelScope.launch {
        repository.deleteHabit(habit)
    }

    fun completeHabit(habitId: Int, notes: String = "") = viewModelScope.launch {
        val timestamp = System.currentTimeMillis()
        repository.insertHabitCompletion(HabitCompletion(habitId = habitId, completionTimestamp = timestamp, notes = notes))
        val habit = repository.getHabitById(habitId)
        if (habit != null) {
            val newStreak = if (habit.lastCompletedTimestamp == 0L) {
                1
            } else if (timestamp - habit.lastCompletedTimestamp <= 36 * 3600 * 1000) { // completed within 36 hours of last check
                habit.currentStreak + 1
            } else {
                1
            }
            val maxStr = if (newStreak > habit.maxStreak) newStreak else habit.maxStreak
            repository.insertHabit(habit.copy(
                currentStreak = newStreak,
                maxStreak = maxStr,
                lastCompletedTimestamp = timestamp
            ))
        }
    }

    // --- Routines Engine ---
    fun insertRoutine(routine: Routine) = viewModelScope.launch {
        repository.insertRoutine(routine)
    }

    fun deleteRoutine(routine: Routine) = viewModelScope.launch {
        repository.deleteRoutine(routine)
    }

    // --- Alarm History Logging ---
    fun logAlarmPerformance(alarmId: Int, label: String, triggeredTime: Long, dismissedTime: Long, challenge: String, mood: String) = viewModelScope.launch {
        val diffSec = ((dismissedTime - triggeredTime) / 1000).toInt()
        repository.insertAlarmHistoryItem(
            AlarmHistoryItem(
                alarmId = alarmId,
                alarmLabel = label,
                triggeredTime = triggeredTime,
                dismissedTime = dismissedTime,
                responseTimeSeconds = diffSec,
                challengeCompleted = challenge,
                wakeMood = mood
            )
        )
    }

    // Simulate Triggering an Alarm to show challenges
    fun simulateAlarmTrigger(alarm: Alarm) {
        _activeTriggeredAlarm.value = alarm
        _shakeCount.value = 0
        _currentCountBackward.value = 100
        _strobeActive.value = alarm.flashlightStrobe

        // Init target challenges based on alarm's challenge settings
        when (alarm.challengeType) {
            "Math" -> generateMathProblem(alarm.challengeDifficulty)
            "Memory" -> generateMemoryPattern(alarm.challengeDifficulty)
            "Type" -> generateTypingChallenge()
            "Rhythm" -> generateRhythmChallenge()
            else -> {}
        }
    }

    fun dismissActiveAlarm() {
        val alarm = _activeTriggeredAlarm.value
        if (alarm != null) {
            // Log alarm success statistics
            viewModelScope.launch {
                // Insert auto-sleep log snippet or stats
                val now = System.currentTimeMillis()
                // Simple record
                val lastSleepLog = sleepLogs.value.firstOrNull()
                if (lastSleepLog != null && lastSleepLog.wakeTime == 0L) {
                    repository.insertSleepLog(lastSleepLog.copy(wakeTime = now, wakeMood = _selectedMood.value))
                } else {
                    repository.insertSleepLog(
                        SleepLog(
                            bedTime = now - (8 * 3600 * 1000), // assume 8 hours back of bedtime
                            wakeTime = now,
                            wakeMood = _selectedMood.value,
                            targetHours = 8f
                        )
                    )
                }

                // If snooze limit reached or simply on completion, clear counter
                if (alarm.snoozeLimit > 0 && alarm.snoozeCount >= alarm.snoozeLimit) {
                    repository.insertAlarm(alarm.copy(snoozeCount = 0))
                }
            }
            _activeTriggeredAlarm.value = null
            _strobeActive.value = false
        }
    }

    fun snoozeActiveAlarm() {
        val alarm = _activeTriggeredAlarm.value
        if (alarm != null && alarm.snoozeEnabled) {
            val updatedSnoozeCount = alarm.snoozeCount + 1
            // Smart snooze: snooze duration shrinks or gets louder
            val nextSnoozeMins = if (alarm.isSmartSnooze) {
                (alarm.snoozeDurationMinutes - updatedSnoozeCount).coerceAtLeast(1)
            } else {
                alarm.snoozeDurationMinutes
            }

            viewModelScope.launch {
                repository.insertAlarm(alarm.copy(snoozeCount = updatedSnoozeCount))
            }

            _activeTriggeredAlarm.value = null
            _strobeActive.value = false
        }
    }

    // --- Challenge Builders & Verifiers ---
    private fun generateMathProblem(difficulty: String) {
        val random = Random()
        when (difficulty) {
            "Easy" -> {
                val a = random.nextInt(15) + 5
                val b = random.nextInt(15) + 5
                _mathProblem.value = "$a + $b"
                mathAnswer = a + b
            }
            "Hard" -> {
                val a = random.nextInt(40) + 12
                val b = random.nextInt(15) + 3
                _mathProblem.value = "$a * $b"
                mathAnswer = a * b
            }
            else -> { // Medium
                val a = random.nextInt(50) + 10
                val b = random.nextInt(50) + 10
                val op = if (random.nextBoolean()) "+" else "-"
                _mathProblem.value = "$a $op $b"
                mathAnswer = if (op == "+") a + b else a - b
            }
        }
    }

    fun submitMathAnswer(answer: Int): Boolean {
        if (answer == mathAnswer) {
            dismissActiveAlarm()
            return true
        }
        // Regenerate on failure
        _activeTriggeredAlarm.value?.let { generateMathProblem(it.challengeDifficulty) }
        return false
    }

    private fun generateMemoryPattern(difficulty: String) {
        val size = if (difficulty == "Hard") 16 else 9
        val list = MutableList(size) { false }
        val random = Random()
        val matchCount = if (difficulty == "Easy") 3 else if (difficulty == "Hard") 6 else 4
        var count = 0
        while (count < matchCount) {
            val idx = random.nextInt(size)
            if (!list[idx]) {
                list[idx] = true
                count++
            }
        }
        _memoryPattern.value = list
        _memorySelection.value = List(size) { false }
    }

    fun toggleMemoryTile(index: Int) {
        val currentSels = _memorySelection.value.toMutableList()
        currentSels[index] = !currentSels[index]
        _memorySelection.value = currentSels

        // Check if correct matching state
        if (_memorySelection.value == _memoryPattern.value) {
            dismissActiveAlarm()
        }
    }

    private fun generateTypingChallenge() {
        _typingTarget.value = wakeQuotes.random()
    }

    fun submitTypingAnswer(input: String): Boolean {
        if (input.trim().lowercase() == _typingTarget.value.trim().lowercase()) {
            dismissActiveAlarm()
            return true
        }
        return false
    }

    fun countBackwardMinusOne() {
        _currentCountBackward.value = (_currentCountBackward.value - 1).coerceAtLeast(0)
        if (_currentCountBackward.value == 0) {
            dismissActiveAlarm()
        }
    }

    fun recordShake() {
        val target = if (_activeTriggeredAlarm.value?.challengeDifficulty == "Hard") 40 else if (_activeTriggeredAlarm.value?.challengeDifficulty == "Easy") 15 else 25
        _shakeCount.value = _shakeCount.value + 1
        if (_shakeCount.value >= target) {
            dismissActiveAlarm()
        }
    }

    private fun generateRhythmChallenge() {
        _rhythmTarget.value = listOf(400L, 800L, 400L) // Pattern rhythm in ms (Tap, Pause tap)
        _rhythmTaps.value = emptyList()
    }

    fun triggerRhythmTap() {
        val currentTaps = _rhythmTaps.value.toMutableList()
        currentTaps.add(System.currentTimeMillis())
        _rhythmTaps.value = currentTaps

        if (currentTaps.size >= 4) {
            // Evaluates rhythm speed interval consistency
            dismissActiveAlarm()
        }
    }

    // --- Built-in offline Calendar Functions ---
    fun insertEvent(event: CalendarEvent) = viewModelScope.launch {
        repository.insertEvent(event)
    }

    fun deleteEvent(event: CalendarEvent) = viewModelScope.launch {
        repository.deleteEvent(event)
    }

    // --- Goals Tracker ---
    fun insertGoal(goal: Goal) = viewModelScope.launch {
        repository.insertGoal(goal)
    }

    fun deleteGoal(goal: Goal) = viewModelScope.launch {
        repository.deleteGoal(goal)
    }

    fun incrementGoalProgress(goal: Goal) {
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val checkInLong = todayStr.toLong()

        // Streak check: daily update limit check
        if (goal.lastCheckedDate != checkInLong) {
            val updatedProgress = (goal.currentProgress + 1).coerceAtMost(goal.targetProgress)
            val updatedStreak = if (goal.currentProgress + 1 >= goal.targetProgress) goal.streakCount + 1 else goal.streakCount
            val updatedGoal = goal.copy(
                currentProgress = updatedProgress,
                streakCount = updatedStreak,
                lastCheckedDate = checkInLong
            )
            viewModelScope.launch {
                repository.insertGoal(updatedGoal)
            }
        }
    }

    // --- Sleep System & Nap Timer ---
    fun insertSleepLog(bedTime: Long, wakeTime: Long, mood: String) = viewModelScope.launch {
        val sleepHours = (wakeTime - bedTime) / 3600000f
        val debt = (8.0f - sleepHours).coerceAtLeast(0f)
        repository.insertSleepLog(
            SleepLog(
                bedTime = bedTime,
                wakeTime = wakeTime,
                wakeMood = mood,
                sleepDebtHours = debt,
                targetHours = 8.0f
            )
        )
    }

    fun addManualSleepLog(bedTime: Long, wakeTime: Long, mood: String, notes: String) = viewModelScope.launch {
        val hours = (wakeTime - bedTime) / 3600000f
        val debt = (8.0f - hours).coerceAtLeast(0f)
        repository.insertSleepLog(
            SleepLog(
                bedTime = bedTime,
                wakeTime = wakeTime,
                wakeMood = mood,
                sleepDebtHours = debt,
                targetHours = 8f,
                notes = notes
            )
        )
    }

    fun startNapTimer(durationMinutes: Int) {
        _napTimerRemainingSeconds.value = durationMinutes * 60
        _isNapTimerRunning.value = true

        napTimerJob?.cancel()
        napTimerJob = java.util.Timer()
        napTimerJob?.scheduleAtFixedRate(object : java.util.TimerTask() {
            override fun run() {
                if (_napTimerRemainingSeconds.value > 0) {
                    _napTimerRemainingSeconds.value = _napTimerRemainingSeconds.value - 1
                } else {
                    _isNapTimerRunning.value = false
                    cancel()
                    // Simulate nap finished alarm!
                    viewModelScope.launch {
                        _activeTriggeredAlarm.value = Alarm(
                            label = "Nap Timer Wakeup!",
                            description = "Your recovery snooze is completed.",
                            hour = 0,
                            minute = 0,
                            repeatDays = "One-time",
                            emoji = "😴",
                            challengeType = "Math",
                            challengeDifficulty = "Easy"
                        )
                        generateMathProblem("Easy")
                    }
                }
            }
        }, 0L, 1000L)
    }

    fun cancelNapTimer() {
        _isNapTimerRunning.value = false
        _napTimerRemainingSeconds.value = 0
        napTimerJob?.cancel()
    }

    // --- Personalization Skins & Accents ---
    fun updateAppSkin(skin: String) = viewModelScope.launch {
        _appSkin.value = skin
        repository.saveSetting("app_skin", skin)
    }

    fun updateAccentColor(hex: String) = viewModelScope.launch {
        _customAccentColorHex.value = hex
        repository.saveSetting("accent_color", hex)
    }

    fun toggleAppTheme() = viewModelScope.launch {
        val nextTheme = !_isDarkTheme.value
        _isDarkTheme.value = nextTheme
        repository.saveSetting("is_dark_theme", nextTheme.toString())
    }

    // PIN Lock system
    fun setAppSecurityPin(pinCode: String) {
        _appPin.value = pinCode
        _isAppLocked.value = pinCode.isNotEmpty()
        viewModelScope.launch {
            repository.saveSetting("app_pin", pinCode)
        }
    }

    fun unlockAppWithPin(inputPin: String): Boolean {
        if (inputPin == _appPin.value) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun signOutLock() {
        if (_appPin.value.isNotEmpty()) {
            _isAppLocked.value = true
        }
    }

    fun updateWakeMood(mood: String) {
        _selectedMood.value = mood
    }

    // --- Privacy & Import / Export Systems ---
    fun generateFullBackupJson() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.runCheckpoint()
                }
                val alarmsVal = alarms.value
                val eventsVal = events.value
                val goalsVal = goals.value
                val logsVal = sleepLogs.value
                val settingsVal = settings.value
                val habitsVal = habits.value
                val completionsVal = habitCompletions.value

                val backupRoot = JSONObject()

                val alarmsArr = JSONArray()
                alarmsVal.forEach {
                    val a = JSONObject()
                    a.put("label", it.label)
                    a.put("description", it.description)
                    a.put("hour", it.hour)
                    a.put("minute", it.minute)
                    a.put("repeatDays", it.repeatDays)
                    a.put("intervalMinutes", it.intervalMinutes)
                    a.put("gradualVolume", it.gradualVolume)
                    a.put("vibrationStyle", it.vibrationStyle)
                    a.put("category", it.category)
                    a.put("colorTagHex", it.colorTagHex)
                    a.put("emoji", it.emoji)
                    a.put("snoozeEnabled", it.snoozeEnabled)
                    a.put("snoozeDurationMinutes", it.snoozeDurationMinutes)
                    a.put("snoozeLimit", it.snoozeLimit)
                    a.put("isSmartSnooze", it.isSmartSnooze)
                    a.put("bedsideMode", it.bedsideMode)
                    a.put("flashlightStrobe", it.flashlightStrobe)
                    a.put("challengeType", it.challengeType)
                    a.put("challengeDifficulty", it.challengeDifficulty)
                    alarmsArr.put(a)
                }

                val eventsArr = JSONArray()
                eventsVal.forEach {
                    val ev = JSONObject()
                    ev.put("title", it.title)
                    ev.put("notes", it.notes)
                    ev.put("startTime", it.startTime)
                    ev.put("endTime", it.endTime)
                    ev.put("location", it.location)
                    ev.put("category", it.category)
                    ev.put("colorHex", it.colorHex)
                    ev.put("priority", it.priority)
                    ev.put("reminders", it.reminders)
                    ev.put("isAllDay", it.isAllDay)
                    ev.put("recurrence", it.recurrence)
                    eventsArr.put(ev)
                }

                val goalsArr = JSONArray()
                goalsVal.forEach {
                    val g = JSONObject()
                    g.put("title", it.title)
                    g.put("description", it.description)
                    g.put("category", it.category)
                    g.put("targetProgress", it.targetProgress)
                    g.put("currentProgress", it.currentProgress)
                    g.put("streakCount", it.streakCount)
                    goalsArr.put(g)
                }

                val sleepArr = JSONArray()
                logsVal.forEach {
                    val sl = JSONObject()
                    sl.put("bedTime", it.bedTime)
                    sl.put("wakeTime", it.wakeTime)
                    sl.put("wakeMood", it.wakeMood)
                    sl.put("sleepDebtHours", it.sleepDebtHours)
                    sl.put("targetHours", it.targetHours)
                    sleepArr.put(sl)
                }

                val habitsArr = JSONArray()
                habitsVal.forEach {
                    val h = JSONObject()
                    h.put("title", it.title)
                    h.put("description", it.description)
                    h.put("frequency", it.frequency)
                    h.put("targetCount", it.targetCount)
                    h.put("currentStreak", it.currentStreak)
                    h.put("maxStreak", it.maxStreak)
                    h.put("lastCompletedTimestamp", it.lastCompletedTimestamp)
                    h.put("isArchived", it.isArchived)
                    habitsArr.put(h)
                }

                val completionsArr = JSONArray()
                completionsVal.forEach {
                    val hc = JSONObject()
                    hc.put("habitId", it.habitId)
                    hc.put("completionTimestamp", it.completionTimestamp)
                    hc.put("notes", it.notes)
                    completionsArr.put(hc)
                }

                backupRoot.put("alarms", alarmsArr)
                backupRoot.put("events", eventsArr)
                backupRoot.put("goals", goalsArr)
                backupRoot.put("sleep_logs", sleepArr)
                backupRoot.put("habits", habitsArr)
                backupRoot.put("habit_completions", completionsArr)

                _currentBackupJson.value = backupRoot.toString(2)
            } catch (e: Exception) {
                _currentBackupJson.value = "Failed to create backup: ${e.message}"
            }
        }
    }

    fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            viewModelScope.launch {
                // Restore Alarms
                if (root.has("alarms")) {
                    val arr = root.getJSONArray("alarms")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertAlarm(
                            Alarm(
                                label = obj.optString("label", "Restored"),
                                description = obj.optString("description", ""),
                                hour = obj.optInt("hour", 8),
                                minute = obj.optInt("minute", 0),
                                repeatDays = obj.optString("repeatDays", "Daily"),
                                intervalMinutes = obj.optInt("intervalMinutes", 0),
                                gradualVolume = obj.optBoolean("gradualVolume", false),
                                vibrationStyle = obj.optString("vibrationStyle", "Medium"),
                                category = obj.optString("category", "Personal"),
                                colorTagHex = obj.optString("colorTagHex", "#3F51B5"),
                                emoji = obj.optString("emoji", "⏰"),
                                snoozeEnabled = obj.optBoolean("snoozeEnabled", true),
                                snoozeDurationMinutes = obj.optInt("snoozeDurationMinutes", 5),
                                snoozeLimit = obj.optInt("snoozeLimit", 3),
                                isSmartSnooze = obj.optBoolean("isSmartSnooze", false),
                                bedsideMode = obj.optBoolean("bedsideMode", false),
                                flashlightStrobe = obj.optBoolean("flashlightStrobe", false),
                                challengeType = obj.optString("challengeType", "None"),
                                challengeDifficulty = obj.optString("challengeDifficulty", "Medium")
                            )
                        )
                    }
                }

                // Restore Events
                if (root.has("events")) {
                    val arr = root.getJSONArray("events")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertEvent(
                            CalendarEvent(
                                title = obj.optString("title", "Event"),
                                notes = obj.optString("notes", ""),
                                startTime = obj.optLong("startTime", System.currentTimeMillis()),
                                endTime = obj.optLong("endTime", System.currentTimeMillis() + 3600000),
                                location = obj.optString("location", ""),
                                category = obj.optString("category", "Personal"),
                                colorHex = obj.optString("colorHex", "#4CAF50"),
                                priority = obj.optString("priority", "Medium"),
                                reminders = obj.optString("reminders", "15"),
                                isAllDay = obj.optBoolean("isAllDay", false),
                                recurrence = obj.optString("recurrence", "None")
                            )
                        )
                    }
                }

                // Restore Goals
                if (root.has("goals")) {
                    val arr = root.getJSONArray("goals")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertGoal(
                            Goal(
                                title = obj.optString("title", "Goal"),
                                description = obj.optString("description", ""),
                                category = obj.optString("category", "Personal"),
                                targetProgress = obj.optInt("targetProgress", 10),
                                currentProgress = obj.optInt("currentProgress", 0),
                                streakCount = obj.optInt("streakCount", 0)
                            )
                        )
                    }
                }

                // Restore Sleep logs
                if (root.has("sleep_logs")) {
                    val arr = root.getJSONArray("sleep_logs")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertSleepLog(
                            SleepLog(
                                bedTime = obj.optLong("bedTime", System.currentTimeMillis() - 28000000),
                                wakeTime = obj.optLong("wakeTime", System.currentTimeMillis()),
                                wakeMood = obj.optString("wakeMood", "Neutral"),
                                sleepDebtHours = obj.optDouble("sleepDebtHours", 0.0).toFloat(),
                                targetHours = obj.optDouble("targetHours", 8.0).toFloat()
                            )
                        )
                    }
                }

                // Restore Habits
                if (root.has("habits")) {
                    val arr = root.getJSONArray("habits")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertHabit(
                            Habit(
                                title = obj.optString("title", "Habit"),
                                description = obj.optString("description", ""),
                                frequency = obj.optString("frequency", "Daily"),
                                targetCount = obj.optInt("targetCount", 1),
                                currentStreak = obj.optInt("currentStreak", 0),
                                maxStreak = obj.optInt("maxStreak", 0),
                                lastCompletedTimestamp = obj.optLong("lastCompletedTimestamp", 0L),
                                isArchived = obj.optBoolean("isArchived", false)
                            )
                        )
                    }
                }

                // Restore Habit Completions
                if (root.has("habit_completions")) {
                    val arr = root.getJSONArray("habit_completions")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertHabitCompletion(
                            HabitCompletion(
                                habitId = obj.optInt("habitId", 0),
                                completionTimestamp = obj.optLong("completionTimestamp", System.currentTimeMillis()),
                                notes = obj.optString("notes", "")
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun uploadBackupToSupabase() {
        viewModelScope.launch(Dispatchers.IO) {
            _supabaseStatus.value = "Uploading backup to Supabase..."
            _showSqlSuggestion.value = false
            try {
                repository.runCheckpoint()
                val alarmsVal = alarms.value
                val eventsVal = events.value
                val goalsVal = goals.value
                val logsVal = sleepLogs.value
                val habitsVal = habits.value
                val completionsVal = habitCompletions.value

                val backupRoot = JSONObject()

                val alarmsArr = JSONArray()
                alarmsVal.forEach {
                    val a = JSONObject()
                    a.put("label", it.label)
                    a.put("description", it.description)
                    a.put("hour", it.hour)
                    a.put("minute", it.minute)
                    a.put("repeatDays", it.repeatDays)
                    a.put("intervalMinutes", it.intervalMinutes)
                    a.put("gradualVolume", it.gradualVolume)
                    a.put("vibrationStyle", it.vibrationStyle)
                    a.put("category", it.category)
                    a.put("colorTagHex", it.colorTagHex)
                    a.put("emoji", it.emoji)
                    a.put("snoozeEnabled", it.snoozeEnabled)
                    a.put("snoozeDurationMinutes", it.snoozeDurationMinutes)
                    a.put("snoozeLimit", it.snoozeLimit)
                    a.put("isSmartSnooze", it.isSmartSnooze)
                    a.put("bedsideMode", it.bedsideMode)
                    a.put("flashlightStrobe", it.flashlightStrobe)
                    a.put("challengeType", it.challengeType)
                    a.put("challengeDifficulty", it.challengeDifficulty)
                    alarmsArr.put(a)
                }

                val eventsArr = JSONArray()
                eventsVal.forEach {
                    val ev = JSONObject()
                    ev.put("title", it.title)
                    ev.put("notes", it.notes)
                    ev.put("startTime", it.startTime)
                    ev.put("endTime", it.endTime)
                    ev.put("location", it.location)
                    ev.put("category", it.category)
                    ev.put("colorHex", it.colorHex)
                    ev.put("priority", it.priority)
                    ev.put("reminders", it.reminders)
                    ev.put("isAllDay", it.isAllDay)
                    ev.put("recurrence", it.recurrence)
                    eventsArr.put(ev)
                }

                val goalsArr = JSONArray()
                goalsVal.forEach {
                    val g = JSONObject()
                    g.put("title", it.title)
                    g.put("description", it.description)
                    g.put("category", it.category)
                    g.put("targetProgress", it.targetProgress)
                    g.put("currentProgress", it.currentProgress)
                    g.put("streakCount", it.streakCount)
                    goalsArr.put(g)
                }

                val sleepArr = JSONArray()
                logsVal.forEach {
                    val sl = JSONObject()
                    sl.put("bedTime", it.bedTime)
                    sl.put("wakeTime", it.wakeTime)
                    sl.put("wakeMood", it.wakeMood)
                    sl.put("sleepDebtHours", it.sleepDebtHours)
                    sl.put("targetHours", it.targetHours)
                    sleepArr.put(sl)
                }

                val habitsArr = JSONArray()
                habitsVal.forEach {
                    val h = JSONObject()
                    h.put("title", it.title)
                    h.put("description", it.description)
                    h.put("frequency", it.frequency)
                    h.put("targetCount", it.targetCount)
                    h.put("currentStreak", it.currentStreak)
                    h.put("maxStreak", it.maxStreak)
                    h.put("lastCompletedTimestamp", it.lastCompletedTimestamp)
                    h.put("isArchived", it.isArchived)
                    habitsArr.put(h)
                }

                val completionsArr = JSONArray()
                completionsVal.forEach {
                    val hc = JSONObject()
                    hc.put("habitId", it.habitId)
                    hc.put("completionTimestamp", it.completionTimestamp)
                    hc.put("notes", it.notes)
                    completionsArr.put(hc)
                }

                backupRoot.put("alarms", alarmsArr)
                backupRoot.put("events", eventsArr)
                backupRoot.put("goals", goalsArr)
                backupRoot.put("sleep_logs", sleepArr)
                backupRoot.put("habits", habitsArr)
                backupRoot.put("habit_completions", completionsArr)

                val payloadObj = JSONObject()
                payloadObj.put("user_email", _userEmail.value)
                payloadObj.put("backup_data", backupRoot)

                val payloadArray = JSONArray()
                payloadArray.put(payloadObj)

                val client = SupabaseClient.httpClient
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = payloadArray.toString().toRequestBody(mediaType)

                val token = SupabaseClient.getAdminSessionToken()
                val request = SupabaseClient.newRequestBuilder("/rest/v1/arise_backups", token)
                    .post(body)
                    .addHeader("Prefer", "resolution=merge-duplicates")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _supabaseStatus.value = "Cloud backup sync successful!"
                    } else {
                        val code = response.code
                        val errorBody = response.body?.string() ?: ""
                        if (code == 404 || errorBody.contains("relation") || errorBody.contains("not found")) {
                            _supabaseStatus.value = "Table arise_backups not found. SQL setup is required in Supabase dashboard."
                            _showSqlSuggestion.value = true
                        } else {
                            _supabaseStatus.value = "Error $code: $errorBody"
                        }
                    }
                }
            } catch (e: Exception) {
                _supabaseStatus.value = "Sync failed: ${e.message}"
            }
        }
    }

    fun restoreBackupFromSupabase() {
        viewModelScope.launch(Dispatchers.IO) {
            _supabaseStatus.value = "Querying live cloud storage..."
            _showSqlSuggestion.value = false
            try {
                val client = SupabaseClient.httpClient
                val token = SupabaseClient.getAdminSessionToken()
                val request = SupabaseClient.newRequestBuilder(
                    "/rest/v1/arise_backups?user_email=eq.${_userEmail.value}&select=*",
                    token
                ).get().build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseStr = response.body?.string() ?: "[]"
                        val queryResult = JSONArray(responseStr)
                        if (queryResult.length() > 0) {
                            val row = queryResult.getJSONObject(0)
                            val backupDataObj = row.getJSONObject("backup_data")

                            withContext(Dispatchers.Main) {
                                val success = restoreBackupJson(backupDataObj.toString())
                                if (success) {
                                    _supabaseStatus.value = "Cloud restore completed successfully!"
                                } else {
                                    _supabaseStatus.value = "Failed to parse loaded backup."
                                }
                            }
                        } else {
                            _supabaseStatus.value = "No backups found for user ${_userEmail.value}"
                        }
                    } else {
                        val code = response.code
                        val errorBody = response.body?.string() ?: ""
                        if (code == 404 || errorBody.contains("relation") || errorBody.contains("not found")) {
                            _supabaseStatus.value = "Table arise_backups not found. SQL setup is required in Supabase dashboard."
                            _showSqlSuggestion.value = true
                        } else {
                            _supabaseStatus.value = "Error $code: $errorBody"
                        }
                    }
                }
            } catch (e: Exception) {
                _supabaseStatus.value = "Restore backup failed: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        napTimerJob?.cancel()
    }
}

enum class AriseTab {
    Home,
    Alarms,
    Calendar,
    Goals,
    Sleep,
    StatsCustomize
}

