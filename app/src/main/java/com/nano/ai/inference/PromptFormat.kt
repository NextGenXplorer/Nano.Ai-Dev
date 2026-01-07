package com.nano.ai.inference

enum class PromptFormat {
    CHATML,
    LLAMA2,
    LLAMA3,
    ALPACA,
    VICUNA,
    MISTRAL,
    RAW;

    companion object {
        fun fromModelName(modelName: String): PromptFormat {
            val lower = modelName.lowercase()
            return when {
                lower.contains("llama-3") || lower.contains("llama3") -> LLAMA3
                lower.contains("llama-2") || lower.contains("llama2") -> LLAMA2
                lower.contains("mistral") -> MISTRAL
                lower.contains("vicuna") -> VICUNA
                lower.contains("alpaca") -> ALPACA
                lower.contains("chatml") || lower.contains("qwen") || lower.contains("yi") -> CHATML
                else -> CHATML // Default to ChatML
            }
        }
    }
}
