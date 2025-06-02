package com.example.superid.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.superid.R
import com.example.superid.data.RegistrationData
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

/**
 * [UserRegistrationForm] é uma função Composable que representa a tela de cadastro de usuário.
 * Ela gerencia a interface do usuário, a validação de entrada e a interação com os callbacks
 * fornecidos para navegação e lógica de registro.
 *
 * @param onNavigateToLogin Callback para navegar para a tela de login.
 * @param onRegisterAttempt Callback acionado quando o usuário tenta se registrar,
 * passando os dados de registro.
 * @param onRegistrationSuccessAndDialogClosed Callback acionado após o usuário fechar o diálogo
 * de sucesso de registro.
 * @param registrationSuccess Booleano que indica se a operação de registro foi bem-sucedida,
 * geralmente observado de um ViewModel ou Activity.
 */
@OptIn(ExperimentalMaterial3Api::class) // APIs experimentais do Material 3.
@Composable
fun UserRegistrationForm(
    onNavigateToLogin: () -> Unit,
    onRegisterAttempt: (data: RegistrationData) -> Unit,
    onRegistrationSuccessAndDialogClosed: () -> Unit,
    registrationSuccess: Boolean
) {
    // Obtém o contexto atual, para operações como exibir Toasts.
    val context = LocalContext.current

    // Estados mutáveis para armazenar os valores dos campos de texto e controlar os erros.
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    // Estados para controlar a exibição de mensagens de erro para cada campo.
    var senhaError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var nomeError by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf("") } // Erro geral não associado a um campo específico.

    // Estado para controlar a visibilidade do diálogo de sucesso de registro.
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Estado para controlar a visibilidade da senha no campo de texto.
    var passwordVisible by remember { mutableStateOf(false) }

    // Gerencia o estado de rolagem da tela, permitindo que o conteúdo seja rolado.
    val scrollState = rememberScrollState()

    /**
     * [LaunchedEffect] observa a variável [registrationSuccess].
     * Se [registrationSuccess] se tornar "true", ele define [showSuccessDialog] como "true",
     * exibindo o diálogo de sucesso.
     */
    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            showSuccessDialog = true
        }
    }

    /**
     * O layout principal do formulário de cadastro.
     * Utiliza [Column] para organizar os elementos verticalmente.
     * - [fillMaxSize] para ocupar todo o espaço disponível.
     * - [imePadding] ajusta o layout quando o teclado virtual aparece.
     * - [verticalScroll] permite a rolagem do conteúdo.
     * - [horizontalAlignment] e [verticalArrangement] centralizam e espalham os elementos.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // [Box] para organizar o fundo colorido e o conteúdo do cabeçalho.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primaryContainer) // Cor de fundo do cabeçalho.
                .padding(vertical = 33.dp)
        ) {
            // [Column] dentro do [Box] para centralizar o título e a logo.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center) // Centraliza o conteúdo dentro do Box.
                    .padding(29.dp)
            ) {
                // Título do aplicativo.
                Text(
                    text = "Super ID",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp)) // Espaçamento entre o título e a logo.
                // Logo do aplicativo.
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo do Super ID",
                    modifier = Modifier
                        .size(80.dp) // Define o tamanho da logo.
                        .clip(RoundedCornerShape(30.dp)) // Aplica um formato arredondado.
                )
            }
        }

        // Mensagem de boas-vindas para o usuário.
        Text(
            text = "Bem-vindo(a) ao Super ID! Cadastre-se",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 30.dp)
        )

        // Campo de nome do usuário.
        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it // Atualiza o estado do nome.
                nomeError = false // Limpa o erro ao digitar.
                generalError = "" // Limpa o erro geral.
            },
            label = { Text("Digite seu nome", style = MaterialTheme.typography.labelLarge) },
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
            singleLine = true, // Permite apenas uma linha de texto.
            shape = RoundedCornerShape(15.dp), // Borda arredondada para o campo.
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (nomeError) Color.Red else MaterialTheme.colorScheme.outline,
            )
        )
        // Exibe mensagem de erro se [nomeError] for true.
        if (nomeError) {
            Text(
                text = "Por favor, digite seu nome.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Campo de e-mail do usuário.
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it // Atualiza o estado do e-mail.
                emailError = false // Limpa o erro ao digitar.
                generalError = "" // Limpa o erro geral.
            },
            label = { Text("Digite seu e-mail", style = MaterialTheme.typography.labelLarge) },
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                // Vermelho se houver erro de e-mail ou erro geral.
                unfocusedBorderColor = if (emailError || generalError.isNotEmpty()) Color.Red else MaterialTheme.colorScheme.outline,
            )
        )
        // Exibe mensagem de erro se [emailError] for true.
        if (emailError) {
            Text(
                text = "Por favor, digite um e-mail válido.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Campo de senha do usuário.
        OutlinedTextField(
            value = senha,
            onValueChange = {
                senha = it // Atualiza o estado da senha.
                senhaError = false // Limpa o erro ao digitar.
                generalError = "" // Limpa o erro geral.
            },
            label = {
                Text("Digite sua senha (mínimo 8 caracteres)", style = MaterialTheme.typography.labelLarge)
            },
            // Define a transformação visual para ocultar/mostrar a senha.
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            // Ícone à direita para alternar a visibilidade da senha.
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            modifier = Modifier
                .width(315.dp)
                .padding(horizontal = 16.dp),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (senhaError) Color.Red else MaterialTheme.colorScheme.outline,
            )
        )
        // Exibe mensagem de erro se [senhaError] for true.
        if (senhaError) {
            Text(
                text = "A senha precisa ter no mínimo 8 caracteres.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Exibe mensagem de erro geral, se houver.
        if (generalError.isNotEmpty()) {
            Text(text = generalError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        // Botão para submeter o formulário de cadastro.
        Button(
            onClick = {
                var hasValidationError = false // Flag para indicar se há erros de validação.
                generalError = "" // Limpa qualquer erro geral anterior.

                // Validação do campo de nome: verifica se está em branco.
                if (nome.isBlank()) {
                    nomeError = true
                    hasValidationError = true
                }
                // Validação do campo de e-mail: verifica se está em branco e se é um e-mail válido.
                if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = true
                    hasValidationError = true
                }
                // Validação do campo de senha: verifica se tem no mínimo 8 caracteres.
                if (senha.length < 8) {
                    senhaError = true
                    hasValidationError = true
                }

                // Se não houver erros de validação, tenta registrar o usuário.
                if (!hasValidationError) {
                    onRegisterAttempt(RegistrationData(nome, email, senha))
                } else {
                    // Exibe um Toast informando o usuário sobre os erros de validação.
                    Toast.makeText(
                        context,
                        "Por favor, preencha todos os campos corretamente.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .width(161.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, // Cor de fundo do botão.
                contentColor = MaterialTheme.colorScheme.onPrimary // Cor do texto do botão.
            )
        ) {
            Text("Cadastrar", style = MaterialTheme.typography.labelLarge)
        }

        // Seção para o link de login, para usuários que já possuem conta.
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            // Botão de texto que navega para a tela de login.
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "Já tem conta? Login",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }

    /**
     * [AlertDialog] que é exibido quando o registro é bem-sucedido.
     * Ele informa o usuário sobre o sucesso do cadastro e a importância da verificação de e-mail.
     * O "onDismissRequest" está vazio, o que significa que o diálogo só pode ser fechado pelo botão.
     */
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* O diálogo não é dismissível pelo toque fora ou botão voltar */ },
            title = {
                Text("Cadastro Realizado com Sucesso!")
            },
            text = {
                Text("Seu cadastro foi concluído. Você pode fazer login agora. A verificação do e-mail não é obrigatória para o login normal, mas é essencial para usar a funcionalidade de 'Login Sem Senha' e Recuperação de senha no futuro. Recomendamos a verificação para sua segurança e conveniência.")
            },
            confirmButton = {
                // Botão "Fazer Login" que fecha o diálogo e aciona o callback de sucesso.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        showSuccessDialog = false // Esconde o diálogo.
                        onRegistrationSuccessAndDialogClosed() // Aciona o callback.
                    }) {
                        Text("Fazer Login")
                    }
                }
            },
        )
    }
}