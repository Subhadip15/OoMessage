package com.subhadip.oomessage.network.socket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class ChatClient {
    private var socket: Socket? = null

    suspend fun connectToServer(serverIp: String, onConnected: (Socket) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                socket = Socket()
                // Try to connect to the Server's IP on port 8888 with a 5-second timeout
                socket?.connect(InetSocketAddress(serverIp, 8888), 5000)

                // If successful, pass the connected socket back to MainActivity
                socket?.let { onConnected(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun disconnect() {
        socket?.close()
    }
}