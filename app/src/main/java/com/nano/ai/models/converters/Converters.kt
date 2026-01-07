package com.nano.ai.models.converters

import androidx.room.TypeConverter
import com.nano.ai.models.enums.MessageRole
import com.nano.ai.models.enums.MessageStatus
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

    @TypeConverter
    fun fromMessageRole(value: MessageRole): String = value.name

    @TypeConverter
    fun toMessageRole(value: String): MessageRole = MessageRole.valueOf(value)

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = MessageStatus.valueOf(value)
}