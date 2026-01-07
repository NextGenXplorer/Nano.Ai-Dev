package com.nano.ai.inference

data class InferenceConfig(
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val maxTokens: Int = 2048,
    val repeatPenalty: Float = 1.1f,
    val contextLength: Int = 4096,
    val threads: Int = 4,
    val batchSize: Int = 512,
    val seed: Int = -1,
    val stopSequences: List<String> = emptyList()
) {
    companion object {
        val DEFAULT = InferenceConfig()

        fun fromSettings(
            temperature: Float,
            topP: Float,
            topK: Int,
            maxTokens: Int,
            repeatPenalty: Float,
            contextLength: Int
        ) = InferenceConfig(
            temperature = temperature,
            topP = topP,
            topK = topK,
            maxTokens = maxTokens,
            repeatPenalty = repeatPenalty,
            contextLength = contextLength
        )
    }
}
