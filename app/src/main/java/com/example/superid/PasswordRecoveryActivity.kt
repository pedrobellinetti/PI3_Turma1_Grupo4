package com.example.superid

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.superid.databinding.LayoutRecuperacaoSenhaBinding
import com.google.firebase.auth.FirebaseAuth

class RecuperacaoSenhaActivity : AppCompatActivity() {
    private lateinit var binding: LayoutRecuperacaoSenhaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutRecuperacaoSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()

        binding.btnEnviarRecuperacao.setOnClickListener() {
            val email = binding.etEmailRecuperar.text.toString()

            if (email.isNotBlank()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener  { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Email de redefinição enviado com sucesso!",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Erro: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Digite seu email", Toast.LENGTH_SHORT).show()
            }
        }
    }


}