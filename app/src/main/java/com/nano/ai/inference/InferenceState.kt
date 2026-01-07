package com.nano.ai.inference

sealed class InferenceState {
    data object Idle : InferenceState()
    data class Loading(val modelPath: String, val progress: Float = 0f) : InferenceState()
    data class Ready(val modelName: String) : InferenceState()
    data class Generating(val tokensGenerated: Int = 0, val tokensPerSecond: Float = 0f) : InferenceState()
    data class Error(val message: String, val exception: Throwable? = null) : InferenceState()

    val isReady: Boolean get() = this is Ready
    val isGenerating: Boolean get() = this is Generating
    val isLoading: Boolean get() = this is Loading
    val isIdle: Boolean get() = this is Idle
    val isError: Boolean get() = this is Error
}
