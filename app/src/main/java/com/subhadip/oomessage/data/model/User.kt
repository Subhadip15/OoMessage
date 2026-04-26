package com.subhadip.oomessage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val macAddress: String, // Unique identifier for devices in Wi-Fi P2P
    val deviceName: String,
    val lastConnected: Long = System.currentTimeMillis()
)