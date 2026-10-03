package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionUiModel
import com.example.ui.util.PersianNumberHelper

@Composable
fun QuestionItemCard(
    question: QuestionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecorded = question.isRecorded
    val status = question.status

    val containerColor = if (isRecorded) {
        status.containerColor
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isRecorded) {
        status.color
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("question_item_${question.questionNumber}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.2.dp, borderColor),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRecorded) 1.5.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top mini indicator dot or badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecorded) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(status.color)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = status.color,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "گ ${PersianNumberHelper.toPersianDigits(question.correctOption)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )

                    Text(
                        text = "نزده",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Big Question Number
            Text(
                text = PersianNumberHelper.toPersianDigits(question.questionNumber),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRecorded) status.onContainerColor else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Short status label
            Text(
                text = when (status) {
                    QuestionStatus.MASTERED_CORRECT -> "درست"
                    QuestionStatus.GUESSED_CORRECT -> "شانسی"
                    QuestionStatus.UNANSWERED -> "نزده"
                    QuestionStatus.INCORRECT -> "غلط"
                    QuestionStatus.UNRECORDED -> "—"
                },
                fontSize = 11.sp,
                fontWeight = if (isRecorded) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isRecorded) status.color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}
