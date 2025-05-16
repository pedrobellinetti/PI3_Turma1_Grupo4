package com.example.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import com.example.superid.ui.screens.LoginForm
import com.example.superid.UserRegistrationActivity

class LoginFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            LoginForm(
                onNavigateToRegister = {
                    startActivity(Intent(context, UserRegistrationActivity::class.java))
                },
                onLoginSuccess = {
                    finish()
                },
                onNavigateToForgotPassword = {
                    startActivity(Intent(context, RecuperacaoSenhaActivity::class.java))
                }
            )
        }
    }
}