package com.example.superid.ui.screens

import android.content.Context
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
fun PasswordFormScreen(
    uid: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val masterPasswordDemo = "PinMestrePI2025!"

    // SharedPreferences instance
    val sharedPreferences = remember { context.getSharedPreferences("SuperID_Prefs", Context.MODE_PRIVATE) }
    val CATEGORIES_KEY = "user_custom_categories"
    val CATEGORY_DELIMITER = "___" // Um delimitador improvável de aparecer nos nomes de categoria

    // Definir as categorias iniciais (padrão)
    val defaultCategories = remember { mutableStateListOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") }

    // Estado para as categorias do usuário (incluindo as padrão e as personalizadas)
    val userCategories = remember { mutableStateListOf<String>() }

    var nome by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senhaValor by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") } // Irá refletir a categoria selecionada
    var menuExpandido by remember { mutableStateOf(false) }
    var novaCategoria by remember { mutableStateOf("") }
    var mostrarCampoNovaCategoria by remember { mutableStateOf(false) }

    // Efeito para carregar as categorias personalizadas do SharedPreferences ao iniciar a tela
    LaunchedEffect(Unit) { // Use Unit como chave para executar apenas uma vez
        // Adiciona as categorias padrão primeiro
        userCategories.addAll(defaultCategories)

        // Carrega categorias personalizadas do SharedPreferences
        val savedCategoriesString = sharedPreferences.getString(CATEGORIES_KEY, null)
        if (!savedCategoriesString.isNullOrBlank()) {
            val customCategories = savedCategoriesString.split(CATEGORY_DELIMITER).filter { it.isNotBlank() }
            customCategories.forEach { categoryName ->
                if (!userCategories.contains(categoryName)) { // Evita duplicatas
                    userCategories.add(categoryName)
                }
            }
        }

        // Define a label inicial após carregar todas as categorias
        if (label.isBlank() && userCategories.isNotEmpty()) {
            label = userCategories.first()
        }
    }

    // Função para salvar as categorias no SharedPreferences
    fun saveCategoriesToSharedPreferences() {
        val customCategoriesToSave = userCategories.filter { it !in defaultCategories } // Salva apenas as personalizadas
        val categoriesString = customCategoriesToSave.joinToString(CATEGORY_DELIMITER)
        sharedPreferences.edit().putString(CATEGORIES_KEY, categoriesString).apply()
    }

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
                        onBack()
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
                .verticalScroll(scrollState) // Adicione este modificador
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
                    userCategories.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria, style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                label = categoria
                                mostrarCampoNovaCategoria = false
                                novaCategoria = ""
                                menuExpandido = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Adicionar nova categoria", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            mostrarCampoNovaCategoria = true
                            label = ""
                            menuExpandido = false
                        }
                    )
                }
            }

            if (mostrarCampoNovaCategoria) {
                OutlinedTextField(
                    value = novaCategoria,
                    onValueChange = { novaCategoria = it },
                    label = { Text("Nome da nova categoria", style = MaterialTheme.typography.bodyLarge) },
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
                    if (nome.isBlank()) {
                        Toast.makeText(context, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                    } else if (senhaValor.isBlank()) {
                        Toast.makeText(context, "Senha é obrigatória", Toast.LENGTH_SHORT).show()
                    } else if (label.isBlank() && !mostrarCampoNovaCategoria) {
                        Toast.makeText(context, "Por favor, selecione ou adicione uma categoria.", Toast.LENGTH_SHORT).show()
                    } else if (mostrarCampoNovaCategoria && novaCategoria.isBlank()) {
                        Toast.makeText(context, "Nome da nova categoria é obrigatório.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val categoriaFinal = if (mostrarCampoNovaCategoria && novaCategoria.isNotBlank()) {
                            novaCategoria.trim()
                        } else {
                            label
                        }

                        if (mostrarCampoNovaCategoria && novaCategoria.isNotBlank()) {
                            if (!userCategories.contains(categoriaFinal)) {
                                userCategories.add(categoriaFinal)
                                saveCategoriesToSharedPreferences()
                                Toast.makeText(context, "Nova categoria '${categoriaFinal}' adicionada localmente!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Categoria '${categoriaFinal}' já existe.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val saltBytes = EncryptionUtil.generateSalt()

                        val encryptedResult = EncryptionUtil.encrypt(senhaValor, masterPasswordDemo, saltBytes)

                        var encryptedPass: String? = null
                        var iv: String? = null
                        var saltBase64: String? = null

                        if (encryptedResult != null) {
                            val (tempEncryptedPass, tempIv, tempSaltBase64) = encryptedResult
                            encryptedPass = tempEncryptedPass
                            iv = tempIv
                            saltBase64 = tempSaltBase64
                        } else {
                            Toast.makeText(context, "Erro ao criptografar a senha.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (encryptedPass != null && iv != null && saltBase64 != null) {
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

                            db.collection("users").document(uid).collection("passwords")
                                .add(novaSenha)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Senha cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                                    onSuccess()
                                    nome = ""
                                    login = ""
                                    descricao = ""
                                    label = userCategories.firstOrNull() ?: ""
                                    senhaValor = ""
                                    novaCategoria = ""
                                    mostrarCampoNovaCategoria = false
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Erro ao cadastrar senha: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
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