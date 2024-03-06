package com.example.myapplication.event


import android.os.IBinder


class BLEServiceEvent(val mLBinder:IBinder) {
    var LBinder = mLBinder
    fun getBinder():IBinder{
        return LBinder
    }
}