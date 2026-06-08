package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent { MyApplicationTheme { Greeting("Robolectric") } }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun arise_main_screen_screenshot() {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    val database = com.example.core.database.AriseDatabase.getDatabase(context)
    val dao = database.ariseDao()
    val repository = com.example.core.database.AriseRepository(dao, database.sleepSessionDao())
    val app = context as android.app.Application
    val vm = com.example.ui.viewmodel.AriseViewModel(app, repository)

    composeTestRule.setContent {
      com.example.ui.AriseMainScreen(vm)
    }
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/arise_main.png")
  }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String) {
  androidx.compose.material3.Text(text = "Hello $name!")
}

