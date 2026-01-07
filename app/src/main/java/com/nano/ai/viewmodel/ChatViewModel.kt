package com.nano.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nano.ai.inference.InferenceConfig
import com.nano.ai.inference.InferenceState
import com.nano.ai.inference.LlamaService
import com.nano.ai.inference.PromptBuilder
import com.nano.ai.inference.PromptFormat
import com.nano.ai.models.enums.MessageRole
import com.nano.ai.models.enums.MessageStatus
import com.nano.ai.models.table_schema.Conversation
import com.nano.ai.models.table_schema.Message
import com.nano.ai.repo.ChatRepository
import com.nano.ai.repo.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val llamaService: LlamaService,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    val inferenceState: StateFlow<InferenceState> = llamaService.state

    val conversations = chatRepository.getActiveConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var generationJob: Job? = null
    private var currentAssistantMessageId: String? = null

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = chatRepository.getConversationById(conversationId)
            _currentConversation.value = conversation

            if (conversation != null) {
                chatRepository.getMessages(conversationId).collect { msgs ->
                    _messages.value = msgs
                }
            }
        }
    }

    fun createNewConversation(title: String = "New Chat") {
        viewModelScope.launch {
            val selectedModelId = settingsRepository.selectedModelId.first()
            val conversation = chatRepository.createConversation(title, selectedModelId)
            _currentConversation.value = conversation
            _messages.value = emptyList()
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isGenerating.value) return

        val conversation = _currentConversation.value ?: run {
            viewModelScope.launch {
                createNewConversation(text.take(50))
                sendMessageInternal(text)
            }
            return
        }

        viewModelScope.launch {
            sendMessageInternal(text)
        }
    }

    private suspend fun sendMessageInternal(text: String) {
        val conversation = _currentConversation.value ?: return

        // Clear input
        _inputText.value = ""

        // Add user message
        val userMessage = chatRepository.sendMessage(
            conversationId = conversation.id,
            content = text,
            role = MessageRole.USER
        )

        // Update local state
        _messages.value = _messages.value + userMessage

        // Generate response
        generateResponse()
    }

    private fun generateResponse() {
        val conversation = _currentConversation.value ?: return

        if (!llamaService.isModelLoaded()) {
            viewModelScope.launch {
                val errorMessage = chatRepository.sendMessage(
                    conversationId = conversation.id,
                    content = "Error: No model loaded. Please load a model first.",
                    role = MessageRole.ASSISTANT,
                    status = MessageStatus.ERROR
                )
                _messages.value = _messages.value + errorMessage
            }
            return
        }

        generationJob = viewModelScope.launch {
            _isGenerating.value = true

            try {
                // Create pending assistant message
                val assistantMessage = chatRepository.createPendingAssistantMessage(conversation.id)
                currentAssistantMessageId = assistantMessage.id
                _messages.value = _messages.value + assistantMessage

                // Build prompt
                val systemPrompt = settingsRepository.systemPrompt.first()
                val promptFormat = llamaService.getCurrentModelPath()?.let {
                    PromptFormat.fromModelName(it)
                } ?: PromptFormat.CHATML

                val promptBuilder = PromptBuilder(promptFormat)
                val prompt = promptBuilder.buildPrompt(_messages.value.filter {
                    it.status == MessageStatus.COMPLETED && it.id != assistantMessage.id
                }, systemPrompt)

                // Get inference config from settings
                val config = InferenceConfig(
                    temperature = settingsRepository.temperature.first(),
                    topP = settingsRepository.topP.first(),
                    topK = settingsRepository.topK.first(),
                    maxTokens = settingsRepository.maxTokens.first(),
                    repeatPenalty = settingsRepository.repeatPenalty.first(),
                    contextLength = settingsRepository.contextLength.first(),
                    stopSequences = promptBuilder.getStopTokens()
                )

                // Generate and stream response
                val responseBuilder = StringBuilder()
                val startTime = System.currentTimeMillis()

                llamaService.generate(prompt, config).collect { token ->
                    responseBuilder.append(token)

                    // Update message content with streaming status
                    chatRepository.updateMessageContent(
                        assistantMessage.id,
                        responseBuilder.toString(),
                        MessageStatus.STREAMING
                    )

                    // Update local state
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == assistantMessage.id) {
                            msg.copy(content = responseBuilder.toString(), status = MessageStatus.STREAMING)
                        } else msg
                    }
                }

                // Finalize message
                val generationTimeMs = System.currentTimeMillis() - startTime
                chatRepository.updateMessageContent(
                    assistantMessage.id,
                    responseBuilder.toString(),
                    MessageStatus.COMPLETED
                )
                chatRepository.updateMessageMetrics(
                    assistantMessage.id,
                    responseBuilder.length, // Approximate token count
                    generationTimeMs
                )

                // Update local state
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == assistantMessage.id) {
                        msg.copy(
                            content = responseBuilder.toString(),
                            status = MessageStatus.COMPLETED,
                            generationTimeMs = generationTimeMs
                        )
                    } else msg
                }

                // Update conversation title if it's the first message
                if (_messages.value.size <= 2) {
                    chatRepository.updateConversationTitle(
                        conversation.id,
                        _messages.value.firstOrNull { it.role == MessageRole.USER }?.content?.take(50) ?: "New Chat"
                    )
                }

            } catch (e: Exception) {
                currentAssistantMessageId?.let { msgId ->
                    chatRepository.updateMessageContent(msgId, "Error: ${e.message}", MessageStatus.ERROR)
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == msgId) {
                            msg.copy(content = "Error: ${e.message}", status = MessageStatus.ERROR)
                        } else msg
                    }
                }
            } finally {
                _isGenerating.value = false
                currentAssistantMessageId = null
            }
        }
    }

    fun stopGeneration() {
        llamaService.stopGeneration()
        generationJob?.cancel()
        _isGenerating.value = false
    }

    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            chatRepository.deleteMessage(message)
            _messages.value = _messages.value.filter { it.id != message.id }
        }
    }

    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversation)
            if (_currentConversation.value?.id == conversation.id) {
                _currentConversation.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun archiveConversation(conversationId: String) {
        viewModelScope.launch {
            chatRepository.archiveConversation(conversationId)
        }
    }

    fun clearCurrentConversation() {
        _currentConversation.value = null
        _messages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        generationJob?.cancel()
    }
}
