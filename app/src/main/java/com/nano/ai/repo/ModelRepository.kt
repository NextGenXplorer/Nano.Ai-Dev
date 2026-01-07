package com.nano.ai.repo

import com.nano.ai.database.dao.ModelConfigDao
import com.nano.ai.database.dao.ModelDao
import com.nano.ai.models.enums.ProviderType
import com.nano.ai.models.table_schema.Model
import com.nano.ai.models.table_schema.ModelConfig
import kotlinx.coroutines.flow.Flow

class ModelRepository(private val modelDao: ModelDao, private val configDao: ModelConfigDao) {

    fun getAllModels(): Flow<List<Model>> = modelDao.getAll()

    fun getActiveModels(): Flow<List<Model>> = modelDao.getAllActive()

    fun getModelsByProvider(providerType: ProviderType): Flow<List<Model>> =
        modelDao.getByProvider(providerType)

    suspend fun getModelById(id: String): Model? = modelDao.getById(id)

    suspend fun insertModel(model: Model) = modelDao.insert(model)

    suspend fun updateModel(model: Model) = modelDao.update(model)

    suspend fun deleteModel(model: Model) = modelDao.delete(model)

    suspend fun setModelActive(id: String, isActive: Boolean) =
        modelDao.updateActiveStatus(id, isActive)

    suspend fun getConfigByModelId(modelId: String): ModelConfig? = configDao.getByModelId(modelId)

    suspend fun insertConfig(config: ModelConfig) = configDao.insert(config)

    suspend fun updateConfig(config: ModelConfig) = configDao.update(config)

    suspend fun deleteConfig(config: ModelConfig) = configDao.delete(config)
}