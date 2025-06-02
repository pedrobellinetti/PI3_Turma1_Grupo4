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

/**
 * Tela para editar uma senha existente no aplicativo.
 * Permite ao usuário modificar o nome, login, descrição, valor da senha e categoria de uma entrada.
 * A senha é descriptografada para edição e recriptografada ao ser salva.
 * Também gerencia categorias personalizadas de senhas.
 *
 * @param uid O User ID do Firebase do usuário logado.
 * @param senhaToEdit O objeto [Senha] a ser editado.
 * @param onSuccess Callback a ser executado após o salvamento bem-sucedido.
 * @param onBack Callback para retornar à tela anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(
    uid: String,
    senhaToEdit: Senha?,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current // Contexto do aplicativo para operações como SharedPreferences e Toast.
    val db = Firebase.firestore // Instância do Firebase Firestore.
    val masterPasswordDemo = "PinMestrePI2025!"

    // SharedPreferences para armazenar e recuperar categorias customizadas do usuário.
    val sharedPreferences = remember { context.getSharedPreferences("SuperID_Prefs", Context.MODE_PRIVATE) }
    val CATEGORIES_KEY = "user_custom_categories" // Chave para armazenar categorias no SharedPreferences.
    val CATEGORY_DELIMITER = "___" // Delimitador para separar categorias no SharedPreferences.

    // Lista de categorias padrão, não mutável pelo usuário.
    val defaultCategories = remember { mutableStateListOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") }
    // Lista de categorias do usuário, incluindo as padrão e as customizadas.
    val userCategories = remember { mutableStateListOf<String>() }

    // Estados para os campos de entrada da senha.
    var nome by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senhaValor by remember { mutableStateOf("") } // Valor da senha descriptografada para edição.
    var label by remember { mutableStateOf("") } // Categoria selecionada.
    var menuExpandido by remember { mutableStateOf(false) } // Estado do dropdown de categorias.
    var novaCategoria by remember { mutableStateOf("") } // Nome para uma nova categoria.
    var mostrarCampoNovaCategoria by remember { mutableStateOf(false) } // Visibilidade do campo para nova categoria.

    // Estado para controlar a visibilidade da senha no campo de texto.
    var passwordVisible by remember { mutableStateOf(false) }

    // Variáveis para armazenar os dados de criptografia originais da senha.
    var originalEncryptedPass: String? by remember { mutableStateOf(null) }
    var originalIv: String? by remember { mutableStateOf(null) }
    var originalSalt: String? by remember { mutableStateOf(null) }

    // Estados para controlar o diálogo de confirmação de exclusão de categoria.
    var showDeleteCategoryConfirmationDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf("") } // Categoria a ser excluída no diálogo.

    /**
     * Salva as categorias personalizadas do usuário no SharedPreferences.
     * Filtra as categorias padrão para salvar apenas as customizadas.
     */
    fun saveCategoriesToSharedPreferences() {
        val customCategoriesToSave = userCategories.filter { it !in defaultCategories }
        val categoriesString = customCategoriesToSave.joinToString(CATEGORY_DELIMITER)
        sharedPreferences.edit().putString(CATEGORIES_KEY, categoriesString).apply()
    }

    /**
     * Deleta uma categoria personalizada da lista do usuário.
     * Impede a exclusão da categoria "Sites Web".
     *
     * @param categoryName O nome da categoria a ser deletada.
     */
    fun deleteCategory(categoryName: String) {
        if (categoryName == "Sites Web") {
            Toast.makeText(context, "A categoria 'Sites Web' não pode ser excluída.", Toast.LENGTH_LONG).show()
        } else {
            // Remove a categoria da lista e atualiza o SharedPreferences.
            if (userCategories.remove(categoryName)) {
                saveCategoriesToSharedPreferences()
                Toast.makeText(context, "Categoria '$categoryName' excluída com sucesso!", Toast.LENGTH_SHORT).show()
                // Se a categoria excluída era a selecionada, seleciona a primeira disponível.
                if (label == categoryName) {
                    label = userCategories.firstOrNull() ?: ""
                }
            } else {
                Toast.makeText(context, "Erro: Categoria '$categoryName' não encontrada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Carrega as categorias salvas do SharedPreferences ao iniciar o Composable.
    LaunchedEffect(Unit) {
        userCategories.addAll(defaultCategories) // Adiciona categorias padrão.

        val savedCategoriesString = sharedPreferences.getString(CATEGORIES_KEY, null)
        if (!savedCategoriesString.isNullOrBlank()) {
            // Adiciona categorias customizadas salvas, evitando duplicatas.
            val customCategories = savedCategoriesString.split(CATEGORY_DELIMITER).filter { it.isNotBlank() }
            customCategories.forEach { categoryName ->
                if (!userCategories.contains(categoryName)) {
                    userCategories.add(categoryName)
                }
            }
        }
    }

    // Carrega os dados da senha a ser editada do Firestore.
    LaunchedEffect(senhaToEdit?.id) {
        if (senhaToEdit?.id == null) {
            Toast.makeText(context, "Senha para edição não fornecida.", Toast.LENGTH_SHORT).show()
            onBack()
        } else {
            db.collection("users").document(uid).collection("passwords").document(senhaToEdit.id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val senhaData = document.toObject(Senha::class.java)
                        if (senhaData != null) {
                            // Popula os campos com os dados da senha.
                            nome = senhaData.nome
                            login = senhaData.login
                            descricao = senhaData.descricao
                            label = senhaData.categoria

                            // Garante que a categoria da senha editada esteja na lista de categorias do usuário.
                            if (!userCategories.contains(label)) {
                                userCategories.add(label)
                            }

                            // Armazena os dados de criptografia originais.
                            originalEncryptedPass = senhaData.senhaCriptografada
                            originalIv = senhaData.iv
                            originalSalt = senhaData.salt

                            // Descriptografa a senha para exibição no campo de texto.
                            if (originalEncryptedPass != null && originalIv != null && originalSalt != null) {
                                val decrypted = EncryptionUtil.decrypt(
                                    originalEncryptedPass!!,
                                    originalIv!!,
                                    originalSalt!!,
                                    masterPasswordDemo
                                )
                                senhaValor = decrypted ?: "Erro ao descriptografar" // Exibe erro se a descriptografia falhar.
                            } else {
                                senhaValor = "Dados de criptografia ausentes" // Mensagem se dados essenciais estiverem faltando.
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
                    Toast.makeText(
                        context,
                        "Erro ao carregar senha: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    onBack()
                }
        }
    }

    val scrollState = rememberScrollState() // Estado de rolagem para a tela.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Top Bar Personalizada ---
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
                    onClick = { onBack() }, // Botão para voltar.
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
                    modifier = Modifier.weight(1f) // Centraliza o texto.
                )
                Spacer(modifier = Modifier.width(60.dp)) // Espaçador para balancear o layout.
            }
        }

        Spacer(Modifier.padding(24.dp))

        // --- Campos de Edição da Senha ---
        Column(
            modifier = Modifier
                .imePadding() // Ajusta o padding para o teclado virtual.
                .verticalScroll(scrollState) // Permite rolagem.
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.width(315.dp).padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Login (Opcional)", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.width(315.dp).padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )
            // Campo da Senha com controle de visibilidade.
            OutlinedTextField(
                value = senhaValor,
                onValueChange = { senhaValor = it },
                label = { Text("Senha", style = MaterialTheme.typography.bodyLarge) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.width(315.dp).padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição (Opcional)", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.width(315.dp).padding(bottom = 16.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )

            // --- Dropdown para Seleção/Adição de Categoria ---
            ExposedDropdownMenuBox(
                expanded = menuExpandido,
                onExpandedChange = { menuExpandido = !menuExpandido },
                modifier = Modifier.width(315.dp).padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria", style = MaterialTheme.typography.bodyLarge) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                    modifier = Modifier.menuAnchor().width(315.dp),
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
                            // Ícone de exclusão para categorias customizáveis.
                            trailingIcon = {
                                if (categoria != "Sites Web") { // "Sites Web" não pode ser excluído.
                                    IconButton(onClick = {
                                        categoryToDelete = categoria
                                        showDeleteCategoryConfirmationDialog = true // Abre o diálogo de confirmação.
                                        menuExpandido = false // Fecha o menu.
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
                    // Opção para adicionar nova categoria.
                    DropdownMenuItem(
                        text = { Text("Adicionar nova categoria", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            mostrarCampoNovaCategoria = true
                            label = "" // Limpa a categoria selecionada.
                            menuExpandido = false
                        }
                    )
                }
            }

            // Campo para digitar o nome da nova categoria, se ativado.
            if (mostrarCampoNovaCategoria) {
                OutlinedTextField(
                    value = novaCategoria,
                    onValueChange = { novaCategoria = it },
                    label = { Text("Nome da nova categoria", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier.width(315.dp).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }

            // --- Diálogo de Confirmação de Exclusão de Categoria ---
            if (showDeleteCategoryConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteCategoryConfirmationDialog = false },
                    title = { Text("Confirmar Exclusão") },
                    text = { Text("Tem certeza que deseja excluir a categoria '${categoryToDelete}'? Senhas associadas a esta categoria não serão afetadas, apenas a categoria será removida da sua lista.") },
                    confirmButton = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(onClick = { showDeleteCategoryConfirmationDialog = false }) {
                                    Text("Cancelar")
                                }
                                Button(onClick = {
                                    deleteCategory(categoryToDelete) // Chama a função de exclusão.
                                    showDeleteCategoryConfirmationDialog = false // Fecha o diálogo.
                                }) {
                                    Text("Excluir")
                                }
                            }
                        }
                    }
                )
            }

            // --- Botão de Salvar Alterações ---
            Button(
                onClick = {
                    // Validações dos campos antes de salvar.
                    if (nome.isBlank()) {
                        Toast.makeText(context, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                    } else if (senhaValor.isBlank()) {
                        Toast.makeText(context, "Senha é obrigatória", Toast.LENGTH_SHORT).show()
                    } else if (label.isBlank() && !mostrarCampoNovaCategoria) {
                        Toast.makeText(context, "Por favor, selecione ou adicione uma categoria.", Toast.LENGTH_SHORT).show()
                    } else if (mostrarCampoNovaCategoria && novaCategoria.isBlank()) {
                        Toast.makeText(context, "Nome da nova categoria é obrigatório.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Determina a categoria final a ser usada.
                        val categoriaFinal = if (mostrarCampoNovaCategoria && novaCategoria.isNotBlank()) {
                            novaCategoria.trim()
                        } else {
                            label
                        }

                        // Adiciona a nova categoria à lista do usuário, se aplicável.
                        if (mostrarCampoNovaCategoria && novaCategoria.isNotBlank()) {
                            if (!userCategories.contains(categoriaFinal)) {
                                userCategories.add(categoriaFinal)
                                saveCategoriesToSharedPreferences()
                                Toast.makeText(context, "Nova categoria '${categoriaFinal}' adicionada!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Categoria '${categoriaFinal}' já existe.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val newSaltBytes = EncryptionUtil.generateSalt() // Gera um novo salt para a recriptografia.
                        val encryptedResult = EncryptionUtil.encrypt(senhaValor, masterPasswordDemo, newSaltBytes) // Recriptografa a senha.

                        if (encryptedResult != null) {
                            val (newEncryptedPass, newIv, newSaltBase64) = encryptedResult

                            // Cria um mapa de atualizações para o Firestore.
                            val updates = hashMapOf<String, Any>(
                                "nome" to nome,
                                "login" to login,
                                "descricao" to descricao,
                                "senhaCriptografada" to newEncryptedPass,
                                "iv" to newIv,
                                "salt" to newSaltBase64,
                                "categoria" to categoriaFinal
                            )

                            // Atualiza o documento da senha no Firestore.
                            db.collection("users").document(uid).collection("passwords").document(senhaToEdit?.id!!)
                                .update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                                    onSuccess() // Callback de sucesso.
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