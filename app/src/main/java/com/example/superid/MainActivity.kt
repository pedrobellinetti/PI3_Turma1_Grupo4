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
    val db = Firebase.firestore
    val uid = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid ?: "") }
    val senhas = remember { mutableStateListOf<Senha>() }

    // Carregar senhas
    LaunchedEffect(uid.value) { // uid.value para acessar o valor dentro do LaunchedEffect
        if (uid.value.isNotEmpty()) { // Verifique se o UID não está vazio
            db.collection("users").document(uid.value).collection("passwords")
                .get()
                .addOnSuccessListener { result ->
                    senhas.clear()
                    for (doc in result) {
                        senhas.add(doc.toObject(Senha::class.java).copy(id = doc.id))
                    }
                }
                .addOnFailureListener { e ->
                    // Tratar falha ao carregar senhas
                    println("Erro ao carregar senhas: $e")
                }
        }
    }

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
                senhas = senhas,
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

