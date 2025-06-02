package com.example.superid.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utilitário para operações de criptografia e descriptografia.
 * Utiliza o algoritmo AES no modo CBC com padding PKCS5Padding.
 * Para derivação de chaves a partir de senhas, usa PBKDF2WithHmacSHA256.
 */
object EncryptionUtil {

    // --- Constantes de Configuração ---
    private const val ALGORITHM = "AES" // Algoritmo de criptografia simétrica.
    private const val PBE_ALGORITHM = "PBKDF2WithHmacSHA256" // Algoritmo para derivar a chave da senha.
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding" // Modo de operação e padding do AES.

    private const val KEY_LENGTH_BITS = 256 // Tamanho da chave AES em bits (32 bytes).
    private const val ITERATIONS = 10000    // Número de iterações para PBKDF2.

    /**
     * Deriva uma SecretKey AES de uma senha mestra e um salt.
     * Essencial para segurança, evita o uso direto da senha como chave.
     *
     * @param masterPassword A senha mestra do usuário.
     * @param salt Um array de bytes aleatório (salt) usado na derivação.
     * @return Uma "SecretKey" AES.
     */
    fun deriveKey(masterPassword: String, salt: ByteArray): SecretKey {
        // Prepara a especificação da chave com senha, salt, iterações e tamanho.
        val spec = PBEKeySpec(masterPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        // Obtém a fábrica de chaves para PBKDF2.
        val skf = SecretKeyFactory.getInstance(PBE_ALGORITHM)
        // Gera a chave secreta e a codifica.
        val secretKey = skf.generateSecret(spec).encoded
        // Retorna a chave gerada como SecretKeySpec para AES.
        return SecretKeySpec(secretKey, ALGORITHM)
    }

    /**
     * Gera um salt aleatório de 16 bytes.
     * O salt garante que a mesma senha mestra gere chaves diferentes,
     * aumentando a segurança e resistindo a ataques de dicionário.
     *
     * @return Um array de bytes com o salt gerado.
     */
    fun generateSalt(): ByteArray {
        val random = SecureRandom() // Gerador de números aleatórios seguro.
        val salt = ByteArray(16) // Array de 16 bytes para o salt.
        random.nextBytes(salt) // Preenche com bytes aleatórios.
        return salt
    }

    /**
     * Criptografa um texto usando AES, chave derivada da senha mestra e um IV aleatório.
     *
     * @param plainText O texto a ser criptografado.
     * @param masterPassword A senha mestra do usuário.
     * @param salt O salt usado para derivar a chave (deve ser armazenado).
     * @return Um 'Triple' contendo o texto criptografado (Base64), o IV (Base64) e o salt (Base64),
     * ou 'null' em caso de erro.
     */
    fun encrypt(plainText: String, masterPassword: String, salt: ByteArray): Triple<String, String, String>? {
        return try {
            val key = deriveKey(masterPassword, salt) // Deriva a chave.
            val iv = ByteArray(16) // Cria um IV de 16 bytes.
            SecureRandom().nextBytes(iv) // Gera IV aleatório.
            val ivSpec = IvParameterSpec(iv) // Cria especificação do IV.

            val cipher = Cipher.getInstance(TRANSFORMATION) // Obtém instância do Cipher.
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec) // Inicializa para criptografia.

            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8)) // Criptografa.

            // Retorna dados criptografados, IV e salt, todos em Base64.
            Triple(
                Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
                Base64.encodeToString(iv, Base64.NO_WRAP),
                Base64.encodeToString(salt, Base64.NO_WRAP)
            )
        } catch (e: Exception) {
            e.printStackTrace() // Imprime erro para depuração.
            null
        }
    }

    /**
     * Descriptografa um texto previamente criptografado.
     * Requer o texto criptografado, IV, salt e a mesma senha mestra usados na criptografia.
     *
     * @param encryptedText O texto criptografado (Base64).
     * @param ivString O IV usado na criptografia (Base64).
     * @param saltString O salt usado na derivação da chave (Base64).
     * @param masterPassword A senha mestra original.
     * @return O texto descriptografado (String), ou 'null' em caso de erro.
     */
    fun decrypt(encryptedText: String, ivString: String, saltString: String, masterPassword: String): String? {
        return try {
            // Decodifica dados de Base64 para ByteArray.
            val encryptedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val iv = Base64.decode(ivString, Base64.NO_WRAP)
            val salt = Base64.decode(saltString, Base64.NO_WRAP)

            val key = deriveKey(masterPassword, salt) // Deriva a chave novamente.
            val ivSpec = IvParameterSpec(iv) // Recria especificação do IV.

            val cipher = Cipher.getInstance(TRANSFORMATION) // Obtém instância do Cipher.
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec) // Inicializa para descriptografia.

            val decryptedBytes = cipher.doFinal(encryptedBytes) // Descriptografa.
            String(decryptedBytes, Charsets.UTF_8) // Converte bytes para String.
        } catch (e: Exception) {
            e.printStackTrace() // Imprime erro para depuração.
            null
        }
    }
}