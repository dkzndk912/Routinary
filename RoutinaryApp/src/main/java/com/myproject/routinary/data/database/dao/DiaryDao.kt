package com.myproject.routinary.data.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.myproject.routinary.data.database.entity.Diary
import kotlinx.coroutines.flow.Flow

import com.myproject.routinary.data.database.entity.RoutinaryDate

data class JoinedDiary(
    @Embedded
    val parent: RoutinaryDate, // 부모 엔티티를 포함

    @Relation(
        parentColumn = "dateID", // 부모 엔티티에서 참조할 컬럼 (기본 키)
        entityColumn = "dateID"  // 자식 엔티티에서 외래 키 역할을 하는 컬럼
    )
    val children: List<Diary> // 이 부모에 속한 모든 자식 엔티티 리스트
)

@Dao // 1. DAO임을 선언
interface DiaryDao {
    @Insert // 2. 삽입 쿼리
    suspend fun insertDiary(diary: Diary) // suspend는 코루틴 내에서 비동기 작업을 수행함을 의미

    @Query("SELECT * FROM Diary ORDER BY dateID asc") // 3. 조회 쿼리 (SQL 직접 작성)
    fun getAllDiaries(): Flow<List<Diary>> // Flow는 데이터 변경 시 실시간으로 알림을 받을 수 있게 함

    @Query("DELETE FROM Diary") // 4. 삭제 쿼리
    suspend fun deleteAll()

    @Query("SELECT dateID FROM RoutinaryDate WHERE dateID = :id LIMIT 1")
    suspend fun getDateIDById(id: String): String?

    // @Update, @Delete 등 다른 어노테이션도 사용 가능
}