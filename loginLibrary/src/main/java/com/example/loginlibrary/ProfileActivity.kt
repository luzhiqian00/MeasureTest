package com.example.loginlibrary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.loginlibrary.databinding.ActivityProfileBinding
import com.example.loginlibrary.secure.SecureStorage

class ProfileActivity : AppCompatActivity() {
    private val binding by lazy { ActivityProfileBinding.inflate(layoutInflater) }
    private val exitButton by lazy { binding.exitLoginButton }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        exitButton.setOnClickListener {
            // 删除 userEmailKey
            SecureStorage.encryptAndSave("userEmailKey", "")
            // 删除 passwordKey
            SecureStorage.encryptAndSave("passwordKey", "")
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
    }
}