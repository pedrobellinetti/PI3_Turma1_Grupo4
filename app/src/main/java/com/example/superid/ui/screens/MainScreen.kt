package com.example.superid.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth


// Tela principal
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterial3Api
@Composable
fun MainScreen(onLogout: () -> Unit, uid: String) {
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: "Usuário"

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Bem-vindo, $userEmail!", style = MaterialTheme.typography.headlineSmall)

        Button(onClick = {
            auth.signOut()
            onLogout()
        }) {
            Text("Sair")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        PasswordManagerScreen(uid = uid) // Corrigido para usar PascalCase
    }
}