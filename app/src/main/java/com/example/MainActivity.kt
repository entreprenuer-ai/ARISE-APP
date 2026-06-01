package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AriseDatabase
import com.example.data.AriseRepository
import com.example.ui.AriseMainScreen
import com.example.ui.viewmodel.AriseViewModel
import com.example.ui.viewmodel.AriseViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 100% Secure Local Database & Repository Initialization
        val database = AriseDatabase.getDatabase(applicationContext)
        val repository = AriseRepository(database.ariseDao())

        // ViewModel Factory Initialization
        val viewModel = ViewModelProvider(
            this,
            AriseViewModelFactory(application, repository)
        )[AriseViewModel::class.java]

        setContent {
            // Render beautiful ARISE Application Frame
            AriseMainScreen(viewModel)
        }
    }
}
