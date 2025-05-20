package com.example.superid.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.superid.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var senhaError by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Container superior personalizado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                .padding(vertical = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            // Botão de voltar alinhado à esquerda
            IconButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar para Cadastro",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(30.dp)
            ) {
                Text(
                    text = "Super ID",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.padding(7.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo do Super ID",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(30.dp))
                )
            }
        }
        Text(
            text = "Entre na sua conta",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 30.dp)
        )

        // Campos de Email e Senha
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Digite seu e-mail", style = MaterialTheme.typography.labelLarge) },
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (emailError) Color.Red else MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = if (emailError) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        if (emailError) {
            Text(text = "Por favor, digite seu e-mail.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Digite sua senha", style = MaterialTheme.typography.labelLarge) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (senhaError) Color.Red else MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = if (senhaError) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        if (senhaError) {
            Text(text = "Sua senha contém no mínimo 6 caracteres.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        TextButton(
            onClick = onNavigateToForgotPassword,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 60.dp)
        ) {
            Text(
                "Esqueceu sua senha?",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }

        // Botão Entrar
        Button(
            onClick = {
                var hasValidationError = false
                var currentEmailError = false
                var currentSenhaError = false

                if (email.isBlank()) {
                    emailError = true
                    currentEmailError = true
                    hasValidationError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = true
                    hasValidationError = true
                } else {
                    emailError = false
                }

                if (senha.length < 6) {
                    senhaError = true
                    currentSenhaError = true
                    hasValidationError = true
                } else if (senha.isBlank()) {
                    senhaError = true
                    currentSenhaError = true
                    hasValidationError = true
                } else {
                    senhaError = false
                }

                if (!hasValidationError) {
                    auth.signInWithEmailAndPassword(email, senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful && auth.currentUser?.isEmailVerified == true) {
                                Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_LONG).show()
                                onLoginSuccess()
                            } else {
                                val errorMessage = when (task.exception) {
                                    is FirebaseAuthException -> {
                                        when ((task.exception as FirebaseAuthException).errorCode) {
                                            "ERROR_WRONG_PASSWORD" -> "Senha incorreta."
                                            "ERROR_USER_DISABLED" -> "Esta conta de usuário foi desativada."
                                            "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas de login. Tente novamente mais tarde."
                                            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está em uso."
                                            else -> "Usuário não encontrado."
                                        }
                                    }
                                    else -> "Ocorreu um erro inesperado."
                                }
                                Toast.makeText(context, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        }
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
            Text("Entrar", style = MaterialTheme.typography.labelLarge)
        }


        // Links inferiores
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 32.dp)
        ) {
            TextButton(onClick = onNavigateToRegister) {
                Text("Não tem conta? Cadastre-se", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}