package com.example.superid.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility // Ícone de olho aberto
import androidx.compose.material.icons.filled.VisibilityOff // Ícone de olho fechado


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(
    uid: String,
    senhaToEdit: Senha?,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val masterPasswordDemo = "PinMestrePI2025!"

    val sharedPreferences = remember { context.getSharedPreferences("SuperID_Prefs", Context.MODE_PRIVATE) }
    val CATEGORIES_KEY = "user_custom_categories"
    val CATEGORY_DELIMITER = "___"

    val defaultCategories = remember { mutableStateListOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") }

    val userCategories = remember { mutableStateListOf<String>() }

    var nome by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senhaValor by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var menuExpandido by remember { mutableStateOf(false) }
    var novaCategoria by remember { mutableStateOf("") }
    var mostrarCampoNovaCategoria by remember { mutableStateOf(false) }

    // Variável de estado para controlar a visibilidade da senha
    var passwordVisible by remember { mutableStateOf(false) }

    var originalEncryptedPass: String? by remember { mutableStateOf(null) }
    var originalIv: String? by remember { mutableStateOf(null) }
    var originalSalt: String? by remember { mutableStateOf(null) }

    // Variáveis de estado para controlar a visibilidade do diálogo de confirmação de exclusão de categoria
    var showDeleteCategoryConfirmationDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf("") }

    fun saveCategoriesToSharedPreferences() {
        val customCategoriesToSave = userCategories.filter { it !in defaultCategories }
        val categoriesString = customCategoriesToSave.joinToString(CATEGORY_DELIMITER)
        sharedPreferences.edit().putString(CATEGORIES_KEY, categoriesString).apply()
    }

    // Função para deletar uma categoria
    fun deleteCategory(categoryName: String) {
        if (categoryName == "Sites Web") {
            Toast.makeText(context, "A categoria 'Sites Web' não pode ser excluída.", Toast.LENGTH_LONG).show()
        } else {
            // Verifica se a categoria existe e a remove
            if (userCategories.remove(categoryName)) {
                saveCategoriesToSharedPreferences()
                Toast.makeText(context, "Categoria '$categoryName' excluída com sucesso!", Toast.LENGTH_SHORT).show()
                // Se a categoria excluída era a selecionada, seleciona a primeira disponível
                if (label == categoryName) {
                    label = userCategories.firstOrNull() ?: ""
                }
            } else {
                Toast.makeText(context, "Erro: Categoria '$categoryName' não encontrada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        userCategories.addAll(defaultCategories)

        val savedCategoriesString = sharedPreferences.getString(CATEGORIES_KEY, null)
        if (!savedCategoriesString.isNullOrBlank()) {
            val customCategories = savedCategoriesString.split(CATEGORY_DELIMITER).filter { it.isNotBlank() }
            customCategories.forEach { categoryName ->
                if (!userCategories.contains(categoryName)) {
                    userCategories.add(categoryName)
                }
            }
        }
    }

    LaunchedEffect(senhaToEdit?.id) {
        if (senhaToEdit?.id == null) {
            Toast.makeText(context, "Senha para edição não fornecida.", Toast.LENGTH_SHORT).show()
            onBack()
            return@LaunchedEffect
        }

        db.collection("users").document(uid).collection("passwords").document(senhaToEdit.id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val senhaData = document.toObject(Senha::class.java)
                    if (senhaData != null) {
                        nome = senhaData.nome
                        login = senhaData.login
                        descricao = senhaData.descricao
                        label = senhaData.categoria

                        // Garante que a categoria da senha editada está na lista de categorias do usuário
                        if (!userCategories.contains(label)) {
                            userCategories.add(label)
                        }

                        originalEncryptedPass = senhaData.senhaCriptografada
                        originalIv = senhaData.iv
                        originalSalt = senhaData.salt

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
                        onBack()
                    }
                } else {
                    Toast.makeText(context, "Senha não encontrada", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao carregar senha: ${e.message}", Toast.LENGTH_SHORT).show()
                onBack()
            }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                .imePadding()
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
            // ---- CAMPO DA SENHA COM O OLHINHO ----
            OutlinedTextField(
                value = senhaValor,
                onValueChange = { senhaValor = it },
                label = { Text("Senha", style = MaterialTheme.typography.bodyLarge) },
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
                            },
                            // Adiciona um ícone de exclusão para categorias que podem ser excluídas
                            trailingIcon = {
                                if (categoria != "Sites Web") { // Apenas mostra o ícone para categorias que não são "Sites Web"
                                    IconButton(onClick = {
                                        categoryToDelete = categoria
                                        showDeleteCategoryConfirmationDialog = true
                                        menuExpandido = false // Fecha o menu
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir categoria",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
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

            // Diálogo de confirmação de exclusão de categoria
            if (showDeleteCategoryConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteCategoryConfirmationDialog = false },
                    title = { Text("Confirmar Exclusão") },
                    text = { Text("Tem certeza que deseja excluir a categoria '${categoryToDelete}'? Senhas associadas a esta categoria não serão afetadas, apenas a categoria será removida da sua lista.") },
                    confirmButton = {
                        // Envolve a Row em um Box com Modifier.fillMaxWidth() para centralizar
                        Box(
                            modifier = Modifier.fillMaxWidth(), // Este Box se expande
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Botão "Cancelar"
                                TextButton(
                                    onClick = { showDeleteCategoryConfirmationDialog = false }
                                ) {
                                    Text("Cancelar")
                                }
                                // Botão "Excluir" (agora o segundo)
                                Button(
                                    onClick = {
                                        deleteCategory(categoryToDelete)
                                        showDeleteCategoryConfirmationDialog = false
                                    }
                                ) {
                                    Text("Excluir")
                                }
                            }
                        }
                    }
                )
            }


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
                                Toast.makeText(context, "Nova categoria '${categoriaFinal}' adicionada!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Categoria '${categoriaFinal}' já existe.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val newSaltBytes = EncryptionUtil.generateSalt()

                        val encryptedResult = EncryptionUtil.encrypt(senhaValor, masterPasswordDemo, newSaltBytes)

                        if (encryptedResult != null) {
                            val (newEncryptedPass, newIv, newSaltBase64) = encryptedResult

                            val updates = hashMapOf<String, Any>(
                                "nome" to nome,
                                "login" to login,
                                "descricao" to descricao,
                                "senhaCriptografada" to newEncryptedPass,
                                "iv" to newIv,
                                "salt" to newSaltBase64,
                                "categoria" to categoriaFinal
                            )

                            db.collection("users").document(uid).collection("passwords").document(senhaToEdit?.id!!)
                                .update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Erro ao atualizar senha: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Erro ao criptografar a senha para salvar.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text("Salvar alterações", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}