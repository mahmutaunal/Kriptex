package com.mahmutalperenunal.kriptex.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mahmutalperenunal.kriptex.data.model.EncryptedText
import kotlinx.coroutines.flow.Flow

@Dao
interface EncryptedTextDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(text: EncryptedText)

    @Query("SELECT * FROM encrypted_texts ORDER BY timestamp DESC")
    fun getAll(): Flow<List<EncryptedText>>

    @Delete
    suspend fun delete(text: EncryptedText)
}