package com.myproject.routinary.data.database.repository

import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

import com.myproject.routinary.data.database.dao.DateDao
import com.myproject.routinary.data.database.entity.RoutinaryDate

import javax.inject.Inject

class DateRepository @Inject constructor(private val dateDao: DateDao) {

    // ViewModel이 요청할 수 있도록 Flow를 그대로 노출합니다.
    val allDates: Flow<List<RoutinaryDate>> = dateDao.getAllDates()

    // DAO의 삽입 함수를 호출합니다.
    suspend fun insert(date: RoutinaryDate): Boolean {
        if (null == dateDao.getDateIDById(date.dateID)) {
            dateDao.insertDate(date)
            return true
        } else {
            return false
        }
    }

    suspend fun plusNumbering(dateID: String) {
        dateDao.plusNumbering(dateID)
    }

    suspend fun minusNumbering(dateID: String) {
        dateDao.minusNumbering(dateID)
    }

    suspend fun deleteAll() {
        dateDao.deleteAll()
    }

    // (여기에 필요하다면 네트워크 API 호출 로직도 추가될 수 있습니다.)
}