package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY questionNumber ASC")
    fun getQuestionsForSubject(subjectId: Long): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND questionNumber = :questionNumber LIMIT 1")
    fun getQuestion(subjectId: Long, questionNumber: Int): Flow<Question?>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND questionNumber = :questionNumber LIMIT 1")
    suspend fun getQuestionSuspend(subjectId: Long, questionNumber: Int): Question?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestion(question: Question): Long

    @Update
    suspend fun updateQuestion(question: Question)

    @Query("DELETE FROM questions WHERE subjectId = :subjectId AND questionNumber = :questionNumber")
    suspend fun deleteQuestionByNumber(subjectId: Long, questionNumber: Int)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Long)

    @Query("DELETE FROM questions WHERE subjectId = :subjectId")
    suspend fun deleteAllQuestionsForSubject(subjectId: Long)
}
