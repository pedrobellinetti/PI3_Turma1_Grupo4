package com.example.superid


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.superid.ui.screens.UserRegistrationForm
import com.example.superid.ui.theme.SuperIDTheme

class UserRegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperIDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UserRegistrationForm(onNavigateToLogin = {
                        //TODO redirecionar para a tela de senhas
                        finish()
                    })
                }
            }
        }
    }
}