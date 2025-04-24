package com.example.superid

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.superid.databinding.LayoutTelaLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFormActivity() : AppCompatActivity() {
    private lateinit var binding: LayoutTelaLoginBinding

    override fun onCreate(savedInstancedState: Bundle?) {
        super.onCreate(savedInstancedState)
        binding = LayoutTelaLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()

        binding.btnEntrar.setOnClickListener {
            val email = binding.etNomeLogin.text.toString()
            val senha = binding.etSenhaLogin.text.toString()

            if (email.isNotBlank() && senha.isNotBlank()) {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val status: String
                            if (auth.currentUser?.isEmailVerified == true) {
                                Toast.makeText(
                                    this,
                                    "Login realizado com sucesso!",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Erro: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        }
                    }
            } else {
                Toast.makeText(
                    this,
                    "Digite seu email e senha",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
