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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(onContinueClick: () -> Unit) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                    .padding(vertical = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(30.dp)
                ) {
                    Text(
                        text = "Super ID",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.padding(7.dp))
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo do Super ID",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(30.dp))
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .width(161.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Continuar", fontSize = 18.sp, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background // Garante que o fundo do Scaffold seja opaco
    ) { paddingValues ->
        // Conteúdo principal que pode rolar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Título de boas-vindas
            Text(
                text = "Bem-vindo(a) ao SuperID!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
            )

            // Conteúdo da Boas-Vindas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp), // Padding lateral para o texto
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Seja muito bem-vindo ao SuperID, um projeto nascido com propósito, dedicação e muita vontade de inovar. Esperamos que você aproveite essa jornada conosco e que o SuperID se torne seu aliado confiável no mundo digital.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "🚀 Por que o SuperID?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Porque entendemos que guardar e acessar suas credenciais com segurança deveria ser algo simples e inteligente. Com o SuperID, você tem em mãos uma forma moderna e eficiente de gerenciar senhas e até fazer login em sites parceiros sem precisar digitar nenhuma senha - tudo com segurança, praticidade e um toque de inovação.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "🔒 Sua segurança em primeiro lugar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
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