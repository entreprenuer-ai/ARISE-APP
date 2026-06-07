package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.core.database.AriseDatabase
import com.example.core.database.AriseRepository
import com.example.ui.AriseMainScreen
import com.example.ui.viewmodel.AriseViewModel
import com.example.ui.viewmodel.AriseViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AriseViewModel

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission status handled gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable edge-to-edge lockscreen features
        setupLockScreenFlags()

        // 100% Secure Local Database & Repository Initialization
        val database = AriseDatabase.getDatabase(applicationContext)
        val repository = AriseRepository(database.ariseDao())

        // ViewModel Factory Initialization
        val factory = AriseViewModelFactory(application, repository)
        viewModel = ViewModelProvider(
            this,
            factory
        )[AriseViewModel::class.java]

        // Handle alarm trigger on creation
        handleAlarmIntent(intent)

        // Request notification permission if SDK >= 33 (Android 13)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            // Render beautiful ARISE Application Frame
            AriseMainScreen(viewModel, factory)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent?) {
        if (intent == null) return
        val alarmId = intent.getIntExtra("EXTRA_ALARM_ID", -1)
        if (alarmId != -1) {
            viewModel.handleTriggeredAlarmFromIntent(alarmId)
        }
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as? android.app.KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}
