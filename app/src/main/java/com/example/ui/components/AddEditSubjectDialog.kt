package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.util.PersianNumberHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditSubjectDialog(
    subjectToEdit: Subject? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, totalQuestions: Int) -> Unit
) {
    var name by remember(subjectToEdit) { mutableStateOf(subjectToEdit?.name ?: "") }
    var totalQuestionsStr by remember(subjectToEdit) {
        mutableStateOf(subjectToEdit?.totalQuestions?.toString() ?: "")
    }
    var nameError by remember { mutableStateOf<String?>(null) }
    var countError by remember { mutableStateOf<String?>(null) }

    val quickCounts = listOf(50, 100, 150, 200, 300, 400, 500)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (subjectToEdit == null) "ایجاد درس جدید" else "ویرایش درس",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "اطلاعات درس و تعداد تست‌های مورد نظر را وارد کنید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("نام درس (مثلاً: شیمی، فیزیک)") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_subject_name"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = totalQuestionsStr,
                    onValueChange = {
                        // Keep only digits
                        val digits = it.filter { char -> char.isDigit() }
                        totalQuestionsStr = digits
                        if (digits.isNotBlank()) countError = null
                    },
                    label = { Text("تعداد تست‌ها (مثلاً: ۳۰۰)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = countError != null,
                    supportingText = countError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_subject_count"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "انتخاب سریع تعداد تست:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickCounts.forEach { count ->
                        val isSelected = totalQuestionsStr == count.toString()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.clickable {
                                totalQuestionsStr = count.toString()
                                countError = null
                            }
                        ) {
                            Text(
                                text = "${PersianNumberHelper.toPersianDigits(count)} تست",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (name.isBlank()) {
                        nameError = "لطفاً نام درس را وارد کنید"
                        hasError = true
                    }
                    val count = totalQuestionsStr.toIntOrNull()
                    if (count == null || count <= 0) {
                        countError = "لطفاً تعداد تست معتبری وارد کنید"
                        hasError = true
                    }

                    if (!hasError && count != null) {
                        onConfirm(name.trim(), count)
                    }
                },
                modifier = Modifier.testTag("save_subject_button")
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
