package com.example.superid.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.PasswordFormActivity
import com.example.superid.Senha
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import com.example.superid.PasswordManagerScreenActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerScreen(uid: String, senhas: MutableList<Senha>, onLogout: () -> Unit, onCreatePassword: (String) -> Unit) {
    val context = LocalContext.current
    val db = Firebase.firestore
    var searchText by remember { mutableStateOf("") }
    val filteredWebSites = remember(senhas, searchText) {
        senhas.filter {
            it.categoria == "Sites Web" && it.descricao.contains(
                searchText,
                ignoreCase = true
            )
        }
    }
    val filteredAplicativos = remember(senhas, searchText) {
        senhas.filter {
            it.categoria == "Aplicativos" && it.descricao.contains(
                searchText,
                ignoreCase = true
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Para alinhar a SearchBar à esquerda e o ícone à direita
        ) {
            SearchBar(
                modifier = Modifier
                    .weight(1f) // A SearchBar ocupa a maior parte do espaço
                    .padding(bottom = 8.dp, end = 8.dp), // Adiciona um pequeno espaço à direita
                query = searchText,
                onQueryChange = { searchText = it },
                onSearch = { /* TODO: Implementar ação de pesquisa se necessário */ },
                active = false,
                onActiveChange = { /* TODO: Implementar lógica de ativação se necessário */ },
                placeholder = { Text("Pesquisar senhas") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Pesquisar") }
            ) {
                // Conteúdo adicional da SearchBar quando ativa
            }
            IconButton(onClick = onLogout, modifier = Modifier
                .padding(bottom = 8.dp)
                .height(40.dp)
            ){
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

        // Lista de Web Sites filtrados
        if (filteredWebSites.isNotEmpty()) {
            Text(
                "Web Sites",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredWebSites) { senha ->
                    PasswordItem(
                        senha = senha,
                        uid = uid,
                        onSenhaRemoved = { senhaRemovida ->
                            senhas.remove(senhaRemovida)
                        },
                        onEditClicked = { senhaParaEditar ->
                            // TODO: Implementar a navegação para a tela de edição
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Lista de Aplicativos filtrados
        if (filteredAplicativos.isNotEmpty()) {
            Text(
                "Aplicativos",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredAplicativos) { senha ->
                    PasswordItem(
                        senha = senha,
                        uid = uid,
                        onSenhaRemoved = { senhaRemovida ->
                            senhas.remove(senhaRemovida)
                        },
                        onEditClicked = { senhaParaEditar ->
                            // TODO: Implementar a navegação para a tela de edição
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(
                onClick = { onCreatePassword(uid) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Text("+ Criar")
            }
        }
    }
}

@Composable
fun PasswordItem(senha: Senha, uid: String, onSenhaRemoved: (Senha) -> Unit, onEditClicked: (Senha) -> Unit) {
    val db = Firebase.firestore
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD8BFD8).copy(alpha = 0.6f) // Lilás claro
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
                Text(text = senha.descricao, fontWeight = FontWeight.Medium)
                Text(text = senha.login, color = Color.Gray, fontSize = 14.sp)
                Text(text = "*******", color = Color.Gray, fontSize = 14.sp) // Exibir senha mascarada
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
                            onEditClicked(senha)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Editar Senha"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir Senha") },
                        onClick = {
                            expanded = false
                            db.collection("users").document(uid).collection("passwords").document(senha.id)
                                .delete()
                                .addOnSuccessListener {
                                    println("Documento deletado com sucesso!")
                                    onSenhaRemoved(senha) // Chama a função para atualizar a lista
                                }
                                .addOnFailureListener { e ->
                                    println("Erro ao deletar o documento: $e")
                                }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Excluir Senha"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordManagerScreen(uid: String) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val senhas = remember { mutableStateListOf<Senha>() }

    // Carregar senhas
    LaunchedEffect(uid) {
        db.collection("users").document(uid).collection("passwords")
            .get()
            .addOnSuccessListener { result ->
                senhas.clear()
                for (doc in result) {
                    senhas.add(doc.toObject(Senha::class.java).copy(id = doc.id))
                }
            }
            .addOnFailureListener { e ->
                // Tratar falha ao carregar senhas
                println("Erro ao carregar senhas: $e")
            }
    }

    PasswordManagerScreen(
        uid = uid,
        senhas = senhas,
        onLogout = { /* TODO: Implementar ação de sair */ },
        onCreatePassword = { currentUid ->
            val intent = Intent(context, PasswordFormActivity::class.java)
            intent.putExtra("uid", currentUid)
            context.startActivity(intent)
        }
    )
}