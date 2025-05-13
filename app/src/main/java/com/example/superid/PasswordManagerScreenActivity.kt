package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class PasswordManagerScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(context, PasswordManagerScreenActivity::class.java)
                        intent.putExtra("uid", uid)
                        context.startActivity(intent)
                    }
                }
            ) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Bem-vindo à Tela de Senhas", style = MaterialTheme.typography.headlineSmall)

        }
    }
}