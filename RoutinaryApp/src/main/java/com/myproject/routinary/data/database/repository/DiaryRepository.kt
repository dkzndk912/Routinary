package com.myproject.routinary.data.database.repository

import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

import com.myproject.routinary.data.database.dao.DateDao
import com.myproject.routinary.data.database.dao.DiaryDao
import com.myproject.routinary.data.database.entity.Diary
import com.myproject.routinary.data.database.entity.RoutinaryDate
import kotlinx.coroutines.delay

import javax.inject.Inject

class DiaryRepository @Inject constructor(private val diaryDao: DiaryDao) {

    // ViewModel이 요청할 수 있도록 Flow를 그대로 노출합니다.
    val allDiaries: Flow<List<Diary>> = diaryDao.getAllDiaries()

    // DAO의 삽입 함수를 호출합니다.
    suspend fun insert(diary: Diary) : Boolean {
        delay(10L)
        if (null != diaryDao.getParentDateIDById(diary.dateID)) {
            if (diaryDao.hasDateID(diary.dateID)) {
                update(diary)
                return false
            } else {
                val newEntry = diary.copy()
                diaryDao.insertDiary(newEntry)
                return true
            }
        } else {
            return false
        }
    }

    suspend fun update(diary: Diary) {
        diaryDao.update(diary.dateID, diary.diaryTitle, diary.diaryContent)
    }

    suspend fun deleteAll() {
        diaryDao.deleteAll()
    }

    // (여기에 필요하다면 네트워크 API 호출 로직도 추가될 수 있습니다.)
}