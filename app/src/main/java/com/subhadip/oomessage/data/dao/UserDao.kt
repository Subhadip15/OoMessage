package com.subhadip.oomessage.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.subhadip.oomessage.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Inserts a new peer, or updates their name/lastConnected time if we've met them before
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: User)

    // Gets a reactive list of all peers we have chatted with
    @Query("SELECT * FROM users ORDER BY lastConnected DESC")
    fun getAllUsers(): Flow<List<User>>

    // Fetches a specific user synchronously
    @Query("SELECT * FROM users WHERE macAddress = :macAddress LIMIT 1")
    suspend fun getUserByMac(macAddress: String): User?
}