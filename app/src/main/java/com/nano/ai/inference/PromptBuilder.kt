package com.nano.ai.inference

import com.nano.ai.models.enums.MessageRole
import com.nano.ai.models.table_schema.Message

class PromptBuilder(private val format: PromptFormat = PromptFormat.CHATML) {

    fun buildPrompt(
        messages: List<Message>,
        systemPrompt: String? = null
    ): String {
        return when (format) {
            PromptFormat.CHATML -> buildChatML(messages, systemPrompt)
            PromptFormat.LLAMA2 -> buildLlama2(messages, systemPrompt)
            PromptFormat.LLAMA3 -> buildLlama3(messages, systemPrompt)
            PromptFormat.ALPACA -> buildAlpaca(messages, systemPrompt)
            PromptFormat.VICUNA -> buildVicuna(messages, systemPrompt)
            PromptFormat.MISTRAL -> buildMistral(messages, systemPrompt)
            PromptFormat.RAW -> buildRaw(messages, systemPrompt)
        }
    }

    private fun buildChatML(messages: List<Message>, systemPrompt: String?): String {
        val sb = StringBuilder()

        systemPrompt?.let {
            sb.append("<|im_start|>system\n$it<|im_end|>\n")
        }

        messages.forEach { msg ->
            val role = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
            }
            sb.append("<|im_start|>$role\n${msg.content}<|im_end|>\n")
        }

        sb.append("<|im_start|>assistant\n")
        return sb.toString()
    }

    private fun buildLlama2(messages: List<Message>, systemPrompt: String?): String {
        val sb = StringBuilder()

        sb.append("<s>[INST] ")
        systemPrompt?.let {
            sb.append("<<SYS>>\n$it\n<</SYS>>\n\n")
        }

        var isFirstUser = true
        messages.forEach { msg ->
            when (msg.role) {
                MessageRole.USER -> {
                    if (!isFirstUser) {
                        sb.append("<s>[INST] ")
                    }
                    sb.append(msg.content)
                    sb.append(" [/INST]")
                    isFirstUser = false
                }
                MessageRole.ASSISTANT -> {
                    sb.append(" ${msg.content} </s>")
                }
                MessageRole.SYSTEM -> {
                    // System messages handled above
                }
            }
        }

        return sb.toString()
    }

    private fun buildLlama3(messages: List<Message>, systemPrompt: String?): String {
        val sb = StringBuilder()

        sb.append("<|begin_of_text|>")

        systemPrompt?.let {
            sb.append("<|start_header_id|>system<|end_header_id|>\n\n$it<|eot_id|>")
        }

        messages.forEach { msg ->
            val role = when (msg.role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
            }
            sb.append("<|start_header_id|>$role<|end_header_id|>\n\n${msg.content}<|eot_id|>")
        }

        sb.append("<|start_header_id|>assistant<|end_header_id|>\n\n")
        return sb.toString()
    }

    private fun buildAlpaca(messages: List<Message>, systemPrompt: String?): String {
        val sb = StringBuilder()

        systemPrompt?.let {
            sb.append("### System:\n$it\n\n")
        }

        messages.forEach { msg ->
            when (msg.role) {
                MessageRole.USER -> sb.append("### Instruction:\n${msg.content}\n\n")
                MessageRole.ASSISTANT -> sb.append("### Response:\n${msg.content}\n\n")
                MessageRole.SYSTEM -> sb.append("### System:\n${msg.content}\n\n")
            }
        }

        sb.append("### Response:\n")
        return sb.toString()
    }

    private fun buildVicuna(messages: List<Message>, systemPrompt: String?): String {
        val sb = StringBuilder()

        systemPrompt?.let {
            sb.append("$it\n\n")
        }

        messages.forEach { msg ->
            when (msg.role) {
                MessageRole.USER -> sb.append("USER: ${msg.content}\n")
                MessageRole.ASSISTANT -> sb.append("ASSISTANT: ${msg.content}\n")
                MessageRole.SYSTEM -> sb.append("${msg.content}\n")
            }
        }

        sb.append("ASSISTANT:")
        return sb.toString()
    }

    private fun buildMistral(messages: List<Message>, systemPrompt: String?): String {
        val sb = StringBuilder()

        sb.append("<s>")

        var userContent = StringBuilder()
        systemPrompt?.let {
            userContent.append("$it\n\n")
        }

        messages.forEachIndexed { index, msg ->
            when (msg.role) {
                MessageRole.USER -> {
                    if (index == 0) {
                        userContent.append(msg.content)
                        sb.append("[INST] ${userContent} [/INST]")
                    } else {
                        sb.append("[INST] ${msg.content} [/INST]")
                    }
                }
                MessageRole.ASSISTANT -> {
                    sb.append("${msg.content}</s>")
                }
                MessageRole.SYSTEM -> {
                    // Handled with first user message
                }
            }
        }

        return sb.toString()
    }

    private fun buildRaw(messages: List<Message>, systemPrompt: String?): String {
        val sb = StringBuilder()

        systemPrompt?.let {
            sb.append("$it\n\n")
        }

        messages.forEach { msg ->
            sb.append("${msg.content}\n")
        }

        return sb.toString()
    }

    fun getStopTokens(): List<String> {
        return when (format) {
            PromptFormat.CHATML -> listOf("<|im_end|>", "<|im_start|>")
            PromptFormat.LLAMA2 -> listOf("</s>", "[INST]")
            PromptFormat.LLAMA3 -> listOf("<|eot_id|>", "<|start_header_id|>")
            PromptFormat.ALPACA -> listOf("### Instruction:", "### Response:", "###")
            PromptFormat.VICUNA -> listOf("USER:", "ASSISTANT:")
            PromptFormat.MISTRAL -> listOf("</s>", "[INST]")
            PromptFormat.RAW -> emptyList()
        }
    }
}
