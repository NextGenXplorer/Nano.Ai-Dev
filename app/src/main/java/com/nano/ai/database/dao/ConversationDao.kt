package com.nano.ai.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nano.ai.models.table_schema.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: Conversation)

    @Update
    suspend fun update(conversation: Conversation)

    @Delete
    suspend fun delete(conversation: Conversation)

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): Conversation?

    @Query("SELECT * FROM conversations WHERE is_archived = 0 ORDER BY updated_at DESC")
    fun getAllActive(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE is_archived = 1 ORDER BY updated_at DESC")
    fun getAllArchived(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations ORDER BY updated_at DESC")
    fun getAll(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE model_id = :modelId ORDER BY updated_at DESC")
    fun getByModelId(modelId: String): Flow<List<Conversation>>

    @Query("UPDATE conversations SET is_archived = :isArchived WHERE id = :id")
    suspend fun updateArchivedStatus(id: String, isArchived: Boolean)

    @Query("UPDATE conversations SET title = :title, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateTitle(id: String, title: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET updated_at = :updatedAt WHERE id = :id")
    suspend fun updateTimestamp(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM conversations WHERE is_archived = 0")
    fun getActiveCount(): Flow<Int>
}
