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
    uid: String,
    onLogout: () -> Unit,
    onCreatePassword: () -> Unit,
    onEditPassword: (Senha) -> Unit,
    onReadQrCode: () -> Unit
) {
    val db = Firebase.firestore
    val senhas = remember { mutableStateListOf<Senha>() }
    var listenerRegistration: ListenerRegistration? = null
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val masterPasswordForDecryption by remember { mutableStateOf("PinMestrePI2025!") }
    val auth = FirebaseAuth.getInstance()

    var isEmailVerified by remember { mutableStateOf(auth.currentUser?.isEmailVerified ?: false) }

    // DisposableEffect para carregar status de verificação e listener do Firestore
    DisposableEffect(uid, lifecycleOwner) {
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

        lifecycleOwner.lifecycle.addObserver(observer)

        // Firestore listener
        listenerRegistration = db.collection("users").document(uid).collection("passwords")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("Erro ao ouvir mudanças nas senhas: $e")
                    return@addSnapshotListener
                }

                // Limpa a lista para reconstruí-la com as últimas mudanças
                // Isso é importante para evitar duplicações ou itens antigos
                senhas.clear()
                if (snapshots != null) {
                    for (doc in snapshots.documents) {
                        doc.toObject(Senha::class.java)?.copy(id = doc.id)?.let {
                            senhas.add(it)
                        }
                    }
                }
            }
        onDispose {
            listenerRegistration?.remove()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Lógica para remover senha (centralizada aqui para Toast e atualização de lista)
    val onRemoveSenha: (Senha) -> Unit = { senhaParaRemover ->
        db.collection("users").document(uid).collection("passwords").document(senhaParaRemover.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Senha excluída com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao excluir senha: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    PasswordListContent(
        uid = uid,
        senhas = senhas,
        onLogout = onLogout,
        masterPasswordInput = masterPasswordForDecryption,
        onCreatePassword = onCreatePassword, // Passa o callback
        onSenhaRemoved = onRemoveSenha, // Passa a função centralizada de remover
        onEditPassword = onEditPassword, // Passa o callback
        onReadQrCode = onReadQrCode, // Passa o callback
        isEmailVerified = isEmailVerified
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListContent(
    uid: String,
    senhas: List<Senha>,
    onLogout: () -> Unit,
    masterPasswordInput: String,
    onCreatePassword: () -> Unit, // Callback para criar senha
    onSenhaRemoved: (Senha) -> Unit, // Callback para remover senha
    onEditPassword: (Senha) -> Unit, // Callback para editar senha
    onReadQrCode: () -> Unit, // Callback para ler QR code
    isEmailVerified: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    val masterPasswordToUse = if (masterPasswordInput.isNotBlank()) masterPasswordInput else "PinMestrePI2025!"
    val qrcodeButtonEnabled = isEmailVerified // RF4: "Login Sem Senha" disponível apenas com e-mail verificado

    Box(
        modifier = modifier.fillMaxSize()
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
                SearchBar(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp, end = 8.dp),
                    query = searchText,
                    onQueryChange = { searchText = it },
                    onSearch = { /* A pesquisa já é em tempo real via onQueryChange */ },
                    active = false,
                    onActiveChange = { /* Não é necessário para uma SearchBar que não ativa/expande */ },
                    placeholder = { Text("Pesquisar senhas") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Pesquisar") }
                ) { /* Content not used */ }
                IconButton(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) {
                    Icon(imageVector = Icons.Outlined.ExitToApp, contentDescription = "Sair")
                }
            }

            Text(
                "Senhas Cadastradas",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp)
                    .wrapContentWidth(Alignment.Start),
                textAlign = TextAlign.Start
            )

            val senhasFiltradas = if (searchText.isBlank()) {
                senhas
            } else {
                senhas.filter {
                    it.nome.contains(searchText, ignoreCase = true) ||
                            it.login.contains(searchText, ignoreCase = true) ||
                            it.descricao.contains(searchText, ignoreCase = true) ||
                            it.categoria.contains(searchText, ignoreCase = true) ||
                            it.senhaCriptografada.contains(searchText, ignoreCase = true)
                }
            }
            val senhasAgrupadas = senhasFiltradas.groupBy { it.categoria }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 75.dp)
            ) {
                senhasAgrupadas.forEach { (categoria, listaSenhas) ->
                    item {
                        Text(
                            text = categoria,
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                    items(listaSenhas) { senha ->
                        PasswordItem(
                            senha = senha,
                            masterPasswordInput = masterPasswordToUse,
                            onSenhaRemoved = onSenhaRemoved, // Passa o callback centralizado
                            onEditClicked = onEditPassword // Passa o callback
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = qrcodeButtonEnabled) { onReadQrCode() }
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = "Ler QR Code",
                    tint = if (qrcodeButtonEnabled) MaterialTheme.colorScheme.onBackground else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Ler QR Code",
                    fontSize = 12.sp,
                    color = if (qrcodeButtonEnabled) MaterialTheme.colorScheme.onBackground else Color.Gray
                )
            }

            FloatingActionButton(
                onClick = onCreatePassword, // Chama o callback para criar nova senha
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, "Adicionar nova senha")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PasswordItem(
    senha: Senha,
    masterPasswordInput: String,
    onSenhaRemoved: (Senha) -> Unit, // Recebe o callback para remover
    onEditClicked: (Senha) -> Unit // Recebe o callback para editar
) {
    var expanded by remember { mutableStateOf(false) }
    // Adicione esta nova variável de estado para controlar a visibilidade da senha
    var showPassword by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val masterPasswordToUse = if (masterPasswordInput.isNotBlank()) masterPasswordInput else "PinMestrePI2025!"

    val decryptedPassword = remember(senha.senhaCriptografada, senha.iv, senha.salt, masterPasswordToUse) {
        EncryptionUtil.decrypt(senha.senhaCriptografada, senha.iv, senha.salt, masterPasswordToUse)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showPassword = !showPassword },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                Text(text = senha.nome, fontWeight = FontWeight.Medium)
                Text(text = senha.login, color = Color.Black, fontSize = 14.sp)
                Text(
                    text = if (showPassword) decryptedPassword ?: "Erro ao descriptografar" else "********",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(text = senha.descricao, color = Color.Black, fontSize = 14.sp)
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opções")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar Senha") },
                        onClick = {
                            expanded = false
                            onEditClicked(senha) // Chama o callback passando o objeto Senha
                        },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = "Editar Senha") }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir Senha") },
                        onClick = {
                            expanded = false
                            onSenhaRemoved(senha) // Chama o callback para remover
                        },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Excluir Senha") }
                    )
                }
            }
        }
    }
}