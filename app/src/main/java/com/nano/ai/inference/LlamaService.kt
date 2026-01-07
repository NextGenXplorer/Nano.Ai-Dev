package com.nano.ai.inference

import android.content.Context
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaModel
import de.kherud.llama.ModelParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File

class LlamaService(private val context: Context) {

    private var model: LlamaModel? = null
    private var currentModelPath: String? = null

    private val _state = MutableStateFlow<InferenceState>(InferenceState.Idle)
    val state: StateFlow<InferenceState> = _state.asStateFlow()

    @Volatile
    private var shouldStop = false

    suspend fun loadModel(
        modelPath: String,
        contextLength: Int = 4096,
        threads: Int = 4,
        gpuLayers: Int = 0
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Unload existing model if different
            if (currentModelPath != modelPath) {
                unloadModel()
            }

            // Check if already loaded
            if (model != null && currentModelPath == modelPath) {
                _state.value = InferenceState.Ready(File(modelPath).name)
                return@withContext Result.success(Unit)
            }

            _state.value = InferenceState.Loading(modelPath, 0f)

            val modelFile = File(modelPath)
            if (!modelFile.exists()) {
                _state.value = InferenceState.Error("Model file not found: $modelPath")
                return@withContext Result.failure(Exception("Model file not found"))
            }

            val params = ModelParameters()
                .setNCtx(contextLength)
                .setNThreads(threads)
                .setNGpuLayers(gpuLayers)

            _state.value = InferenceState.Loading(modelPath, 0.5f)

            model = LlamaModel(modelPath, params)
            currentModelPath = modelPath

            _state.value = InferenceState.Ready(modelFile.name)
            Result.success(Unit)
        } catch (e: Exception) {
            _state.value = InferenceState.Error("Failed to load model: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun generate(
        prompt: String,
        config: InferenceConfig = InferenceConfig.DEFAULT
    ): Flow<String> = callbackFlow {
        val currentModel = model
        if (currentModel == null) {
            close(Exception("No model loaded"))
            return@callbackFlow
        }

        shouldStop = false
        var tokensGenerated = 0
        val startTime = System.currentTimeMillis()

        try {
            _state.value = InferenceState.Generating(0, 0f)

            val inferParams = InferenceParameters(prompt)
                .setTemperature(config.temperature)
                .setTopP(config.topP)
                .setTopK(config.topK)
                .setNPredict(config.maxTokens)
                .setRepeatPenalty(config.repeatPenalty)

            if (config.seed >= 0) {
                inferParams.setSeed(config.seed)
            }

            config.stopSequences.forEach { stop ->
                inferParams.setStopStrings(stop)
            }

            for (output in currentModel.generate(inferParams)) {
                if (shouldStop) {
                    break
                }

                trySend(output.text)
                tokensGenerated++

                val elapsed = (System.currentTimeMillis() - startTime) / 1000f
                val tokensPerSecond = if (elapsed > 0) tokensGenerated / elapsed else 0f
                _state.value = InferenceState.Generating(tokensGenerated, tokensPerSecond)
            }

            _state.value = InferenceState.Ready(currentModelPath?.let { File(it).name } ?: "Unknown")
            close()
        } catch (e: Exception) {
            _state.value = InferenceState.Error("Generation failed: ${e.message}", e)
            close(e)
        }

        awaitClose {
            shouldStop = true
        }
    }

    fun stopGeneration() {
        shouldStop = true
    }

    suspend fun unloadModel() = withContext(Dispatchers.IO) {
        try {
            model?.close()
            model = null
            currentModelPath = null
            _state.value = InferenceState.Idle
        } catch (e: Exception) {
            _state.value = InferenceState.Error("Failed to unload model: ${e.message}", e)
        }
    }

    fun isModelLoaded(): Boolean = model != null

    fun getCurrentModelPath(): String? = currentModelPath

    fun getModelDirectory(): File {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
        return modelsDir
    }

    suspend fun copyModelToInternal(sourceUri: android.net.Uri, fileName: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val destFile = File(getModelDirectory(), fileName)

                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                } ?: return@withContext Result.failure(Exception("Failed to open source file"))

                Result.success(destFile.absolutePath)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteModel(modelPath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (currentModelPath == modelPath) {
                unloadModel()
            }
            val file = File(modelPath)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAvailableModels(): List<File> {
        return getModelDirectory().listFiles { file ->
            file.extension.lowercase() == "gguf"
        }?.toList() ?: emptyList()
    }
}
