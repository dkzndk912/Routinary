package com.myproject.routinary.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myproject.routinary.data.database.entity.Diary
import com.myproject.routinary.data.database.entity.RoutinaryDate
import com.myproject.routinary.data.database.entity.Schedule
import com.myproject.routinary.data.database.repository.DateRepository
import com.myproject.routinary.data.database.repository.DiaryRepository
import com.myproject.routinary.data.database.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor (private val repository: ScheduleRepository) : ViewModel() {

    private val _isScheduleAdded = MutableStateFlow<Boolean?>(null)
    val isScheduleAdded: StateFlow<Boolean?> = _isScheduleAdded.asStateFlow()
    // 💡 1. Flow를 Compose State로 변환 (StateFlow나 LiveData 사용 가능)
    // 이 상태를 Compose에서 관찰(collectAsState)하여 UI에 반영합니다.
    val allSchedules: StateFlow<List<Schedule>> = repository.allSchedules
        .stateIn(
            scope = viewModelScope, // ViewModel의 생명 주기에 맞게 동작하도록 설정
            started = SharingStarted.WhileSubscribed(5000), // 구독자가 있을 때 활성화
            initialValue = emptyList()
        )

    val maxId: StateFlow<Int?> = repository.maxId
        .stateIn(
            scope = viewModelScope, // ViewModel의 생명 주기에 맞게 동작하도록 설정
            started = SharingStarted.WhileSubscribed(5000), // 구독자가 있을 때 활성화
            initialValue = 1
        )

    // 💡 2. 사용자 이벤트를 처리하는 함수
    fun addNewSchedule(dateID : String, title : String, content : String, allowFlag: Boolean, alarmTime: String) {
        // 비동기 작업을 위해 viewModelScope 코루틴을 사용
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val newSchedule = Schedule(
                    dateID = dateID,
                    scheduleTtile = title,
                    scheduleContent = content,
                    alarmAllow = allowFlag,
                    alarmTime = alarmTime
                )
                // Repository의 insert 함수는 suspend 함수여야 합니다.
                repository.insert(newSchedule)
            }
            _isScheduleAdded.value = result
        }
    }

    fun updateSchedule(scheduleID: Int, dateID : String, title : String, content : String, allowFlag: Boolean, alarmTime: String) {
        // 비동기 작업을 위해 viewModelScope 코루틴을 사용
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val newSchedule = Schedule(scheduleID = scheduleID, dateID =  dateID, scheduleTtile = title, scheduleContent = content, alarmAllow = allowFlag, alarmTime = alarmTime)
                // Repository의 insert 함수는 suspend 함수여야 합니다.
                repository.insert(newSchedule)
            }
            _isScheduleAdded.value = result
        }
    }

    fun delete(scheduleID: Int) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.delete(scheduleID) // Repository에 데이터 삽입 요청
                true
            }
            _isScheduleAdded.value = result
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll() // Repository에 데이터 삽입 요청
        }
    }

    fun setIsScheduleAddedNull() {
        _isScheduleAdded.value = null
    }
}