package org.example.project.di

import org.example.project.db.DeviceInfo
import org.example.project.db.NetworkMonitor
import org.example.project.db.BatteryInfo
import org.koin.dsl.module

// Platform-specific bindings (DeviceInfo, NetworkMonitor, BatteryInfo)
// are provided via androidPlatformModule() in androidMain
// This module acts as the common declaration
val platformModule = module {
    // DeviceInfo, NetworkMonitor, BatteryInfo are bound in androidPlatformModule
    // to allow Android Context injection
}
