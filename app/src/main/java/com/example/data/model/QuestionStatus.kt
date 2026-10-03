package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusIncorrect
import com.example.ui.theme.StatusIncorrectContainer
import com.example.ui.theme.StatusIncorrectOnContainer
import com.example.ui.theme.StatusLucky
import com.example.ui.theme.StatusLuckyContainer
import com.example.ui.theme.StatusLuckyOnContainer
import com.example.ui.theme.StatusMastered
import com.example.ui.theme.StatusMasteredContainer
import com.example.ui.theme.StatusMasteredOnContainer
import com.example.ui.theme.StatusUnanswered
import com.example.ui.theme.StatusUnansweredContainer
import com.example.ui.theme.StatusUnansweredOnContainer
import com.example.ui.theme.StatusUnrecorded
import com.example.ui.theme.StatusUnrecordedContainer
import com.example.ui.theme.StatusUnrecordedOnContainer

enum class QuestionStatus(
    val title: String,
    val description: String,
    val color: Color,
    val containerColor: Color,
    val onContainerColor: Color
) {
    MASTERED_CORRECT(
        title = "بلد بودم و درست زدم",
        description = "سؤال را کاملاً بلد بودم و با اطمینان پاسخ صحیح دادم.",
        color = StatusMastered,
        containerColor = StatusMasteredContainer,
        onContainerColor = StatusMasteredOnContainer
    ),
    GUESSED_CORRECT(
        title = "درست زدم ولی شانسی",
        description = "پاسخ درست بود، اما با اطمینان کامل نبود و تقریباً شانسی انتخاب کردم.",
        color = StatusLucky,
        containerColor = StatusLuckyContainer,
        onContainerColor = StatusLuckyOnContainer
    ),
    UNANSWERED(
        title = "بلد نبودم / نزدم",
        description = "مطلب سؤال را بلد نبودم یا نتوانستم آن را حل کنم و در نتیجه سؤال را پاسخ ندادم.",
        color = StatusUnanswered,
        containerColor = StatusUnansweredContainer,
        onContainerColor = StatusUnansweredOnContainer
    ),
    INCORRECT(
        title = "غلط زدم",
        description = "سؤال را پاسخ دادم اما گزینه انتخابی اشتباه بود.",
        color = StatusIncorrect,
        containerColor = StatusIncorrectContainer,
        onContainerColor = StatusIncorrectOnContainer
    ),
    UNRECORDED(
        title = "ثبت‌نشده",
        description = "هنوز وضعیت و گزینه این تست ثبت نشده است.",
        color = StatusUnrecorded,
        containerColor = StatusUnrecordedContainer,
        onContainerColor = StatusUnrecordedOnContainer
    );

    val isRecorded: Boolean
        get() = this != UNRECORDED
}
