package com.mahmutalperenunal.kriptex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mahmutalperenunal.kriptex.data.dao.EncryptedTextDao
import com.mahmutalperenunal.kriptex.data.model.EncryptedText

@Database(entities = [EncryptedText::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun encryptedTextDao(): EncryptedTextDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE encrypted_texts ADD COLUMN type TEXT NOT NULL DEFAULT 'TEXT'")
            }
        }

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crypto_sentinel_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}