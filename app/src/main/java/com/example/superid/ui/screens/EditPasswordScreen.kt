package com.example.superid.ui.screens

import android.widget.Toast
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.superid.utils.EncryptionUtil
import com.example.superid.Senha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(senhaId: String, uid: String, onSenhaAtualizada: () -> Unit) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val categoriasIniciais = remember { mutableStateListOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") }

    var nome by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senhaValor by remember { mutableStateOf("") } // Esta é a senha PURA para o campo de texto
    var label by remember { mutableStateOf(categoriasIniciais.firstOrNull() ?: "") }
    var menuExpandido by remember { mutableStateOf(false) }
    var novaCategoria by remember { mutableStateOf("") }
    var mostrarCampoNovaCategoria by remember { mutableStateOf(false) }

    // Variáveis para armazenar os valores criptografados originais do Firestore
    var originalEncryptedPass: String? by remember { mutableStateOf(null) }
    var originalIv: String? by remember { mutableStateOf(null) }
    var originalSalt: String? by remember { mutableStateOf(null) }

    val masterPasswordDemo = "PinMestrePI2025!"

    LaunchedEffect(senhaId) {
        db.collection("users").document(uid).collection("passwords").document(senhaId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val senhaData = document.toObject(Senha::class.java)
                    if (senhaData != null) {
                        nome = senhaData.nome
                        login = senhaData.login
                        descricao = senhaData.descricao
                        label = senhaData.categoria
                        originalEncryptedPass = senhaData.senhaCriptografada
                        originalIv = senhaData.iv
                        originalSalt = senhaData.salt

                        // Descriptografar a senha para preencher o campo
                        if (originalEncryptedPass != null && originalIv != null && originalSalt != null) {
                            val decrypted = EncryptionUtil.decrypt(
                                originalEncryptedPass!!,
                                originalIv!!,
                                originalSalt!!,
                                masterPasswordDemo
                            )
                            senhaValor = decrypted ?: "Erro ao descriptografar"
                        } else {
                            senhaValor = "Dados de criptografia ausentes"
                        }

                    } else {
                        Toast.makeText(context, "Dados da senha inválidos", Toast.LENGTH_SHORT).show()
                        (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                    }
                } else {
                    Toast.makeText(context, "Senha não encontrada", Toast.LENGTH_SHORT).show()
                    (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao carregar senha: ${e.message}", Toast.LENGTH_SHORT).show()
                (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
            }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Barra Superior Personalizada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 43.dp)
                .padding(bottom = 24.dp)
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
                    },
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    "Editar Senha",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(60.dp))
            }
        }

        Spacer(Modifier.padding(24.dp))

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier
                    .width(315.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Login (Opcional)", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier
                    .width(315.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )
            OutlinedTextField(
                value = senhaValor,
                onValueChange = { senhaValor = it },
                label = { Text("Senha", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier
                    .width(315.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição (Opcional)", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier
                    .width(315.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )

            ExposedDropdownMenuBox(
                expanded = menuExpandido,
                onExpandedChange = { menuExpandido = !menuExpandido },
                modifier = Modifier
                    .width(315.dp)
                    .padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria", style = MaterialTheme.typography.bodyLarge) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                    modifier = Modifier
                        .menuAnchor()
                        .width(315.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    categoriasIniciais.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                label = categoria
                                menuExpandido = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Adicionar nova categoria", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            mostrarCampoNovaCategoria = true
                            menuExpandido = false
                        }
                    )
                }
            }

            if (mostrarCampoNovaCategoria) {
                OutlinedTextField(
                    value = novaCategoria,
                    onValueChange = { novaCategoria = it },
                    label = { Text("Adicionar categoria (Opcional)", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier
                        .width(315.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }

            // Botão de salvar alterações
            Button(
                onClick = {
                    if (senhaValor.isBlank()) {
                        Toast.makeText(context, "Senha é obrigatória", Toast.LENGTH_SHORT).show()
                    } else {
                        // 1. Gerar um novo salt para a criptografia da senha atualizada
                        val newSaltBytes = EncryptionUtil.generateSalt()

                        // 2. Criptografar a senha ATUAL (senhaValor) usando a senha mestra e o novo salt
                        val encryptedResult = EncryptionUtil.encrypt(senhaValor, masterPasswordDemo, newSaltBytes)

                        // Mudei o 'return@Button' para um 'if' que envolve o resto da lógica
                        if (encryptedResult != null) {
                            val (newEncryptedPass, newIv, newSaltBase64) = encryptedResult

                            val categoriaFinal = if (mostrarCampoNovaCategoria && novaCategoria.isNotBlank()) {
                                novaCategoria.trim().also { nova ->
                                    if (nova.isNotBlank() && !categoriasIniciais.contains(nova)) {
                                        categoriasIniciais.add(nova)
                                    }
                                }
                            } else {
                                label
                            }

                            // Atualizar o mapa com os novos valores criptografados
                            val updates = hashMapOf<String, Any>(
                                "nome" to nome,
                                "login" to login,
                                "descricao" to descricao,
                                "senhaCriptografada" to newEncryptedPass, // Nova senha criptografada
                                "iv" to newIv,                             // Novo IV
                                "salt" to newSaltBase64,                   // Novo Salt
                                "categoria" to categoriaFinal
                            )

                            db.collection("users").document(uid).collection("passwords").document(senhaId)
                                .update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                                    onSenhaAtualizada()
                                    (context as? ComponentActivity)?.finish() // Volta para a tela anterior
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Erro ao atualizar senha: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Se encryptedResult for null, significa que houve um erro na criptografia
                            Toast.makeText(context, "Erro ao criptografar a senha para salvar.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text("Salvar alterações", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}