// File: androidMain/kotlin/org/example/project/di/AppModule.android.kt
// Android-specific platform bindings yang memerlukan Context
package org.example.project.di

import org.example.project.db.BatteryInfo
import org.example.project.db.DatabaseDriverFactory
import org.example.project.db.DeviceInfo
import org.example.project.db.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Android platform module: binding yang butuh androidContext()
fun androidPlatformModule() = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { DeviceInfo() }
    single { NetworkMonitor(androidContext()) }
    single { BatteryInfo(androidContext()) }
}

// Backward-compatibility alias
val appModule = androidPlatformModule()