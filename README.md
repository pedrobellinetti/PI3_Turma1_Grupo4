# PROJETO INTEGRADOR 3 - Puc Campinas / Engenheira De Software 3° Semestre

## Integrantes:
- Gabriel Branco De Medeiros
- Pedro Bellinetti Silva
- Priscila Amorim Dos Santos
- Wesley Caires Da Costa

# Documentação Completa do Projeto SuperID

## Índice
1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [Requisitos](#requisitos)
3. [Instalação e Uso do Aplicativo (Para Usuários)](#instalação-e-uso-do-aplicativo-para-usuários)
4. [Instalação e Configuração (Para Desenvolvedores)](#instalação-e-configuração-para-desenvolvedores)
5. [Guia de Uso das Telas](#guia-de-uso-das-telas)
6. [Fluxos de Usuário](#fluxos-de-usuário)
7. [Segurança](#segurança)

## Visão Geral do Projeto

O SuperID é um aplicativo de gerenciamento de senhas para Android desenvolvido em Kotlin com Jetpack Compose. Ele permite aos usuários armazenar suas senhas de forma segura, com criptografia, e acessá-las facilmente quando necessário. O aplicativo utiliza o Firebase para autenticação de usuários e armazenamento seguro de dados.

### Principais Funcionalidades

- Autenticação de usuários (registro, login, recuperação de senha)
- Armazenamento seguro de senhas com criptografia
- Visualização e gerenciamento de senhas
- Escaneamento de QR Code para acesso rápido
- Verificação de e-mail para segurança adicional
- Interface moderna com Jetpack Compose

# Requisitos

### Requisitos de Desenvolvimento

- Android Studio Arctic Fox (2020.3.1) ou superior
- Kotlin 1.5.31 ou superior
- Gradle 7.0.2 ou superior
- JDK 11
- Conta no Firebase

### Requisitos de Execução

- Android 8.0 (API 26) ou superior
- Conexão com a internet para autenticação e sincronização de dados
- Google Play Services atualizado

---

## Instalação e Uso do Aplicativo (Para Usuários)

Esta seção é dedicada aos usuários finais que desejam instalar e começar a usar o SuperID em seus dispositivos Android. Se você é um desenvolvedor e deseja configurar o ambiente para contribuir com o projeto, consulte a seção **"Instalação e Configuração (Para Desenvolvedores)"** abaixo.

### Como Baixar e Instalar o SuperID

1.  **Baixe o Arquivo APK:**
    * O aplicativo SuperID está disponível para download como um arquivo APK (Android Package Kit) nesta página de release (role para baixo até a seção "Assets" ou "Ativos").
    * Clique no arquivo **`app-debug.apk`** para iniciar o download em seu dispositivo.

2.  **Permitir Instalações de Fontes Desconhecidas:**
    * Por padrão, o Android bloqueia a instalação de aplicativos obtidos fora da Google Play Store por razões de segurança. Você precisará permitir esta opção temporariamente:
        * Vá para as **Configurações** do seu dispositivo Android.
        * Procure por opções como **"Aplicativos e notificações"**, **"Segurança"**, **"Biometria e segurança"** ou **"Privacidade"** (o nome exato pode variar dependendo da versão do Android e do fabricante do seu telefone).
        * Dentro dessas configurações, procure por **"Instalar aplicativos desconhecidos"** ou **"Fontes desconhecidas"**.
        * Localize o aplicativo que você usou para baixar o APK (geralmente seu navegador, como Google Chrome ou Firefox, ou o aplicativo "Meus Arquivos"/"Gerenciador de Arquivos") e **permita a instalação** a partir dessa fonte.
        * *É altamente recomendável desativar essa permissão após a instalação do SuperID para manter a segurança do seu dispositivo.*

3.  **Instalar o APK:**
    * Após baixar o arquivo `app-debug.apk` e permitir as instalações de fontes desconhecidas, vá até a pasta onde o arquivo foi salvo em seu dispositivo (geralmente a pasta **"Downloads"**).
    * Toque no arquivo `app-debug.apk` para iniciar o processo de instalação.
    * Siga as instruções na tela para concluir a instalação do SuperID.

### Como Começar a Usar o SuperID

Após a instalação, abra o aplicativo SuperID em seu dispositivo:

1.  **Registro:**
    * Se você é um novo usuário, clique em "Registrar" ou "Criar Conta".
    * Preencha seu e-mail e crie uma código.
    * **Importante:** Verifique sua caixa de entrada (incluindo a pasta de spam) para um e-mail de verificação enviado pelo Firebase Authentication. Clique no link para verificar sua conta.

2.  **Login:**
    * Após o registro (e verificação, se aplicável), retorne à tela de login.
    * Insira seu e-mail e código cadastrados.
    * Clique em "Login" para acessar a tela principal do SuperID.

3.  **Gerenciamento de Senhas:**
    * Na tela principal, você poderá adicionar novas senhas, visualizar suas senhas criptografadas e gerenciá-las.
    * **Dica:** Você pode definir uma senha mestre no aplicativo para uma camada extra de segurança ao visualizar suas credenciais.

---

## Instalação e Configuração (Para Desenvolvedores)

### Passo 1: Clonar o Repositório

```bash
git clone [https://github.com/pedrobellinetti/PI3_Turma1_Grupo4.git](https://github.com/pedrobellinetti/PI3_Turma1_Grupo4.git)
cd superid
Passo 2: Configurar o Firebase
Acesse o Console do Firebase
Crie um novo projeto ou use um existente
Adicione um aplicativo Android:
Pacote: com.example.superid
Apelido: SuperID
Baixe o arquivo google-services.json e coloque-o na pasta app/ do projeto
Ative o Firebase Authentication:
No console do Firebase, vá para "Authentication" > "Sign-in method"
Ative o provedor "Email/Password"
Configure o Firestore:
No console do Firebase, vá para "Firestore Database"
Crie um banco de dados em modo de produção
Escolha a região mais próxima de seus usuários
Passo 3: Configurar as Regras de Segurança do Firestore
No console do Firebase, vá para "Firestore Database" > "Rules" e substitua as regras existentes por:

JavaScript

{
  "rules": {
    "senhas": {
      "$senhaId": {
        ".read": "request.auth != null && resource.data.userId == request.auth.uid",
        ".write": "request.auth != null && 
                  (resource == null || resource.data.userId == request.auth.uid) && 
                  request.resource.data.userId == request.auth.uid",
        ".validate": "newData.hasChildren(['nome', 'login', 'senhaCriptografada', 'iv', 'salt', 'userId']) && 
                     newData.child('userId').val() == request.auth.uid"
      }
    },
    "usuarios": {
      "$userId": {
        ".read": "request.auth != null && $userId == request.auth.uid",
        ".write": "request.auth != null && $userId == request.auth.uid",
        ".validate": "newData.hasChildren(['email']) && 
                     newData.child('email').val() == request.auth.token.email"
      }
    }
  }
}
Passo 4: Configurar o Projeto no Android Studio
Abra o projeto no Android Studio
Atualize o arquivo build.gradle (nível do projeto):
<!-- end list -->

Gradle

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
Atualize o arquivo build.gradle (nível do módulo):
<!-- end list -->

Gradle

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.superid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.superid"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependencies for any other desired Firebase products
    // [https://firebase.google.com/docs/android/setup#available-libraries](https://firebase.google.com/docs/android/setup#available-libraries)

    // Banco de Dados
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Autenticação
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Armazenamento
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-storage-ktx")

    // QR Scanner MLKit
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    //CameraX
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.0.0-alpha32")

    // Para usar 'await()' com ProcessCameraProvider.getInstance()
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.material3:material3:1.2.0-beta01")
    implementation("androidx.compose.material:material-icons-extended")

}
Passo 5: Sincronizar o Projeto
Clique em "Sync Now" quando solicitado pelo Android Studio para sincronizar as alterações do Gradle.

Guia de Uso das Telas
LoginForm.kt
Tela de login que permite aos usuários acessarem suas contas.

RegisterScreen.kt
Tela de registro para novos usuários.

PasswordManagerScreen.kt
Tela principal que exibe a lista de senhas do usuário.

PasswordFormScreen.kt
Formulário para adicionar ou editar senhas.

Utilitários
EncryptionUtil.kt
Utilitário para criptografar e descriptografar senhas.

Kotlin

object EncryptionUtil {
    fun encrypt(plainText: String, password: String): EncryptionResult? {
        // Implementação da criptografia
    }
    
    fun decrypt(encryptedData: String, iv: String, salt: String, password: String): String? {
        // Implementação da descriptografia
    }
}

data class EncryptionResult(
    val encryptedData: String,
    val iv: String,
    val salt: String
)
Fluxos de Usuário
Fluxo de Autenticação
Registro:

Usuário acessa a tela de registro
Preenche e-mail e código
Submete o formulário
Recebe e-mail de verificação
Confirma e-mail (opcional)
Login:

Usuário acessa a tela de login
Insere credenciais
Sistema verifica autenticação
Se e-mail não verificado, mostra alerta
Se autenticado, acessa tela principal
Recuperação de Senha:

Usuário acessa tela de recuperação
Insere e-mail
Recebe link de recuperação
Redefine senha
Fluxo de Gerenciamento de Senhas
Visualização de Senhas:

Usuário acessa tela principal
Sistema carrega senhas do usuário
Usuário pode definir senha mestre para visualização
Adição de Senha:

Usuário clica no botão de adicionar
Preenche formulário com detalhes da senha
Sistema criptografa e salva a senha
Edição/Exclusão de Senha:

Usuário seleciona opções em uma senha
Escolhe editar ou excluir
Sistema processa a operação
Escaneamento de QR Code:

Usuário seleciona opção de QR Code
Escaneia código com a câmera
Sistema processa informações do QR Code
Segurança
Autenticação
O SuperID utiliza o Firebase Authentication para gerenciar a autenticação de usuários:

Registro e login com e-mail e código
Verificação de e-mail para segurança adicional
Recuperação de senha via e-mail
Armazenamento Seguro
As senhas são armazenadas de forma segura:

Criptografia Local: Antes de serem enviadas ao Firestore, as senhas são criptografadas localmente usando AES
Senha Mestre: O usuário pode definir uma senha mestre para visualização das senhas
Armazenamento Seguro: Os dados criptografados são armazenados no Firestore
Regras de Segurança do Firestore
As regras de segurança garantem que:

Cada usuário só pode acessar suas próprias senhas
Não é possível modificar senhas de outros usuários
Todas as operações exigem autenticação
Validação de dados é aplicada para garantir integridade
