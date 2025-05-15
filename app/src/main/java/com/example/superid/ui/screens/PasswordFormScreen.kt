package com.example.superid.ui.screens

import android.content.Intent
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.superid.Senha
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordFormScreen(uid: String, onSenhaSalva: () -> Unit) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val categoriasDisponiveis = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico", "Outros")
    var senhaCriada by remember { mutableStateOf("") }

    var login by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senhaValor by remember { mutableStateOf("") }
    var label by remember { mutableStateOf(categoriasDisponiveis.firstOrNull() ?: "") }
    var menuExpandido by remember { mutableStateOf(false) }

    fun gerarAccessToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Barra Superior Personalizada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 33.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                    }
                    , modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    "Criar Senha", // Título da tela
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(48.dp)) // Espaço para alinhar o título
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Login", style = MaterialTheme.typography.labelLarge) },
                modifier = Modifier
                    .width(315.dp)
                    .padding(bottom = 8.dp),
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
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("URL", style = MaterialTheme.typography.labelLarge) },
                modifier = Modifier
                    .width(315.dp)
                    .padding(bottom = 8.dp),
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
            OutlinedTextField(
                value = senhaValor,
                onValueChange = { senhaValor = it },
                label = { Text("Senha", style = MaterialTheme.typography.labelLarge) },
                modifier = Modifier
                    .width(315.dp)
                    .padding(bottom = 16.dp),
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

            ExposedDropdownMenuBox(
                expanded = menuExpandido,
                onExpandedChange = { menuExpandido = !menuExpandido },
                modifier = Modifier
                    .width(315.dp)
                    .padding(bottom = 24.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria", style = MaterialTheme.typography.labelLarge) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                    modifier = Modifier
                        .menuAnchor()
                        .width(315.dp),
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

                ExposedDropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    categoriasDisponiveis.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                label = categoria
                                menuExpandido = false
                            }
                        )
                    }
                }
            }

            // Botão de criação da senha
            Button(
                onClick = {
                    val novaSenha = Senha(
                        categoria = label,
                        login = login,
                        descricao = descricao,
                        senhaCriptografada = senhaValor,
                        accessToken = gerarAccessToken()
                    )

                    // Salvar no Firestore
                    db.collection("users").document(uid).collection("passwords")
                        .add(novaSenha)
                        .addOnSuccessListener {
                            senhaCriada = "Senha para $login foi criada com sucesso!"
                            onSenhaSalva() // Invoca o callback após salvar a senha

                            // Enviar dados para PasswordManagerScreen usando Intent
                            val intent = Intent()
                            intent.putExtra("senhaCriptografada", senhaValor) // Corrigido para senhaValor
                            intent.putExtra("login", login)
                            intent.putExtra("descricao", descricao)
                            intent.putExtra("categoria", label)
                            intent.putExtra("uid", uid)
                            (context as? ComponentActivity)?.setResult(android.app.Activity.RESULT_OK, intent) // ActivityResult
                            (context as? ComponentActivity)?.finish() // Encerra esta activity e volta para a anterior

                        }
                        .addOnFailureListener { e ->
                            senhaCriada = "Erro ao criar senha: ${e.message}"
                        }

                    // Limpar os campos após salvar
                    login = ""
                    descricao = ""
                    label = categoriasDisponiveis.firstOrNull() ?: ""
                    senhaValor = ""
                },
                modifier = Modifier
                    .width(161.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Criar", style = MaterialTheme.typography.labelLarge)
            }

            // Exibição do feedback da senha criada
            if (senhaCriada.isNotEmpty()) {
                Text(
                    text = senhaCriada,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}