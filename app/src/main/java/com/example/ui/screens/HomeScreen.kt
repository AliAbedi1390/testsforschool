package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddEditSubjectDialog
import com.example.ui.components.StatisticsDialog
import com.example.ui.components.SubjectCard
import com.example.ui.util.PersianNumberHelper
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val subjectsWithStats by viewModel.subjectsWithStats.collectAsStateWithLifecycle()
    val isAddDialogOpen by viewModel.isAddSubjectDialogOpen.collectAsStateWithLifecycle()
    val subjectToEdit by viewModel.subjectToEdit.collectAsStateWithLifecycle()
    val subjectToDelete by viewModel.subjectToDelete.collectAsStateWithLifecycle()
    val subjectForStats by viewModel.subjectForStatsDialog.collectAsStateWithLifecycle()

    // Calculated overall stats
    val totalSubjects = subjectsWithStats.size
    val totalQuestionsAll = subjectsWithStats.sumOf { it.subject.totalQuestions }
    val totalRecordedAll = subjectsWithStats.sumOf { it.stats.recordedCount }
    val overallPercentage = if (totalQuestionsAll > 0) {
        (totalRecordedAll.toFloat() / totalQuestionsAll) * 100f
    } else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "تستیار",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مدیریت و ثبت تست‌های درسی",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddSubjectDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("افزودن درس", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_subject_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Overall Summary Card
            if (subjectsWithStats.isNotEmpty()) {
                item {
                    GlobalOverviewCard(
                        totalSubjects = totalSubjects,
                        totalQuestions = totalQuestionsAll,
                        totalRecorded = totalRecordedAll,
                        overallPercentage = overallPercentage
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "لیست درس‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${PersianNumberHelper.toPersianDigits(totalSubjects)} درس",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Subject Cards
            if (subjectsWithStats.isEmpty()) {
                item {
                    EmptySubjectsView(
                        onAddSubject = { viewModel.openAddSubjectDialog() }
                    )
                }
            } else {
                items(
                    items = subjectsWithStats,
                    key = { it.subject.id }
                ) { subjectWithStats ->
                    SubjectCard(
                        subjectWithStats = subjectWithStats,
                        onClick = { viewModel.openSubject(subjectWithStats.subject.id) },
                        onEdit = { viewModel.openEditSubjectDialog(subjectWithStats.subject) },
                        onDelete = { viewModel.requestDeleteSubject(subjectWithStats.subject) },
                        onViewStats = { viewModel.showStatsDialog(subjectWithStats) }
                    )
                }
            }
        }
    }

    // Add Subject Dialog
    if (isAddDialogOpen) {
        AddEditSubjectDialog(
            subjectToEdit = null,
            onDismiss = { viewModel.closeAddSubjectDialog() },
            onConfirm = { name, totalQuestions ->
                viewModel.saveNewSubject(name, totalQuestions)
            }
        )
    }

    // Edit Subject Dialog
    subjectToEdit?.let { subject ->
        AddEditSubjectDialog(
            subjectToEdit = subject,
            onDismiss = { viewModel.closeEditSubjectDialog() },
            onConfirm = { name, totalQuestions ->
                viewModel.updateSubject(name, totalQuestions)
            }
        )
    }

    // Delete Subject Confirmation Dialog
    subjectToDelete?.let { subject ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteSubject() },
            title = {
                Text(
                    text = "حذف درس «${subject.name}»",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "آیا مطمئن هستید که می‌خواهید این درس و تمامی تست‌های ثبت‌شده آن را حذف کنید؟ این عملیات غیرقابل بازگشت است."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteSubject() },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("حذف درس", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteSubject() }) {
                    Text("انصراف")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Detailed Statistics Dialog
    subjectForStats?.let { subjectWithStats ->
        StatisticsDialog(
            subjectWithStats = subjectWithStats,
            onDismiss = { viewModel.dismissStatsDialog() }
        )
    }
}

@Composable
private fun GlobalOverviewCard(
    totalSubjects: Int,
    totalQuestions: Int,
    totalRecorded: Int,
    overallPercentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "وضعیت کل مطالعه",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewPill(
                    label = "کل درس‌ها",
                    value = "${PersianNumberHelper.toPersianDigits(totalSubjects)} درس"
                )
                OverviewPill(
                    label = "کل تست‌ها",
                    value = "${PersianNumberHelper.toPersianDigits(totalQuestions)} تست"
                )
                OverviewPill(
                    label = "تست‌های زده‌شده",
                    value = "${PersianNumberHelper.toPersianDigits(totalRecorded)} تست"
                )
                OverviewPill(
                    label = "پیشرفت کل",
                    value = PersianNumberHelper.toPersianPercentage(overallPercentage)
                )
            }
        }
    }
}

@Composable
private fun OverviewPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun EmptySubjectsView(onAddSubject: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "هنوز درسی اضافه نکرده‌اید",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "برای شروع، درس‌های خود مانند شیمی، فیزیک یا ریاضی را همراه با تعداد تست دلخواه تعریف کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onAddSubject,
                modifier = Modifier.testTag("empty_state_add_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("افزودن اولین درس", fontWeight = FontWeight.Bold)
            }
        }
    }
}
