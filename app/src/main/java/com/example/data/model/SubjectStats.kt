package com.example.data.model

data class SubjectStats(
    val totalQuestions: Int,
    val recordedCount: Int,
    val remainingCount: Int,
    val progressPercentage: Float,
    val masteredCount: Int,
    val masteredPercentage: Float,
    val guessedCount: Int,
    val guessedPercentage: Float,
    val unansweredCount: Int,
    val unansweredPercentage: Float,
    val incorrectCount: Int,
    val incorrectPercentage: Float,
    val unrecordedCount: Int,
    val unrecordedPercentage: Float
) {
    companion object {
        fun calculate(total: Int, questions: List<Question>): SubjectStats {
            val validTotal = if (total > 0) total else 0
            var mastered = 0
            var guessed = 0
            var unanswered = 0
            var incorrect = 0

            for (q in questions) {
                // only consider questions within current total questions limit
                if (q.questionNumber in 1..validTotal) {
                    when (q.status) {
                        QuestionStatus.MASTERED_CORRECT -> mastered++
                        QuestionStatus.GUESSED_CORRECT -> guessed++
                        QuestionStatus.UNANSWERED -> unanswered++
                        QuestionStatus.INCORRECT -> incorrect++
                        QuestionStatus.UNRECORDED -> {}
                    }
                }
            }

            val recorded = mastered + guessed + unanswered + incorrect
            val remaining = (validTotal - recorded).coerceAtLeast(0)
            val progressPct = if (validTotal > 0) (recorded.toFloat() / validTotal) * 100f else 0f

            fun calcPct(count: Int): Float =
                if (validTotal > 0) (count.toFloat() / validTotal) * 100f else 0f

            return SubjectStats(
                totalQuestions = validTotal,
                recordedCount = recorded,
                remainingCount = remaining,
                progressPercentage = progressPct,
                masteredCount = mastered,
                masteredPercentage = calcPct(mastered),
                guessedCount = guessed,
                guessedPercentage = calcPct(guessed),
                unansweredCount = unanswered,
                unansweredPercentage = calcPct(unanswered),
                incorrectCount = incorrect,
                incorrectPercentage = calcPct(incorrect),
                unrecordedCount = remaining,
                unrecordedPercentage = calcPct(remaining)
            )
        }
    }
}

data class SubjectWithStats(
    val subject: Subject,
    val stats: SubjectStats
)
