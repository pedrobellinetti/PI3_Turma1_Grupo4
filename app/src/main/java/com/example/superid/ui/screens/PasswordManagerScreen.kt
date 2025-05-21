package com.example.superid.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.EditPasswordActivity
import com.example.superid.Senha
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerScreen(uid: String, onLogout: () -> Unit, onCreatePassword: (String) -> Unit, onEditPassword: (String) -> Unit) {
    val db = Firebase.firestore
    val senhas = remember { mutableStateListOf<Senha>() }
    var listenerRegistration: ListenerRegistration? = null
    val context = LocalContext.current

    val onSenhaRemoved: (Boolean, String?) -> Unit = { sucesso, mensagem ->
        Toast.makeText(
            context,
            mensagem ?: if (sucesso) "Senha excluída com sucesso!" else "Erro ao excluir senha.",
            Toast.LENGTH_SHORT
        ).show()
    }

    DisposableEffect(uid) {
        listenerRegistration = db.collection("users").document(uid).collection("passwords")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("Erro ao ouvir mudanças: $e")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    for (docChange in snapshots.documentChanges) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                docChange.document.toObject(Senha::class.java).copy(id = docChange.document.id)
                                    .let { newSenha ->
                                        senhas.add(newSenha)
                                    }
                            }
                            DocumentChange.Type.MODIFIED -> {
                                docChange.document.toObject(Senha::class.java).copy(id = docChange.document.id)
                                    .let { updatedSenha ->
                                        val index = senhas.indexOfFirst { it.id == updatedSenha.id }
                                        if (index != -1) {
                                            senhas[index] = updatedSenha
                                        }
                                    }
                            }
                            DocumentChange.Type.REMOVED -> {
                                senhas.removeAll { it.id == docChange.document.id }
                            }
                        }
                    }
                }
            }
        onDispose {
            listenerRegistration?.remove()
        }
    }

    PasswordListContent(
        uid = uid,
        senhas = senhas,
        onLogout = onLogout,
        onCreatePassword = onCreatePassword,
        onSenhaRemoved = { senhaParaRemover ->
            db.collection("users").document(uid).collection("passwords").document(senhaParaRemover.id)
                .delete()
                .addOnSuccessListener {
                    println("Documento deletado com sucesso!")
                    onSenhaRemoved(true, null)
                }
                .addOnFailureListener { e ->
                    println("Erro ao deletar: $e")
                    onSenhaRemoved(false, "Falha ao excluir senha.")
                }
        },
        onEditPassword = { senhaId ->
            val intent = Intent(context, EditPasswordActivity::class.java)
            intent.putExtra("UID", uid)
            intent.putExtra("SENHA_ID", senhaId)
            context.startActivity(intent)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListContent(
    uid: String,
    senhas: List<Senha>,
    onLogout: () -> Unit,
    onCreatePassword: (String) -> Unit,
    onSenhaRemoved: (Senha) -> Unit,
    onEditPassword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    var searchText by remember { mutableStateOf("") }

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
                    onSearch = { /* A pesquisa já é em tempo real via onQueryChange, nenhuma ação adicional é necessária aqui */ },
                    active = false, // Mantém a SearchBar sempre no estado inativo/compacto
                    onActiveChange = { /* Não é necessário para uma SearchBar que não ativa/expande */ },
                    placeholder = { Text("Pesquisar senhas") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Pesquisar") }
                ) {
                    // Conteúdo adicional da SearchBar quando ativa (não será exibido pois 'active' é sempre false)
                }
                IconButton(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) {
                    Icon(imageVector = Icons.Outlined.ExitToApp, contentDescription = "Sair")
                }
            }

            Text(
                "Senhas Cadastradas",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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
                            uid = uid,
                            onSenhaRemoved = onSenhaRemoved,
                            onEditClicked = { senhaParaEditar ->
                                onEditPassword(senhaParaEditar.id ?: "")
                            }
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
            Button(
                onClick = { /* TODO: Implementar para abrir a camera  */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp, focusedElevation = 0.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("Ler QRcode", textAlign = TextAlign.Start)
            }

            FloatingActionButton(
                onClick = { onCreatePassword(uid) },
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
fun PasswordItem(senha: Senha, uid: String, onSenhaRemoved: (Senha) -> Unit, onEditClicked: (Senha) -> Unit) {
    val db = Firebase.firestore
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                Text(text = senha.senhaCriptografada, color = Color.Black, fontSize = 14.sp)
                Text(text = senha.descricao, fontWeight = FontWeight.Medium)
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
                        onClick = { expanded = false; onEditClicked(senha) },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = "Editar Senha") }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir Senha") },
                        onClick = {
                            expanded = false
                            db.collection("users").document(uid).collection("passwords").document(senha.id)
                                .delete()
                                .addOnSuccessListener {
                                    println("Documento deletado com sucesso!")
                                }
                                .addOnFailureListener { e ->
                                    println("Erro ao deletar: $e")
                                    Toast.makeText(context, "Falha ao excluir senha.", Toast.LENGTH_SHORT).show()
                                }
                            onSenhaRemoved(senha)
                        },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Excluir Senha") }
                    )
                }
            }
        }
    }
}