package org.example.project.db

actual class BatteryInfo {
    actual fun getBatteryLevel(): Int = 100
    actual fun isCharging(): Boolean = false
}