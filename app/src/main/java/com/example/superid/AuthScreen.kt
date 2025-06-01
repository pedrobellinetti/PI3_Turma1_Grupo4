package com.example.superid

// Define as diferentes telas da aplicação para facilitar a navegação
enum class AuthScreen {
    LOADING,
    WELCOME,
    TERMS,
    LOGIN,
    REGISTER,
    MAIN_PASSWORD_MANAGER, // Tela principal de gerenciamento de senhas
    CREATE_PASSWORD,       // Tela para criar uma nova senha
    EDIT_PASSWORD,         // Tela para editar uma senha existente
    QR_LOGIN,              // Tela de login por QR Code
    RECOVERY               // Tela de recuperação de senha
}