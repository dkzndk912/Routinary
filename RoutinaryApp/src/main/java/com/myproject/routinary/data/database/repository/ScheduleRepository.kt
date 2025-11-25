package com.myproject.routinary.data.database.repository

import kotlinx.coroutines.flow.Flow

import com.myproject.routinary.data.database.dao.ScheduleDao
import com.myproject.routinary.data.database.entity.Diary
import com.myproject.routinary.data.database.entity.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

import javax.inject.Inject

class ScheduleRepository @Inject constructor(private val scheduleDao: ScheduleDao) {

    // ViewModel이 요청할 수 있도록 Flow를 그대로 노출합니다.
    val allSchedules: Flow<List<Schedule>> = scheduleDao.getAllSchedules()
    val maxId: Flow<Int?> = scheduleDao.getMaxNumber()

    // DAO의 삽입 함수를 호출합니다.
    suspend fun insert(schedule: Schedule) : Boolean {
        delay(15L)
        if (null != scheduleDao.getParentDateIDById(schedule.dateID)) {
            if (scheduleDao.hasDateID(schedule.dateID) and scheduleDao.hasScheduleID(schedule.scheduleID)) {
                update(schedule)
                return true
            } else {
                val result = withContext(Dispatchers.IO) {
                    val newEntry = schedule.copy()
                    scheduleDao.insertSchedule(newEntry)
                    true
                }
                return result
            }
        } else {
            return false
        }
    }

    suspend fun update(schedule: Schedule) {
        scheduleDao.update(schedule.scheduleID, schedule.scheduleTtile,
            schedule.scheduleContent, schedule.alarmAllow,
            schedule.alarmTime)
    }

    suspend fun delete(schedueId: Int) {
        scheduleDao.delete(schedueId)
    }

    suspend fun deleteAll() {
        scheduleDao.deleteAll()
    }

    // (여기에 필요하다면 네트워크 API 호출 로직도 추가될 수 있습니다.)
}