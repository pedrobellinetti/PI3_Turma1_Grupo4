package com.example.superid.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.R

/**
 * [WelcomeScreen] é uma função Composable que exibe a tela de boas-vindas do aplicativo Super ID.
 * Ela apresenta informações sobre o propósito do aplicativo e um botão para o usuário continuar.
 *
 * @param onContinueClick Callback acionado quando o botão "Continuar" é clicado, navegando para a tela de Termos
 */
@OptIn(ExperimentalMaterial3Api::class) // Indica o uso de APIs experimentais do Material 3.
@Composable
fun WelcomeScreen(onContinueClick: () -> Unit) {
    /**
     * [Scaffold] fornece a estrutura básica de layout para a tela, incluindo:
     * - [topBar]: A barra superior do aplicativo.
     * - [bottomBar]: A barra inferior, que contém o botão "Continuar".
     * - Conteúdo principal: A área central que pode ser rolada.
     */
    Scaffold(
        // Top Bar (Cabeçalho)
        topBar = {
            // [Box] para organizar o fundo colorido e o conteúdo do cabeçalho.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer, // Cor de fundo do cabeçalho.
                    )
                    .padding(vertical = 30.dp), // Preenchimento vertical para o cabeçalho.
                contentAlignment = Alignment.Center // Centraliza o conteúdo horizontalmente.
            ) {
                // [Column] para organizar o título "Super ID" e a logo verticalmente.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Centraliza os itens da coluna.
                    modifier = Modifier
                        .align(Alignment.Center) // Garante que a coluna esteja centralizada no Box.
                        .padding(30.dp) // Preenchimento interno para o conteúdo da coluna.
                ) {
                    // Título do aplicativo.
                    Text(
                        text = "Super ID",
                        style = MaterialTheme.typography.headlineMedium, // Estilo de texto predefinido do Material Theme.
                        color = MaterialTheme.colorScheme.onPrimaryContainer // Cor do texto baseada no esquema de cores.
                    )
                    Spacer(modifier = Modifier.padding(7.dp)) // Espaçamento entre o título e a logo.
                    // Imagem da logo do Super ID.
                    Image(
                        painter = painterResource(id = R.drawable.logo), // Carrega a imagem da pasta de recursos.
                        contentDescription = "Logo do Super ID", // Descrição para acessibilidade.
                        modifier = Modifier
                            .size(80.dp) // Define o tamanho da imagem.
                            .clip(RoundedCornerShape(30.dp)) // Aplica um formato de cantos arredondados.
                    )
                }
            }
        },

        // Bottom Bar (Barra Inferior)
        bottomBar = {
            // [Column] para centralizar o botão "Continuar" na parte inferior.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp), // Preenchimento horizontal e vertical.
                horizontalAlignment = Alignment.CenterHorizontally // Centraliza o botão.
            ) {
                // Botão "Continuar".
                Button(
                    onClick = onContinueClick, // Define a ação do clique no botão.
                    modifier = Modifier
                        .width(161.dp) // Largura fixa do botão.
                        .height(56.dp), // Altura fixa do botão.
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Cor de fundo do botão.
                        contentColor = MaterialTheme.colorScheme.onPrimary // Cor do texto do botão.
                    )
                ) {
                    Text("Continuar", fontSize = 18.sp, style = MaterialTheme.typography.labelLarge) // Texto do botão.
                }
            }
        },

        // Cor de fundo do Scaffold para garantir opacidade e consistência visual.
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Conteúdo Principal (Scrollable)
        // [Column] principal para o conteúdo da tela, que pode ser rolado.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplica os preenchimentos do Scaffold (topBar e bottomBar).
                .verticalScroll(rememberScrollState()), // Permite a rolagem vertical do conteúdo.
            horizontalAlignment = Alignment.CenterHorizontally, // Centraliza os itens horizontalmente.
        ) {
            // Título principal de boas-vindas.
            Text(
                text = "Bem-vindo(a) ao SuperID!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), // Estilo de texto com negrito.
                color = MaterialTheme.colorScheme.onBackground, // Cor do texto.
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp) // Preenchimento superior e inferior.
            )

            // [Column] para organizar o texto explicativo sobre o Super ID.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp), // Preenchimento lateral para o bloco de texto.
                horizontalAlignment = Alignment.Start // Alinha o texto à esquerda.
            ) {
                // Parágrafo introdutório sobre o Super ID.
                Text(
                    text = "Seja muito bem-vindo ao SuperID, um projeto nascido com propósito, dedicação e muita vontade de inovar. Esperamos que você aproveite essa jornada conosco e que o SuperID se torne seu aliado confiável no mundo digital.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Justify, // Justifica o texto para melhor legibilidade.
                    modifier = Modifier.padding(bottom = 16.dp) // Preenchimento inferior.
                )

                // Subtítulo "Por que o SuperID?".
                Text(
                    text = "🚀 Por que o SuperID?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Parágrafo explicando a proposta de valor do Super ID.
                Text(
                    text = "Porque entendemos que guardar e acessar suas credenciais com segurança deveria ser algo simples e inteligente. Com o SuperID, você tem em mãos uma forma moderna e eficiente de gerenciar senhas e até fazer login em sites parceiros sem precisar digitar nenhuma senha - tudo com segurança, praticidade e um toque de inovação.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Subtítulo "Sua segurança em primeiro lugar".
                Text(
                    text = "🔒 Sua segurança em primeiro lugar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Parágrafo sobre a segurança no Super ID.
                Text(
                    text = "Sabemos que proteger seus dados é essencial. Por isso, pensamos com carinho em cada detalhe: desde a criação da sua conta até o uso de criptografia para proteger suas senhas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}