package com.example.loginlibrary.secure

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.proto.KeyTemplate
import com.google.crypto.tink.streamingaead.StreamingAeadConfig


object SecureStorage {
    private const val PREF_NAME = "SecurePrefs"
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPreferences = EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun encryptAndSave(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun decrypt(key: String): String {
        return sharedPreferences.getString(key, "") ?: ""
    }
}

