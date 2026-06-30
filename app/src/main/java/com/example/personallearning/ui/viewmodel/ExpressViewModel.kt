package com.example.personallearning.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personallearning.data.local.AppDatabase
import com.example.personallearning.data.model.ExpressEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExerciseState(
    val id: String, val title: String, val subtitle: String,
    val icon: String, val durationMin: Int,
    val content: String = "", val done: Boolean = false
)

class ExpressViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).expressDao()
    private val today = LocalDate.now().toString()

    val allEntries: Flow<List<ExpressEntry>> = dao.getAllEntries()

    private val _todayEntry = MutableStateFlow<ExpressEntry?>(null)
    val todayEntry: StateFlow<ExpressEntry?> = _todayEntry.asStateFlow()

    val exercises = listOf(
        ExerciseState("retelling", "复述训练", "用自己的话复述核心观点", "📖", 3),
        ExerciseState("keywords", "关键词串联", "用3个词撑起一段话", "🔑", 2),
        ExerciseState("observation", "微观观察", "描述今天被忽略的小事", "🔍", 3),
        ExerciseState("listening", "听觉预线", "听一段音频并复述", "🎧", 3),
        ExerciseState("compression", "压缩表达", "2分钟内说清一件事", "🗣️", 2),
    )

    private val _exerciseStates = MutableStateFlow(exercises)
    val exerciseStates: StateFlow<List<ExerciseState>> = _exerciseStates.asStateFlow()

    init { loadToday() }

    fun loadToday() {
        viewModelScope.launch {
            val entry = dao.getEntryByDate(today)
            _todayEntry.value = entry
            _exerciseStates.value = exercises.map { ex ->
                val text = when (ex.id) {
                    "retelling" -> entry?.retelling ?: ""
                    "keywords" -> entry?.keywords ?: ""
                    "observation" -> entry?.observation ?: ""
                    "listening" -> entry?.listening ?: ""
                    "compression" -> entry?.compression ?: ""
                    else -> ""
                }
                ex.copy(content = text, done = text.isNotBlank())
            }
        }
    }

    fun saveExercise(exId: String, text: String) {
        val updatedStates = _exerciseStates.value.map {
            if (it.id == exId) it.copy(content = text, done = text.isNotBlank()) else it
        }
        _exerciseStates.value = updatedStates

        viewModelScope.launch {
            val entry = _todayEntry.value ?: ExpressEntry(date = today)
            val newEntry = when (exId) {
                "retelling" -> entry.copy(retelling = text)
                "keywords" -> entry.copy(keywords = text)
                "observation" -> entry.copy(observation = text)
                "listening" -> entry.copy(listening = text)
                "compression" -> entry.copy(compression = text)
                else -> entry
            }
            val doneCount = updatedStates.count { it.done }
            val completedEntry = newEntry.copy(duration = doneCount * 3)
            if (_todayEntry.value == null) {
                val id = dao.insert(completedEntry)
                _todayEntry.value = completedEntry.copy(id = id)
            } else {
                dao.update(completedEntry)
                _todayEntry.value = completedEntry
            }
        }
    }

    val todayCompleted get() = _exerciseStates.value.count { it.done }
    val todayTotal get() = _exerciseStates.value.size
}
