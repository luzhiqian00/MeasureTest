package com.example.myapplication.ble

import java.util.HashMap

/**
 * SampleGatt对象用于存储关于蓝牙GATT属性的信息。
 */
object SampleGatt {

    // 属性UUID和对应名称的HashMap。
    private var attributes: HashMap<String, String> = HashMap<String, String>()

    // 心率测量特征的UUID。
    var HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"

    // 客户端特征配置描述符的UUID。
    var CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

    // 初始化属性映射。
    init {
        // 预定义的属性UUID和名称。
        attributes["0000180d-0000-1000-8000-00805f9b34fb"] = "心率服务"
        attributes["0000180a-0000-1000-8000-00805f9b34fb"] = "设备信息服务"
        // 示例特征。
        attributes[HEART_RATE_MEASUREMENT] = "心率测量"
        attributes["00002a29-0000-1000-8000-00805f9b34fb"] = "制造商名称字符串"
    }

    /**
     * 根据UUID查找属性的名称。
     *
     * @param uuid 属性的UUID。
     * @param defaultName 如果UUID未找到，则返回的默认名称。
     * @return 属性的名称，如果未找到则返回默认名称。
     */
    fun lookup(uuid: String, defaultName: String): String {
        val name = attributes[uuid]
        return name ?: defaultName
    }
}
