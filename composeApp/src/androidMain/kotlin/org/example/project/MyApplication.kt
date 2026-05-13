// File: androidMain/kotlin/org/example/project/MyApplication.kt
package org.example.project

import android.app.Application
import org.example.project.di.allModules
import org.example.project.di.androidPlatformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MyApplication)
            modules(
                androidPlatformModule(),   // Android-specific: Context, DB driver, sensors
                *allModules.toTypedArray() // 4 common modules: data, network, platform, viewmodel
            )
        }
    }
}