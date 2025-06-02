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
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @param sharedPreferences Instância de [SharedPreferences] para gerenciar o estado da sessão.
 * @param onNavigateToRegister Callback para navegar para a tela de registro.
 * @param onLoginSuccess Callback para executar após um login bem-sucedido.
 * @param onNavigateToForgotPassword Callback para navegar para a tela de recuperação de senha.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
    sharedPreferences: SharedPreferences,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current // Obtém o contexto atual para exibir Toasts.
    var email by remember { mutableStateOf("") } // Estado para o campo de e-mail.
    var senha by remember { mutableStateOf("") } // Estado para o campo de senha.
    var emailError by remember { mutableStateOf(false) } // Estado para erro de validação do e-mail.
    var senhaError by remember { mutableStateOf(false) } // Estado para erro de validação da senha.
    val auth = FirebaseAuth.getInstance() // Instância do Firebase Authentication.
    val db = FirebaseFirestore.getInstance() // Instância do Firebase Firestore.

    // Estado para controlar a visibilidade do diálogo de e-mail não verificado.
    var showEmailVerificationDialog by remember { mutableStateOf(false) }

    // Estado para controlar a visibilidade da senha no campo de texto (ícone de "olho").
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState() // Estado para permitir rolagem da tela.

    /**
     * Reenvia o e-mail de verificação para o usuário logado atualmente.
     * Exibe um Toast com o resultado da operação.
     */
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
            .imePadding() // Ajusta o layout para o teclado virtual.
            .verticalScroll(scrollState), // Permite rolagem do conteúdo.
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Container Superior Personalizado com Título e Logo ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                .padding(vertical = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            // Botão de voltar para a tela de Cadastro.
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
        // Título da tela de login.
        Text(
            text = "Entre na sua conta",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 30.dp)
        )

        // --- Campo de Entrada de E-mail ---
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
        // Mensagem de erro para o e-mail.
        if (emailError) {
            Text(text = "Por favor, digite seu e-mail.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        // --- Campo de Entrada de Senha com Controle de Visibilidade ---
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Digite sua senha", style = MaterialTheme.typography.labelLarge) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
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
        // Mensagem de erro para a senha.
        if (senhaError) {
            Text(text = "Sua senha contém no mínimo 8 caracteres.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        // Botão para navegar para a recuperação de senha.
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

        // --- Botão de Entrar ---
        Button(
            onClick = {
                var hasValidationError = false // Flag para controle de erros de validação.

                // Validação do campo de e-mail.
                if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = true
                    hasValidationError = true
                } else {
                    emailError = false
                }

                // Validação do campo de senha.
                if (senha.length < 8 || senha.isBlank()) {
                    senhaError = true
                    hasValidationError = true
                } else {
                    senhaError = false
                }

                // Se não houver erros de validação, tenta fazer login no Firebase.
                if (!hasValidationError) {
                    auth.signInWithEmailAndPassword(email, senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null) {
                                    if (!user.isEmailVerified) {
                                        // Se o e-mail não estiver verificado, exibe o diálogo de verificação.
                                        showEmailVerificationDialog = true
                                    } else {
                                        // Se o e-mail estiver verificado, atualiza o Firestore e prossegue.
                                        user.uid?.let { uid ->
                                            val userDocRef = db.collection("users").document(uid)
                                            userDocRef.update("isEmailVerified", true)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_LONG).show()
                                                    sharedPreferences.edit().putBoolean("emailValidado", true).apply() // Marca e-mail como validado.
                                                    onLoginSuccess() // Callback de sucesso de login.
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Erro ao atualizar status de verificação de e-mail: ${e.message}", Toast.LENGTH_LONG).show()
                                                    onLoginSuccess() // Ainda permite prosseguir mesmo com erro de atualização.
                                                }
                                        } ?: run {
                                            Toast.makeText(context, "Login realizado com sucesso! Mas não foi possível atualizar o status de verificação.", Toast.LENGTH_LONG).show()
                                            onLoginSuccess()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Erro: Usuário não encontrado após login.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                // Lida com erros de login do Firebase Authentication.
                                val errorMessage = when (task.exception) {
                                    is FirebaseAuthException -> {
                                        when ((task.exception as FirebaseAuthException).errorCode) {
                                            "ERROR_WRONG_PASSWORD" -> "Senha incorreta."
                                            "ERROR_USER_DISABLED" -> "Esta conta de usuário foi desativada."
                                            "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas de login. Tente novamente mais tarde."
                                            "ERROR_USER_NOT_FOUND" -> "Usuário não encontrado."
                                            else -> "Ocorreu um erro inesperado. Verifique sua senha e e-mail"
                                        }
                                    }
                                    else -> "Ocorreu um erro inesperado. Verifique sua senha e e-mail"
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

        // --- Diálogo de Verificação de E-mail Pendente ---
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Botão para reenviar o e-mail de verificação.
                        Button(onClick = {
                            resendVerificationEmail()
                            showEmailVerificationDialog = false
                        }) {
                            Text("Reenviar")
                        }
                        Spacer(Modifier.width(30.dp))
                        // Botão para prosseguir mesmo sem verificação (o status no Firestore não será atualizado).
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

        // --- Link para Cadastrar-se ---
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