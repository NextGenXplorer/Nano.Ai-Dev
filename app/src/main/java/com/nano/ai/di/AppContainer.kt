package com.nano.ai.di

import android.content.Context
import com.nano.ai.database.AppDatabase
import com.nano.ai.repo.ModelRepository
import com.nano.ai.viewmodel.ThemeViewModel

object AppContainer {

    private lateinit var themeViewModel: ThemeViewModel
    private lateinit var database: AppDatabase
    private lateinit var modelRepository: ModelRepository

    fun init(context: Context){
        themeViewModel = ThemeViewModel()
        database = AppDatabase.getDatabase(context)
        modelRepository = ModelRepository(
            modelDao = database.modelDao(),
            configDao = database.modelConfigDao()
        )
    }

    fun getThemeViewModel() = themeViewModel

    fun getDatabase() = database

    fun getModelRepository() = modelRepository
}