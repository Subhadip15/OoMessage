package com.subhadip.oomessage.crypto

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement

class KeyExchange {
    // Generate an Elliptic Curve Key Pair (Public and Private)
    private val keyPair = KeyPairGenerator.getInstance("EC").apply {
        initialize(256)
    }.generateKeyPair()

    // We will send this public key across the socket
    val publicKeyBytes: ByteArray = keyPair.public.encoded

    // Calculate the AES secret using our private key + their public key
    fun getSharedSecret(peerPublicKeyBytes: ByteArray): ByteArray {
        val keyFactory = KeyFactory.getInstance("EC")
        val peerPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(peerPublicKeyBytes))

        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(keyPair.private)
        keyAgreement.doPhase(peerPublicKey, true)

        // Returns a 32-byte (256-bit) shared secret
        return keyAgreement.generateSecret()
    }
}