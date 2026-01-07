package com.nano.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nano.ai.repo.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Theme settings
    val darkMode: StateFlow<Boolean> = settingsRepository.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val dynamicColor: StateFlow<Boolean> = settingsRepository.dynamicColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Inference settings
    val temperature: StateFlow<Float> = settingsRepository.temperature
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_TEMPERATURE)

    val topP: StateFlow<Float> = settingsRepository.topP
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_TOP_P)

    val topK: StateFlow<Int> = settingsRepository.topK
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_TOP_K)

    val maxTokens: StateFlow<Int> = settingsRepository.maxTokens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_MAX_TOKENS)

    val repeatPenalty: StateFlow<Float> = settingsRepository.repeatPenalty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_REPEAT_PENALTY)

    val contextLength: StateFlow<Int> = settingsRepository.contextLength
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_CONTEXT_LENGTH)

    // Model settings
    val selectedModelId: StateFlow<String?> = settingsRepository.selectedModelId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val systemPrompt: StateFlow<String> = settingsRepository.systemPrompt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_SYSTEM_PROMPT)

    // UI settings
    val showTokenCount: StateFlow<Boolean> = settingsRepository.showTokenCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val showGenerationTime: StateFlow<Boolean> = settingsRepository.showGenerationTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoScroll: StateFlow<Boolean> = settingsRepository.autoScroll
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Theme setters
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColor(enabled)
        }
    }

    // Inference setters
    fun setTemperature(value: Float) {
        viewModelScope.launch {
            settingsRepository.setTemperature(value)
        }
    }

    fun setTopP(value: Float) {
        viewModelScope.launch {
            settingsRepository.setTopP(value)
        }
    }

    fun setTopK(value: Int) {
        viewModelScope.launch {
            settingsRepository.setTopK(value)
        }
    }

    fun setMaxTokens(value: Int) {
        viewModelScope.launch {
            settingsRepository.setMaxTokens(value)
        }
    }

    fun setRepeatPenalty(value: Float) {
        viewModelScope.launch {
            settingsRepository.setRepeatPenalty(value)
        }
    }

    fun setContextLength(value: Int) {
        viewModelScope.launch {
            settingsRepository.setContextLength(value)
        }
    }

    // Model setters
    fun setSelectedModelId(modelId: String?) {
        viewModelScope.launch {
            settingsRepository.setSelectedModelId(modelId)
        }
    }

    fun setSystemPrompt(prompt: String) {
        viewModelScope.launch {
            settingsRepository.setSystemPrompt(prompt)
        }
    }

    // UI setters
    fun setShowTokenCount(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowTokenCount(enabled)
        }
    }

    fun setShowGenerationTime(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowGenerationTime(enabled)
        }
    }

    fun setAutoScroll(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoScroll(enabled)
        }
    }

    // Reset
    fun resetInferenceSettings() {
        viewModelScope.launch {
            settingsRepository.resetInferenceSettings()
        }
    }

    fun clearAllSettings() {
        viewModelScope.launch {
            settingsRepository.clearAll()
        }
    }
}
