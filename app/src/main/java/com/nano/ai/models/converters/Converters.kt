package com.nano.ai.models.converters

import androidx.room.TypeConverter
import com.nano.ai.models.enums.PathType
import com.nano.ai.models.enums.ProviderType

class Converters {
    @TypeConverter
    fun fromProviderType(value: ProviderType): String = value.name
    
    @TypeConverter
    fun toProviderType(value: String): ProviderType = ProviderType.valueOf(value)
    
    @TypeConverter
    fun fromPathType(value: PathType): String = value.name
    
    @TypeConverter
    fun toPathType(value: String): PathType = PathType.valueOf(value)
}