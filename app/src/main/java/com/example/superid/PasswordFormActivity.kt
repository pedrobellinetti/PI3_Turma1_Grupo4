package com.example.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.superid.ui.screens.PasswordFormScreen
import com.example.superid.ui.theme.SuperIDTheme

class PasswordFormActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uid = intent.getStringExtra("uid") ?: ""

        setContent {
            SuperIDTheme {
                PasswordFormScreen(uid = uid) {
                    finish() // Fecha essa tela e volta para a anterior
                }
            }
        }
    }
}
