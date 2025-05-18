package com.example.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.superid.ui.screens.PasswordManagerScreen
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth

class PasswordManagerScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        setContent {
            SuperIDTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PasswordManagerScreen(
                        uid = uid,
                        onLogout = {  },
                        onCreatePassword = { currentUid ->
                            val intent = Intent(this, PasswordFormActivity::class.java)
                            intent.putExtra("uid", currentUid)
                            startActivity(intent)
                        },
                        onEditPassword = { passwordId ->
                            val intent = Intent(this, PasswordFormActivity::class.java)
                            intent.putExtra("uid", uid)
                            intent.putExtra("passwordId", passwordId)
                            intent.putExtra("isEditMode", true)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
