package com.myproject.routinary.data.database.repository

import kotlinx.coroutines.flow.Flow

import com.myproject.routinary.data.database.dao.RoutineDao
import com.myproject.routinary.data.database.entity.Diary
import com.myproject.routinary.data.database.entity.Routine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

import javax.inject.Inject

class RoutineRepository @Inject constructor(private val routineDao: RoutineDao) {

    // ViewModel이 요청할 수 있도록 Flow를 그대로 노출합니다.
    val allRoutines: Flow<List<Routine>> = routineDao.getAllRoutines()
    val maxId: Flow<Int?> = routineDao.getMaxNumber()

    // DAO의 삽입 함수를 호출합니다.
    suspend fun insert(routine: Routine) : Boolean {
        delay(15L)
        if (null != routineDao.getParentDateIDById(routine.dateID)) {
            if (routineDao.hasDateID(routine.dateID) and routineDao.hasRoutineID(routine.routineID)) {
                update(routine)
                return true
            } else {
                val result = withContext(Dispatchers.IO) {
                    val newEntry = routine.copy()
                    routineDao.insertRoutine(newEntry)
                    true
                }
                return result
            }
        } else {
            return false
        }
    }

    suspend fun update(routine: Routine) {
        routineDao.update(routine.routineID, routine.routineTtile,
            routine.routineContent, routine.alarmAllow,
            routine.alarmTime)
    }

    suspend fun delete(schedueId: Int) {
        routineDao.delete(schedueId)
    }

    suspend fun deleteAll() {
        routineDao.deleteAll()
    }

    // (여기에 필요하다면 네트워크 API 호출 로직도 추가될 수 있습니다.)
}