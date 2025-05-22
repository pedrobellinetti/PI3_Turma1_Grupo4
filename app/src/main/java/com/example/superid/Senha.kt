package com.example.superid

data class Senha(
    val id: String = "",
    val categoria: String = "",
    val login: String = "",
    val descricao: String = "",
    val accessToken: String = "",
    var nome: String = "",

    // Estes devem ser var para o Firestore poder desserializar se o documento for novo/parcial
    var senhaCriptografada: String = "",
    var iv: String = "",
    var salt: String = ""
)
