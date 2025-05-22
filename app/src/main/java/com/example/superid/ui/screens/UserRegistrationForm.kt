package com.example.superid.ui.screens

import android.content.Context
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRegistrationForm(
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var nomeError by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance() // Instância do Firestore

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
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 33.dp)
        ) {

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

        Text(
            text = "Bem-vindo(a) ao Super ID! Cadastre-se",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 30.dp)
        )

        // Campos de Nome, E-mail e Senha
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Digite seu nome", style = MaterialTheme.typography.labelLarge) },
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
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
            Text(text = "Por favor, digite um e-mail válido.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Digite sua senha (mínimo 6 caracteres)", style = MaterialTheme.typography.labelLarge) },
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
            Text(text = "A senha precisa ter no mínimo 6 caracteres.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        // Botão Cadastrar
        Button(
            onClick = {
                var hasValidationError = false

                if (nome.isBlank()) {
                    nomeError = true
                    hasValidationError = true
                } else {
                    nomeError = false
                }

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
                } else {
                    senhaError = false
                }

                if (!hasValidationError) {
                    auth.createUserWithEmailAndPassword(email, senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val uid = user?.uid
                                val androidId = getAndroidId(context) // Obter o Android ID

                                if (uid != null && androidId.isNotEmpty()) { // Verificação para garantir que o Android ID não é vazio
                                    val userData = hashMapOf(
                                        "nome" to nome,
                                        "email" to email,
                                        "uid" to uid,
                                        "imei(Android ID)" to androidId, // Usando "imei" como nome do campo, mas o valor é o Android ID
                                        "data_criacao" to Timestamp.now() // Para saber quando o registro foi criado.
                                    )

                                    db.collection("users")
                                        .document(uid) // Usar o UID como ID do documento
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Cadastro realizado com sucesso! Verifique seu e-mail.", Toast.LENGTH_LONG).show()
                                            user.sendEmailVerification()
                                                ?.addOnCompleteListener { verificationTask ->
                                                    if (verificationTask.isSuccessful) {
                                                        Toast.makeText(context, "E-mail de verificação enviado.", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Falha ao enviar e-mail de verificação.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            onNavigateToLogin()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Erro ao salvar dados do usuário: ${e.message}", Toast.LENGTH_LONG).show()
                                            // Se o salvamento no Firestore falhar, remove o usuário do Authentication
                                            user.delete()
                                        }
                                } else {
                                    Toast.makeText(context, "Erro: UID ou Android ID não disponíveis.", Toast.LENGTH_LONG).show()
                                    user?.delete() // Deleta o usuário do Authentication se não conseguir UID ou Android ID
                                }
                            } else {
                                val errorMessage = when (task.exception) {
                                    is FirebaseAuthException -> {
                                        when ((task.exception as FirebaseAuthException).errorCode) {
                                            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está em uso."
                                            else -> "Ocorreu um erro ao realizar o cadastro."
                                        }
                                    }
                                    else -> "Ocorreu um erro inesperado durante o cadastro."
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
            Text("Cadastrar", style = MaterialTheme.typography.labelLarge)
        }

        // Link para Login
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

// Função para obter o Android ID
fun getAndroidId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}