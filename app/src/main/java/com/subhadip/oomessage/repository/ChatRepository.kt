package com.subhadip.oomessage.repository

import com.subhadip.oomessage.data.dao.MessageDao
import com.subhadip.oomessage.data.model.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val messageDao: MessageDao
) {

    // Exposes the chat history to the ViewModel.
    // Room automatically updates this Flow when a new message is inserted.
    fun getMessagesForPeer(peerMacAddress: String): Flow<List<Message>> {
        return messageDao.getMessagesWithPeer(peerMacAddress)
    }

    // Called when you type a message and hit "Send"
    suspend fun saveOutgoingMessage(text: String, peerMacAddress: String) {
        val message = Message(
            peerMacAddress = peerMacAddress,
            text = text,
            isSentByMe = true
        )
        messageDao.insertMessage(message)
    }

    // Called when the ConnectionHandler spits out a decrypted incoming message
    suspend fun saveIncomingMessage(text: String, peerMacAddress: String) {
        val message = Message(
            peerMacAddress = peerMacAddress,
            text = text,
            isSentByMe = false
        )
        messageDao.insertMessage(message)
    }
}