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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.compose.*
import com.myproject.routinary.data.database.entity.Diary
import com.myproject.routinary.data.database.entity.RoutinaryDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
        dateViewModel: DateViewModel = hiltViewModel(),
        diaryViewModel: DiaryViewModel = hiltViewModel()
) {
//     💡 1. ViewModel의 StateFlow를 State로 변환하여 관찰
//     userList의 값이 변경되면 이 Composable이 자동으로 재구성(Recompose)됩니다.
    val dateList: List<RoutinaryDate> by dateViewModel.allDates.collectAsStateWithLifecycle()
    val diaryList: List<Diary> by diaryViewModel.allDiaries.collectAsStateWithLifecycle()
    val isDateIDAdded by dateViewModel.isDateAdded.collectAsState()

    val localDateMap: Map<LocalDate, Boolean> = dateListToLocalDateMap(dateList)

    // 1. 다이얼로그(글쓰기 화면)의 표시 여부를 관리하는 상태
    var showWritingScreen by remember { mutableStateOf(false) }

    // 2. 저장된 텍스트를 표시하기 위한 상태
    var savedText by remember { mutableStateOf("아직 저장된 내용이 없습니다.") }

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
                    actionLabel = "확인",
                    duration = SnackbarDuration.Short
                )
                // 필요하다면 다시 null로 초기화하여 다음 상호작용을 준비
                // viewModel._isDateAdded.value = null (ViewModel 내부에서 처리 권장
                dateViewModel.setIsDateAddedNull()
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp, // 여기에 원하는 그림자 깊이를 설정합니다.
                // Surface의 색상을 TopAppBar의 기본 색상(surface)과 일치시킵니다.
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(title = { Text("메인 화면") })
            }
        },
        snackbarHost = {
            // SnackbarHost에 HostState를 전달하여 스낵바를 화면 하단에 띄울 준비
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            Column(
                // modifier: UI 요소의 크기, 여백 등을 설정합니다.
                modifier = Modifier
                    .fillMaxSize() // 화면을 꽉 채웁니다.
                    .padding(paddingValues)
                    .padding(16.dp),
                // verticalArrangement: 수직 방향 정렬을 가운데로 맞춥니다.
                verticalArrangement = Arrangement.Top,
                // horizontalAlignment: 수평 방향 정렬을 가운데로 맞춥니다.
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Calendar(localDateMap)

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

                Button(onClick = { showWritingScreen = true }) {
                    Text("새 글쓰기 화면 열기")
                }

                Text(
                    text = savedText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )

    if (showWritingScreen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // ModalBottomSheet는 기본적으로 화면 하단에 붙고 좌우를 가득 채웁니다.
        ModalBottomSheet(
            onDismissRequest = { showWritingScreen = false },
            sheetState = sheetState,
            // ModalBottomSheet의 높이를 화면의 50%로 설정
            modifier = Modifier.fillMaxHeight(0.5f)
        ) {
            // 시트의 내용물 컴포저블을 호출합니다.
            WritingSheetContent(
                // 취소 버튼이나 외부 클릭 시 시트 닫기
                onDismiss = { showWritingScreen = false },
                // '저장' 버튼을 눌렀을 때 호출될 함수 (저장된 텍스트를 업데이트)
                onSave = { newText ->
                    savedText = newText
                    dateViewModel.addNewDate(dateViewModel.createDateID())
                    diaryViewModel.addNewDiary(dateViewModel.createDateID(), "test title", newText)
                    showWritingScreen = false // 저장 후 시트 닫기
                }
            )
        }
    }
}

@Composable
fun Day(day: CalendarDay, isSelected: Boolean, hasDate: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = if (isSelected || hasDate) Color.Green else Color.Transparent)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.date.dayOfMonth.toString())
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
fun Calendar(localDateMap : Map<LocalDate, Boolean>) {
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

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

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
        Text(text = currentMonth.toString(), style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(18.dp)) // 사이에 공간을 둡니다.
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        Box(
            modifier = Modifier
                .background(Color.LightGray)
        ) {
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    Day(day, isSelected = selectedDate == day.date, hasDate = localDateMap[day.date]?:false) { day ->
                        selectedDate = if (selectedDate == day.date) null else day.date
                    }
                }
            )
        }
    }
}

@Composable
fun WritingSheetContent(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    // 텍스트 필드에 입력된 값을 관리하는 상태
    var textState by remember { mutableStateOf("") }

    // ModalBottomSheet의 콘텐츠 영역입니다.
    Column(
        // ✨ fillMaxHeight() 제거: 이제 Column은 콘텐츠의 높이만큼만 차지합니다.
        modifier = Modifier
            .wrapContentHeight() // 콘텐츠 높이에 맞게 감싸기
            .fillMaxWidth() // 너비는 가득 채우기
            .padding(horizontal = 20.dp) // 좌우 내부 여백 설정
            .padding(top = 16.dp, bottom = 20.dp), // 상하 내부 여백 설정

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✨ Spacer(weight(1f)) 제거: 더 이상 콘텐츠를 밀어낼 필요가 없습니다.

        Text(
            text = "새로운 글 작성",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 텍스트 입력 공간
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("내용을 입력하세요") },
            // 고정 높이를 유지합니다.
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            // 취소 버튼
            TextButton(onClick = onDismiss) {
                Text("취소")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 저장 버튼 (텍스트 가져오기 및 저장)
            Button(
                onClick = {
                    // 입력된 텍스트(textState)를 가져와서 onSave 콜백 함수로 전달
                    onSave(textState)
                },
                // 입력된 내용이 있을 때만 버튼 활성화
                enabled = textState.isNotBlank()
            ) {
                Text("저장")
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun CalendarPreview() {
//    RoutinerTheme {
//        Calendar()
//    }
//}

fun dateListToLocalDateMap(dateList: List<RoutinaryDate>): Map<LocalDate, Boolean> {
    val localDateMap: MutableMap<LocalDate, Boolean> = mutableMapOf()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    dateList.forEach { date ->
        localDateMap[LocalDate.parse(date.dateID, formatter)] = true
    }

    return localDateMap.toMap()
}

//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    RoutinerTheme {
//        MainScreen()
//    }
//}