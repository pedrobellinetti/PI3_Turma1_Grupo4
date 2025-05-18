package com.example.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.superid.ui.screens.EditPasswordScreen
import com.example.superid.ui.theme.SuperIDTheme

class EditPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = intent.getStringExtra("UID")
        val senhaId = intent.getStringExtra("SENHA_ID")

        if (uid == null || senhaId == null) {
            finish()
            return
        }

        setContent {
            SuperIDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditPasswordScreen(
                        senhaId = senhaId,
                        uid = uid,
                        onSenhaAtualizada = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}