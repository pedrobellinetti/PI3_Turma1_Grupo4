package com.example.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.superid.ui.screens.LoginForm

class LoginFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginForm(
                onNavigateToRegister = {
                    // startActivity(Intent(this, RegisterActivity::class.java))
                },
                onLoginSuccess = {
                    finish() // Fecha essa tela e volta à anterior
                }
            )
        }
    }
}
