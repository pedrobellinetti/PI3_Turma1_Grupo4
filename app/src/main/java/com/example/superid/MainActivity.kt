package com.example.superid

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.superid.ui.screens.LoginForm
import com.example.superid.ui.screens.PasswordManagerScreen
import com.example.superid.ui.screens.PasswordRecoveryScreen
import com.example.superid.ui.screens.QrScanScreen
import com.example.superid.ui.screens.UserRegistrationForm
import com.example.superid.ui.screens.WelcomeScreen
import com.example.superid.ui.screens.TermsOfServiceScreen
import com.example.superid.ui.screens.EditPasswordScreen
import com.example.superid.ui.screens.PasswordFormScreen
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.firestore
import com.example.superid.ui.screens.getAndroidId
import android.content.Intent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    // AuthApp é a função Composable que gerencia todo o fluxo de autenticação e navegação.
                    AuthApp(Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current // Obtém o contexto Android para acessar recursos como SharedPreferences e Toast.
    // sharedPreferences: Usado para armazenar dados persistentes e simples, como o estado de aceitação dos termos e login.
    val sharedPreferences = remember { context.getSharedPreferences("SuperID_Prefs", Context.MODE_PRIVATE) }

    // auth: Instância do Firebase Authentication para gerenciar a autenticação de usuários.
    val auth = FirebaseAuth.getInstance()
    // db: Instância do Firebase Firestore, o banco de dados NoSQL baseado em documentos.
    val db = Firebase.firestore

    // currentScreen: Estado mutável que controla qual tela é exibida atualmente.
    var currentScreen by remember { mutableStateOf(AuthScreen.LOADING) }

    // uid: Estado mutável para armazenar o User ID (UID) do usuário logado.
    // É inicializado com o UID do usuário atual do Firebase, se houver.
    val uid = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid ?: "") }

    // passwordDataToEdit: Estado mutável para passar os dados de uma senha para a tela de edição.
    var passwordDataToEdit by remember { mutableStateOf<Senha?>(null) } // Supondo que 'Senha' é uma classe ou data class existente

    // Estado para indicar sucesso no registro
    var registrationSuccess by remember { mutableStateOf(false) }

    /**
     * LaunchedEffect(Unit): Um efeito colateral que é executado apenas uma vez durante a inicialização da tela.
     * Usado para determinar a tela inicial da aplicação ao carregar.
     */
    LaunchedEffect(Unit) {
        val termosAceitos = sharedPreferences.getBoolean("termosAceitos", false)
        val usuarioLogado = auth.currentUser != null
        val primeiroAcessoApp = sharedPreferences.getBoolean("primeiroAcessoApp", true)

        if (usuarioLogado) {
            // Se o usuário já está logado, navega diretamente para o gerenciador de senhas.
            currentScreen = AuthScreen.MAIN_PASSWORD_MANAGER
            uid.value = auth.currentUser?.uid ?: ""
            // Atualiza a flag de primeiro acesso para 'false' se já passou por isso.
            sharedPreferences.edit().putBoolean("primeiroAcessoApp", false).apply()
        } else if (primeiroAcessoApp) {
            // Se é o primeiro acesso ao aplicativo, exibe a tela de boas-vindas.
            currentScreen = AuthScreen.WELCOME
        } else {
            // Se não é o primeiro acesso e o usuário não está logado, verifica a aceitação dos termos.
            if (!termosAceitos) {
                // Se os termos não foram aceitos (ex: app reinstalado, dados limpos), exibe a tela de termos.
                currentScreen = AuthScreen.TERMS
            } else {
                // Caso contrário (termos já aceitos, mas usuário não logado), exibe a tela de login.
                currentScreen = AuthScreen.LOGIN
            }
        }
    }

    /**
     * Box: atua como o host para a tela atualmente selecionada.
     */
    Box(modifier = modifier.fillMaxSize()) {
        /**
         * when (currentScreen): Uma estrutura de controle que exibe uma Composable diferente
         * com base no valor do estado `currentScreen`, controlando a navegação.
         */
        when (currentScreen) {
            AuthScreen.LOADING -> { /* Pode exibir um indicador de carregamento aqui */ }

            AuthScreen.WELCOME -> WelcomeScreen(
                onContinueClick = {
                    // Após a tela de boas-vindas, marca que o primeiro acesso foi concluído
                    // e navega para a tela de termos de serviço.
                    sharedPreferences.edit().putBoolean("primeiroAcessoApp", false).apply()
                    currentScreen = AuthScreen.TERMS
                }
            )

            AuthScreen.TERMS -> {
                // Exibe a tela de Termos.
                TermsOfServiceScreen(
                    onAcceptTermsAndRegisterSuccess = {
                        // Callback acionado quando o usuário aceita os termos.
                        // Atualiza a preferência de termos aceitos e navega para a tela de registro.
                        sharedPreferences.edit().putBoolean("termosAceitos", true).apply()
                        Toast.makeText(context, "Termos aceitos com sucesso!", Toast.LENGTH_SHORT).show()
                        currentScreen = AuthScreen.REGISTER
                    },
                    onDeclineTerms = {
                        // Lida com a recusa dos termos, voltando para a tela de boas-vindas
                        Toast.makeText(context, "Você precisa aceitar os termos para prosseguir.", Toast.LENGTH_LONG).show()
                        currentScreen = AuthScreen.WELCOME
                    }
                )
            }

            AuthScreen.REGISTER -> UserRegistrationForm(
                onNavigateToLogin = { currentScreen = AuthScreen.LOGIN },
                onRegisterAttempt = { data ->
                    // Reinicia o estado de sucesso para evitar reexibição acidental do diálogo
                    registrationSuccess = false
                    auth.createUserWithEmailAndPassword(data.email, data.senha)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val currentUid = user?.uid
                                val androidId = getAndroidId(context)

                                if (currentUid != null && androidId.isNotEmpty()) {
                                    val userData = hashMapOf(
                                        "nome" to data.nome,
                                        "email" to data.email,
                                        "uid" to currentUid,
                                        "imei(Android ID)" to androidId,
                                        "data_criacao" to com.google.firebase.Timestamp.now(),
                                        "isEmailVerified" to false
                                    )

                                    db.collection("users")
                                        .document(currentUid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            user.sendEmailVerification()
                                                ?.addOnCompleteListener { verificationTask ->
                                                    if (!verificationTask.isSuccessful) {
                                                        Toast.makeText(context, "Falha ao enviar e-mail de verificação. Você ainda pode usar outras funcionalidades.", Toast.LENGTH_SHORT).show()
                                                    }
                                                    registrationSuccess = true
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Erro ao salvar dados do usuário: ${e.message}. Tente novamente.", Toast.LENGTH_LONG).show()
                                            currentScreen = AuthScreen.REGISTER
                                        }
                                } else {
                                    Toast.makeText(context, "Erro: Não foi possível obter identificadores do dispositivo. Tente novamente.", Toast.LENGTH_LONG).show()
                                    currentScreen = AuthScreen.REGISTER
                                }
                            } else {
                                val errorMessage = when (task.exception) {
                                    is FirebaseAuthException -> {
                                        when ((task.exception as FirebaseAuthException).errorCode) {
                                            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está em uso. Por favor, faça login ou use outro e-mail."
                                            "ERROR_WEAK_PASSWORD" -> "A senha é muito fraca. Use no mínimo 6 caracteres para sua senha mestre."
                                            else -> "Ocorreu um erro ao realizar o cadastro. ${task.exception?.message}"
                                        }
                                    }
                                    else -> "Ocorreu um erro inesperado durante o cadastro. Verifique sua conexão."
                                }
                                Toast.makeText(context, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
                                currentScreen = AuthScreen.REGISTER
                            }
                        }
                },
                onRegistrationSuccessAndDialogClosed = {
                    currentScreen = AuthScreen.LOGIN
                },
                registrationSuccess = registrationSuccess
            )

            AuthScreen.LOGIN -> LoginForm(
                sharedPreferences = sharedPreferences, // Passa SharedPreferences para a tela de login.
                onNavigateToRegister = { currentScreen = AuthScreen.REGISTER }, // Permite navegar para o registro.
                onLoginSuccess = {
                    // Após um login bem-sucedido, atualiza o estado de login e navega para o gerenciador de senhas.
                    sharedPreferences.edit().putBoolean("usuarioLogado", true).apply()
                    uid.value = auth.currentUser?.uid ?: "" // Atualiza o UID do usuário logado.
                    currentScreen = AuthScreen.MAIN_PASSWORD_MANAGER
                },
                onNavigateToForgotPassword = { currentScreen = AuthScreen.RECOVERY } // Permite navegar para recuperação de senha.
            )

            AuthScreen.MAIN_PASSWORD_MANAGER -> PasswordManagerScreen(
                uid = uid.value, // Passa o UID do usuário logado para a tela.
                onLogout = {
                    // Realiza o logout do Firebase, limpa as preferências de login e retorna à tela de login.
                    auth.signOut()
                    sharedPreferences.edit().putBoolean("usuarioLogado", false).apply()
                    uid.value = ""
                    currentScreen = AuthScreen.LOGIN
                },
                onCreatePassword = { currentScreen = AuthScreen.CREATE_PASSWORD }, // Navega para criar uma nova senha.
                onEditPassword = { senhaToEdit ->
                    passwordDataToEdit = senhaToEdit // Define a senha a ser editada.
                    currentScreen = AuthScreen.EDIT_PASSWORD // Navega para a tela de edição.
                },
                onReadQrCode = { currentScreen = AuthScreen.CAMERA_SCREEN } // Navega para a leitura de QR Code.
            )

            AuthScreen.CREATE_PASSWORD -> PasswordFormScreen(
                uid = uid.value, // Passa o UID para associar a senha ao usuário.
                onSuccess = {
                    Toast.makeText(context, "Senha salva com sucesso!", Toast.LENGTH_SHORT).show()
                    currentScreen = AuthScreen.MAIN_PASSWORD_MANAGER // Retorna ao gerenciador após salvar.
                },
                onBack = { currentScreen = AuthScreen.MAIN_PASSWORD_MANAGER } // Retorna ao gerenciador sem salvar.
            )

            AuthScreen.EDIT_PASSWORD -> EditPasswordScreen(
                uid = uid.value, // Passa o UID.
                senhaToEdit = passwordDataToEdit, // Passa os dados da senha a ser editada.
                onSuccess = {
                    Toast.makeText(context, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                    passwordDataToEdit = null // Limpa o estado da senha em edição.
                    currentScreen = AuthScreen.MAIN_PASSWORD_MANAGER // Retorna ao gerenciador após atualizar.
                },
                onBack = {
                    passwordDataToEdit = null // Limpa o estado da senha em edição.
                    currentScreen = AuthScreen.MAIN_PASSWORD_MANAGER // Retorna ao gerenciador sem atualizar.
                }
            )

            AuthScreen.QR_LOGIN -> QrScanScreen(
                onLoginAprovado = { currentScreen = AuthScreen.MAIN_PASSWORD_MANAGER }, // Login via QR aprovado, vai para o gerenciador.
                onBack = { currentScreen = AuthScreen.LOGIN } // Retorna para a tela de login.
            )

            AuthScreen.RECOVERY -> PasswordRecoveryScreen(
                sharedPreferences = sharedPreferences,
                onNavigateToLogin = { currentScreen = AuthScreen.LOGIN } // Após recuperação, volta para o login.
            )

            AuthScreen.CAMERA_SCREEN -> {
                // Inicia a CameraActivity
                LaunchedEffect(Unit) { // Usa LaunchedEffect para executar o Intent uma única vez
                    val intent = Intent(context, CameraActivity::class.java)
                    context.startActivity(intent)
                    // A tela de navegação subjacente muda para MAIN_PASSWORD_MANAGER imediatamente após o lançamento da CameraActivity.
                    // A CameraActivity continuará a ser exibida sobre MAIN_PASSWORD_MANAGER.
                    currentScreen = AuthScreen.MAIN_PASSWORD_MANAGER
                }
            }
        }
    }
}