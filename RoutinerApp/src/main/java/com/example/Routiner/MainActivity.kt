package com.example.Routiner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.Routiner.ui.theme.RoutinerTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.compose.*
import java.time.YearMonth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // MyFirstAppTheme 안에 카운터 앱을 만듭니다.
            // git push and pull test from N to P
            // git push and pull test from P to N
            RoutinerTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    Column(
        // modifier: UI 요소의 크기, 여백 등을 설정합니다.
        modifier = Modifier
            .fillMaxSize() // 화면을 꽉 채웁니다.
            .padding(16.dp),
        // verticalArrangement: 수직 방향 정렬을 가운데로 맞춥니다.
        verticalArrangement = Arrangement.Top,
        // horizontalAlignment: 수평 방향 정렬을 가운데로 맞춥니다.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3. Text: 화면에 숫자를 표시하는 위젯입니다.
        Text(text = "캘린더", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(18.dp)) // 사이에 공간을 둡니다.

        HorizontalCalendar(
            state = state,
            dayContent = { Day(it) }
        )
    }


}
@Composable
fun Day(day: CalendarDay) {
    Box(
        modifier = Modifier
            .aspectRatio(1f), // This is important for square sizing!
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.date.dayOfMonth.toString())
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    RoutinerTheme {
        MainScreen()
    }
}