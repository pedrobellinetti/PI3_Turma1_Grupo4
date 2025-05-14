package com.example.superid.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.superid.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordRecoveryScreen(onNavigateToLogin: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barra Superior Personalizada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 33.dp)
        ) {
            // Botão de voltar alinhado à esquerda
            IconButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar para Login",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Conteúdo centralizado: Título + Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(29.dp)
            ) {
                Text(
                    text = "Super ID",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo do Super ID",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(30.dp))
                )
            }
        }

        // Título da Tela
        Text(
            text = "Recuperar Senha",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 30.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Campo de E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Digite seu e-mail", style = MaterialTheme.typography.labelLarge) },
            singleLine = true,
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Enviamos um link para seu e-mail para redefinir(caso esteja cadastrado)",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                val errorMessage = when (task.exception) {
                                    is FirebaseAuthException -> {
                                        when ((task.exception as FirebaseAuthException).errorCode) {
                                            "ERROR_INVALID_EMAIL" -> "Formato de e-mail inválido."
                                            "ERROR_USER_NOT_FOUND" -> "Se o e-mail estiver correto, pode não haver conta associada a ele."
                                            else -> "Ocorreu um erro ao enviar o e-mail. Tente novamente."
                                        }
                                    }
                                    else -> "Erro inesperado. Verifique o e-mail digitado e tente novamente."
                                }
                                Toast.makeText(context, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Por favor, digite seu e-mail.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .width(161.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Recuperar", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mensagem de Status
        if (status.isNotEmpty()) {
            Text(text = status, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Link para Voltar ao Login (agora como TextButton na parte inferior)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "Voltar para Login",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}