**PROJETO INTEGRADOR 3 - Puc Campinas / Engenheira De Software 3° Semestre**

# Integrantes:
-  Gabriel Branco De Medeiros
-  Pedro Bellinetti Silva
-  Priscila Amorim Dos Santos
-  Wesley Caires Da Costa

O objetivo principal do projeto SuperID é desenvolver um gerenciamento de autenticações que permita aos usuários gerenciar suas credenciais de forma segura e realizar logins sem senha em sites parceiros. O projeto é dividido em duas partes:

Aplicativo Mobile (Android): Desenvolvido em Kotlin, o aplicativo permite ao usuário criar uma conta, armazenar senhas de forma criptografada e gerenciar suas credenciais.
Integração Web: Através de APIs e Firebase Functions, o SuperID permite que sites parceiros ofereçam a opção de login sem senha, utilizando um sistema de autenticação baseado em QR Code.

## Índice
1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [Requisitos](#requisitos)
3. [Instalação e configuração](#instalação-e-configuração)
4. [Guia de Uso das Telas](#guia-de-uso-das-telas)

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

## Instalação e Configuração

### Passo 1: Clonar o Repositório

```bash
git clone https://github.com/seu-usuario/superid.git
cd superid
```

### Passo 2: Configurar o Firebase

1. Acesse o [Console do Firebase](https://console.firebase.google.com/)
2. Crie um novo projeto ou use um existente
3. Adicione um aplicativo Android:
   - Pacote: `com.example.superid`
   - Apelido: `SuperID`
4. Baixe o arquivo `google-services.json` e coloque-o na pasta `app/` do projeto
5. Ative o Firebase Authentication:
   - No console do Firebase, vá para "Authentication" > "Sign-in method"
   - Ative o provedor "Email/Password"
6. Configure o Firestore:
   - No console do Firebase, vá para "Firestore Database"
   - Crie um banco de dados em modo de produção
   - Escolha a região mais próxima de seus usuários

### Passo 3: Configurar as Regras de Segurança do Firestore

No console do Firebase, vá para "Firestore Database" > "Rules" e substitua as regras existentes por:

```javascript
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
```

### Passo 4: Configurar o Projeto no Android Studio

1. Abra o projeto no Android Studio
2. Atualize o arquivo `build.gradle` (nível do projeto):

```gradle
buildscript {
    ext {
        compose_version = '1.1.1'
        kotlin_version = '1.6.10'
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.0.4'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath 'com.google.gms:google-services:4.3.10'
    }
}
```

3. Atualize o arquivo `build.gradle` (nível do módulo):

```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'com.google.gms.google-services'
}

android {
    compileSdk 32

    defaultConfig {
        applicationId "com.example.superid"
        minSdk 26
        targetSdk 32
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = '11'
    }
    buildFeatures {
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion compose_version
    }
    packagingOptions {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
}

dependencies {
    // Android Core
    implementation 'androidx.core:core-ktx:1.7.0'
    implementation 'androidx.appcompat:appcompat:1.4.1'
    implementation 'com.google.android.material:material:1.5.0'
    
    // Jetpack Compose
    implementation "androidx.compose.ui:ui:$compose_version"
    implementation "androidx.compose.material3:material3:1.0.0-alpha13"
    implementation "androidx.compose.ui:ui-tooling-preview:$compose_version"
    implementation "androidx.activity:activity-compose:1.4.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.4.1"
    
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:30.0.0')
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.1'
    
    // QR Code
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
    androidTestImplementation "androidx.compose.ui:ui-test-junit4:$compose_version"
    debugImplementation "androidx.compose.ui:ui-tooling:$compose_version"
}
```

### Passo 5: Sincronizar o Projeto

Clique em "Sync Now" quando solicitado pelo Android Studio para sincronizar as alterações do Gradle.

### Telas Principais

#### LoginForm.kt

Tela de login que permite aos usuários acessarem suas contas.

#### RegisterScreen.kt

Tela de registro para novos usuários.

#### PasswordManagerScreen.kt

Tela principal que exibe a lista de senhas do usuário.

#### PasswordFormScreen.kt

Formulário para adicionar ou editar senhas.
