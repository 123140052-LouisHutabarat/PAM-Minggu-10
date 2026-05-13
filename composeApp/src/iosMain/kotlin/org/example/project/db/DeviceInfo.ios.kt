package org.example.project.db

actual class DeviceInfo {
    actual fun getDeviceName(): String = "iOS Device"
    actual fun getOsVersion(): String = "iOS"
    actual fun getAppVersion(): String = "1.0.0"
}