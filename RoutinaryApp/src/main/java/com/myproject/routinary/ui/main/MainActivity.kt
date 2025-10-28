package com.myproject.routinary.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.myproject.routinary.ui.theme.RoutinerTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.compose.*
import com.myproject.routinary.data.database.entity.RoutinaryDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
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
fun MainScreen(
        dateViewModel: DateViewModel = hiltViewModel()
) {
//     💡 1. ViewModel의 StateFlow를 State로 변환하여 관찰
//     userList의 값이 변경되면 이 Composable이 자동으로 재구성(Recompose)됩니다.
     val dateList: List<RoutinaryDate> by dateViewModel.allDates.collectAsStateWithLifecycle()
    val isDateIDAdded by dateViewModel.isDateAdded.collectAsState()

    // 1. SnackbarHostState 생성 및 기억
    // 스낵바를 표시/숨김 상태를 제어하는 핵심 객체
    val snackbarHostState = remember { SnackbarHostState() }

    // 2. CoroutineScope 생성 및 기억
    // 비동기적으로 showSnackbar를 호출하기 위해 필요함
    val scope = rememberCoroutineScope()

    LaunchedEffect(isDateIDAdded) {
        isDateIDAdded?.let { isSuccess ->
            val message = if (isSuccess) {
                 "dateID 추가 성공"
            } else {
                 "dateID 추가 실패 (중복)"
            }

            // isAddedResult가 null이 아닐 때만 스낵바를 띄웁니다.
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = "확인"
                )
                // 필요하다면 다시 null로 초기화하여 다음 상호작용을 준비
                // viewModel._isDateAdded.value = null (ViewModel 내부에서 처리 권장
                dateViewModel.setIsDateAddedNull()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            // SnackbarHost에 HostState를 전달하여 스낵바를 화면 하단에 띄울 준비
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
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

                Calendar()

                Row (
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
//            Button(onClick = { dateViewModel.addNewDate(dateViewModel.createDateID()) })
//            { Text("DateID 추가") }
                    Button(onClick = { dateViewModel.addNewDate(dateViewModel.createDateID()) })
                    {
                        Text("dateID 추가")
                    }
                    Button(onClick = { dateViewModel.deleteAll() })
                    {
                        Text("모두 삭제")
                    }
                }

                Text(text = "Date 목록", style = MaterialTheme.typography.headlineLarge)
                dateList.forEach { date ->
                    Text(text = "dateID: ${date.dateID}, numbering = ${date.numbering}")
                }
            }
        }
    )
}

@Composable
fun Day(day: CalendarDay) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val fontSizeState by animateIntAsState(
        targetValue = if (isPressed) 18 else 16,
        label = "press_scale_animation"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f) // This is important for square sizing!
            .clickable (
                interactionSource = interactionSource,
                indication = null,
                onClick = { println("dd") }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = fontSizeState.sp
        )
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

@Composable
fun Calendar() {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
    val daysOfWeek = remember { daysOfWeek() } // Available from the library

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        outDateStyle = OutDateStyle.EndOfGrid,
        firstDayOfWeek = daysOfWeek.first()
    )

    Column(
        // modifier: UI 요소의 크기, 여백 등을 설정합니다.
        modifier = Modifier
            .padding(16.dp),
        // verticalArrangement: 수직 방향 정렬을 가운데로 맞춥니다.
        verticalArrangement = Arrangement.Top,
        // horizontalAlignment: 수평 방향 정렬을 가운데로 맞춥니다.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3. Text: 화면에 숫자를 표시하는 위젯입니다.
        Text(text = "캘린더", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(18.dp)) // 사이에 공간을 둡니다.
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        Box(
            modifier = Modifier
                .background(Color.LightGray)
        ) {
            HorizontalCalendar(
                state = state,
                dayContent = { Day(it) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    RoutinerTheme {
        Calendar()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    RoutinerTheme {
//        MainScreen()
//    }
//}