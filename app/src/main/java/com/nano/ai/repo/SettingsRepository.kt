package com.nano.ai.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        // Theme settings
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")

        // Inference settings
        private val TEMPERATURE = floatPreferencesKey("temperature")
        private val TOP_P = floatPreferencesKey("top_p")
        private val TOP_K = intPreferencesKey("top_k")
        private val MAX_TOKENS = intPreferencesKey("max_tokens")
        private val REPEAT_PENALTY = floatPreferencesKey("repeat_penalty")
        private val CONTEXT_LENGTH = intPreferencesKey("context_length")

        // Model settings
        private val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
        private val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")

        // UI settings
        private val SHOW_TOKEN_COUNT = booleanPreferencesKey("show_token_count")
        private val SHOW_GENERATION_TIME = booleanPreferencesKey("show_generation_time")
        private val AUTO_SCROLL = booleanPreferencesKey("auto_scroll")

        // Defaults
        const val DEFAULT_TEMPERATURE = 0.7f
        const val DEFAULT_TOP_P = 0.9f
        const val DEFAULT_TOP_K = 40
        const val DEFAULT_MAX_TOKENS = 2048
        const val DEFAULT_REPEAT_PENALTY = 1.1f
        const val DEFAULT_CONTEXT_LENGTH = 4096
        const val DEFAULT_SYSTEM_PROMPT = "You are a helpful AI assistant."
    }

    // Theme settings
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLOR] ?: true }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    // Inference settings
    val temperature: Flow<Float> = context.dataStore.data.map { it[TEMPERATURE] ?: DEFAULT_TEMPERATURE }
    val topP: Flow<Float> = context.dataStore.data.map { it[TOP_P] ?: DEFAULT_TOP_P }
    val topK: Flow<Int> = context.dataStore.data.map { it[TOP_K] ?: DEFAULT_TOP_K }
    val maxTokens: Flow<Int> = context.dataStore.data.map { it[MAX_TOKENS] ?: DEFAULT_MAX_TOKENS }
    val repeatPenalty: Flow<Float> = context.dataStore.data.map { it[REPEAT_PENALTY] ?: DEFAULT_REPEAT_PENALTY }
    val contextLength: Flow<Int> = context.dataStore.data.map { it[CONTEXT_LENGTH] ?: DEFAULT_CONTEXT_LENGTH }

    suspend fun setTemperature(value: Float) {
        context.dataStore.edit { it[TEMPERATURE] = value }
    }

    suspend fun setTopP(value: Float) {
        context.dataStore.edit { it[TOP_P] = value }
    }

    suspend fun setTopK(value: Int) {
        context.dataStore.edit { it[TOP_K] = value }
    }

    suspend fun setMaxTokens(value: Int) {
        context.dataStore.edit { it[MAX_TOKENS] = value }
    }

    suspend fun setRepeatPenalty(value: Float) {
        context.dataStore.edit { it[REPEAT_PENALTY] = value }
    }

    suspend fun setContextLength(value: Int) {
        context.dataStore.edit { it[CONTEXT_LENGTH] = value }
    }

    // Model settings
    val selectedModelId: Flow<String?> = context.dataStore.data.map { it[SELECTED_MODEL_ID] }
    val systemPrompt: Flow<String> = context.dataStore.data.map { it[SYSTEM_PROMPT] ?: DEFAULT_SYSTEM_PROMPT }

    suspend fun setSelectedModelId(modelId: String?) {
        context.dataStore.edit {
            if (modelId != null) {
                it[SELECTED_MODEL_ID] = modelId
            } else {
                it.remove(SELECTED_MODEL_ID)
            }
        }
    }

    suspend fun setSystemPrompt(prompt: String) {
        context.dataStore.edit { it[SYSTEM_PROMPT] = prompt }
    }

    // UI settings
    val showTokenCount: Flow<Boolean> = context.dataStore.data.map { it[SHOW_TOKEN_COUNT] ?: true }
    val showGenerationTime: Flow<Boolean> = context.dataStore.data.map { it[SHOW_GENERATION_TIME] ?: true }
    val autoScroll: Flow<Boolean> = context.dataStore.data.map { it[AUTO_SCROLL] ?: true }

    suspend fun setShowTokenCount(enabled: Boolean) {
        context.dataStore.edit { it[SHOW_TOKEN_COUNT] = enabled }
    }

    suspend fun setShowGenerationTime(enabled: Boolean) {
        context.dataStore.edit { it[SHOW_GENERATION_TIME] = enabled }
    }

    suspend fun setAutoScroll(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_SCROLL] = enabled }
    }

    // Reset to defaults
    suspend fun resetInferenceSettings() {
        context.dataStore.edit {
            it[TEMPERATURE] = DEFAULT_TEMPERATURE
            it[TOP_P] = DEFAULT_TOP_P
            it[TOP_K] = DEFAULT_TOP_K
            it[MAX_TOKENS] = DEFAULT_MAX_TOKENS
            it[REPEAT_PENALTY] = DEFAULT_REPEAT_PENALTY
            it[CONTEXT_LENGTH] = DEFAULT_CONTEXT_LENGTH
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
