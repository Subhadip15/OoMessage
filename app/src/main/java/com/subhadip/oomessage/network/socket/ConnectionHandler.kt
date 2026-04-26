package com.subhadip.oomessage.network.socket

import com.subhadip.oomessage.crypto.CryptoManager
import com.subhadip.oomessage.crypto.KeyExchange
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class ConnectionHandler(
    private val socket: Socket,
    private val onMessageReceived: (String) -> Unit,
    private val onDisconnected: () -> Unit
) {
    private var inputStream: DataInputStream? = null
    private var outputStream: DataOutputStream? = null
    private var listeningJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // Our encryption layer, initialized after the handshake
    private var cryptoManager: CryptoManager? = null

    init {
        try {
            inputStream = DataInputStream(socket.getInputStream())
            outputStream = DataOutputStream(socket.getOutputStream())
            performHandshakeAndListen()
        } catch (e: Exception) {
            e.printStackTrace()
            disconnect()
        }
    }

    private fun performHandshakeAndListen() {
        listeningJob = scope.launch {
            try {
                // --- STEP 1: THE ECDH HANDSHAKE ---
                val keyExchange = KeyExchange()
                val myPublicKey = keyExchange.publicKeyBytes

                // Send my public key size, then the key itself
                outputStream?.writeInt(myPublicKey.size)
                outputStream?.write(myPublicKey)
                outputStream?.flush()

                // Receive peer's public key size, then the key itself
                val peerKeySize = inputStream?.readInt() ?: throw Exception("Handshake failed")
                val peerPublicKey = ByteArray(peerKeySize)
                inputStream?.readFully(peerPublicKey)

                // Derive the shared secret and initialize AES-GCM
                val sharedSecret = keyExchange.getSharedSecret(peerPublicKey)
                cryptoManager = CryptoManager(sharedSecret)

                // --- STEP 2: SECURE MESSAGE LOOP ---
                while (isActive && socket.isConnected) {
                    val encryptedMessage = inputStream?.readUTF()
                    if (encryptedMessage != null) {
                        // Decrypt the AES payload and send plaintext to the Compose UI
                        val decryptedPlaintext = cryptoManager?.decrypt(encryptedMessage)
                        if (decryptedPlaintext != null) {
                            withContext(Dispatchers.Main) {
                                onMessageReceived(decryptedPlaintext)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                disconnect()
            }
        }
    }

    fun sendMessage(plaintext: String) {
        scope.launch {
            try {
                // Encrypt the plaintext into an AES payload before it ever touches the network
                val encryptedMessage = cryptoManager?.encrypt(plaintext)
                if (encryptedMessage != null) {
                    outputStream?.writeUTF(encryptedMessage)
                    outputStream?.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun disconnect() {
        try {
            listeningJob?.cancel()
            inputStream?.close()
            outputStream?.close()
            socket.close()

            // Notify the UI on the main thread
            scope.launch(Dispatchers.Main) {
                onDisconnected()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}