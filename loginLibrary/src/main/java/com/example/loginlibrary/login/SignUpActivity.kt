package com.example.loginlibrary.login

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginlibrary.databinding.ActivitySignUpBinding
import com.permissionx.guolindev.PermissionX
import okhttp3.*
import java.io.IOException

class SignUpActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }
    private val signUpButton by lazy { binding.SignUpButton }
    private val userNameEdit by lazy { binding.UserNameEdit }
    private val emailEdit by lazy { binding.EmailEdit }
    private val passwordEdit by lazy { binding.PassWordEdit }
    private val passwordAgainEdit by lazy { binding.PassWordAgainEdit }
    private val email2Edit by lazy { binding.Email2Edit }
    private val yanzhengmaButton by lazy { binding.yanzhengmaButton }
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setPermissionX()

        // 添加立即注册按钮点击事件监听器
        signUpButton.setOnClickListener {
            val username = userNameEdit.text.toString()
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            val passwordAgain = passwordAgainEdit.text.toString()
            val verificationCode = email2Edit.text.toString()

            // 检查用户名、密码、电子邮箱、验证码是否都已填写
            if (username.isBlank() || email.isBlank() || password.isBlank() || verificationCode.isBlank()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 检查两次密码是否相同
            if (password != passwordAgain) {
                Toast.makeText(this, "密码不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 发送请求，检查邮箱和验证码是否匹配
            val requestBody = FormBody.Builder()
                .add("username", username)
                .add("email", email)
                .add("password", password)
                .add("verification_code", verificationCode)
                .build()

            val request = Request.Builder()
                .url("http://39.105.8.110:6666/register")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@SignUpActivity, "请求失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.d("SignUpActivity", "请求失败: ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            // 验证码匹配，可以注册
                            Toast.makeText(this@SignUpActivity, "注册成功", Toast.LENGTH_SHORT).show()
                            // 在这里添加注册逻辑
                        } else {
                            Toast.makeText(this@SignUpActivity, "注册失败: ${response.message}", Toast.LENGTH_SHORT).show()
                            Log.d("SignUpActivity", "注册失败: ${response.message}")
                        }
                    }
                }
            })
        }


// 添加发送验证码按钮点击事件监听器
        yanzhengmaButton.setOnClickListener {
            val username = userNameEdit.text.toString()
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            val passwordAgain = passwordAgainEdit.text.toString()
            val email2 = email2Edit.text.toString()

            // 检查用户名、密码、电子邮箱是否都已填写
            if (username.isBlank() || email.isBlank() || password.isBlank() || passwordAgain.isBlank()) {
                // 如果有任何一项未填写，弹出 Toast 提示用户
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 检查密码和验证密码是否相同
            if (password != passwordAgain) {
                // 如果密码和验证密码不相同，弹出 Toast 提示用户
                Toast.makeText(this, "密码不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 构造POST请求发送验证码
            val requestBody = FormBody.Builder()
                .add("email", email)
                .build()

            val request = Request.Builder()
                .url("http://39.105.8.110:6666/send_verification_code")  // 服务器URL
                .post(requestBody)
                .build()

            // 异步执行请求
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@SignUpActivity, "请求失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@SignUpActivity, "验证码已发送", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@SignUpActivity, "发送失败: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })

            // 在这里执行发送验证码逻辑
            // 示例：这里只是简单地打印信息
            println("Send verification code")
        }
    }


    /**
     * 申请动态权限
     */
    private fun setPermissionX(){
        // 初始化权限请求
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.INTERNET
            )
            // 解释请求权限的理由
            .onExplainRequestReason { scope, deniedList ->
                val message = "PermissionX需要您同意以下权限才能正常使用"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }
            // 发起权限请求
            .request { allGranted, _, deniedList ->
                if (!allGranted) {
                    // 如果有权限被拒绝，显示提示消息
                    Toast.makeText(this, "您拒绝了如下权限：$deniedList", Toast.LENGTH_SHORT).show()
                }
            }
    }
}


