package com.example.superid.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

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