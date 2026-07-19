package com.example.personallearning.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personallearning.data.model.DaoHenEntry
import com.example.personallearning.data.repository.DaoHenRepository
import com.example.personallearning.data.repository.SyncResult
import com.example.personallearning.data.repository.SyncConflict
import com.example.personallearning.data.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

class DaoHenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DaoHenRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val saveTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private var saveJob: Job? = null
    private val saveMutex = Mutex()

    val allEntries: StateFlow<List<DaoHenEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentEntry = MutableStateFlow<DaoHenEntry?>(null)
    val currentEntry: StateFlow<DaoHenEntry?> = _currentEntry.asStateFlow()

    private val _yesterdayStone = MutableStateFlow("")
    val yesterdayStone: StateFlow<String> = _yesterdayStone.asStateFlow()

    private val _saveStatus = MutableStateFlow("")
    val saveStatus: StateFlow<String> = _saveStatus.asStateFlow()

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    private val _syncConflicts = MutableStateFlow<List<SyncConflict>>(emptyList())
    val syncConflicts: StateFlow<List<SyncConflict>> = _syncConflicts.asStateFlow()

    private val _pendingAction = MutableStateFlow<DaoHenEntry?>(null)
    val pendingAction: StateFlow<DaoHenEntry?> = _pendingAction.asStateFlow()

    val todayProgress: StateFlow<DaoHenProgress> = allEntries
        .map { entries ->
            val today = LocalDate.now().toString()
            entries.firstOrNull { it.date == today }.toProgress()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DaoHenProgress())

    val weeklySummary: StateFlow<WeeklySummary> = allEntries
        .map { entries -> entries.toWeeklySummary() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklySummary())

    init {
        loadSelectedDate()
        loadPendingAction()
    }

    fun saveTags(tags: Set<String>) {
        val entry = _currentEntry.value ?: DaoHenEntry(date = _selectedDate.value.toString())
        saveEntryImmediately(entry.copy(tags = tags.sorted().joinToString(",")))
    }

    fun verifyPendingAction(status: Int, note: String = "") {
        val entry = _pendingAction.value ?: return
        viewModelScope.launch {
            val updated = entry.copy(actionStatus = status, actionNote = note)
            repository.saveEntry(updated)
            _pendingAction.value = null
        }
    }

    private fun loadPendingAction() {
        viewModelScope.launch {
            _pendingAction.value = repository.getPendingAction(LocalDate.now().toString())
        }
    }

    private fun saveEntryImmediately(entry: DaoHenEntry) {
        _currentEntry.value = entry
        viewModelScope.launch {
            repository.saveEntry(entry)
            _saveStatus.value = "已保存 ${java.time.LocalTime.now().format(saveTimeFormatter)}"
        }
    }

    fun selectDate(date: LocalDate) {
        if (_selectedDate.value == date) return
        _saveStatus.value = ""
        _selectedDate.value = date
        _currentEntry.value = null
        _yesterdayStone.value = ""
        loadSelectedDate()
    }

    fun moveDate(days: Long) {
        selectDate(_selectedDate.value.plusDays(days))
    }

    fun loadSelectedDate() {
        viewModelScope.launch {
            val date = _selectedDate.value.toString()
            saveJob?.join()
            val entry = repository.getEntryByDate(date)
            val previousStone = repository.getYesterdayStone(date) ?: ""
            if (_selectedDate.value.toString() == date) {
                _currentEntry.value = entry
                _yesterdayStone.value = previousStone
            }
        }
    }

    fun saveAnswer(field: String, value: String) {
        val entry = _currentEntry.value ?: DaoHenEntry(date = _selectedDate.value.toString())
        val updated = when (field) {
            "q1" -> entry.copy(q1 = value)
            "q2" -> entry.copy(q2 = value)
            "q3" -> entry.copy(q3 = value)
            "q4" -> entry.copy(q4 = value)
            "q5" -> entry.copy(q5 = value)
            "q6" -> entry.copy(q6 = value)
            "q7" -> entry.copy(q7 = value)
            else -> entry
        }
        _currentEntry.value = updated
        _saveStatus.value = "保存中..."
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(350)
            saveMutex.withLock {
                repository.saveEntry(updated)
            }
            if (_currentEntry.value == updated) {
                _saveStatus.value = "已保存 ${java.time.LocalTime.now().format(saveTimeFormatter)}"
            }
        }
    }

    fun sync() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Syncing
            saveJob?.join()
            val serverUrl = settingsRepository.settings.first().serverUrl
            when (val result = repository.syncAll(serverUrl)) {
                is SyncResult.Success -> {
                    _syncState.value = SyncUiState.Success(
                        "同步完成：上传 ${result.pushedCount} 条，拉取 ${result.pulledCount} 条"
                    )
                }
                is SyncResult.Failure -> {
                    _syncState.value = SyncUiState.Failure(result.message)
                }
                is SyncResult.Conflict -> {
                    _syncConflicts.value = result.conflicts
                    _syncState.value = SyncUiState.Conflict(result.conflicts.size)
                }
            }
            loadSelectedDate()
        }
    }

    fun resolveConflict(keepLocal: Boolean) {
        val conflict = _syncConflicts.value.firstOrNull() ?: return
        viewModelScope.launch {
            _syncState.value = SyncUiState.Syncing
            val result = if (keepLocal) {
                val serverUrl = settingsRepository.settings.first().serverUrl
                repository.resolveWithLocal(serverUrl, conflict)
            } else {
                repository.resolveWithRemote(conflict)
                SyncResult.Success()
            }

            if (result is SyncResult.Failure) {
                _syncState.value = SyncUiState.Failure(result.message)
                return@launch
            }

            _syncConflicts.value = _syncConflicts.value.drop(1)
            if (_syncConflicts.value.isEmpty()) {
                _syncState.value = SyncUiState.Success("冲突已处理，记录已安全保存")
            } else {
                _syncState.value = SyncUiState.Conflict(_syncConflicts.value.size)
            }
            loadSelectedDate()
        }
    }
}

data class DaoHenProgress(
    val answeredCount: Int = 0,
    val isComplete: Boolean = false,
    val mainStone: String = ""
)

data class WeeklySummary(
    val recordedDays: Int = 0,
    val completedDays: Int = 0,
    val verifiedActions: Int = 0,
    val fulfilledActions: Int = 0,
    val topTags: List<Pair<String, Int>> = emptyList()
)

sealed interface SyncUiState {
    data object Idle : SyncUiState
    data object Syncing : SyncUiState
    data class Success(val message: String) : SyncUiState
    data class Failure(val message: String) : SyncUiState
    data class Conflict(val count: Int) : SyncUiState
}

private fun DaoHenEntry?.toProgress(): DaoHenProgress {
    if (this == null) return DaoHenProgress()

    val answers = listOf(q1, q2, q3, q4, q5, q6, q7)
    val count = answers.count { it.isNotBlank() }
    return DaoHenProgress(
        answeredCount = count,
        isComplete = count == answers.size,
        mainStone = q6
    )
}

private fun List<DaoHenEntry>.toWeeklySummary(): WeeklySummary {
    val today = LocalDate.now()
    val start = today.with(DayOfWeek.MONDAY)
    val weekEntries = filter {
        val date = runCatching { LocalDate.parse(it.date) }.getOrNull()
        date != null && !date.isBefore(start) && !date.isAfter(today)
    }
    val tagCounts = weekEntries
        .flatMap { it.tags.split(',').map(String::trim).filter(String::isNotBlank) }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .take(3)
        .map { it.key to it.value }
    return WeeklySummary(
        recordedDays = weekEntries.count { listOf(it.q1, it.q2, it.q3, it.q4, it.q5, it.q6, it.q7).any(String::isNotBlank) },
        completedDays = weekEntries.count { listOf(it.q1, it.q2, it.q3, it.q4, it.q5, it.q6, it.q7).all(String::isNotBlank) },
        verifiedActions = weekEntries.count { it.actionStatus > 0 },
        fulfilledActions = weekEntries.count { it.actionStatus == 1 },
        topTags = tagCounts
    )
}
