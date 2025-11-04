package com.myproject.routinary.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Diary",
    foreignKeys = [
        ForeignKey(
            entity = RoutinaryDate::class,
            parentColumns = ["dateID"], // 부모 엔티티의 기본 키
            childColumns = ["dateID"],  // 자식 엔티티의 외래 키 컬럼
            onDelete = ForeignKey.CASCADE
        )
    ],
    // 쿼리 성능을 위해 외래 키 컬럼에 Index 설정 권장
    indices = [Index(value = ["dateID"])]
    ) // 1. Entity임을 선언하고 테이블 이름 지정
data class Diary(
    @PrimaryKey(autoGenerate = true) // 2. 기본 키(Primary Key) 지정, 자동 증가 설정
    val diaryID: Int = 0,
    val dateID: String,

    val diaryTitle: String,
    val diaryContent: String
)