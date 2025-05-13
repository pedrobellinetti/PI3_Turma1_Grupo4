package com.example.superid.ui.screens

//import androidx.compose.material.*
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.example.superid.RecuperacaoSenhaActivity
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PasswordRecoveryScreen(recuperarSenha: () -> Unit) {
    // Pegar context aqui para não dar erro dentro de TextButton
    val context = LocalContext.current


    // Implementação da tela de recuperação de senha

    var email by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Recuperação de senha", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        status = if (task.isSuccessful) {
                            "Email de redefinição enviado com sucesso!"
                        } else {
                            "Erro ao enviar email de redefinição: ${task.exception?.message}"
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar instruções de redefinição")
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            val intent = Intent(context, RecuperacaoSenhaActivity::class.java)
            context.startActivity(intent)
//            recuperarSenha()
        }) {
            Text("Voltar a tela de login")
        }
    }


    Spacer(Modifier.height(16.dp))

    if (status.isNotEmpty()) {
        Text(text = status)
    }
}
