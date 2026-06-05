package com.example.features.backup.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.core.network.SupabaseClient

class BackupViewModel(
    private val repository: AriseRepository
) : ViewModel() {

    private val _supabaseUrl = MutableStateFlow("https://qlxdvttdeozrpqbtacrp.supabase.co")
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseAnonKey = MutableStateFlow("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFseGR2dHRkZW96cnBxYnRhY3JwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAzMTcwMjgsImV4cCI6MjA5NTg5MzAyOH0.7VAV1Z4urhx8iWa3-ZDX7JlLbJmXmZMGURL9YEIu6-4")
    val supabaseAnonKey: StateFlow<String> = _supabaseAnonKey.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow("")
    val authErrorMessage: StateFlow<String> = _authErrorMessage.asStateFlow()

    private val _supabaseStatus = MutableStateFlow("Please log in to synchronize with cloud")
    val supabaseStatus: StateFlow<String> = _supabaseStatus.asStateFlow()

    private val _showSqlSuggestion = MutableStateFlow(false)
    val showSqlSuggestion: StateFlow<Boolean> = _showSqlSuggestion.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allSettings.collect { list ->
                val emailSetting = list.find { it.key == "logged_in_email" }
                val email = emailSetting?.value ?: ""
                if (email.isNotEmpty()) {
                    _userEmail.value = email
                    _isLoggedIn.value = true
                    _supabaseStatus.value = "Connected as $email (USER)"
                } else {
                    _userEmail.value = ""
                    _isLoggedIn.value = false
                    _supabaseStatus.value = "Please log in to synchronize with cloud"
                }
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

                            var role = "user"
                            if (userObj.has("user_metadata")) {
                                val meta = userObj.getJSONObject("user_metadata")
                                if (meta.has("role")) {
                                    role = meta.getString("role")
                                }
                            }

                            withContext(Dispatchers.Main) {
                                _userEmail.value = userEmailVal
                                _isLoggedIn.value = true
                                _authLoading.value = false
                                _authErrorMessage.value = ""
                                _supabaseStatus.value = "Connected as $userEmailVal (${role.uppercase()})"

                                viewModelScope.launch {
                                    repository.saveSetting("logged_in_email", userEmailVal)
                                    repository.saveSetting("logged_in_role", role)
                                    repository.saveSetting("session_token", token)
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

    fun registerUser(emailStr: String, passwordStr: String) {
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
                        put("role", "user")
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

    fun logoutUser() {
        _isLoggedIn.value = false
        _userEmail.value = ""
        _authErrorMessage.value = ""
        _supabaseStatus.value = "Please log in to synchronize with cloud"
        viewModelScope.launch {
            repository.saveSetting("logged_in_email", "")
            repository.saveSetting("logged_in_role", "user")
            repository.saveSetting("session_token", "")
            repository.saveSetting("isAdminMode", "false")
        }
    }

    private val alarms = repository.allAlarms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val events = repository.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val goals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val sleepLogs = repository.allSleepLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val habits = repository.allHabits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val habitCompletions = repository.allHabitCompletions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schemaSqlSuggestion = """
        -- RUN THIS IN SUPABASE SQL EDITOR TO SETUP BACKUP SCHEMA FOR ARISE APP
        
        create table if tube_exists arise_backups (
          user_email text primary key check (char_length(user_email) >= 3),
          backup_data jsonb not null default '{}'::jsonb,
          updated_at timestamp with time zone default timezone('utc'::text, now()) not null
        );
        
        -- Enable Row Level Security (RLS) to keep files encrypted & safe!
        alter table arise_backups enable row level security;
        
        -- Setup public anonymous select and insert permissions
        create policy "Allow Anonymous Select" on arise_backups for select using (true);
        create policy "Allow Anonymous Insert" on arise_backups for insert with check (true);
        create policy "Allow Anonymous Update" on arise_backups for update using (true);
    """.trimIndent()

    fun updateSupabaseUrl(url: String) {
        _supabaseUrl.value = url
    }

    fun updateSupabaseAnonKey(key: String) {
        _supabaseAnonKey.value = key
    }

    fun updateUserEmail(email: String) {
        _userEmail.value = email
    }

    fun updateSupabaseStatus(status: String) {
        _supabaseStatus.value = status
    }

    suspend fun generateFullBackupJson(): String = withContext(Dispatchers.Default) {
        val backupRoot = JSONObject()

        val alarmsArr = JSONArray()
        alarms.value.forEach {
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
        events.value.forEach {
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
        goals.value.forEach {
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
        sleepLogs.value.forEach {
            val sl = JSONObject()
            sl.put("bedTime", it.bedTime)
            sl.put("wakeTime", it.wakeTime)
            sl.put("wakeMood", it.wakeMood)
            sl.put("sleepDebtHours", it.sleepDebtHours)
            sl.put("targetHours", it.targetHours)
            sleepArr.put(sl)
        }

        val habitsArr = JSONArray()
        habits.value.forEach {
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
        habitCompletions.value.forEach {
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

        backupRoot.toString()
    }

    suspend fun restoreBackupJson(jsonString: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val root = JSONObject(jsonString)

            if (root.has("alarms")) {
                val arr = root.getJSONArray("alarms")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    repository.insertAlarm(
                        Alarm(
                            label = obj.getString("label"),
                            description = obj.optString("description", ""),
                            hour = obj.getInt("hour"),
                            minute = obj.getInt("minute"),
                            repeatDays = obj.optString("repeatDays", "Mon,Tue,Wed,Thu,Fri"),
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

            if (root.has("events")) {
                val arr = root.getJSONArray("events")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    repository.insertEvent(
                        CalendarEvent(
                            title = obj.getString("title"),
                            notes = obj.optString("notes", ""),
                            startTime = obj.getLong("startTime"),
                            endTime = obj.getLong("endTime"),
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

            if (root.has("goals")) {
                val arr = root.getJSONArray("goals")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    repository.insertGoal(
                        Goal(
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            category = obj.optString("category", "Personal"),
                            targetProgress = obj.optInt("targetProgress", 10),
                            currentProgress = obj.optInt("currentProgress", 0),
                            streakCount = obj.optInt("streakCount", 0)
                        )
                    )
                }
            }

            if (root.has("sleep_logs")) {
                val arr = root.getJSONArray("sleep_logs")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    repository.insertSleepLog(
                        SleepLog(
                            bedTime = obj.getLong("bedTime"),
                            wakeTime = obj.getLong("wakeTime"),
                            wakeMood = obj.optString("wakeMood", "Neutral"),
                            sleepDebtHours = obj.optDouble("sleepDebtHours", 0.0).toFloat(),
                            targetHours = obj.optDouble("targetHours", 8.0).toFloat()
                        )
                    )
                }
            }

            if (root.has("habits")) {
                val arr = root.getJSONArray("habits")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    repository.insertHabit(
                        Habit(
                            title = obj.getString("title"),
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
                val backupDataString = generateFullBackupJson()
                val backupRoot = JSONObject(backupDataString)

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
}
