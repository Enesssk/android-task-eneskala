package com.eneskala.androidtaskkotlin.data.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class TokenManager(private val context: Context) {

    private val keyAlias = "my_key_alias"
    private val keyStore =
        java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    init {
        generateKey()
    }

    private fun generateKey() {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    fun saveToken(token: String) {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
            val iv = cipher.iv
            val encryptedToken = cipher.doFinal(token.toByteArray())

            context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().apply {
                putString("token_iv", Base64.encodeToString(iv, Base64.DEFAULT))
                putString("token", Base64.encodeToString(encryptedToken, Base64.DEFAULT))
                apply()
            }
            Log.d("TokenManager", "Token successfully saved.")
        } catch (e: Exception) {
            Log.e("TokenManager", "Error saving token", e)
        }
    }

    fun getToken(): String? {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val ivString = prefs.getString("token_iv", null)
        val encryptedTokenString = prefs.getString("token", null)

        if (ivString == null || encryptedTokenString == null) {
            Log.d("TokenManager", "Token or IV not found")
            return null
        }

        return try {
            val iv = Base64.decode(ivString, Base64.DEFAULT) // IV'yi geri al
            val encryptedToken = Base64.decode(encryptedTokenString, Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                getKey(),
                GCMParameterSpec(128, iv)
            )
            val decryptedToken = String(cipher.doFinal(encryptedToken))
            Log.d("TokenManager", "Retrieved Token: $decryptedToken")
            decryptedToken
        } catch (e: Exception) {
            Log.e("TokenManager", "Error retrieving token", e)
            null
        }
    }

    fun clearToken() {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun getKey(): SecretKey {
        return keyStore.getKey(keyAlias, null) as SecretKey
    }
}