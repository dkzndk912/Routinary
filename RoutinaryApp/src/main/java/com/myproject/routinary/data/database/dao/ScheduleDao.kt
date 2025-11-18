package com.myproject.routinary.data.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.myproject.routinary.data.database.entity.Diary
import kotlinx.coroutines.flow.Flow

import com.myproject.routinary.data.database.entity.RoutinaryDate
import com.myproject.routinary.data.database.entity.Schedule

//data class JoinedDiary(
//    @Embedded
//    val parent: RoutinaryDate, // 부모 엔티티를 포함
//
//    @Relation(
//        parentColumn = "dateID", // 부모 엔티티에서 참조할 컬럼 (기본 키)
//        entityColumn = "dateID"  // 자식 엔티티에서 외래 키 역할을 하는 컬럼
//    )
//    val children: List<Diary> // 이 부모에 속한 모든 자식 엔티티 리스트
//)

@Dao // 1. DAO임을 선언
interface ScheduleDao {
    @Insert // 2. 삽입 쿼리
    suspend fun insertSchedule(schedule: Schedule) // suspend는 코루틴 내에서 비동기 작업을 수행함을 의미

    @Query("SELECT * FROM Schedule ORDER BY dateID asc") // 3. 조회 쿼리 (SQL 직접 작성)
    fun getAllSchedules(): Flow<List<Schedule>> // Flow는 데이터 변경 시 실시간으로 알림을 받을 수 있게 함

    @Query("DELETE FROM Schedule") // 4. 삭제 쿼리
    suspend fun deleteAll()

    @Query("SELECT dateID FROM RoutinaryDate WHERE dateID = :id LIMIT 1")
    suspend fun getParentDateIDById(id: String): String?

    @Query("SELECT EXISTS(SELECT 1 FROM Schedule WHERE dateID = :id)")
    suspend fun hasDateID(id: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM Schedule WHERE scheduleID = :id)")
    suspend fun hasScheduleID(id: Int): Boolean

    @Query("UPDATE Schedule SET scheduleTtile = :newTitle, scheduleContent = :newContent, alarmAllow = :newFlag, alarmTime = :newTime WHERE dateID = :id")
    suspend fun update(id: String, newTitle: String, newContent: String, newFlag: Boolean, newTime: String): Int

    // @Update, @Delete 등 다른 어노테이션도 사용 가능
}