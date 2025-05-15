package com.example.superid.ui.screens

import android.content.Intent
import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.superid.PasswordFormActivity
import com.example.superid.Senha
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import androidx.compose.ui.graphics.Color

fun gerarAccessToken(): String {
    val bytes = ByteArray(192)
    SecureRandom().nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerScreen(uid: String) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val senhas = remember { mutableStateListOf<Senha>() }
    val categoriasDisponiveis = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico", "Outros")

    var categoria by remember { mutableStateOf("Sites Web") }
    var login by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var idEdicao by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Carregar senhas
    LaunchedEffect(uid) {
        db.collection("users").document(uid).collection("passwords")
            .get()
            .addOnSuccessListener { result ->
                senhas.clear()
                for (doc in result) {
                    val item = doc.toObject(Senha::class.java).copy(id = doc.id)
                    senhas.add(item)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text("Senhas cadastradas:", style = MaterialTheme.typography.headlineSmall)

            senhas.groupBy { it.categoria }.forEach { (nomeCategoria, lista) ->
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text("Categoria: $nomeCategoria", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    lista.forEach { itemSenha ->
                        Card(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Descrição: ${itemSenha.descricao}")
                                Text("Login: ${itemSenha.login}")
                                Text("Senha: ${itemSenha.senhaCriptografada.take(8)}...")

                                Spacer(modifier = Modifier.height(8.dp))

                                Row {
                                    Button(onClick = {
                                        login = itemSenha.login
                                        descricao = itemSenha.descricao
                                        categoria = itemSenha.categoria
                                        senha = itemSenha.senhaCriptografada
                                        idEdicao = itemSenha.id
                                    }) {
                                        Text("Editar")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = {
                                        db.collection("users").document(uid)
                                            .collection("passwords").document(itemSenha.id).delete()
                                        senhas.remove(itemSenha)
                                    }) {
                                        Text("Excluir")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botão flutuante para adicionar nova senha
        ExtendedFloatingActionButton (
            onClick = {
                val intent = Intent(context, PasswordFormActivity::class.java)
                intent.putExtra("uid", uid)
                context.startActivity(intent)
            },
            containerColor = Color(0xFF8000FF),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("+ CREATE", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
