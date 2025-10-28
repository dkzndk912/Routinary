package com.myproject.routinary.data.database.repository

import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

import com.myproject.routinary.data.database.dao.DateDao
import com.myproject.routinary.data.database.entity.RoutinaryDate

import javax.inject.Inject

class DateRepository @Inject constructor(private val dateDao: DateDao) {

    // ViewModel이 요청할 수 있도록 Flow를 그대로 노출합니다.
    val allDates: Flow<List<RoutinaryDate>> = dateDao.getAllDates()

    // DAO의 삽입 함수를 호출합니다.
    suspend fun insert(date: RoutinaryDate): Boolean {
        val currentMaxNumber : Int? = dateDao.getMaxNumber()
        val nextNumber : Int = if (currentMaxNumber == null) {
            0
        } else {
            currentMaxNumber + 1
        }

        if (null == dateDao.getDateIDById(date.dateID)) {
            val newEntry = date.copy(numbering = nextNumber)
            dateDao.insertDate(newEntry)
            return true
        } else {
            return false
        }
    }

    suspend fun deleteAll() {
        dateDao.deleteAll()
    }

    // (여기에 필요하다면 네트워크 API 호출 로직도 추가될 수 있습니다.)
}