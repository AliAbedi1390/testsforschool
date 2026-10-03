package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.QuestionStatus
import com.example.data.model.SubjectWithStats
import com.example.ui.theme.StatusIncorrect
import com.example.ui.theme.StatusLucky
import com.example.ui.theme.StatusMastered
import com.example.ui.theme.StatusUnanswered
import com.example.ui.theme.StatusUnrecorded
import com.example.ui.util.PersianNumberHelper

@Composable
fun StatisticsDialog(
    subjectWithStats: SubjectWithStats,
    onDismiss: () -> Unit
) {
    val subject = subjectWithStats.subject
    val stats = subjectWithStats.stats

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Title and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "آمار و تحلیل تست‌ها",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "درس: ${subject.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High-level overview cards
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OverviewMetric(
                                label = "کل تست‌ها",
                                value = PersianNumberHelper.toPersianDigits(stats.totalQuestions)
                            )
                            OverviewMetric(
                                label = "ثبت‌شده",
                                value = PersianNumberHelper.toPersianDigits(stats.recordedCount)
                            )
                            OverviewMetric(
                                label = "باقی‌مانده",
                                value = PersianNumberHelper.toPersianDigits(stats.remainingCount)
                            )
                            OverviewMetric(
                                label = "پیشرفت کلی",
                                value = PersianNumberHelper.toPersianPercentage(stats.progressPercentage)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Multi-color segmented progress bar
                        SegmentedProgressBar(
                            mastered = stats.masteredCount,
                            guessed = stats.guessedCount,
                            unanswered = stats.unansweredCount,
                            incorrect = stats.incorrectCount,
                            unrecorded = stats.unrecordedCount,
                            total = stats.totalQuestions
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "تفکیک عملکرد بر اساس وضعیت‌ها",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 1. بلد بودم و درست زدم
                StatStatusRow(
                    status = QuestionStatus.MASTERED_CORRECT,
                    count = stats.masteredCount,
                    percentage = stats.masteredPercentage,
                    color = StatusMastered
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. درست زدم ولی شانسی
                StatStatusRow(
                    status = QuestionStatus.GUESSED_CORRECT,
                    count = stats.guessedCount,
                    percentage = stats.guessedPercentage,
                    color = StatusLucky
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. بلد نبودم / نزدم
                StatStatusRow(
                    status = QuestionStatus.UNANSWERED,
                    count = stats.unansweredCount,
                    percentage = stats.unansweredPercentage,
                    color = StatusUnanswered
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. غلط زدم
                StatStatusRow(
                    status = QuestionStatus.INCORRECT,
                    count = stats.incorrectCount,
                    percentage = stats.incorrectPercentage,
                    color = StatusIncorrect
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. ثبت‌نشده
                StatStatusRow(
                    status = QuestionStatus.UNRECORDED,
                    count = stats.unrecordedCount,
                    percentage = stats.unrecordedPercentage,
                    color = StatusUnrecorded
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("بستن آمار")
                }
            }
        }
    }
}

@Composable
private fun OverviewMetric(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SegmentedProgressBar(
    mastered: Int,
    guessed: Int,
    unanswered: Int,
    incorrect: Int,
    unrecorded: Int,
    total: Int
) {
    if (total <= 0) return

    val totalWeight = total.toFloat()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (mastered > 0) {
            Box(
                modifier = Modifier
                    .weight(mastered.toFloat() / totalWeight)
                    .height(10.dp)
                    .background(StatusMastered)
            )
        }
        if (guessed > 0) {
            Box(
                modifier = Modifier
                    .weight(guessed.toFloat() / totalWeight)
                    .height(10.dp)
                    .background(StatusLucky)
            )
        }
        if (unanswered > 0) {
            Box(
                modifier = Modifier
                    .weight(unanswered.toFloat() / totalWeight)
                    .height(10.dp)
                    .background(StatusUnanswered)
            )
        }
        if (incorrect > 0) {
            Box(
                modifier = Modifier
                    .weight(incorrect.toFloat() / totalWeight)
                    .height(10.dp)
                    .background(StatusIncorrect)
            )
        }
        if (unrecorded > 0) {
            Box(
                modifier = Modifier
                    .weight(unrecorded.toFloat() / totalWeight)
                    .height(10.dp)
                    .background(StatusUnrecorded.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun StatStatusRow(
    status: QuestionStatus,
    count: Int,
    percentage: Float,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = status.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${PersianNumberHelper.toPersianDigits(count)} تست",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "(${PersianNumberHelper.toPersianPercentage(percentage)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
