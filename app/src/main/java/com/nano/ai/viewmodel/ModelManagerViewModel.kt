package com.nano.ai.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nano.ai.inference.InferenceState
import com.nano.ai.inference.LlamaService
import com.nano.ai.models.enums.PathType
import com.nano.ai.models.enums.ProviderType
import com.nano.ai.models.table_schema.Model
import com.nano.ai.models.table_schema.ModelConfig
import com.nano.ai.repo.ModelRepository
import com.nano.ai.repo.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class ModelImportState(
    val isImporting: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null,
    val success: Boolean = false
)

class ModelManagerViewModel(
    private val modelRepository: ModelRepository,
    private val llamaService: LlamaService,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val models = modelRepository.getAllModels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeModels = modelRepository.getActiveModels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importState = MutableStateFlow(ModelImportState())
    val importState: StateFlow<ModelImportState> = _importState.asStateFlow()

    private val _selectedModel = MutableStateFlow<Model?>(null)
    val selectedModel: StateFlow<Model?> = _selectedModel.asStateFlow()

    val inferenceState: StateFlow<InferenceState> = llamaService.state

    init {
        viewModelScope.launch {
            settingsRepository.selectedModelId.collect { modelId ->
                if (modelId != null) {
                    _selectedModel.value = modelRepository.getModelById(modelId)
                } else {
                    _selectedModel.value = null
                }
            }
        }
    }

    fun importModel(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _importState.value = ModelImportState(isImporting = true, progress = 0f)

            try {
                // Copy file to internal storage
                val result = llamaService.copyModelToInternal(uri, fileName)

                result.fold(
                    onSuccess = { internalPath ->
                        _importState.value = _importState.value.copy(progress = 0.7f)

                        // Get file size
                        val fileSize = File(internalPath).length()

                        // Create model entry
                        val model = Model(
                            modelName = fileName.removeSuffix(".gguf"),
                            providerType = ProviderType.GGUF,
                            modelPath = internalPath,
                            pathType = PathType.FILE,
                            fileSize = fileSize,
                            isActive = true
                        )

                        modelRepository.insertModel(model)

                        // Create default config (using JSON strings)
                        val loadingParams = """{"contextLength":4096,"threads":4,"gpuLayers":0}"""
                        val inferenceParams = """{"temperature":0.7,"topP":0.9,"topK":40,"maxTokens":2048,"repeatPenalty":1.1}"""
                        val config = ModelConfig(
                            modelId = model.id,
                            modelLoadingParams = loadingParams,
                            modelInferenceParams = inferenceParams
                        )
                        modelRepository.insertConfig(config)

                        _importState.value = ModelImportState(
                            isImporting = false,
                            progress = 1f,
                            success = true
                        )
                    },
                    onFailure = { error ->
                        _importState.value = ModelImportState(
                            isImporting = false,
                            error = error.message ?: "Import failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _importState.value = ModelImportState(
                    isImporting = false,
                    error = e.message ?: "Import failed"
                )
            }
        }
    }

    fun selectModel(model: Model) {
        viewModelScope.launch {
            _selectedModel.value = model
            settingsRepository.setSelectedModelId(model.id)
        }
    }

    fun loadModel(model: Model) {
        viewModelScope.launch {
            val contextLength = settingsRepository.contextLength.first()
            llamaService.loadModel(
                modelPath = model.modelPath,
                contextLength = contextLength
            )
            selectModel(model)
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            llamaService.unloadModel()
        }
    }

    fun deleteModel(model: Model) {
        viewModelScope.launch {
            // Delete file
            llamaService.deleteModel(model.modelPath)

            // Delete from database
            modelRepository.deleteModel(model)

            // Clear selection if this was selected
            if (_selectedModel.value?.id == model.id) {
                _selectedModel.value = null
                settingsRepository.setSelectedModelId(null)
            }
        }
    }

    fun setModelActive(modelId: String, isActive: Boolean) {
        viewModelScope.launch {
            modelRepository.setModelActive(modelId, isActive)
        }
    }

    fun updateModelConfig(modelId: String, config: ModelConfig) {
        viewModelScope.launch {
            modelRepository.updateConfig(config)
        }
    }

    suspend fun getModelConfig(modelId: String): ModelConfig? {
        return modelRepository.getConfigByModelId(modelId)
    }

    fun getAvailableModelsOnDisk(): List<File> {
        return llamaService.getAvailableModels()
    }

    fun clearImportState() {
        _importState.value = ModelImportState()
    }

    fun isModelLoaded(): Boolean = llamaService.isModelLoaded()

    fun getLoadedModelPath(): String? = llamaService.getCurrentModelPath()
}
