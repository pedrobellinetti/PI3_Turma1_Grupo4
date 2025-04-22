//package com.example.superid
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import java.util.UUID
//
//class Login {
//
//    // Cadastro do usuário
//    @Composable
//    fun UserRegistrationForm(onSuccess: () -> Unit,
//                             onNavigateToLogin: () -> Unit
//    ) {
//        var nome by remember { mutableStateOf("") }
//        var email by remember { mutableStateOf("") }
//        var password by remember { mutableStateOf("") }
//        var status by remember { mutableStateOf("") }
//
//        val context = LocalContext.current
//
//        Column {
//            var modifier = Modifier.padding(16.dp)
//            OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") })
//            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") })
//            OutlinedTextField(
//                value = password,
//                onValueChange = { password = it },
//                label = { Text("Senha Mestre") })
//
//            Button({
//                val auth = FirebaseAuth.getInstance()
//                val firestore = Firebase.firestore
//
//                auth.createUserWithEmailAndPassword(email, password)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val user = auth.currentUser
//                            user?.sendEmailVerification()
//
//                            val uid = user?.uid ?: ""
//                            val fakeImei = UUID.randomUUID().toString()
//                            val userDoc = hashMapOf("nome" to nome, "imei" to fakeImei)
//                            firestore.collection("users").document(uid).set(userDoc)
//                                .addOnSuccessListener {
//                                    status = "Usuário cadastrado com sucesso!"
//                                    onSuccess()
//                                }
//                                .addOnFailureListener { e ->
//                                    status = "Erro ao cadastrar usuário: ${e.message}"
//                                }
//                        } else {
//                            status = "Erro ao cadastrar usuário: ${task.exception?.message}"
//                        }
//                    }
//            }) {
//                Text("Cadastrar")
//            }
//            Text(status)
//        }
//    }
//
//// Controlar a tela atual
//
//    @Composable
//    fun AuthApp() {
//        var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }
//
//        when (currentScreen) {
//            AuthScreen.LOGIN -> LoginForm(
//                onNavigateToRegister = { currentScreen = AuthScreen.REGISTER },
//                onLoginSuccess = { "TODO: Continuar para a próxima tela" }
//            )
//            AuthScreen.REGISTER -> UserRegistrationForm(
//                onNavigateToLogin = { currentScreen = AuthScreen.LOGIN },
//                onSuccess = { currentScreen = AuthScreen.LOGIN }
//            )
//            AuthScreen.MAIN -> mainScreen(
//                onLogout = { currentScreen = AuthScreen.LOGIN }
//            )
//        }
//    }
//
//// Tela de Login
//
//    @Composable
//    fun LoginForm(
//        onNavigateToRegister: () -> Unit,
//        onLoginSuccess: () -> Unit
//    ) {
//        var email by remember { mutableStateOf("") }
//        var senha by remember { mutableStateOf("") }
//        var status by remember { mutableStateOf("") }
//
//        val auth = FirebaseAuth.getInstance()
//
//        Column(Modifier.padding(16.dp)) {
//            OutlinedTextField(
//                value = email, onValueChange = { email = it },
//                label = { Text("Email") })
//            OutlinedTextField(
//                value = senha, onValueChange = { senha = it },
//                label = { Text("Senha") })
//
//            Button(onClick = {
//                auth.signInWithEmailAndPassword(email, senha)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            if (auth.currentUser?.isEmailVerified == true) {
//                                status = "Login realizado com sucesso!"
//                                onLoginSuccess()
//                            } else {
//                                status = "Verifique seu e-mail antes de fazer login!"
//                            }
//                        } else {
//                            status = "Erro ao fazer login: ${task.exception?.message}"
//                        }
//                    }
//            }) {
//                Text("Entrar")
//            }
//            TextButton(onClick = onNavigateToRegister) {
//                Text("Não tem conta? Registre-se!")
//            }
//            Text(status)
//        }
//    }
//
//// Tela principal
//
//    @Composable
//    fun mainScreen(onLogout: () -> Unit) {
//        val auth = FirebaseAuth.getInstance()
//        val userEmail = auth.currentUser?.email ?: "Usuário"
//
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(text = "Bem-vindo, $userEmail!", style = MaterialTheme.typography.headlineSmall)
//            Text(text = "Essa é a sua tela principal após login no SuperID")
//
//            Button(onClick = {
//                auth.signOut()
//                onLogout() // Volta para a tela de login
//            }) {
//                Text("Sair")
//            }
//        }
//    }
//}