package com.mahmutalperenunal.kriptex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mahmutalperenunal.kriptex.data.dao.EncryptedTextDao
import com.mahmutalperenunal.kriptex.data.model.EncryptedText

@Database(entities = [EncryptedText::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun encryptedTextDao(): EncryptedTextDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crypto_sentinel_db"
                ).build().also { instance = it }
            }
    }
}