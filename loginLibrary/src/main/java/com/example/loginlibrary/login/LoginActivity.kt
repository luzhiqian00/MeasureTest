package com.example.loginlibrary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.loginlibrary.databinding.ActivityLoginBinding
import com.example.loginlibrary.login.SignUpActivity
import com.example.loginlibrary.secure.SecureStorage
import okhttp3.*
import java.io.IOException


class LoginActivity : AppCompatActivity() {
    // 伴生对象，类似于静态对象
    companion object {
        private const val TAG = "LoginActivity"
        private const val ipAddress = "http://39.105.8.110:6666"
    }
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val loginButton by lazy { binding.LoginButton }
    private val signUpButton by lazy { binding.SignUpButton }
    private val UserEmailEdit by lazy{binding.UserEmailEdit}
    private val passwordEdit by lazy { binding.PassWordEdit }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SecureStorage.init(this)
        // 检查是否存在记住的凭据
        val storedUserEmail = SecureStorage.decrypt("userEmailKey")
        val storedPassword = SecureStorage.decrypt("passwordKey")



        // 尝试自动登录
        if (storedUserEmail.isNotEmpty() && storedPassword.isNotEmpty()) {
            performLogin(storedUserEmail, storedPassword)
        }


        setContentView(binding.root)
        loginButton.setOnClickListener{
            val userEmail = UserEmailEdit.text.toString()
            val password = passwordEdit.text.toString()

            if (userEmail.isNotEmpty() && password.isNotEmpty()) {
                performLogin(userEmail, password)
            } else {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // 添加注册按钮点击事件监听器
        signUpButton.setOnClickListener {
            val intent = Intent(this, com.example.loginlibrary.login.SignUpActivity::class.java)
            startActivity(intent)
        }
    }



    private fun performLogin(email: String, password: String) {
        val url = "$ipAddress/login"  // 修改为你的后端登录路由

        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        // 将用户邮箱和密码加密并保存到安全存储中
                        SecureStorage.encryptAndSave("userEmailKey", email)
                        SecureStorage.encryptAndSave("passwordKey", password)

                        // 在子模块中创建和启动Intent
                        val intent = Intent("com.example.MAIN_ACTIVITY")
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        this@LoginActivity.startActivity(intent)

                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}