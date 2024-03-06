package com.example.myapplication.event

/**
 * EventBus传递的消息
 */
enum class EventEnum {
    NEW_TASK,       // 任务报表类型刷新
    NEW_TASK_STATUS // 任务状态刷新
}