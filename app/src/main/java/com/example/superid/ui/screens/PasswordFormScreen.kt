package com.example.superid.ui.screens

import android.util.Base64
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.superid.Senha
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordFormScreen(uid: String, onSenhaSalva: () -> Unit) {
    val db = Firebase.firestore
    val categoriasDisponiveis = listOf("Sites Web", "Aplicativos", "Acesso Físico", "Outros")

    var categoria by remember { mutableStateOf("Sites Web") }
    var login by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var menuExpandido by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text("Cadastrar nova senha", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        OutlinedTextField(value = login, onValueChange = { login = it }, label = { Text("Login") })
        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") })
        OutlinedTextField(value = senha, onValueChange = { senha = it }, label = { Text("Senha") })

        ExposedDropdownMenuBox(
            expanded = menuExpandido,
            onExpandedChange = { menuExpandido = !menuExpandido }
        ) {
            OutlinedTextField(
                value = categoria,
                onValueChange = {},
                readOnly = true,
                label = { Text("Categoria") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = menuExpandido,
                onDismissRequest = { menuExpandido = false }
            ) {
                categoriasDisponiveis.forEach { opcao ->
                    DropdownMenuItem(
                        text = { Text(opcao) },
                        onClick = {
                            categoria = opcao
                            menuExpandido = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(onClick = {
            val novaSenha = Senha(
                categoria = categoria,
                login = login,
                descricao = descricao,
                senhaCriptografada = senha,
                accessToken = gerarAccessToken()
            )

            db.collection("users").document(uid).collection("passwords")
                .add(novaSenha)
                .addOnSuccessListener {
                    onSenhaSalva()
                }

            login = ""
            descricao = ""
            categoria = "Sites Web"
            senha = ""
        }) {
            Text("Salvar senha")
        }
    }
}

