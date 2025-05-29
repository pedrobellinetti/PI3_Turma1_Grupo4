package com.example.superid.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.superid.data.RegistrationData
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationForm(
    onNavigateToLogin: () -> Unit,
    onRegisterAttempt: (data: RegistrationData) -> Unit,
    onRegistrationSuccessAndDialogClosed: () -> Unit,
    registrationSuccess: Boolean // Estado passado da MainActivity
) {
    val context = LocalContext.current

    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var nomeError by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Estado para controlar a visibilidade da senha no campo de texto
    var passwordVisible by remember { mutableStateOf(false) }

    // LaunchedEffect para observar o estado de sucesso do registro vindo da MainActivity
    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            showSuccessDialog = true // Se o registro for bem-sucedido, exibe o diálogo
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 33.dp)
        ) {
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

        Text(
            text = "Bem-vindo(a) ao Super ID! Cadastre-se",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 30.dp)
        )

        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it
                nomeError = false
                generalError = ""
            },
            label = { Text("Digite seu nome", style = MaterialTheme.typography.labelLarge) },
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (nomeError) Color.Red else MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = if (nomeError) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        if (nomeError) {
            Text(
                text = "Por favor, digite seu nome.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false
                generalError = ""
            },
            label = { Text("Digite seu e-mail", style = MaterialTheme.typography.labelLarge) },
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (emailError || generalError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = if (emailError || generalError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        if (emailError) {
            Text(
                text = "Por favor, digite um e-mail válido.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
                senhaError = false
                generalError = ""
            },
            label = {
                Text(
                    "Digite sua senha (mínimo 6 caracteres)",
                    style = MaterialTheme.typography.labelLarge
                )
            },
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
            Text(
                text = "A senha precisa ter no mínimo 6 caracteres.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (generalError.isNotEmpty()) {
            Text(text = generalError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        // Botão "Cadastrar"
        Button(
            onClick = {
                var hasValidationError = false
                generalError = ""

                if (nome.isBlank()) {
                    nomeError = true
                    hasValidationError = true
                }
                if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                        .matches()
                ) {
                    emailError = true
                    hasValidationError = true
                }
                if (senha.length < 6) {
                    senhaError = true
                    hasValidationError = true
                }

                if (!hasValidationError) {
                    // Chama a lógica de registro na MainActivity
                    onRegisterAttempt(RegistrationData(nome, email, senha))
                } else {
                    Toast.makeText(
                        context,
                        "Por favor, preencha todos os campos corretamente.",
                        Toast.LENGTH_SHORT
                    ).show()
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
            Text("Cadastrar", style = MaterialTheme.typography.labelLarge)
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "Já tem conta? Login",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }

    // O diálogo de sucesso
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text("Cadastro Realizado com Sucesso!")
            },
            text = {
                Text("Seu cadastro foi concluído. Você pode fazer login agora. A verificação do e-mail não é obrigatória para o login normal, mas é essencial para usar a funcionalidade de 'Login Sem Senha' no futuro. Recomendamos a verificação para sua segurança e conveniência.")
            },
            confirmButton = {
                // Use a Row para centralizar o botão
                Row(
                    modifier = Modifier.fillMaxWidth(), // Faz a Row ocupar a largura total do diálogo
                    horizontalArrangement = Arrangement.Center // Centraliza o conteúdo horizontalmente
                ) {
                    Button(onClick = {
                        showSuccessDialog = false // Oculta o diálogo
                        onRegistrationSuccessAndDialogClosed() // Notifica a MainActivity para navegar
                    }) {
                        Text("Fazer Login")
                    }
                }
            },
        )
    }
}