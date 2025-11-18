package com.myproject.routinary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room // 데이터베이스 빌더 사용을 위해 필요
import com.myproject.routinary.data.database.dao.DateDao
import com.myproject.routinary.data.database.dao.DiaryDao
import com.myproject.routinary.data.database.dao.ScheduleDao
import com.myproject.routinary.data.database.entity.Diary

import com.myproject.routinary.data.database.entity.RoutinaryDate
import com.myproject.routinary.data.database.entity.Schedule

// 1. @Database 어노테이션
@Database(
    entities = [
        RoutinaryDate::class,
        Diary::class,
        Schedule::class
               ], // 💡 1. 포함할 모든 Entity 클래스 목록
    version = 5,                           // 💡 2. 데이터베이스 버전 관리
    exportSchema = false                 // (선택 사항) 스키마 내보내기 설정
)
// 2. RoomDatabase를 상속받는 추상 클래스로 정의
abstract class RoutinaryDB : RoomDatabase() {

    // 💡 3. DAO 접근자: 추상 메서드로 정의
    abstract fun dateDao(): DateDao
    abstract fun diaryDao(): DiaryDao
    abstract fun scheduleDao(): ScheduleDao
    // abstract fun bookDao(): BookDao // 다른 DAO가 있다면 추가

    // 💡 4. 싱글톤 패턴 구현 (가장 중요)
    companion object {
        @Volatile
        private var INSTANCE: RoutinaryDB? = null

        fun getDatabase(context: Context): RoutinaryDB {
            // 이전에 생성된 인스턴스가 있다면 반환
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoutinaryDB::class.java,
                    "RoutinaryDB" // 💡 데이터베이스 파일 이름
                )
                    // .addMigrations(...) // 데이터베이스 버전 변경 시 마이그레이션 로직 추가
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}