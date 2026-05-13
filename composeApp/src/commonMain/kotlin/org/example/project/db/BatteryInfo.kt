package org.example.project.db

expect class BatteryInfo {
    fun getBatteryLevel(): Int
    fun isCharging(): Boolean
}