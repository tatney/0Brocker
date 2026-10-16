package com.homeapp

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Android Keystore protects refresh tokens; preferences contain only ciphertext. */
class SecureCloudSession(context: Context) {
    private val prefs=context.getSharedPreferences("cloud-session",Context.MODE_PRIVATE)
    private val alias="homeapp-cloud-session-v1"
    private fun key(): SecretKey {
        val store=KeyStore.getInstance("AndroidKeyStore").apply {load(null)}
        (store.getKey(alias,null) as? SecretKey)?.let {return it}
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    fun save(value: String?) {
        if(value==null) {prefs.edit().clear().apply();return}
        val cipher=Cipher.getInstance("AES/GCM/NoPadding").apply {init(Cipher.ENCRYPT_MODE,key())}
        val encrypted=cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        prefs.edit().putString("data",Base64.encodeToString(encrypted,Base64.NO_WRAP)).putString("iv",Base64.encodeToString(cipher.iv,Base64.NO_WRAP)).apply()
    }
    fun load(): String? = runCatching {
        val data=prefs.getString("data",null)?:return null
        val iv=prefs.getString("iv",null)?:return null
        Cipher.getInstance("AES/GCM/NoPadding").apply {init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,Base64.decode(iv,Base64.NO_WRAP)))}.doFinal(Base64.decode(data,Base64.NO_WRAP)).toString(Charsets.UTF_8)
    }.getOrNull()
}
