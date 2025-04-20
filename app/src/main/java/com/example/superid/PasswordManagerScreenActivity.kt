package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.superid.databinding.LayoutTelaSenhasBinding
import com.google.firebase.auth.FirebaseAuth

class MainScreenActivity : AppCompatActivity() {
    lateinit var binding: LayoutTelaSenhasBinding

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        binding.fabAdd.setOnClickListener {
            val uid = auth.currentUser?.uid.toString()
            if (uid == null) {
                // TODO: Cuidar do caso se o usuário não estiver autenticado
                Toast.makeText(
                    this,
                    "Usuário não autenticado",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = Intent(this, this::class.java)
                intent.putExtra("uid", uid)
                startActivity(intent)
                finish()
            }
        }
    }
}