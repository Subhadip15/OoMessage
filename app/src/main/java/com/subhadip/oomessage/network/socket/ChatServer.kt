package com.subhadip.oomessage.network.socket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.Socket

class ChatServer(private val port: Int = 8888) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    suspend fun startServer(onClientConnected: (Socket) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true

                // Keep listening for incoming connections
                while (isRunning) {
                    val clientSocket = serverSocket!!.accept() // This blocks until a peer connects
                    onClientConnected(clientSocket)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopServer()
            }
        }
    }

    fun stopServer() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}