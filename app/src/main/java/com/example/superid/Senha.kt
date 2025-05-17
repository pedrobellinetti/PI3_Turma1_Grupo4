package com.example.superid

data class Senha(
    val id: String = "",
    val categoria: String = "",
    val login: String = "",
    val descricao: String = "",
    val senhaCriptografada: String = "",
    val accessToken: String = "",
    var nome: String = ""
)
