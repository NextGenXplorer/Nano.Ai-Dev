package com.nano.ai.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nano.ai.models.enums.MessageStatus
import com.nano.ai.models.table_schema.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<Message>)

    @Update
    suspend fun update(message: Message)

    @Delete
    suspend fun delete(message: Message)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: String): Message?

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    fun getByConversationId(conversationId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    suspend fun getByConversationIdSync(conversationId: String): List<Message>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: String): Message?

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: MessageStatus)

    @Query("UPDATE messages SET content = :content, status = :status WHERE id = :id")
    suspend fun updateContent(id: String, content: String, status: MessageStatus = MessageStatus.COMPLETED)

    @Query("UPDATE messages SET token_count = :tokenCount, generation_time_ms = :generationTimeMs WHERE id = :id")
    suspend fun updateMetrics(id: String, tokenCount: Int, generationTimeMs: Long)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun deleteByConversationId(conversationId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE conversation_id = :conversationId")
    fun getMessageCount(conversationId: String): Flow<Int>

    @Query("SELECT SUM(token_count) FROM messages WHERE conversation_id = :conversationId")
    suspend fun getTotalTokenCount(conversationId: String): Int?
}
