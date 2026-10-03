package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Question
import com.example.data.model.QuestionStatus
import com.example.data.model.QuestionUiModel
import com.example.data.model.Subject
import com.example.data.model.SubjectWithStats
import com.example.data.repository.TestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data class Questions(val subjectId: Long) : Screen
}

class MainViewModel(
    private val repository: TestRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val subjectsWithStats: StateFlow<List<SubjectWithStats>> = repository.subjectsWithStats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeSubjectId = MutableStateFlow<Long?>(null)
    val activeSubjectId: StateFlow<Long?> = _activeSubjectId.asStateFlow()

    // Active Subject with Stats
    val activeSubjectWithStats: StateFlow<SubjectWithStats?> = combine(
        _activeSubjectId,
        subjectsWithStats
    ) { id, list ->
        if (id == null) null else list.find { it.subject.id == id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Questions observed for active subject
    private val activeQuestionsFlow = _activeSubjectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getQuestionsForSubject(id)
    }

    // Filter status: null = ALL
    private val _selectedFilter = MutableStateFlow<QuestionStatus?>(null)
    val selectedFilter: StateFlow<QuestionStatus?> = _selectedFilter.asStateFlow()

    // Search query for question number
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // All QuestionUiModels for active subject (1..totalQuestions)
    val allQuestionsUiModels: StateFlow<List<QuestionUiModel>> = combine(
        activeSubjectWithStats,
        activeQuestionsFlow
    ) { subjectStats, dbQuestions ->
        if (subjectStats == null) return@combine emptyList()
        val total = subjectStats.subject.totalQuestions
        val recordedMap = dbQuestions.associateBy { it.questionNumber }

        (1..total).map { num ->
            val record = recordedMap[num]
            if (record != null) {
                QuestionUiModel(
                    questionNumber = num,
                    subjectId = subjectStats.subject.id,
                    id = record.id,
                    correctOption = record.correctOption,
                    status = record.status,
                    createdAt = record.createdAt,
                    updatedAt = record.updatedAt
                )
            } else {
                QuestionUiModel(
                    questionNumber = num,
                    subjectId = subjectStats.subject.id,
                    status = QuestionStatus.UNRECORDED
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Question list according to status filter and search query
    val filteredQuestions: StateFlow<List<QuestionUiModel>> = combine(
        allQuestionsUiModels,
        _selectedFilter,
        _searchQuery
    ) { questions, filter, query ->
        var result = questions

        // Filter by status
        if (filter != null) {
            result = result.filter { it.status == filter }
        }

        // Filter by search query (question number)
        val cleanQuery = query.trim()
        if (cleanQuery.isNotBlank()) {
            val targetNum = cleanQuery.toIntOrNull()
            result = if (targetNum != null) {
                result.filter { it.questionNumber.toString().contains(cleanQuery) }
            } else {
                result
            }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dialog & Editing States
    private val _editingQuestionNumber = MutableStateFlow<Int?>(null)
    val editingQuestion: StateFlow<QuestionUiModel?> = combine(
        allQuestionsUiModels,
        _editingQuestionNumber
    ) { questions, num ->
        if (num == null) null else questions.find { it.questionNumber == num }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _subjectToEdit = MutableStateFlow<Subject?>(null)
    val subjectToEdit: StateFlow<Subject?> = _subjectToEdit.asStateFlow()

    private val _isAddSubjectDialogOpen = MutableStateFlow(false)
    val isAddSubjectDialogOpen: StateFlow<Boolean> = _isAddSubjectDialogOpen.asStateFlow()

    private val _subjectToDelete = MutableStateFlow<Subject?>(null)
    val subjectToDelete: StateFlow<Subject?> = _subjectToDelete.asStateFlow()

    private val _subjectForStatsDialog = MutableStateFlow<SubjectWithStats?>(null)
    val subjectForStatsDialog: StateFlow<SubjectWithStats?> = _subjectForStatsDialog.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedSampleIfEmpty()
        }
    }

    // Navigation actions
    fun openSubject(subjectId: Long) {
        _activeSubjectId.value = subjectId
        _selectedFilter.value = null
        _searchQuery.value = ""
        _currentScreen.value = Screen.Questions(subjectId)
    }

    fun navigateHome() {
        _currentScreen.value = Screen.Home
        _activeSubjectId.value = null
        _editingQuestionNumber.value = null
        _selectedFilter.value = null
        _searchQuery.value = ""
    }

    // Filter and search actions
    fun setFilter(status: QuestionStatus?) {
        _selectedFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun jumpToQuestion(questionNumber: Int) {
        val active = activeSubjectWithStats.value ?: return
        if (questionNumber in 1..active.subject.totalQuestions) {
            _selectedFilter.value = null
            _searchQuery.value = ""
            _editingQuestionNumber.value = questionNumber
        }
    }

    // Question recording and editing
    fun openQuestionEdit(questionNumber: Int) {
        _editingQuestionNumber.value = questionNumber
    }

    fun closeQuestionEdit() {
        _editingQuestionNumber.value = null
    }

    fun saveQuestionRecord(
        correctOption: Int,
        status: QuestionStatus,
        andNext: Boolean
    ) {
        val subjectId = _activeSubjectId.value ?: return
        val currentNum = _editingQuestionNumber.value ?: return
        val total = activeSubjectWithStats.value?.subject?.totalQuestions ?: currentNum

        viewModelScope.launch {
            repository.recordQuestion(
                subjectId = subjectId,
                questionNumber = currentNum,
                correctOption = correctOption,
                status = status
            )

            if (andNext && currentNum < total) {
                _editingQuestionNumber.value = currentNum + 1
            } else if (!andNext) {
                // Keep dialog open or user can close
            }
        }
    }

    fun clearQuestionRecord() {
        val subjectId = _activeSubjectId.value ?: return
        val currentNum = _editingQuestionNumber.value ?: return

        viewModelScope.launch {
            repository.deleteQuestionRecord(subjectId, currentNum)
        }
    }

    fun navigatePreviousQuestion() {
        val current = _editingQuestionNumber.value ?: return
        if (current > 1) {
            _editingQuestionNumber.value = current - 1
        }
    }

    fun navigateNextQuestion() {
        val current = _editingQuestionNumber.value ?: return
        val total = activeSubjectWithStats.value?.subject?.totalQuestions ?: current
        if (current < total) {
            _editingQuestionNumber.value = current + 1
        }
    }

    // Subject management
    fun openAddSubjectDialog() {
        _isAddSubjectDialogOpen.value = true
    }

    fun closeAddSubjectDialog() {
        _isAddSubjectDialogOpen.value = false
    }

    fun openEditSubjectDialog(subject: Subject) {
        _subjectToEdit.value = subject
    }

    fun closeEditSubjectDialog() {
        _subjectToEdit.value = null
    }

    fun saveNewSubject(name: String, totalQuestions: Int) {
        viewModelScope.launch {
            repository.addSubject(name, totalQuestions)
            _isAddSubjectDialogOpen.value = false
        }
    }

    fun updateSubject(name: String, totalQuestions: Int) {
        val subject = _subjectToEdit.value ?: return
        viewModelScope.launch {
            repository.updateSubject(subject.id, name, totalQuestions)
            _subjectToEdit.value = null
        }
    }

    fun requestDeleteSubject(subject: Subject) {
        _subjectToDelete.value = subject
    }

    fun cancelDeleteSubject() {
        _subjectToDelete.value = null
    }

    fun confirmDeleteSubject() {
        val subject = _subjectToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteSubject(subject)
            _subjectToDelete.value = null
            if (_activeSubjectId.value == subject.id) {
                navigateHome()
            }
        }
    }

    // Stats dialog
    fun showStatsDialog(subjectWithStats: SubjectWithStats) {
        _subjectForStatsDialog.value = subjectWithStats
    }

    fun dismissStatsDialog() {
        _subjectForStatsDialog.value = null
    }
}

class MainViewModelFactory(
    private val repository: TestRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
