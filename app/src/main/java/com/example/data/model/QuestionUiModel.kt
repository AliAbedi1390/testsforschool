package com.example.data.model

data class QuestionUiModel(
    val questionNumber: Int,
    val subjectId: Long,
    val id: Long? = null,
    val correctOption: Int? = null,
    val status: QuestionStatus = QuestionStatus.UNRECORDED,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    val isRecorded: Boolean
        get() = status != QuestionStatus.UNRECORDED && correctOption != null && correctOption in 1..4
}
