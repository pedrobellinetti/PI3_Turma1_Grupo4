package com.example.superid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid.ui.screens.LoginForm
import com.example.superid.ui.screens.MainScreen
import com.example.superid.ui.screens.PasswordManagerScreen
import com.example.superid.ui.screens.PasswordRecoveryScreen
import com.example.superid.ui.screens.QrScanScreen
import com.example.superid.ui.screens.UserRegistrationForm
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth

//import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenuDefaults

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AuthApp()
                }
            }
        }
    }
}

// Controlar a tela atual
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthApp() {
    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
    val context = LocalContext.current

    when (currentScreen) {
        AuthScreen.LOGIN -> LoginForm(
            onNavigateToRegister = { currentScreen = AuthScreen.REGISTER },
            onLoginSuccess = { currentScreen = AuthScreen.MAIN },
            onNavigateToForgotPassword = {
                context.startActivity(Intent(context, RecuperacaoSenhaActivity::class.java))
            }
        )

        AuthScreen.REGISTER -> UserRegistrationForm(
            onNavigateToLogin = { currentScreen = AuthScreen.LOGIN }
        )

        AuthScreen.MAIN -> {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            MainScreen(onLogout = { currentScreen = AuthScreen.LOGIN }, uid = uid)
        }

        AuthScreen.QR_LOGIN -> QrScanScreen(
            onLoginAprovado = { currentScreen = AuthScreen.MAIN }
        )

        AuthScreen.RECOVERY -> PasswordRecoveryScreen(
            onNavigateToLogin = { currentScreen = AuthScreen.LOGIN }
        )
    }
}
