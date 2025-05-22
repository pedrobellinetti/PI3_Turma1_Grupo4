package com.example.superid.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {

    private const val ALGORITHM = "AES"
    private const val PBE_ALGORITHM = "PBKDF2WithHmacSHA256" // Algoritmo para derivar a chave
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding" // Modo de criptografia

    private const val KEY_LENGTH_BITS = 256 // Tamanho da chave AES em bits (256 bits = 32 bytes)
    private const val ITERATIONS = 10000    // Número de iterações para PBKDF2

    // --- Derivação da Chave AES a partir da Senha Mestra ---
    fun deriveKey(masterPassword: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(masterPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val skf = SecretKeyFactory.getInstance(PBE_ALGORITHM)
        val secretKey = skf.generateSecret(spec).encoded
        return SecretKeySpec(secretKey, ALGORITHM)
    }

    // --- Geração de Salt ---
    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16) // 16 bytes para o salt (128 bits)
        random.nextBytes(salt)
        return salt
    }

    // --- Criptografia ---
    fun encrypt(plainText: String, masterPassword: String, salt: ByteArray): Triple<String, String, String>? {
        return try {
            val key = deriveKey(masterPassword, salt)
            val iv = ByteArray(16) // 16 bytes para o IV (para AES/CBC)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            Triple(
                Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
                Base64.encodeToString(iv, Base64.NO_WRAP),
                Base64.encodeToString(salt, Base64.NO_WRAP)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- Descriptografia ---
    fun decrypt(encryptedText: String, ivString: String, saltString: String, masterPassword: String): String? {
        return try {
            val encryptedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val iv = Base64.decode(ivString, Base64.NO_WRAP)
            val salt = Base64.decode(saltString, Base64.NO_WRAP)

            val key = deriveKey(masterPassword, salt)
            val ivSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}