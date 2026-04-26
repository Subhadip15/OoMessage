package com.subhadip.oomessage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val peerMacAddress: String, // The MAC address of the person you are chatting with
    val text: String,           // The decrypted text payload
    val timestamp: Long = System.currentTimeMillis(),
    val isSentByMe: Boolean     // True if you sent it, False if you received it
)