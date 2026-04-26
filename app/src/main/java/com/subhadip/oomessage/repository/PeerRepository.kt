package com.subhadip.oomessage.repository

import com.subhadip.oomessage.data.dao.UserDao
import com.subhadip.oomessage.data.model.User
import kotlinx.coroutines.flow.Flow

class PeerRepository(private val userDao: UserDao) {

    // Returns a continuous Flow of all known peers.
    // The UI can collect this to display the contact list.
    fun getAllKnownPeers(): Flow<List<User>> = userDao.getAllUsers()

    suspend fun saveOrUpdatePeer(macAddress: String, deviceName: String) {
        val user = User(
            macAddress = macAddress,
            deviceName = deviceName,
            lastConnected = System.currentTimeMillis()
        )
        userDao.insertOrUpdateUser(user)
    }

    suspend fun getPeerByMac(macAddress: String): User? {
        return userDao.getUserByMac(macAddress)
    }
}