package com.myproject.routinary

import dagger.hilt.android.HiltAndroidApp
import android.app.Application

@HiltAndroidApp // 💡 Hilt의 최상위 컨테이너임을 명시
class RoutinaryApplication : Application() {
    // 이 클래스 내부에 코드를 추가할 필요는 없습니다.
}