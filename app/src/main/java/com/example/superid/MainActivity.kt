package com.example.superid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.superid.ui.screens.LoginForm
import com.example.superid.ui.screens.PasswordManagerScreen
import com.example.superid.ui.screens.PasswordRecoveryScreen
import com.example.superid.ui.screens.QrScanScreen
import com.example.superid.ui.screens.UserRegistrationForm
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.util.UUID

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
    val db = Firebase.firestore
    val uid = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid ?: "") }
    val senhas = remember { mutableStateListOf<Senha>() }

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
            PasswordManagerScreen(
                uid = uid.value, // Passa o valor do estado
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    currentScreen = AuthScreen.LOGIN
                },
                onCreatePassword = { currentUid ->
                    val intent = Intent(context, PasswordFormActivity::class.java)
                    intent.putExtra("uid", currentUid)
                    context.startActivity(intent)
                }
            )
        }

        AuthScreen.QR_LOGIN -> QrScanScreen(
            onLoginAprovado = { currentScreen = AuthScreen.MAIN }
        )

        AuthScreen.RECOVERY -> PasswordRecoveryScreen(
            onNavigateToLogin = { currentScreen = AuthScreen.LOGIN }
        )

        else -> {
            //TODO
        }
    }
}

