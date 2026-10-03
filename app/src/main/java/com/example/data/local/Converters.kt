package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.QuestionStatus

class Converters {
    @TypeConverter
    fun fromQuestionStatus(status: QuestionStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toQuestionStatus(value: String?): QuestionStatus {
        if (value == null) return QuestionStatus.UNRECORDED
        return try {
            QuestionStatus.valueOf(value)
        } catch (_: Exception) {
            QuestionStatus.UNRECORDED
        }
    }
}
