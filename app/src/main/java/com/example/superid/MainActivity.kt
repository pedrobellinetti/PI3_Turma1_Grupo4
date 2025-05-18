package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.screens.LoginForm
import com.example.superid.ui.screens.PasswordManagerScreen
import com.example.superid.ui.screens.PasswordRecoveryScreen
import com.example.superid.ui.screens.QrScanScreen
import com.example.superid.ui.screens.UserRegistrationForm
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    AuthApp(Modifier.padding(paddingValues))
                }
            }
        }
    }
}

// Controlar a tela atual
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthApp(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(AuthScreen.TERMS) }
    val context = LocalContext.current
    val db = Firebase.firestore
    val uid = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid ?: "") }
    val senhas = remember { mutableStateListOf<Senha>() }
    var senhaIdParaEditar by remember { mutableStateOf<String?>(null) } // Estado para armazenar o ID da senha a ser editada

    when (currentScreen) {
        AuthScreen.TERMS -> TermsOfServiceScreen(
            onAcceptTerms = {
                currentScreen = AuthScreen.REGISTER // ou LOGIN, dependendo do fluxo desejado
            },
            onDeclineTerms = {
                Toast.makeText(context, "Você precisa aceitar os termos para usar o aplicativo.", Toast.LENGTH_LONG).show()
            }
        )

        AuthScreen.LOGIN -> LoginForm(
            onNavigateToRegister = { currentScreen = AuthScreen.REGISTER },
            onLoginSuccess = { currentScreen = AuthScreen.MAIN },
            onNavigateToForgotPassword = {
                context.startActivity(Intent(context, RecuperacaoSenhaActivity::class.java))
            }
        )

        AuthScreen.REGISTER -> UserRegistrationForm(
            onNavigateToLogin = { currentScreen = AuthScreen.LOGIN }
        )

        AuthScreen.MAIN -> {
            PasswordManagerScreen(
                uid = uid.value,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    currentScreen = AuthScreen.LOGIN
                },
                onCreatePassword = { currentUid ->
                    val intent = Intent(context, PasswordFormActivity::class.java)
                    intent.putExtra("uid", currentUid)
                    context.startActivity(intent)
                },
                onEditPassword = { senhaId ->
                    senhaIdParaEditar = senhaId
                    val intent = Intent(context, EditPasswordActivity::class.java)
                    intent.putExtra("UID", uid.value)
                    intent.putExtra("SENHA_ID", senhaId)
                    context.startActivity(intent)
                }
            )
        }

        AuthScreen.QR_LOGIN -> QrScanScreen(
            onLoginAprovado = { currentScreen = AuthScreen.MAIN }
        )

        AuthScreen.RECOVERY -> PasswordRecoveryScreen(
            onNavigateToLogin = { currentScreen = AuthScreen.LOGIN }
        )
        else -> {
            //TODO
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    onAcceptTerms: () -> Unit,
    onDeclineTerms: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                title = {
                    Text(
                        stringResource(R.string.terms_of_service_title),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                modifier = Modifier.height(170.dp)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                OutlinedButton(onClick = onDeclineTerms) {
                    Text(stringResource(R.string.decline))
                }
                Button(onClick = onAcceptTerms) {
                    Text(stringResource(R.string.accept))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        stringResource(R.string.terms_of_service_agreement),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Start,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Text(
                stringResource(R.string.terms_of_service_content),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}