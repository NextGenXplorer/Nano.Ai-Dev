package com.nano.ai.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nano.ai.database.dao.ModelConfigDao
import com.nano.ai.database.dao.ModelDao
import com.nano.ai.models.converters.Converters
import com.nano.ai.models.table_schema.Model
import com.nano.ai.models.table_schema.ModelConfig

@Database(
    entities = [Model::class, ModelConfig::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun modelDao(): ModelDao
    abstract fun modelConfigDao(): ModelConfigDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "llm_models_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}