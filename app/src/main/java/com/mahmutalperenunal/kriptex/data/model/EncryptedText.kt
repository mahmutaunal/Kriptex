package com.mahmutalperenunal.kriptex.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encrypted_texts")
data class EncryptedText(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalText: String,
    val encryptedText: String,
    val qrContent: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)