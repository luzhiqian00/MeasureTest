package com.example.myapplication.event
import org.greenrobot.eventbus.EventBus

object EMEventBus {
    fun register(sub: Any){
        if(!EventBus.getDefault().isRegistered(sub))
            EventBus.getDefault().register(sub)
    }

    fun unRegister(sub: Any){
        EventBus.getDefault().unregister(sub)
    }

    fun postSticky(event:Any){
        EventBus.getDefault().postSticky(event)
    }

    fun post(event:Any){
        EventBus.getDefault().post(event)
    }
}