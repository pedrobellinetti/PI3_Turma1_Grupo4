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
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Tela de Recuperação de Senha.
 * Permite que o usuário solicite um link de redefinição de senha para seu e-mail.
 * Inclui validação do status de verificação do e-mail no Firestore
 * antes de enviar o link de redefinição.
 *
 * @param sharedPreferences Instância de [SharedPreferences] para armazenar o status de redefinição de senha.
 * @param onNavigateToLogin Callback para navegar de volta à tela de login.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordRecoveryScreen(
    sharedPreferences: SharedPreferences,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current // Obtém o contexto atual para exibir Toasts.
    var email by remember { mutableStateOf("") } // Estado para o campo de e-mail.
    var status by remember { mutableStateOf("") } // Estado para mensagens de status (feedback).
    val db = FirebaseFirestore.getInstance() // Instância do Firestore para interagir com o banco de dados.

    // Estado para controlar a visibilidade do diálogo de e-mail não verificado.
    var showUnverifiedEmailDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Barra Superior Personalizada com Logo e Título ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 33.dp)
        ) {
            // Botão de voltar para a tela de Login.
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

            // Conteúdo centralizado na barra superior: Título do App e Logo.
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

        // --- Título da Tela de Recuperação ---
        Text(
            text = "Recuperar Senha",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 30.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // --- Campo de Entrada de E-mail ---
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

        // --- Botão de Recuperar Senha ---
        Button(
            onClick = {
                if (email.isNotBlank()) {
                    // Consulta o Firestore para verificar se o e-mail está cadastrado e verificado.
                    db.collection("users")
                        .whereEqualTo("email", email.trim())
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                // E-mail não encontrado no Firestore.
                                // Para segurança, o Firebase Auth não diferencia e-mail inexistente de não verificado.
                                Toast.makeText(
                                    context,
                                    "Se o e-mail estiver correto, pode não haver conta associada a ele ou não verificada.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                // E-mail encontrado, obtém o status de verificação.
                                val userDoc = documents.first()
                                val isEmailVerified = userDoc.getBoolean("isEmailVerified") ?: false

                                if (isEmailVerified) {
                                    // Se o e-mail estiver verificado, envia o link de redefinição de senha via Firebase Auth.
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // Marca no SharedPreferences que a senha foi redefinida.
                                                sharedPreferences.edit().putBoolean("senhaRedefinida", true).apply()
                                                Toast.makeText(
                                                    context,
                                                    "Enviamos um link para seu e-mail para redefinir a senha.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onNavigateToLogin() // Navega de volta para a tela de login.
                                            } else {
                                                // Lida com erros específicos do Firebase Auth durante o envio do e-mail.
                                                val errorMessage = when (task.exception) {
                                                    is FirebaseAuthException -> {
                                                        when ((task.exception as FirebaseAuthException).errorCode) {
                                                            "ERROR_INVALID_EMAIL" -> "Formato de e-mail inválido."
                                                            "ERROR_USER_NOT_FOUND" -> "Nenhuma conta encontrada com este e-mail."
                                                            else -> "Ocorreu um erro ao enviar o e-mail: ${task.exception?.message}"
                                                        }
                                                    }
                                                    else -> "Erro inesperado. Verifique o e-mail digitado e tente novamente."
                                                }
                                                Toast.makeText(context, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                } else {
                                    // Se o e-mail não estiver verificado, exibe um diálogo de aviso.
                                    showUnverifiedEmailDialog = true
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            // Lida com falhas na consulta ao Firestore.
                            Toast.makeText(context, "Erro ao verificar o e-mail: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    // Solicita ao usuário que digite o e-mail.
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

        // --- Mensagem de Status (se houver) ---
        if (status.isNotEmpty()) {
            Text(text = status, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Diálogo para E-mail Não Verificado ---
        if (showUnverifiedEmailDialog) {
            AlertDialog(
                onDismissRequest = { showUnverifiedEmailDialog = false }, // Fecha o diálogo ao clicar fora.
                title = { Text("E-mail Não Verificado") },
                text = {
                    Text("Não é possível redefinir a senha para e-mails que não foram verificados. Por favor, verifique seu e-mail para prosseguir. Caso precise de um novo e-mail de verificação, faça login novamente e clique em 'Reenviar' ")
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = { showUnverifiedEmailDialog = false }) {
                            Text("Entendi")
                        }
                    }
                }
            )
        }

        // --- Link para Voltar ao Login ---
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
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}