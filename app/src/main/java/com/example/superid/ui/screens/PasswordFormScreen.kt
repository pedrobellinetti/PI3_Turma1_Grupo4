package com.example.superid.ui.screens

import android.content.Context
import android.util.Base64
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
import com.example.superid.Senha
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import com.example.superid.utils.EncryptionUtil
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.Delete

/**
 * ## PasswordFormScreen Composable
 *
 * Esta tela permite ao usuário adicionar uma nova senha.
 * Ela inclui campos para nome, login, descrição, o valor da senha e uma categoria.
 * A senha é criptografada antes de ser salva no Firestore. O usuário pode
 * selecionar categorias existentes ou criar novas.
 *
 * @param uid O User ID do Firebase do usuário logado, usado para armazenar a senha em sua coleção.
 * @param onSuccess Callback a ser executado após o cadastro bem-sucedido da senha.
 * @param onBack Callback para retornar à tela anterior (Home).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordFormScreen(
    uid: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current // Contexto do aplicativo para operações como SharedPreferences e Toast.
    val db = Firebase.firestore // Instância do Firebase Firestore.
    val masterPasswordDemo = "PinMestrePI2025!"

    // SharedPreferences para gerenciar categorias personalizadas do usuário.
    val sharedPreferences = remember { context.getSharedPreferences("SuperID_Prefs", Context.MODE_PRIVATE) }
    val CATEGORIES_KEY = "user_custom_categories" // Chave para as categorias customizadas.
    val CATEGORY_DELIMITER = "___" // Delimitador para salvar/carregar categorias como uma string.

    // Lista de categorias padrão (não podem ser excluídas).
    val defaultCategories = remember { mutableStateListOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") }
    // Lista mutável de categorias do usuário, incluindo as padrão e as personalizadas.
    val userCategories = remember { mutableStateListOf<String>() }

    // Estados para os campos de entrada da senha.
    var nome by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senhaValor by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") } // Categoria selecionada no dropdown.
    var menuExpandido by remember { mutableStateOf(false) } // Estado do dropdown de categorias.
    var novaCategoria by remember { mutableStateOf("") } // Nome para uma nova categoria a ser adicionada.
    var mostrarCampoNovaCategoria by remember { mutableStateOf(false) } // Controla a visibilidade do campo "nova categoria".

    // Estados para o diálogo de confirmação de exclusão de categoria.
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf("") } // Armazena o nome da categoria a ser excluída.

    // Efeito colateral para carregar categorias personalizadas e definir a categoria inicial.
    LaunchedEffect(Unit) {
        userCategories.addAll(defaultCategories) // Adiciona as categorias padrão primeiro.

        // Carrega categorias personalizadas salvas anteriormente.
        val savedCategoriesString = sharedPreferences.getString(CATEGORIES_KEY, null)
        if (!savedCategoriesString.isNullOrBlank()) {
            val customCategories = savedCategoriesString.split(CATEGORY_DELIMITER).filter { it.isNotBlank() }
            customCategories.forEach { categoryName ->
                if (!userCategories.contains(categoryName)) { // Evita adicionar duplicatas.
                    userCategories.add(categoryName)
                }
            }
        }

        // Define a primeira categoria disponível como selecionada por padrão, se nenhuma estiver selecionada.
        if (label.isBlank() && userCategories.isNotEmpty()) {
            label = userCategories.first()
        }
    }

    /**
     * Salva as categorias personalizadas do usuário no SharedPreferences.
     * Somente as categorias que não estão na lista 'defaultCategories' são salvas.
     */
    fun saveCategoriesToSharedPreferences() {
        val customCategoriesToSave = userCategories.filter { it !in defaultCategories }
        val categoriesString = customCategoriesToSave.joinToString(CATEGORY_DELIMITER)
        sharedPreferences.edit().putString(CATEGORIES_KEY, categoriesString).apply()
    }

    /**
     * Deleta uma categoria personalizada.
     * A categoria "Sites Web" é protegida contra exclusão.
     * Após a exclusão, a lista de categorias é atualizada e o SharedPreferences é salvo.
     *
     * @param categoryName O nome da categoria a ser deletada.
     */
    fun deleteCategory(categoryName: String) {
        if (categoryName == "Sites Web") {
            Toast.makeText(context, "A categoria 'Sites Web' não pode ser excluída.", Toast.LENGTH_LONG).show()
        } else {
            if (userCategories.remove(categoryName)) { // Tenta remover a categoria.
                saveCategoriesToSharedPreferences() // Salva as alterações no SharedPreferences.
                Toast.makeText(context, "Categoria '$categoryName' excluída com sucesso!", Toast.LENGTH_SHORT).show()
                // Se a categoria removida era a atualmente selecionada, seleciona a primeira disponível.
                if (label == categoryName) {
                    label = userCategories.firstOrNull() ?: ""
                }
            } else {
                Toast.makeText(context, "Erro: Categoria '$categoryName' não encontrada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Gera um token de acesso aleatório e seguro em formato Base64.
     * Usado para o campo 'accessToken' da senha.
     *
     * @return Uma string Base64 do token de acesso.
     */
    fun gerarAccessToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32) // 32 bytes = 256 bits, um bom tamanho para tokens.
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP) // Converte para Base64 sem quebras de linha.
    }

    val scrollState = rememberScrollState() // Estado de rolagem para o layout.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Barra Superior Personalizada ---
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
                // Botão de voltar.
                IconButton(
                    onClick = { onBack() },
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
                    modifier = Modifier.weight(1f) // Ocupa o espaço restante e centraliza o texto.
                )
                Spacer(modifier = Modifier.width(60.dp)) // Espaçador para alinhamento.
            }
        }

        Spacer(Modifier.padding(24.dp))

        // --- Formulário de Cadastro da Senha ---
        Column(
            modifier = Modifier
                .imePadding() // Ajusta o layout para o teclado virtual.
                .verticalScroll(scrollState) // Permite rolagem do conteúdo.
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo Nome
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.width(315.dp).padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )
            // Campo Login (Opcional)
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Login (Opcional)", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.width(315.dp).padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )
            // Campo Senha
            OutlinedTextField(
                value = senhaValor,
                onValueChange = { senhaValor = it },
                label = { Text("Senha", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.width(315.dp).padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )
            // Campo Descrição (Opcional)
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição (Opcional)", style = MaterialTheme.typography.bodyLarge) },
                modifier = Modifier.width(315.dp).padding(bottom = 16.dp),
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors()
            )

            // --- Dropdown para Seleção de Categoria ---
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
                            // Ícone de exclusão para categorias que podem ser excluídas (não padrão).
                            trailingIcon = {
                                if (categoria != "Sites Web") {
                                    IconButton(onClick = {
                                        categoryToDelete = categoria
                                        showDeleteConfirmationDialog = true // Abre o diálogo de confirmação.
                                        menuExpandido = false // Fecha o menu ao clicar no ícone.
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
                    // Opção para adicionar uma nova categoria.
                    DropdownMenuItem(
                        text = { Text("Adicionar nova categoria", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            mostrarCampoNovaCategoria = true
                            label = "" // Limpa a categoria selecionada para que o novo campo seja o foco.
                            menuExpandido = false
                        }
                    )
                }
            }

            // Campo de texto para o nome da nova categoria, visível condicionalmente.
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
            if (showDeleteConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    title = { Text("Confirmar Exclusão") },
                    text = { Text("Tem certeza que deseja excluir a categoria '${categoryToDelete}'? Senhas associadas a esta categoria não serão afetadas, apenas a categoria será removida da sua lista.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                deleteCategory(categoryToDelete) // Chama a função de exclusão.
                                showDeleteConfirmationDialog = false // Fecha o diálogo.
                            }
                        ) {
                            Text("Excluir")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDeleteConfirmationDialog = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // --- Botão de Cadastrar Senha ---
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
                        // Determina a categoria final a ser usada (selecionada ou nova).
                        val categoriaFinal = if (mostrarCampoNovaCategoria && novaCategoria.isNotBlank()) {
                            novaCategoria.trim()
                        } else {
                            label
                        }

                        // Adiciona a nova categoria à lista do usuário e salva no SharedPreferences, se for o caso.
                        if (mostrarCampoNovaCategoria && novaCategoria.isNotBlank()) {
                            if (!userCategories.contains(categoriaFinal)) {
                                userCategories.add(categoriaFinal)
                                saveCategoriesToSharedPreferences()
                                Toast.makeText(context, "Nova categoria '${categoriaFinal}' adicionada!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Categoria '${categoriaFinal}' já existe.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val saltBytes = EncryptionUtil.generateSalt() // Gera um novo salt para a criptografia.
                        val encryptedResult = EncryptionUtil.encrypt(senhaValor, masterPasswordDemo, saltBytes) // Criptografa a senha.

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
                        }

                        // Se a criptografia foi bem-sucedida, cria o objeto Senha e salva no Firestore.
                        if (encryptedPass != null && iv != null && saltBase64 != null) {
                            val novaSenha = Senha(
                                categoria = categoriaFinal,
                                login = login,
                                descricao = descricao,
                                senhaCriptografada = encryptedPass,
                                iv = iv,
                                salt = saltBase64,
                                accessToken = gerarAccessToken(), // Gera um novo token de acesso.
                                nome = nome
                            )

                            db.collection("users").document(uid).collection("passwords")
                                .add(novaSenha)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Senha cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                                    onSuccess() // Executa o callback de sucesso.
                                    // Limpa os campos após o cadastro.
                                    nome = ""
                                    login = ""
                                    descricao = ""
                                    label = userCategories.firstOrNull() ?: "" // Reseta para a primeira categoria.
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
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors()
            ) {
                Text("Cadastrar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}