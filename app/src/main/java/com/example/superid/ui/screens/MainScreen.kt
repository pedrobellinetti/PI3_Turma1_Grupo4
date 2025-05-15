package com.example.superid.ui.screens

import com.example.superid.LoginFormActivity
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import com.google.firebase.auth.FirebaseAuth

// Tela principal
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterial3Api
@Composable
fun MainScreen(onLogout: () -> Unit, uid: String) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val userEmail = auth.currentUser?.email ?: "Usuário"

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8000FF)),
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
        ) {
            Text("Sair")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        PasswordManagerScreen(uid = uid)
    }
}