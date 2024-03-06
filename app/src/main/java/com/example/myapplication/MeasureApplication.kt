package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class MeasureApplication:Application() {
    companion object {
        /**
         * @SuppressLint("StaticFieldLeak")：这个注解告诉编译器，
         * 我们在这里使用了一个静态字段引用 context,并且知道这样做可能会导致内存泄漏。
         * 因为 context 在整个应用程序的生命周期中持有一个引用，
         * 而且 Kotlin 不会自动将它置空。因此，我们需要自己确保在合适的时候将其置空，
         * 以避免内存泄漏问题。
         */
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context= applicationContext
    }
}