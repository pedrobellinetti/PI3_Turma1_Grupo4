package com.example.superid.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
<<<<<<< HEAD
=======
import com.example.superid.Login
>>>>>>> 6daf279 (v0.1)

@Composable
fun LoginForm(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // Pegar contexto
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.padding(8.dp)
        )

        OutlinedTextField(
            value = senha, onValueChange = { senha = it },
            label = { Text("Senha") },
            modifier = Modifier.padding(16.dp)
        )

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

        TextButton(onClick = {
            if (email.isNotBlank()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        status = if (task.isSuccessful) {
                            "Email de redefinição enviado com sucesso!"
                        } else {
                            "Erro ao enviar email de redefinição: ${task.exception?.message}"
                        }
                    }
            } else {
                status = "Digite seu email para redefinir a senha."
            }
        }) {
            Text("Esqueceu sua senha?")
        }

        if (status.isNotEmpty()) {
            Text(text = status, modifier = Modifier.padding(top = 16.dp))
        }
    }
<<<<<<< HEAD
}

        /*
        Um processador trabalha com palavras de 32 bits.
        A cache associada é quadrada e tem capacidade de 4MB. A MP possui
        capacidade de 8GB. Pergunta-se:

        1. Quantos bits são efetivamente utilizados no endereçamento?

        2. Mapeamento Direto

        3. Qual o número de blocos da MP? E o tamanho do bloco?

        4. 0000    0000    0001    0010    0000    0001    0010    0110
           B = ?  Col=?  Lin.=?   TAG = ?
         */
=======
}
>>>>>>> 6daf279 (v0.1)
