package com.example.superid.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.Senha
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.superid.utils.EncryptionUtil
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerScreen(
    uid: String, // ID único do usuário logado no Firebase.
    onLogout: () -> Unit, // Callback para navegar para a tela de logout.
    onCreatePassword: () -> Unit, // Callback para navegar para a tela de criação de senha.
    onEditPassword: (Senha) -> Unit, // Callback para navegar para a tela de edição de senha, passando a senha a ser editada.
    onReadQrCode: () -> Unit // Callback para iniciar a leitura de QR Code.
) {
    val db = Firebase.firestore // Instância do Firebase Firestore para interagir com o banco de dados.
    val senhas = remember { mutableStateListOf<Senha>() } // Lista mutável de senhas a serem exibidas.
    var listenerRegistration: ListenerRegistration? = null // Gerencia o listener de snapshots do Firestore.
    val context = LocalContext.current // Contexto do aplicativo para operações como Toast.
    val lifecycleOwner = LocalLifecycleOwner.current // Observa o ciclo de vida do Composable.
    val masterPasswordForDecryption by remember { mutableStateOf("PinMestrePI2025!") }
    val auth = FirebaseAuth.getInstance() // Instância do Firebase Auth para verificar o status do usuário.

    var isEmailVerified by remember { mutableStateOf(auth.currentUser?.isEmailVerified ?: false) } // Estado para verificar se o e-mail do usuário está verificado.

    /**
     * DisposableEffect é usado para gerenciar efeitos colaterais que precisam ser limpos, como
     * listeners de banco de dados ou observadores de ciclo de vida.
     * Neste caso, ele carrega o status de verificação de e-mail e configura o listener do Firestore.
     */
    DisposableEffect(uid, lifecycleOwner) {
        // Observador do ciclo de vida para recarregar o status de verificação de e-mail ao resumir a tela.
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                auth.currentUser?.reload()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isEmailVerified = auth.currentUser?.isEmailVerified ?: false
                    } else {
                        Toast.makeText(context, "Falha ao recarregar status do usuário: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer) // Adiciona o observador de ciclo de vida.

        // Listener do Firestore para obter atualizações em tempo real das senhas do usuário.
        listenerRegistration = db.collection("users").document(uid).collection("passwords")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("Erro ao ouvir mudanças nas senhas: $e") // Loga erros de leitura do Firestore.
                    return@addSnapshotListener
                }

                senhas.clear() // Limpa a lista para reconstruí-la com os dados mais recentes.
                if (snapshots != null) {
                    for (doc in snapshots.documents) {
                        // Converte cada documento em um objeto Senha e adiciona à lista.
                        doc.toObject(Senha::class.java)?.copy(id = doc.id)?.let {
                            senhas.add(it)
                        }
                    }
                }
            }
        // Bloco onDispose é chamado quando o Composable sai da composição.
        onDispose {
            listenerRegistration?.remove() // Remove o listener do Firestore para evitar vazamento de memória.
            lifecycleOwner.lifecycle.removeObserver(observer) // Remove o observador do ciclo de vida.
        }
    }

    // Lógica para remover senha, centralizada para lidar com a UI e o Firestore.
    val onRemoveSenha: (Senha) -> Unit = { senhaParaRemover ->
        db.collection("users").document(uid).collection("passwords").document(senhaParaRemover.id)
            .delete() // Deleta o documento da senha no Firestore.
            .addOnSuccessListener {
                Toast.makeText(context, "Senha excluída com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao excluir senha: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Chama o componente principal de conteúdo da lista de senhas.
    PasswordListContent(
        uid = uid,
        senhas = senhas,
        onLogout = onLogout,
        masterPasswordInput = masterPasswordForDecryption, // Passa a senha mestra
        onCreatePassword = onCreatePassword,
        onSenhaRemoved = onRemoveSenha, // Passa o callback para remover senhas.
        onEditPassword = onEditPassword,
        onReadQrCode = onReadQrCode,
        isEmailVerified = isEmailVerified // Passa o status de verificação de e-mail.
    )
}

/**
 * Composable que exibe o conteúdo da lista de senhas, incluindo barra de pesquisa,
 * lista de senhas agrupadas por categoria e botões de ação.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListContent(
    uid: String, // ID único do usuário.
    senhas: List<Senha>, // Lista de senhas a serem exibidas.
    onLogout: () -> Unit, // Callback para logout.
    masterPasswordInput: String, // Senha mestra para decriptografia (HARDCODED).
    onCreatePassword: () -> Unit, // Callback para criar nova senha.
    onSenhaRemoved: (Senha) -> Unit, // Callback para remover senha.
    onEditPassword: (Senha) -> Unit, // Callback para editar senha.
    onReadQrCode: () -> Unit, // Callback para ler QR Code.
    isEmailVerified: Boolean, // Indica se o e-mail do usuário está verificado.
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current // Contexto para exibir Toasts.
    var searchText by remember { mutableStateOf("") } // Estado para o texto da barra de pesquisa.
    // Define a senha mestra a ser usada; se masterPasswordInput estiver vazia, usa a hardcoded.
    val masterPasswordToUse = if (masterPasswordInput.isNotBlank()) masterPasswordInput else "PinMestrePI2025!"
    // Habilita o botão de QR Code apenas se o e-mail estiver verificado (RF4).
    val qrcodeButtonEnabled = isEmailVerified

    Box(
        modifier = modifier.fillMaxSize() // Preenche todo o espaço disponível.
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Barra de pesquisa para filtrar senhas.
                SearchBar(
                    modifier = Modifier
                        .weight(1f) // Ocupa a maior parte da largura.
                        .padding(bottom = 8.dp, end = 8.dp),
                    query = searchText, // Texto atual da pesquisa.
                    onQueryChange = { searchText = it }, // Atualiza o texto da pesquisa.
                    onSearch = { /* A pesquisa já é em tempo real via onQueryChange */ },
                    active = false, // Barra de pesquisa não se expande.
                    onActiveChange = { /* Não é necessário para uma SearchBar que não ativa/expande */ },
                    placeholder = { Text("Pesquisar senhas") }, // Texto de placeholder.
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Pesquisar") } // Ícone de pesquisa.
                ) { }
                // Botão de sair.
                IconButton(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) {
                    Icon(imageVector = Icons.Outlined.ExitToApp, contentDescription = "Sair")
                }
            }

            // Título da seção "Senhas Cadastradas".
            Text(
                "Senhas Cadastradas",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .wrapContentWidth(Alignment.Start),
                textAlign = TextAlign.Start
            )

            // Filtra as senhas com base no texto de pesquisa.
            val senhasFiltradas = if (searchText.isBlank()) {
                senhas
            } else {
                senhas.filter {
                    it.nome.contains(searchText, ignoreCase = true) ||
                            it.login.contains(searchText, ignoreCase = true) ||
                            it.descricao.contains(searchText, ignoreCase = true) ||
                            it.categoria.contains(searchText, ignoreCase = true) ||
                            it.senhaCriptografada.contains(searchText, ignoreCase = true) // Pesquisa também na senha criptografada
                }
            }
            // Agrupa as senhas filtradas por categoria.
            val senhasAgrupadas = senhasFiltradas.groupBy { it.categoria }

            // Lista de rolagem (LazyColumn) para exibir as senhas.
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // Ocupa o restante do espaço vertical.
                    .padding(bottom = 75.dp) // Espaço para o FAB na parte inferior.
            ) {
                senhasAgrupadas.forEach { (categoria, listaSenhas) ->
                    item {
                        // Título da categoria.
                        Text(
                            text = categoria,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                    // Exibe cada senha dentro da categoria.
                    items(listaSenhas) { senha ->
                        PasswordItem(
                            senha = senha,
                            masterPasswordInput = masterPasswordToUse,
                            onSenhaRemoved = onSenhaRemoved, // Passa o callback para remover.
                            onEditClicked = onEditPassword // Passa o callback para editar.
                        )
                    }
                }
            }
        }
        // Botões de ação na parte inferior da tela.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter), // Alinha na parte inferior central.
            horizontalArrangement = Arrangement.SpaceAround, // Distribui o espaço entre os itens.
            verticalAlignment = Alignment.Bottom
        ) {
            // Coluna para o botão "Ler QR Code".
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f) // Ocupa uma parte da largura.
                    .clickable(enabled = qrcodeButtonEnabled) { onReadQrCode() } // Clicável apenas se o QR Code estiver habilitado.
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = "Ler QR Code",
                    tint = if (qrcodeButtonEnabled) MaterialTheme.colorScheme.onBackground else Color.Gray, // Cor condicional.
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Ler QR Code",
                    fontSize = 12.sp,
                    color = if (qrcodeButtonEnabled) MaterialTheme.colorScheme.onBackground else Color.Gray // Cor condicional.
                )
            }

            // Floating Action Button (FAB) para adicionar nova senha.
            FloatingActionButton(
                onClick = onCreatePassword, // Chama o callback para criar nova senha.
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, "Adicionar nova senha")
            }
            Spacer(modifier = Modifier.weight(1f)) // Espaçador para alinhar o FAB.
        }
    }
}

/**
 * Composable que representa um item individual na lista de senhas.
 * Permite visualizar, esconder, editar e excluir a senha.
 */
@Composable
fun PasswordItem(
    senha: Senha, // Objeto Senha a ser exibido.
    masterPasswordInput: String, // Senha mestra para decriptografia (HARDCODED).
    onSenhaRemoved: (Senha) -> Unit, // Callback para remover a senha.
    onEditClicked: (Senha) -> Unit // Callback para editar a senha.
) {
    var expanded by remember { mutableStateOf(false) } // Estado para controlar a expansão do menu de opções.
    var showPassword by remember { mutableStateOf(false) } // Estado para controlar a visibilidade da senha.
    val context = LocalContext.current // Contexto do aplicativo.

    val masterPasswordToUse = if (masterPasswordInput.isNotBlank()) masterPasswordInput else "PinMestrePI2025!" // Senha mestra a ser usada.

    // Decriptografa a senha; o 'remember' garante que a decriptografia só ocorra quando os parâmetros mudarem.
    val decryptedPassword = remember(senha.senhaCriptografada, senha.iv, senha.salt, masterPasswordToUse) {
        EncryptionUtil.decrypt(senha.senhaCriptografada, senha.iv, senha.salt, masterPasswordToUse)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showPassword = !showPassword }, // Clicar no Card alterna a visibilidade da senha.
        shape = MaterialTheme.shapes.medium, // Formato arredondado para o Card.
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer // Cor de fundo do Card.
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = senha.nome, fontWeight = FontWeight.Medium) // Nome da senha.
                Text(text = senha.login, color = Color.Black, fontSize = 14.sp) // Login.
                Text(
                    text = if (showPassword) decryptedPassword ?: "Erro ao descriptografar" else "********", // Exibe a senha decriptografada ou mascarada.
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(text = senha.descricao, color = Color.Black, fontSize = 14.sp) // Descrição.
            }
            Box {
                // Botão de opções (três pontinhos).
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opções")
                }
                // Menu dropdown com opções de Editar e Excluir.
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Opção para editar senha.
                    DropdownMenuItem(
                        text = { Text("Editar Senha") },
                        onClick = {
                            expanded = false
                            onEditClicked(senha) // Chama o callback para editar a senha.
                        },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = "Editar Senha") }
                    )
                    // Opção para excluir senha.
                    DropdownMenuItem(
                        text = { Text("Excluir Senha") },
                        onClick = {
                            expanded = false
                            onSenhaRemoved(senha) // Chama o callback para remover a senha.
                        },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Excluir Senha") }
                    )
                }
            }
        }
    }
}