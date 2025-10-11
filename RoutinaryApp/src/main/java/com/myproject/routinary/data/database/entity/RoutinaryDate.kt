package com.myproject.routinary.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "RoutinaryDate") // 1. Entity임을 선언하고 테이블 이름 지정
data class RoutinaryDate(
    @PrimaryKey // 2. 기본 키(Primary Key) 지정, 자동 증가 설정
    val dateID: String,
    val numbering: Int = 0
)