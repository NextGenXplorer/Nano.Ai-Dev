package com.nano.ai.repo

import com.nano.ai.database.dao.ConversationDao
import com.nano.ai.database.dao.MessageDao
import com.nano.ai.models.enums.MessageRole
import com.nano.ai.models.enums.MessageStatus
import com.nano.ai.models.table_schema.Conversation
import com.nano.ai.models.table_schema.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {
    // Conversation operations
    fun getAllConversations(): Flow<List<Conversation>> = conversationDao.getAll()

    fun getActiveConversations(): Flow<List<Conversation>> = conversationDao.getAllActive()

    fun getArchivedConversations(): Flow<List<Conversation>> = conversationDao.getAllArchived()

    fun getConversationsByModelId(modelId: String): Flow<List<Conversation>> =
        conversationDao.getByModelId(modelId)

    suspend fun getConversationById(id: String): Conversation? = conversationDao.getById(id)

    suspend fun createConversation(title: String, modelId: String?): Conversation {
        val conversation = Conversation(title = title, modelId = modelId)
        conversationDao.insert(conversation)
        return conversation
    }

    suspend fun updateConversation(conversation: Conversation) = conversationDao.update(conversation)

    suspend fun deleteConversation(conversation: Conversation) = conversationDao.delete(conversation)

    suspend fun deleteConversationById(id: String) = conversationDao.deleteById(id)

    suspend fun archiveConversation(id: String, archive: Boolean = true) =
        conversationDao.updateArchivedStatus(id, archive)

    suspend fun updateConversationTitle(id: String, title: String) =
        conversationDao.updateTitle(id, title)

    fun getActiveConversationCount(): Flow<Int> = conversationDao.getActiveCount()

    // Message operations
    fun getMessages(conversationId: String): Flow<List<Message>> =
        messageDao.getByConversationId(conversationId)

    suspend fun getMessagesSync(conversationId: String): List<Message> =
        messageDao.getByConversationIdSync(conversationId)

    suspend fun getMessageById(id: String): Message? = messageDao.getById(id)

    suspend fun getLastMessage(conversationId: String): Message? =
        messageDao.getLastMessage(conversationId)

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        role: MessageRole,
        status: MessageStatus = MessageStatus.COMPLETED
    ): Message {
        val message = Message(
            conversationId = conversationId,
            content = content,
            role = role,
            status = status
        )
        messageDao.insert(message)
        conversationDao.updateTimestamp(conversationId)
        return message
    }

    suspend fun createPendingAssistantMessage(conversationId: String): Message {
        val message = Message(
            conversationId = conversationId,
            content = "",
            role = MessageRole.ASSISTANT,
            status = MessageStatus.PENDING
        )
        messageDao.insert(message)
        return message
    }

    suspend fun updateMessageContent(id: String, content: String, status: MessageStatus) =
        messageDao.updateContent(id, content, status)

    suspend fun updateMessageStatus(id: String, status: MessageStatus) =
        messageDao.updateStatus(id, status)

    suspend fun updateMessageMetrics(id: String, tokenCount: Int, generationTimeMs: Long) =
        messageDao.updateMetrics(id, tokenCount, generationTimeMs)

    suspend fun deleteMessage(message: Message) = messageDao.delete(message)

    suspend fun deleteMessageById(id: String) = messageDao.deleteById(id)

    suspend fun deleteAllMessagesInConversation(conversationId: String) =
        messageDao.deleteByConversationId(conversationId)

    fun getMessageCount(conversationId: String): Flow<Int> =
        messageDao.getMessageCount(conversationId)

    suspend fun getTotalTokenCount(conversationId: String): Int =
        messageDao.getTotalTokenCount(conversationId) ?: 0
}
