package com.nano.ai.models.table_schema

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nano.ai.models.enums.MessageRole
import com.nano.ai.models.enums.MessageStatus
import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversation_id"])]
)
data class Message(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "conversation_id")
    val conversationId: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "role")
    val role: MessageRole,

    @ColumnInfo(name = "status")
    val status: MessageStatus = MessageStatus.COMPLETED,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "token_count")
    val tokenCount: Int? = null,

    @ColumnInfo(name = "generation_time_ms")
    val generationTimeMs: Long? = null
)
