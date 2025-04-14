package com.example.superid

//import com.example.superid.Login
import QrScanScreen
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid.ui.screens.LoginForm
import com.example.superid.ui.screens.MainScreen
import com.example.superid.ui.screens.PasswordManagerScreen
import com.example.superid.ui.screens.PasswordRecoveryScreen
import com.example.superid.ui.screens.gerarAccessToken
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

//import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenuDefaults



class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AuthApp()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SuperIDTheme {
        Greeting("Android")
    }
}

// Cadastro do usuário
@Composable
fun UserRegistrationForm(onSuccess: () -> Unit,
                         onNavigateToLogin: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column {
        var modifier = Modifier.padding(16.dp)
        OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") })
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha Mestre") })

        Button({
            val auth = FirebaseAuth.getInstance()
            val firestore = Firebase.firestore

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()

                        val uid = user?.uid ?: ""
                        val fakeImei = UUID.randomUUID().toString()
                        val userDoc = hashMapOf("nome" to nome, "imei" to fakeImei)
                        firestore.collection("users").document(uid).set(userDoc)
                            .addOnSuccessListener {
                                status = "Usuário cadastrado com sucesso!"
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                status = "Erro ao cadastrar usuário: ${e.message}"
                            }
                    } else {
                        status = "Erro ao cadastrar usuário: ${task.exception?.message}"
                    }
                }
        }) {
            Text("Cadastrar")
        }
        Text(status)
    }
}

// Controlar a tela atual

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthApp() {
    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

    when (currentScreen) {
        AuthScreen.LOGIN -> LoginForm(
            onNavigateToRegister = { currentScreen = AuthScreen.REGISTER },
            onLoginSuccess = { currentScreen = AuthScreen.MAIN }
        )

        AuthScreen.REGISTER -> UserRegistrationForm(
            onNavigateToLogin = { currentScreen = AuthScreen.LOGIN },
            onSuccess = { currentScreen = AuthScreen.LOGIN }
        )

        AuthScreen.MAIN -> {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            MainScreen(onLogout = { currentScreen = AuthScreen.LOGIN }, uid = uid)
        }

        AuthScreen.QR_LOGIN -> QrScanScreen(
            onLoginAprovado = { currentScreen = AuthScreen.MAIN }
        )

        AuthScreen.RECOVERY -> PasswordRecoveryScreen(
            recuperarSenha = { currentScreen = AuthScreen.LOGIN }
        )
    }

// Tela de Login

    @Composable
    fun LoginForm(
        onNavigateToRegister: () -> Unit,
        onLoginSuccess: () -> Unit
    ) {
        var email by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("") }

        val auth = FirebaseAuth.getInstance()

        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") })
            OutlinedTextField(
                value = senha, onValueChange = { senha = it },
                label = { Text("Senha") })

            Button(onClick = {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (auth.currentUser?.isEmailVerified == true) {
                                status = "Login realizado com sucesso!"
                                onLoginSuccess()
                            } else {
                                status = "Verifique seu e-mail antes de fazer login!"
                            }
                        } else {
                            status = "Erro ao fazer login: ${task.exception?.message}"
                        }
                    }
            }) {
                Text("Entrar")
            }
            TextButton(onClick = onNavigateToRegister) {
                Text("Não tem conta? Registre-se!")
            }
            Text(status)
        }
    }

    // Tela principal
    @OptIn(ExperimentalMaterial3Api::class)
    @ExperimentalMaterial3Api
    @Composable
    fun MainScreen(onLogout: () -> Unit, uid: String) {
        val auth = FirebaseAuth.getInstance()
        val userEmail = auth.currentUser?.email ?: "Usuário"

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Bem-vindo, $userEmail!", style = MaterialTheme.typography.headlineSmall)

            Button(onClick = {
                auth.signOut()
                onLogout()
            }) {
                Text("Sair")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            PasswordManagerScreen(uid = uid) // Corrigido para usar PascalCase
        }
    }


// Tela de gerenciamento de senhas

    @ExperimentalMaterial3Api
    @Composable
    fun PasswordManagerScreen(uid: String) {
        val db = Firebase.firestore
        val senhas = remember { mutableStateListOf<Senha>() }
        val categoriasDisponiveis = listOf("Sites Web", "Aplicativos", "Acesso Físico", "Outros")

        var categoria by remember { mutableStateOf("Sites Web") }
        var login by remember { mutableStateOf("") }
        var descricao by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }
        var idEdicao by remember { mutableStateOf<String?>(null) }
        var menuExpandido by remember { mutableStateOf(false) }

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

        Column(Modifier.padding(16.dp)) {
            Text("Cadastrar nova senha")

            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Login") })

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") })

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") })

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

            OutlinedTextField(
                value = categoria, onValueChange = { categoria = it },
                label = { Text("Categoria") })

            Button(onClick = {
                val novaSenha = Senha(
                    categoria = categoria,
                    login = login,
                    descricao = descricao,
                    senhaCriptografada = senha,
                    accessToken = gerarAccessToken()
                )

                val ref = db.collection("users").document(uid).collection("passwords")

                if (idEdicao != null) {
                    // Atualiza senha, mudando a já existe
                    ref.document(idEdicao!!).set(novaSenha)
                        .addOnSuccessListener {
                            // Atualiza lista local após mudança da senha
                            senhas.replaceAll {
                                if (it.id == idEdicao) {
                                    novaSenha.copy(id = idEdicao!!)
                                } else {
                                    it
                                }
                            }
                        }
                } else {
                    // Adiciona nova senha
                    ref.add(novaSenha).addOnSuccessListener { doc ->
                        senhas.add(novaSenha.copy(id = doc.id))
                    }
                }

                // Limpando campos de registro

                login = ""
                descricao = ""
                categoria = ""
                senha = ""
                idEdicao = null
            }) {
                Text(if (idEdicao != null) "Atualizar" else "Salvar senha")
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            Text("Senhas cadastradas:")

            senhas.groupBy { it.categoria }.forEach { (nomeCategoria, lista) ->
                Text("Categoria: $nomeCategoria", style = MaterialTheme.typography.titleMedium)

                lista.forEach { itemSenha ->
                    Column(Modifier.padding(vertical = 8.dp)) {
                        Text("Descrição: ${itemSenha.descricao}")
                        Text("Login: ${itemSenha.login}")
                        Text("Senha: ${itemSenha.senhaCriptografada.take(8)}...")

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
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
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

