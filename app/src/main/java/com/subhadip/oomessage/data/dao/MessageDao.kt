package com.subhadip.oomessage.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.subhadip.oomessage.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: Message)

    // Gets the chat history for a specific conversation, ordered by time
    @Query("SELECT * FROM messages WHERE peerMacAddress = :macAddress ORDER BY timestamp ASC")
    fun getMessagesWithPeer(macAddress: String): Flow<List<Message>>

    // Optional: Useful for showing a preview of the last message in a chat list UI
    @Query("SELECT * FROM messages WHERE peerMacAddress = :macAddress ORDER BY timestamp DESC LIMIT 1")
    fun getLastMessagePreview(macAddress: String): Flow<Message?>
}