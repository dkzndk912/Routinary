package com.myproject.routinary.data

// AppModule.kt (예시: data 패키지 내에 생성)

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import androidx.room.Room
import com.myproject.routinary.data.database.RoutinaryDB
import com.myproject.routinary.data.database.dao.DateDao
import com.myproject.routinary.data.database.dao.DiaryDao
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class) // 💡 앱 생명주기 동안 유지되는 싱글톤 컨테이너
object AppModule {

    // 💡 1. AppDatabase 인스턴스를 제공하는 방법
    @Provides
    @Singleton // 앱이 실행되는 동안 단 하나의 인스턴스만 생성하도록 지정
    fun provideDatabase(@ApplicationContext context: Context): RoutinaryDB {
        return Room.databaseBuilder(
            context,
            RoutinaryDB::class.java,
            "routinary_db"
        )
            .fallbackToDestructiveMigration() // 테이블 구조 달라지면 테이블 삭제 (테스트용)
            .build()

    }

    // 💡 2. DateDao 인스턴스를 제공하는 방법 (위에서 만든 DB 인스턴스를 인자로 받습니다)
    @Provides
    fun provideDateDao(database: RoutinaryDB): DateDao {
        return database.dateDao()
    }

    @Provides
    fun provideDiaryDao(database: RoutinaryDB): DiaryDao {
        return database.diaryDao()
    }
}