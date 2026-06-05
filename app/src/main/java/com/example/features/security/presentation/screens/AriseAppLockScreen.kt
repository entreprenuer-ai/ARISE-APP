package com.example.features.security.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CustomColorScheme
import com.example.features.security.presentation.viewmodel.SecurityViewModel

@Composable
fun AriseAppLockScreen(
    viewModel: SecurityViewModel,
    colors: CustomColorScheme,
    fontFamily: FontFamily
) {
    var passwordInput by remember { mutableStateOf("") }
    var showIncorrectError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "App Encrypted Lock",
            tint = colors.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ARISE PRIVATE ENCRYPTED SENSITIVE ZONE",
            fontFamily = fontFamily,
            color = colors.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "100% On-Device Protection. Enter PIN to unlock.",
            fontFamily = fontFamily,
            color = colors.onBackground.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("App PIN Lock", color = colors.onBackground) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onBackground,
                unfocusedTextColor = colors.onBackground,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.divider
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .testTag("app_pin_input")
        )

        if (showIncorrectError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Incorrect PIN code. Access denied.",
                fontFamily = fontFamily,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val ok = viewModel.unlockAppWithPin(passwordInput)
                if (!ok) {
                    passwordInput = ""
                    showIncorrectError = true
                } else {
                    showIncorrectError = false
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
                .testTag("pin_unlock_button")
        ) {
            Text("Unlock Vault", color = colors.onPrimary, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
        }
    }
}
