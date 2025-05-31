package com.example.superid.ui.screens

import android.content.SharedPreferences
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
    sharedPreferences: SharedPreferences,
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

    // Estado para controlar a visibilidade do primeiro AlertDialog
    var showEmailVerificationDialog by remember { mutableStateOf(false) }

    // Estado para controlar a visibilidade da senha no campo de texto
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Função para reenviar o e-mail de verificação
    fun resendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Novo e-mail de verificação enviado! Verifique sua caixa de entrada e pasta de spam.", Toast.LENGTH_LONG).show()
                } else {
                    val errorMessage = task.exception?.message ?: "Erro ao reenviar e-mail de verificação."
                    Toast.makeText(context, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState),
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
            singleLine = true,
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

        // ---- CAMPO DA SENHA COM O OLHINHO ----
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Digite sua senha", style = MaterialTheme.typography.labelLarge) },
            // VisualTransformation baseado no estado passwordVisible
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                // Ícone de olho (aberto ou fechado)
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Descrição para acessibilidade
                val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
            singleLine = true,
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

                if (email.isBlank()) {
                    emailError = true
                    hasValidationError = true
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = true
                    hasValidationError = true
                } else {
                    emailError = false
                }

                if (senha.length < 6) {
                    senhaError = true
                    hasValidationError = true
                } else if (senha.isBlank()) {
                    senhaError = true
                    hasValidationError = true
                } else {
                    senhaError = false
                }

                if (!hasValidationError) {
                    auth.signInWithEmailAndPassword(email, senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null && !user.isEmailVerified) {
                                    // Se o e-mail NÃO estiver verificado, mostra o diálogo
                                    showEmailVerificationDialog = true
                                } else {
                                    // Se o e-mail ESTIVER verificado, procede com o login normalmente
                                    sharedPreferences.edit().putBoolean("emailValidado", true).apply()
                                    Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_LONG).show()
                                    onLoginSuccess()
                                }
                            } else {
                                val errorMessage = when (task.exception) {
                                    is FirebaseAuthException -> {
                                        when ((task.exception as FirebaseAuthException).errorCode) {
                                            "ERROR_WRONG_PASSWORD" -> "Senha incorreta."
                                            "ERROR_USER_DISABLED" -> "Esta conta de usuário foi desativada."
                                            "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas de login. Tente novamente mais tarde."
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

        // --- Diálogos ---
        if (showEmailVerificationDialog) {
            AlertDialog(
                onDismissRequest = {
                    showEmailVerificationDialog = false
                    Toast.makeText(
                        context,
                        "Verificação de e-mail pendente. Faça login novamente.",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                title = { Text("Verificação de E-mail Pendente") },
                text = {
                    Text("Seu e-mail ainda não foi verificado. Deseja reenviar o e-mail de verificação ou prosseguir mesmo assim?")
                },
                confirmButton = {
                    // Use uma Row para agrupar e centralizar os dois botões
                    Row(
                        modifier = Modifier.fillMaxWidth(), // Faz a Row ocupar a largura total do diálogo
                        horizontalArrangement = Arrangement.Start, // Centraliza os botões horizontalmente
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Botão "Reenviar E-mail de Verificação"
                        Button(onClick = {
                            resendVerificationEmail() // Chame a função de reenviar
                            showEmailVerificationDialog = false // Fecha o diálogo
                        }) {
                            Text("Reenviar")
                        }
                        Spacer(Modifier.width(30.dp)) // Espaço entre os botões
                        // Botão "Prosseguir"
                        Button(onClick = {
                            showEmailVerificationDialog = false
                            onLoginSuccess()
                        }) {
                            Text("Prosseguir")
                        }
                    }
                },
            )
        }


        // Link inferior
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