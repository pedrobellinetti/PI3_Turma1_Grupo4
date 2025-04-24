package com.example.superid

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

class MainActivity : ComponentActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    UserRegistrationForm(onSuccess = {
                        Log.d("Cadastro", "Cadastro concluído")
                    })
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
fun UserRegistrationForm(onSuccess: () -> Unit) {
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
                        val uid = auth.currentUser?.uid
                        val fakeImei = UUID.randomUUID().toString()
                        val userDoc = hashMapOf("nome" to nome, "imei" to fakeImei)
                        firestore.collection("users").document(uid!!).set(userDoc)
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
