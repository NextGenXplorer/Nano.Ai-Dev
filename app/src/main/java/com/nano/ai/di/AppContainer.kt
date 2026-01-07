package com.nano.ai.di

import android.content.Context
import com.nano.ai.database.AppDatabase
import com.nano.ai.inference.LlamaService
import com.nano.ai.repo.ChatRepository
import com.nano.ai.repo.ModelRepository
import com.nano.ai.repo.SettingsRepository
import com.nano.ai.viewmodel.ChatViewModel
import com.nano.ai.viewmodel.ModelManagerViewModel
import com.nano.ai.viewmodel.SettingsViewModel
import com.nano.ai.viewmodel.ThemeViewModel

object AppContainer {

    // Database
    private lateinit var database: AppDatabase

    // Services
    private lateinit var llamaService: LlamaService

    // Repositories
    private lateinit var modelRepository: ModelRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var settingsRepository: SettingsRepository

    // ViewModels (singletons for shared state)
    private lateinit var themeViewModel: ThemeViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var modelManagerViewModel: ModelManagerViewModel

    // ChatViewModel is created per-instance to allow multiple chat sessions
    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext

        // Initialize database
        database = AppDatabase.getDatabase(applicationContext)

        // Initialize services
        llamaService = LlamaService(applicationContext)

        // Initialize repositories
        modelRepository = ModelRepository(
            modelDao = database.modelDao(),
            configDao = database.modelConfigDao()
        )

        chatRepository = ChatRepository(
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao()
        )

        settingsRepository = SettingsRepository(applicationContext)

        // Initialize singleton ViewModels
        themeViewModel = ThemeViewModel()

        settingsViewModel = SettingsViewModel(
            settingsRepository = settingsRepository
        )

        modelManagerViewModel = ModelManagerViewModel(
            modelRepository = modelRepository,
            llamaService = llamaService,
            settingsRepository = settingsRepository
        )
    }

    // Database
    fun getDatabase() = database

    // Services
    fun getLlamaService() = llamaService

    // Repositories
    fun getModelRepository() = modelRepository
    fun getChatRepository() = chatRepository
    fun getSettingsRepository() = settingsRepository

    // ViewModels
    fun getThemeViewModel() = themeViewModel
    fun getSettingsViewModel() = settingsViewModel
    fun getModelManagerViewModel() = modelManagerViewModel

    // Factory for ChatViewModel (creates new instance for each chat)
    fun createChatViewModel(): ChatViewModel {
        return ChatViewModel(
            chatRepository = chatRepository,
            llamaService = llamaService,
            settingsRepository = settingsRepository
        )
    }
}
