package com.example.superid.ui.screens

import android.util.Base64
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
import com.example.superid.Senha
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import com.example.superid.utils.EncryptionUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordFormScreen(uid: String, onSenhaSalva: () -> Unit) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val categoriasIniciais = remember { mutableStateListOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") }
    var nome by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senhaValor by remember { mutableStateOf("") }
    var label by remember { mutableStateOf(categoriasIniciais.firstOrNull() ?: "") }
    var menuExpandido by remember { mutableStateOf(false) }
    var novaCategoria by remember { mutableStateOf("") }
    var mostrarCampoNovaCategoria by remember { mutableStateOf(false) }
    val masterPasswordDemo = "PinMestrePI2025!"

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
                    "Adicionar Senha",
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

            // Botão de criação da senha
            Button(
                onClick = {
                    if (senhaValor.isBlank()) {
                        Toast.makeText(context, "Senha é obrigatória", Toast.LENGTH_SHORT).show()
                    } else {
                        // 1. Gerar um salt para a derivação da chave PBE
                        val saltBytes = EncryptionUtil.generateSalt()

                        // 2. Criptografar a senha usando a senha mestra (masterPasswordDemo) e o salt
                        val encryptedResult = EncryptionUtil.encrypt(senhaValor, masterPasswordDemo, saltBytes)

                        var encryptedPass: String? = null
                        var iv: String? = null
                        var saltBase64: String? = null

                        if (encryptedResult != null) {
                            // Desestruturar e atribuir aos val's declarados acima
                            val (tempEncryptedPass, tempIv, tempSaltBase64) = encryptedResult
                            encryptedPass = tempEncryptedPass
                            iv = tempIv
                            saltBase64 = tempSaltBase64
                        } else {
                            // Se encryptedResult for null, significa que houve um erro na criptografia
                            Toast.makeText(context, "Erro ao criptografar a senha.", Toast.LENGTH_SHORT).show()
                        }

                        // Verificar novamente se a criptografia foi bem-sucedida antes de criar o objeto Senha
                        if (encryptedPass != null && iv != null && saltBase64 != null) {

                            // Mover a lógica de categoriaFinal para ser parte do fluxo de sucesso
                            val categoriaFinal = if (mostrarCampoNovaCategoria && novaCategoria.isNotBlank()) {
                                novaCategoria.trim().also { nova ->
                                    if (nova.isNotBlank() && !categoriasIniciais.contains(nova)) {
                                        categoriasIniciais.add(nova)
                                    }
                                }
                            } else {
                                label
                            }

                            val novaSenha = Senha(
                                categoria = categoriaFinal,
                                login = login,
                                descricao = descricao,
                                senhaCriptografada = encryptedPass,
                                iv = iv,
                                salt = saltBase64,
                                accessToken = gerarAccessToken(),
                                nome = nome
                            )

                            // Salvar no Firestore
                            db.collection("users").document(uid).collection("passwords")
                                .add(novaSenha)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Senha cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                                    onSenhaSalva()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Erro ao cadastrar senha: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        // Limpar campos SEMPRE, independentemente do sucesso da criptografia/salvamento
                        nome = ""
                        login = ""
                        descricao = ""
                        label = categoriasIniciais.firstOrNull() ?: ""
                        senhaValor = ""
                        novaCategoria = ""
                        mostrarCampoNovaCategoria = false
                    }
                },
                modifier = Modifier
                    .width(161.dp)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text("Cadastrar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

