package org.example.project.db

import kotlinx.coroutines.flow.Flow

expect class NetworkMonitor {
    fun isConnected(): Boolean
    fun observeConnectivity(): Flow<Boolean>
}