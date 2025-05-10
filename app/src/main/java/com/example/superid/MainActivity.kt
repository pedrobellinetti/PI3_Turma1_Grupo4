package com.example.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.preference.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recupera o uid passado pelo Intent
        val uid = intent.getStringExtra("uid")

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val hasAcceptedTerms = sharedPreferences.getBoolean("hasAcceptedTerms", false)

        intent.putExtra("uid", uid) // Passa o uid para a próxima Activity

        if (!hasAcceptedTerms) {
            val intent = Intent(this, TermsActivity::class.java)
            intent.putExtra("uid", uid)
            startActivity(intent)
        } else {
            //startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
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
                                val intent = Intent(context, MainActivity::class.java)
                                intent.putExtra("uid", uid) // Passa o uid para a MainActivity
                                context.startActivity(intent)
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
