package com.example.personallearning.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personallearning.data.model.DaoHenEntry
import com.example.personallearning.data.repository.DaoHenRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class DaoHenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DaoHenRepository(application)

    val allEntries: StateFlow<List<DaoHenEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _todayEntry = MutableStateFlow<DaoHenEntry?>(null)
    val todayEntry: StateFlow<DaoHenEntry?> = _todayEntry.asStateFlow()

    private val _yesterdayStone = MutableStateFlow("")
    val yesterdayStone: StateFlow<String> = _yesterdayStone.asStateFlow()

    val todayStr = LocalDate.now().toString()

    init {
        loadToday()
    }

    fun loadToday() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            _todayEntry.value = repository.getEntryByDate(today)
            _yesterdayStone.value = repository.getYesterdayStone(today) ?: ""
        }
    }

    fun saveAnswer(field: String, value: String) {
        val entry = _todayEntry.value ?: DaoHenEntry(date = todayStr)
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
        _todayEntry.value = updated
        viewModelScope.launch { repository.saveEntry(updated) }
    }

    fun sync() {
        viewModelScope.launch {
            repository.syncToServer("http://49.232.149.194:5001")
            repository.pullFromServer("http://49.232.149.194:5001")
            loadToday()
        }
    }
}
