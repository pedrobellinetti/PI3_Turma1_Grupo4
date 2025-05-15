package com.example.superid.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import com.example.superid.PasswordManagerScreenActivity
import com.example.superid.Senha


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerScreen(uid: String, senhas: List<Senha>, onLogout: () -> Unit, onCreatePassword: (String) -> Unit) {
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
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            query = searchText,
            onQueryChange = { searchText = it },
            onSearch = { /* TODO: Implementar ação de pesquisa se necessário */ },
            active = false, // Manter inativo por padrão
            onActiveChange = { /* TODO: Implementar lógica de ativação se necessário */ },
            placeholder = { Text("Pesquisar senhas") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Pesquisar") }
        ) {
            // Conteúdo adicional da SearchBar quando ativa
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
                        onSenhaRemoved = { /* TODO: Implementar remoção */ }) { senhaParaEditar ->
                        onCreatePassword(uid)
                    }
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
                        onSenhaRemoved = { /* TODO: Implementar remoção */ }) { senhaParaEditar ->
                        onCreatePassword(uid)
                    }
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
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF8000FF)
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
            ) {
                Text("Sair")
            }
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
            Row {
                IconButton(onClick = { onEditClicked(senha) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
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
