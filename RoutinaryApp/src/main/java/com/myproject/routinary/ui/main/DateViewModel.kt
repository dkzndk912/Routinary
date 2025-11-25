package com.myproject.routinary.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myproject.routinary.data.database.entity.RoutinaryDate
import com.myproject.routinary.data.database.repository.DateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DateViewModel @Inject constructor (private val repository: DateRepository) : ViewModel() {

    // 💡 1. Flow를 Compose State로 변환 (StateFlow나 LiveData 사용 가능)
    // 이 상태를 Compose에서 관찰(collectAsState)하여 UI에 반영합니다.
    val allDates: StateFlow<List<RoutinaryDate>> = repository.allDates
        .stateIn(
            scope = viewModelScope, // ViewModel의 생명 주기에 맞게 동작하도록 설정
            started = SharingStarted.WhileSubscribed(5000), // 구독자가 있을 때 활성화
            initialValue = emptyList()
        )

    private val _isDateAdded = MutableStateFlow<Boolean?>(null)
    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val isDateAdded: StateFlow<Boolean?> = _isDateAdded.asStateFlow()
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()


    fun createDateID() : String {
        val date = Date()
        val sdf = SimpleDateFormat("yyyyMMdd")

        val dateID = sdf.format(date)

        return dateID
    }

    fun createDateID(date: LocalDate) : String {
        val dtf = DateTimeFormatter.ofPattern("yyyyMMdd")

        val dateID = date.format(dtf)

        return dateID
    }

    // 💡 2. 사용자 이벤트를 처리하는 함수
    fun addNewDate(dateID : String) {
        // 비동기 작업을 위해 viewModelScope 코루틴을 사용
        viewModelScope.launch {
            // withContext(Dispatchers.IO)를 사용하여 비동기 I/O 작업을 수행
            val result = withContext(Dispatchers.IO) {
                val newDate = RoutinaryDate(dateID = dateID)
                // Repository의 insert 함수는 suspend 함수여야 합니다.
                repository.insert(newDate)
            }
            repository.plusNumbering(dateID)
            _isDateAdded.value = result
        }
    }

    fun plusNumbering(dateID: String) {
        viewModelScope.launch {
            repository.plusNumbering(dateID)
        }
    }

    fun minusNumbering(dateID: String) {
        viewModelScope.launch {
            repository.minusNumbering(dateID)
        }
    }



    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll() // Repository에 데이터 삽입 요청
        }
    }

    fun setIsDateAddedNull() {
        _isDateAdded.value = null
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

}