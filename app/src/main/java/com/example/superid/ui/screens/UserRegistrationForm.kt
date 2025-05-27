package com.example.superid.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // Importe este
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll // Importe este
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationForm(
    onNavigateToLogin: () -> Unit,
    onRegisterAttempt: (data: RegistrationData) -> Unit
) {
    val context = LocalContext.current

    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var nomeError by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf("") }

    // Adicione um ScrollState para controlar a rolagem
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState), // Adicione o modificador verticalScroll aqui
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
            Text(text = "Por favor, digite seu nome.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
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
            Text(text = "Por favor, digite um e-mail válido.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it
                senhaError = false
                generalError = ""
            },
            label = { Text("Digite sua senha (mínimo 6 caracteres)", style = MaterialTheme.typography.labelLarge) },
            visualTransformation = PasswordVisualTransformation(),
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
            Text(text = "A senha precisa ter no mínimo 6 caracteres.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        if (generalError.isNotEmpty()) {
            Text(text = generalError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                var hasValidationError = false
                generalError = ""

                if (nome.isBlank()) {
                    nomeError = true
                    hasValidationError = true
                }
                if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = true
                    hasValidationError = true
                }
                if (senha.length < 6) {
                    senhaError = true
                    hasValidationError = true
                }

                if (!hasValidationError) {
                    onRegisterAttempt(RegistrationData(nome, email, senha))
                } else {
                    Toast.makeText(context, "Por favor, preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show()
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
}