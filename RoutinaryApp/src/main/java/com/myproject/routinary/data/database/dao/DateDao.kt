package com.myproject.routinary.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

import com.myproject.routinary.data.database.entity.RoutinaryDate

@Dao // 1. DAO임을 선언
interface DateDao {
    @Insert // 2. 삽입 쿼리
    suspend fun insertDate(routinaryDate: RoutinaryDate) // suspend는 코루틴 내에서 비동기 작업을 수행함을 의미

    @Query("SELECT * FROM RoutinaryDate ORDER BY dateID asc") // 3. 조회 쿼리 (SQL 직접 작성)
    fun getAllDates(): Flow<List<RoutinaryDate>> // Flow는 데이터 변경 시 실시간으로 알림을 받을 수 있게 함

    @Query("UPDATE ROUTINARYDATE SET numbering = numbering + 1 where dateID = :id")
    suspend fun plusNumbering(id: String)

    @Query("UPDATE ROUTINARYDATE SET numbering = numbering - 1 where dateID = :id")
    suspend fun minusNumbering(id: String)

    @Query("DELETE FROM RoutinaryDate") // 4. 삭제 쿼리
    suspend fun deleteAll()

    @Query("SELECT dateID FROM RoutinaryDate WHERE dateID = :id LIMIT 1")
    suspend fun getDateIDById(id: String): String?

    // @Update, @Delete 등 다른 어노테이션도 사용 가능
}