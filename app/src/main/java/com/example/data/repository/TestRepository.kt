package com.example.data.repository

import com.example.data.local.QuestionDao
import com.example.data.local.SubjectDao
import com.example.data.model.Question
import com.example.data.model.QuestionStatus
import com.example.data.model.Subject
import com.example.data.model.SubjectStats
import com.example.data.model.SubjectWithStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

class TestRepository(
    private val subjectDao: SubjectDao,
    private val questionDao: QuestionDao
) {
    val subjectsWithStats: Flow<List<SubjectWithStats>> =
        combine(subjectDao.getAllSubjects(), questionDao.getAllQuestions()) { subjects, questions ->
            val questionsBySubject = questions.groupBy { it.subjectId }
            subjects.map { subject ->
                val subjectQuestions = questionsBySubject[subject.id] ?: emptyList()
                val stats = SubjectStats.calculate(subject.totalQuestions, subjectQuestions)
                SubjectWithStats(subject = subject, stats = stats)
            }
        }

    fun getQuestionsForSubject(subjectId: Long): Flow<List<Question>> {
        return questionDao.getQuestionsForSubject(subjectId)
    }

    fun getSubjectById(subjectId: Long): Flow<Subject?> {
        return subjectDao.getSubjectById(subjectId)
    }

    suspend fun addSubject(name: String, totalQuestions: Int): Long {
        val subject = Subject(
            name = name.trim(),
            totalQuestions = totalQuestions.coerceAtLeast(1),
            createdAt = System.currentTimeMillis()
        )
        return subjectDao.insertSubject(subject)
    }

    suspend fun updateSubject(id: Long, name: String, totalQuestions: Int) {
        val existing = subjectDao.getSubjectByIdSuspend(id) ?: return
        val updated = existing.copy(
            name = name.trim(),
            totalQuestions = totalQuestions.coerceAtLeast(1)
        )
        subjectDao.updateSubject(updated)
    }

    suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(subject)
    }

    suspend fun recordQuestion(
        subjectId: Long,
        questionNumber: Int,
        correctOption: Int,
        status: QuestionStatus
    ): Long {
        val now = System.currentTimeMillis()
        val existing = questionDao.getQuestionSuspend(subjectId, questionNumber)

        val questionToSave = if (existing != null) {
            existing.copy(
                correctOption = correctOption,
                status = status,
                updatedAt = now
            )
        } else {
            Question(
                subjectId = subjectId,
                questionNumber = questionNumber,
                correctOption = correctOption,
                status = status,
                createdAt = now,
                updatedAt = now
            )
        }
        return questionDao.upsertQuestion(questionToSave)
    }

    suspend fun deleteQuestionRecord(subjectId: Long, questionNumber: Int) {
        questionDao.deleteQuestionByNumber(subjectId, questionNumber)
    }

    suspend fun seedSampleIfEmpty() {
        val currentSubjects = subjectDao.getAllSubjects().firstOrNull()
        if (currentSubjects.isNullOrEmpty()) {
            // Seed sample subjects
            val chemistryId = subjectDao.insertSubject(
                Subject(name = "شیمی", totalQuestions = 300)
            )
            val physicsId = subjectDao.insertSubject(
                Subject(name = "فیزیک", totalQuestions = 250)
            )
            val mathId = subjectDao.insertSubject(
                Subject(name = "ریاضی", totalQuestions = 400)
            )

            // Seed some realistic question records for chemistry
            val now = System.currentTimeMillis()
            val sampleStatuses = listOf(
                Pair(1, Pair(2, QuestionStatus.MASTERED_CORRECT)),
                Pair(2, Pair(4, QuestionStatus.MASTERED_CORRECT)),
                Pair(3, Pair(1, QuestionStatus.GUESSED_CORRECT)),
                Pair(4, Pair(3, QuestionStatus.INCORRECT)),
                Pair(5, Pair(2, QuestionStatus.UNANSWERED)),
                Pair(6, Pair(4, QuestionStatus.MASTERED_CORRECT)),
                Pair(7, Pair(1, QuestionStatus.INCORRECT)),
                Pair(8, Pair(3, QuestionStatus.GUESSED_CORRECT)),
                Pair(9, Pair(2, QuestionStatus.MASTERED_CORRECT)),
                Pair(10, Pair(4, QuestionStatus.MASTERED_CORRECT)),
                Pair(11, Pair(1, QuestionStatus.UNANSWERED)),
                Pair(12, Pair(2, QuestionStatus.INCORRECT)),
                Pair(13, Pair(3, QuestionStatus.MASTERED_CORRECT)),
                Pair(14, Pair(4, QuestionStatus.GUESSED_CORRECT)),
                Pair(15, Pair(1, QuestionStatus.MASTERED_CORRECT))
            )

            for ((num, pair) in sampleStatuses) {
                questionDao.upsertQuestion(
                    Question(
                        subjectId = chemistryId,
                        questionNumber = num,
                        correctOption = pair.first,
                        status = pair.second,
                        createdAt = now - (15 - num) * 60_000L,
                        updatedAt = now - (15 - num) * 60_000L
                    )
                )
            }
        }
    }
}
