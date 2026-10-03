package com.example

import com.example.data.model.Question
import com.example.data.model.QuestionStatus
import com.example.data.model.SubjectStats
import com.example.ui.util.PersianNumberHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun persianDigits_conversion_isCorrect() {
        assertEquals("۱۲۳۴۵", PersianNumberHelper.toPersianDigits(12345))
        assertEquals("۰", PersianNumberHelper.toPersianDigits(0))
        assertEquals("۴۵٪", PersianNumberHelper.toPersianPercentage(45.2f))
    }

    @Test
    fun subjectStats_calculation_isCorrect() {
        val total = 300
        val questions = mutableListOf<Question>()
        // 120 mastered
        for (i in 1..120) {
            questions.add(Question(id = i.toLong(), subjectId = 1L, questionNumber = i, correctOption = 1, status = QuestionStatus.MASTERED_CORRECT))
        }
        // 50 guessed
        for (i in 121..170) {
            questions.add(Question(id = i.toLong(), subjectId = 1L, questionNumber = i, correctOption = 2, status = QuestionStatus.GUESSED_CORRECT))
        }
        // 60 unanswered
        for (i in 171..230) {
            questions.add(Question(id = i.toLong(), subjectId = 1L, questionNumber = i, correctOption = 3, status = QuestionStatus.UNANSWERED))
        }
        // 40 incorrect
        for (i in 231..270) {
            questions.add(Question(id = i.toLong(), subjectId = 1L, questionNumber = i, correctOption = 4, status = QuestionStatus.INCORRECT))
        }

        val stats = SubjectStats.calculate(total, questions)

        assertEquals(300, stats.totalQuestions)
        assertEquals(270, stats.recordedCount)
        assertEquals(30, stats.remainingCount)
        assertEquals(120, stats.masteredCount)
        assertEquals(50, stats.guessedCount)
        assertEquals(60, stats.unansweredCount)
        assertEquals(40, stats.incorrectCount)
        assertEquals(30, stats.unrecordedCount)
        assertEquals(40f, stats.masteredPercentage, 0.1f)
    }
}
