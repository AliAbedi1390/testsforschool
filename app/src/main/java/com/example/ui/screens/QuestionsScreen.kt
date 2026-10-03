package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.QuestionStatus
import com.example.data.model.SubjectWithStats
import com.example.ui.components.QuestionEditDialog
import com.example.ui.components.QuestionItemCard
import com.example.ui.components.StatisticsDialog
import com.example.ui.theme.StatusIncorrect
import com.example.ui.theme.StatusLucky
import com.example.ui.theme.StatusMastered
import com.example.ui.theme.StatusUnanswered
import com.example.ui.theme.StatusUnrecorded
import com.example.ui.util.PersianNumberHelper
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeSubjectWithStats by viewModel.activeSubjectWithStats.collectAsStateWithLifecycle()
    val filteredQuestions by viewModel.filteredQuestions.collectAsStateWithLifecycle()
    val allQuestions by viewModel.allQuestionsUiModels.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val editingQuestion by viewModel.editingQuestion.collectAsStateWithLifecycle()
    val subjectForStats by viewModel.subjectForStatsDialog.collectAsStateWithLifecycle()

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isSearchExpanded by remember { mutableStateOf(false) }

    // Handle system back button to return to home
    BackHandler {
        viewModel.navigateHome()
    }

    val subject = activeSubjectWithStats?.subject
    val stats = activeSubjectWithStats?.stats

    if (subject == null || stats == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("در حال بارگذاری اطلاعات درس…")
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${PersianNumberHelper.toPersianDigits(stats.recordedCount)} از ${PersianNumberHelper.toPersianDigits(subject.totalQuestions)} تست ثبت‌شده (${PersianNumberHelper.toPersianPercentage(stats.progressPercentage)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateHome() },
                        modifier = Modifier.testTag("back_to_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت به خانه"
                        )
                    }
                },
                actions = {
                    // Toggle Search Bar
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "جستجو یا رفتن به تست"
                        )
                    }

                    // Open Statistics Dialog
                    IconButton(
                        onClick = { viewModel.showStatsDialog(activeSubjectWithStats!!) },
                        modifier = Modifier.testTag("open_stats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "آمار کامل درس",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Quick Progress strip
            LinearProgressIndicator(
                progress = { (stats.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Search / Jump to Question Number Bar
            AnimatedVisibility(visible = isSearchExpanded) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("شماره تست برای رفتن یا جستجو (مثلاً: ۲۵۰)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    val target = searchQuery.trim().toIntOrNull()
                                    if (target != null && target in 1..subject.totalQuestions) {
                                        viewModel.jumpToQuestion(target)
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_question_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Quick "Go to" button
                        if (searchQuery.isNotBlank() && searchQuery.trim().toIntOrNull() != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        val target = searchQuery.trim().toIntOrNull()
                                        if (target != null) {
                                            viewModel.jumpToQuestion(target)
                                            focusManager.clearFocus()
                                        }
                                    }
                            ) {
                                Text(
                                    text = "برو به تست",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Filter
                item {
                    StatusFilterChip(
                        title = "همه",
                        count = subject.totalQuestions,
                        isSelected = selectedFilter == null,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.setFilter(null) }
                    )
                }

                // 1. بلد بودم و درست زدم
                item {
                    StatusFilterChip(
                        title = QuestionStatus.MASTERED_CORRECT.title,
                        count = stats.masteredCount,
                        isSelected = selectedFilter == QuestionStatus.MASTERED_CORRECT,
                        color = StatusMastered,
                        onClick = { viewModel.setFilter(QuestionStatus.MASTERED_CORRECT) }
                    )
                }

                // 2. درست زدم ولی شانسی
                item {
                    StatusFilterChip(
                        title = QuestionStatus.GUESSED_CORRECT.title,
                        count = stats.guessedCount,
                        isSelected = selectedFilter == QuestionStatus.GUESSED_CORRECT,
                        color = StatusLucky,
                        onClick = { viewModel.setFilter(QuestionStatus.GUESSED_CORRECT) }
                    )
                }

                // 3. بلد نبودم / نزدم
                item {
                    StatusFilterChip(
                        title = QuestionStatus.UNANSWERED.title,
                        count = stats.unansweredCount,
                        isSelected = selectedFilter == QuestionStatus.UNANSWERED,
                        color = StatusUnanswered,
                        onClick = { viewModel.setFilter(QuestionStatus.UNANSWERED) }
                    )
                }

                // 4. غلط زدم
                item {
                    StatusFilterChip(
                        title = QuestionStatus.INCORRECT.title,
                        count = stats.incorrectCount,
                        isSelected = selectedFilter == QuestionStatus.INCORRECT,
                        color = StatusIncorrect,
                        onClick = { viewModel.setFilter(QuestionStatus.INCORRECT) }
                    )
                }

                // 5. ثبت‌نشده
                item {
                    StatusFilterChip(
                        title = QuestionStatus.UNRECORDED.title,
                        count = stats.unrecordedCount,
                        isSelected = selectedFilter == QuestionStatus.UNRECORDED,
                        color = StatusUnrecorded,
                        onClick = { viewModel.setFilter(QuestionStatus.UNRECORDED) }
                    )
                }
            }

            // Results count badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نمایش ${PersianNumberHelper.toPersianDigits(filteredQuestions.size)} تست",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (selectedFilter != null || searchQuery.isNotBlank()) {
                    Text(
                        text = "پاک کردن فیلترها",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                viewModel.setFilter(null)
                                viewModel.setSearchQuery("")
                            }
                            .padding(4.dp)
                    )
                }
            }

            // Questions Virtualized Grid (Adaptive columns, ultra-optimized for thousands of questions)
            if (filteredQuestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "تستی با این مشخصات یافت نشد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "فیلتر وضعیت یا شماره جستجوشده را تغییر دهید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 72.dp),
                    state = gridState,
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("questions_grid")
                ) {
                    items(
                        items = filteredQuestions,
                        key = { it.questionNumber }
                    ) { question ->
                        QuestionItemCard(
                            question = question,
                            onClick = { viewModel.openQuestionEdit(question.questionNumber) }
                        )
                    }
                }
            }
        }
    }

    // Question Edit / Record Dialog
    editingQuestion?.let { question ->
        QuestionEditDialog(
            question = question,
            subjectName = subject.name,
            totalQuestions = subject.totalQuestions,
            onDismiss = { viewModel.closeQuestionEdit() },
            onSave = { correctOption, status, andNext ->
                viewModel.saveQuestionRecord(correctOption, status, andNext)
            },
            onClearRecord = {
                viewModel.clearQuestionRecord()
            },
            onNavigatePrevious = {
                viewModel.navigatePreviousQuestion()
            },
            onNavigateNext = {
                viewModel.navigateNextQuestion()
            }
        )
    }

    // Statistics Dialog
    subjectForStats?.let { subjectWithStats ->
        StatisticsDialog(
            subjectWithStats = subjectWithStats,
            onDismiss = { viewModel.dismissStatsDialog() }
        )
    }
}

@Composable
private fun StatusFilterChip(
    title: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Text(
                        text = PersianNumberHelper.toPersianDigits(count),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.12f),
            selectedLabelColor = color
        ),
        shape = RoundedCornerShape(10.dp)
    )
}
