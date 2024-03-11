package com.example.myapplication

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.example.myapplication.ble.BLEService
import com.example.myapplication.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {

    // 伴生对象，类似于静态对象
    companion object {
        private const val TAG = "MainActivity"
    }

    // 使用懒加载初始化绑定视图
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 获取 MeasureButton 按钮的实例
        val button = binding.MeasureButton
        // 动态申请权限
        setPermissionX()

        // 设置按钮点击事件监听器
        button.setOnClickListener{
            val intent = Intent(this,ScanActivity::class.java)
            startActivity(intent)
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
                Manifest.permission.BLUETOOTH_CONNECT
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
