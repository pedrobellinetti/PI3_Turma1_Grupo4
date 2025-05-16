package com.example.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.superid.ui.screens.PasswordManagerScreen
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth

class PasswordManagerScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        setContent {
            SuperIDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (uid != null) {
                        PasswordManagerScreen(uid = uid)
                    } else {
                        // Lidar com o caso em que o UID é nulo, talvez mostrar uma tela de erro ou voltar para o login
                        // Por exemplo:
                        // Text("Erro: Usuário não autenticado.")
                    }
                }
            }
        }
    }
}