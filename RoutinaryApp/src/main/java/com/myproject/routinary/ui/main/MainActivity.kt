package com.myproject.routinary.ui.main

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.DatePickerDefaults.dateFormatter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.compose.*
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.myproject.routinary.data.database.entity.Diary
import com.myproject.routinary.data.database.entity.RoutinaryDate
import com.myproject.routinary.data.database.entity.Schedule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
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
            // git push and pull test from N to P
            // git push and pull test from P to N
            RoutinerTheme {
                AppNavigation()
            }
        }
    }
}

object Screen {
    const val MAIN = "main_screen"
    const val SCHEDULE = "scheduleWrite_screen/{scheduleID}"
}

@Composable
fun AppNavigation() {
    // NavController 생성 및 기억
    val navController = rememberNavController()
    val dateViewModel: DateViewModel = hiltViewModel()
    val diaryViewModel: DiaryViewModel = hiltViewModel()
    val scheduleViewModel: ScheduleViewModel = hiltViewModel()

    // 화면(Destination)들을 호스팅하는 영역 정의
    NavHost(
        navController = navController,
        startDestination = Screen.MAIN // 앱 시작 시 첫 화면
    ) {
        composable(Screen.MAIN) {
            MainScreen(navController = navController, dateViewModel, diaryViewModel, scheduleViewModel)
        }

        composable(Screen.SCHEDULE,
            arguments = listOf(
                navArgument("scheduleID") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val scheduleID : Int? = backStackEntry.arguments?.getInt("scheduleID")?:-1
            ScheduleWriteScreen(navController = navController,dateViewModel=dateViewModel , scheduleViewModel=scheduleViewModel, scheduleID = scheduleID)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
        navController: NavController,
        dateViewModel: DateViewModel,
        diaryViewModel: DiaryViewModel,
        scheduleViewModel: ScheduleViewModel,
) {
//     💡 1. ViewModel의 StateFlow를 State로 변환하여 관찰
//     userList의 값이 변경되면 이 Composable이 자동으로 재구성(Recompose)됩니다.
    val dateList: List<RoutinaryDate> by dateViewModel.allDates.collectAsStateWithLifecycle()
    val diaryList: List<Diary> by diaryViewModel.allDiaries.collectAsStateWithLifecycle()
    val scheduleList: List<Schedule> by scheduleViewModel.allSchedules.collectAsStateWithLifecycle()
    val isDateIDAdded by dateViewModel.isDateAdded.collectAsState()
    val selectedDate by dateViewModel.selectedDate.collectAsStateWithLifecycle()

    val localDateMap: Map<LocalDate, Boolean> = dateListToLocalDateMap(dateList)
    val diaryMap: Map<String, Diary> = diaryList.associateBy { it.dateID }
    val scheduleMap: Map<String, List<Schedule>> = scheduleList.groupBy { it.dateID }

    // 1. 다이얼로그(글쓰기 화면)의 표시 여부를 관리하는 상태
    var showWritingScreen by remember { mutableStateOf(false) }
    var writeMenuExpanded by remember { mutableStateOf(false) }
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

                Calendar(dateViewModel, selectedDate, localDateMap, diaryMap, scheduleMap)

                Row {
                    Button(onClick = { showWritingScreen = true }) {
                        Text("오늘의 일기쓰기")
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                        // 2. 메뉴를 여는 버튼
                        Button(onClick = { writeMenuExpanded = true }) {
                            Text("작성")
                        }

                        // 3. 드롭다운 메뉴
                        DropdownMenu(
                            expanded = writeMenuExpanded, // 확장 상태 전달
                            onDismissRequest = { writeMenuExpanded = false } // 메뉴 밖을 누르면 닫기
                        ) {

                            DropdownMenuItem(
                                text = { Text("현재 날짜 일기 작성") },
                                onClick = {
                                    // 항목 1 선택 시 실행할 로직
                                    writeMenuExpanded = false // 선택 후 메뉴 닫기
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("현재 날짜 일정 작성") },
                                onClick = {
                                    // 항목 1 선택 시 실행할 로직
                                    navController.navigate(toScheduleWriteScreen(-1) )
                                    writeMenuExpanded = false // 선택 후 메뉴 닫기
                                }
                            )
                            // ... 필요한 만큼 항목 추가
                        }
                    }
                }
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
            // modifier = Modifier.fillMaxHeight(0.5f)
        ) {
            // 시트의 내용물 컴포저블을 호출합니다.
            WritingSheetContent(
                diaryMap,
                dateViewModel,
                // 취소 버튼이나 외부 클릭 시 시트 닫기
                onDismiss = { showWritingScreen = false },
                // '저장' 버튼을 눌렀을 때 호출될 함수 (저장된 텍스트를 업데이트)
                onSave = { newText, newTtile ->
                    dateViewModel.addNewDate(dateViewModel.createDateID())
                    diaryViewModel.addNewDiary(dateViewModel.createDateID(), newTtile, newText)
                    showWritingScreen = false // 저장 후 시트 닫기
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleWriteScreen(
    navController: NavController,
    dateViewModel: DateViewModel,
    scheduleViewModel: ScheduleViewModel,
    scheduleID: Int?
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedDate = remember { dateViewModel.selectedDate.value }
    val dtf = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
    val timeDtf = remember { DateTimeFormatter.ofPattern("a h:mm", Locale.getDefault()) }
    val timesaveDtf = remember { DateTimeFormatter.ofPattern("hh:mm", Locale.getDefault()) }
    val isDateIDAdded by dateViewModel.isDateAdded.collectAsState()

    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(0,0)) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var alarmFlag by remember { mutableStateOf(false) }

    val writeOrModify = if (scheduleID == -1) true else false
    // true : write, false : Modify

    val context = LocalContext.current
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

    // ⏰ 시간 선택 다이얼로그 표시 로직
    if (showTimePicker) {
        // 💡 Android TimePickerDialog 생성
        // onTimeSet: 사용자가 '확인'을 눌렀을 때 호출됨
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                // 4. 시간이 설정되면 상태 업데이트
                selectedTime = LocalTime.of(hour, minute)
                showTimePicker = false // 다이얼로그 숨김
            },
            selectedTime.hour, // 초기 시(Hour) 값
            selectedTime.minute, // 초기 분(Minute) 값
            false // 24시간제 사용 여부 (true: 24h, false: 12h)
        ).show()
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp, // 여기에 원하는 그림자 깊이를 설정합니다.
                // Surface의 색상을 TopAppBar의 기본 색상(surface)과 일치시킵니다.
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(title = { Text("일정 작성") },
                    actions = { Box(
                        modifier = Modifier.aspectRatio(1f)
                            .clickable( onClick = {
                                dateViewModel.addNewDate(dateViewModel.createDateID(selectedDate!!))
                                scheduleViewModel.addNewSchedule(dateViewModel.createDateID(selectedDate), title, content,
                                    alarmFlag,
                                    selectedTime.format(timesaveDtf))
                                    navController.navigate(Screen.MAIN) {
                                        popUpTo("scheduleWrite_screen") { // 메인화면으로 돌아갔는데 다시 뒤로가기 가능. 해결 필요.
                                            inclusive = true
                                        }
                                    }
                                                  },
                                enabled = title.isNotBlank()),
                        contentAlignment = Alignment.Center
                    ) {
                            Text("저장")
                    }
                    }
                )
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
                Row {
                    Box(Modifier.fillMaxWidth(0.25f)
                        .height(80.dp)
                        .clickable(onClick = {}),
                        contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.5f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                fontSize = 16.sp,
                                text = "날짜")
                        }
                    }
                    Box(Modifier.fillMaxWidth()
                        .height(80.dp)
                        .clickable(onClick = {}),
                        contentAlignment = Alignment.Center) {
                        Text(text = selectedDate!!.format(dtf))
                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth(0.9f), DividerDefaults.Thickness, DividerDefaults.color)
                Row {
                    Box(Modifier.fillMaxWidth(0.25f)
                        .height(80.dp),
                        contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.5f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                fontSize = 16.sp,
                                text = "시간")
                        }
                    }
                    Box(Modifier.fillMaxWidth()
                        .height(80.dp)
                        .clickable(onClick = {showTimePicker = true}),
                        contentAlignment = Alignment.Center) {
                        Text(text = "${selectedTime.format(timeDtf)}")
                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth(0.9f), DividerDefaults.Thickness, DividerDefaults.color)
                Row {
                    Box(Modifier.fillMaxWidth(0.25f)
                        .height(80.dp),
                        contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.5f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                fontSize = 16.sp,
                                text = "일정명")
                        }
                    }
                    Box(Modifier.fillMaxWidth()
                        .height(80.dp),
                        contentAlignment = Alignment.Center) {
                        TextField(
                            value = title, // 2. 현재 상태 값을 TextField에 표시
                            onValueChange = { newValue ->
                                // 3. 사용자가 입력할 때마다 상태 값을 새로운 값으로 업데이트
                                title = newValue
                            },
                            singleLine = true
                        )
                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth(0.9f), DividerDefaults.Thickness, DividerDefaults.color)
                Row {
                    Box(Modifier.fillMaxWidth(0.25f)
                        .height(160.dp),
                        contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.5f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                fontSize = 16.sp,
                                text = "메모")
                        }
                    }
                    Box(Modifier.fillMaxWidth()
                        .height(160.dp),
                        contentAlignment = Alignment.Center) {
                        TextField(
                            modifier = Modifier.fillMaxHeight(0.9f),
                            value = content, // 2. 현재 상태 값을 TextField에 표시
                            onValueChange = { newValue ->
                                // 3. 사용자가 입력할 때마다 상태 값을 새로운 값으로 업데이트
                                content = newValue
                            }
                        )
                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth(0.9f), DividerDefaults.Thickness, DividerDefaults.color)
                Row {
                    Box(Modifier.fillMaxWidth(0.25f)
                        .height(80.dp),
                        contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.5f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                fontSize = 16.sp,
                                text = "알림")
                        }
                    }
                    Box(Modifier.fillMaxWidth()
                        .height(80.dp),
                        contentAlignment = Alignment.Center) {
                        Checkbox(
                            checked = alarmFlag, // 2. 현재 상태 값(true/false)을 반영
                            onCheckedChange = { newCheckedState ->
                                // 3. 사용자가 클릭할 때마다 상태 값을 토글 (새로운 값으로 업데이트)
                                alarmFlag = newCheckedState
                            }
                        )
                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth(0.9f), DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
    )
}

@Composable
fun Day(day: CalendarDay, isSelected: Boolean, hasDate: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = if (hasDate) Color.Green else Color.Transparent)
            .border(
                shape = CircleShape,
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color.Magenta else Color.Transparent)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate) Color.Black else Color.Gray)

    }
}

@Composable
fun weekDay(day: WeekDay, isSelected: Boolean, onClick: (WeekDay) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier = Modifier
            .width(screenWidth / 8)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
            shape = RoundedCornerShape(8.dp),
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) Color.Magenta else Color.Transparent)
            .clickable { onClick(day) }
            .wrapContentHeight()
        ,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
//            Text(
//                text = day.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
//                fontSize = 10.sp,
//                fontWeight = FontWeight.Normal,
//            )
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
            )
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(dateViewModel: DateViewModel, selectedDate : LocalDate?, localDateMap : Map<LocalDate, Boolean>, diaryMap: Map<String, Diary>, scheduleMap: Map<String, List<Schedule>>) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
    val daysOfWeek = remember { daysOfWeek() } // Available from the library
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentMonth.minusMonths(100).atStartOfMonth() } // Adjust as needed
    val endDate = remember { currentMonth.plusMonths(100).atEndOfMonth() } // Adjust as needed
    var showDiaryListScreen by remember { mutableStateOf(false) }
    val dtf = DateTimeFormatter.ofPattern("yyyyMMdd")
    val titleDtf = DateTimeFormatter.ofPattern("yyyy년 MM월")
    // val selectedDateScheduleList: List<Schedule> by remember {mutableStateOf(scheduleMap[dateViewModel.createDateID(dateViewModel.selectedDate.value!!)]?:listOf())}
    val selectedDateScheduleList: List<Schedule> =scheduleMap[dateViewModel.createDateID(dateViewModel.selectedDate.value!!)]?:listOf()

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        outDateStyle = OutDateStyle.EndOfGrid,
        firstDayOfWeek = daysOfWeek.first()
    )

    val weekState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstVisibleWeekDate = currentDate,
        firstDayOfWeek = firstDayOfWeek,
    )

    val visibleMonth = rememberFirstMostVisibleMonth(state, viewportPercent = 90f)
    val coroutineScope = rememberCoroutineScope()

    Column(
        // modifier: UI 요소의 크기, 여백 등을 설정합니다.
        modifier = Modifier
            .padding(12.dp),
        // verticalArrangement: 수직 방향 정렬을 가운데로 맞춥니다.
        verticalArrangement = Arrangement.Top,
        // horizontalAlignment: 수평 방향 정렬을 가운데로 맞춥니다.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3. Text: 화면에 숫자를 표시하는 위젯입니다.
        Text(text = visibleMonth.yearMonth.format(titleDtf), fontSize = 22.sp)

        Spacer(modifier = Modifier.height(12.dp)) // 사이에 공간을 둡니다.
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray)
        ) {
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    Day(day, isSelected = selectedDate == day.date, hasDate = localDateMap[day.date]?:false) { day ->
                        dateViewModel.setSelectedDate(day.date)
                        coroutineScope.launch {
                            weekState.animateScrollToDay(WeekDay(day.date.minusDays(3), WeekDayPosition.RangeDate))
                        }
                    }
                }
            )
        }
        WeekCalendar(
            state = weekState,
            dayContent = { day ->
                weekDay (day, isSelected = selectedDate == day.date) { day ->
                    dateViewModel.setSelectedDate(day.date)
                    coroutineScope.launch {
                        weekState.animateScrollToDay(WeekDay(day.date.minusDays(3), WeekDayPosition.RangeDate))
                    }
                    coroutineScope.launch {
                        state.animateScrollToMonth(day.date.yearMonth)
                    }
                } },
            calendarScrollPaged = false,
            userScrollEnabled = false
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                HorizontalDivider(Modifier.fillMaxWidth(0.9f), DividerDefaults.Thickness, DividerDefaults.color)
                Row {
                    Box(Modifier.fillMaxWidth(0.25f)
                        .height(80.dp)
                        .clickable(onClick = {}),
                        contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.5f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(color = Color.Green)
                                .border(
                                    shape = CircleShape,
                                    width = 2.dp,
                                    color = Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "일기")
                        }
                    }
                    Box(Modifier.fillMaxWidth()
                        .height(80.dp)
                        .clickable(onClick = {showDiaryListScreen = true}),
                        contentAlignment = Alignment.Center) {
                        Text(text = diaryMap[selectedDate?.format(dtf)?:""]?.diaryTitle?:"아직 일기가 없습니다.",)
                    }
                }
            }

            items(
                selectedDateScheduleList
            ) { item ->
                HorizontalDivider(Modifier.fillMaxWidth(0.9f), DividerDefaults.Thickness, DividerDefaults.color)
                Row {
                    Box(Modifier.fillMaxWidth(0.25f)
                        .height(80.dp)
                        .clickable(onClick = {}),
                        contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.5f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(color = Color.Yellow)
                                .border(
                                    shape = CircleShape,
                                    width = 2.dp,
                                    color = Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "일정")
                        }
                    }
                    Box(Modifier.fillMaxWidth()
                        .height(80.dp)
                        .clickable(onClick = {showDiaryListScreen = true}),
                        contentAlignment = Alignment.Center) {
                        Text(text = item.scheduleTtile)
                    }
                }
            }
        }
    }

    LaunchedEffect(true) {
            weekState.animateScrollToDay(WeekDay(selectedDate!!.minusDays(3), WeekDayPosition.RangeDate))
    }

    if (showDiaryListScreen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // ModalBottomSheet는 기본적으로 화면 하단에 붙고 좌우를 가득 채웁니다.
        ModalBottomSheet(
            onDismissRequest = { showDiaryListScreen = false },
            sheetState = sheetState,
            // ModalBottomSheet의 높이를 화면의 50%로 설정
            // modifier = Modifier.fillMaxHeight(0.5f)
        ) {
            // 시트의 내용물 컴포저블을 호출합니다.
            DiaryView(diaryMap, selectedDate?.format(dtf)?:"")
        }
    }
}

@Composable
fun WritingSheetContent(
    diaryMap: Map<String, Diary>,
    dateViewModel: DateViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    // 텍스트 필드에 입력된 값을 관리하는 상태
    var textState by remember { mutableStateOf(diaryMap[dateViewModel.createDateID()]?.diaryContent ?: "") }
    var titleTextState by remember { mutableStateOf(diaryMap[dateViewModel.createDateID()]?.diaryTitle ?: "") }

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

        Text(
            text = "일기 작성",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = titleTextState,
            onValueChange = { titleTextState = it },
            label = { Text("제목") },
            // 고정 높이를 유지합니다.
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        // 텍스트 입력 공간
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("내용을 입력하세요") },
            // 고정 높이를 유지합니다.
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
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
                    onSave(textState, titleTextState)
                },
                // 입력된 내용이 있을 때만 버튼 활성화
                enabled = textState.isNotBlank() and titleTextState.isNotBlank()
            ) {
                Text("저장")
            }
        }
    }
}

@Composable
fun DiaryView(diaryMap: Map<String, Diary>, dateID: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(400.dp)
    ) {
        // 1. 제목 (Title)
        Text(
            text = diaryMap[dateID]?.diaryTitle?: "아직 일기가 없습니다.",
            style = MaterialTheme.typography.headlineMedium, // 더 크고 굵은 제목 스타일
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp) // 내용과의 간격
        )

        // 2. 구분선 (Optional: 제목과 내용을 시각적으로 분리)
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

        // 3. 내용 (Content)
        Text(
            text = diaryMap[dateID]?.diaryContent?: "",
            style = MaterialTheme.typography.bodyLarge, // 일반적인 본문 스타일
            modifier = Modifier.padding(top = 12.dp)
                .verticalScroll(rememberScrollState())
        )
    }
}
fun dateListToLocalDateMap(dateList: List<RoutinaryDate>): Map<LocalDate, Boolean> {
    val localDateMap: MutableMap<LocalDate, Boolean> = mutableMapOf()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    dateList.forEach { date ->
        localDateMap[LocalDate.parse(date.dateID, formatter)] = true
    }

    return localDateMap.toMap()
}

@Composable
fun rememberFirstMostVisibleMonth(
    state: CalendarState,
    viewportPercent: Float = 50f,
): CalendarMonth {
    val visibleMonth = remember(state) { mutableStateOf(state.firstVisibleMonth) }
    LaunchedEffect(state) {
        snapshotFlow { state.layoutInfo.firstMostVisibleMonth(viewportPercent) }
            .filterNotNull()
            .collect { month -> visibleMonth.value = month }
    }
    return visibleMonth.value
}

private fun CalendarLayoutInfo.firstMostVisibleMonth(viewportPercent: Float = 50f): CalendarMonth? {
    return if (visibleMonthsInfo.isEmpty()) {
        null
    } else {
        val viewportSize = (viewportEndOffset + viewportStartOffset) * viewportPercent / 100f
        visibleMonthsInfo.firstOrNull { itemInfo ->
            if (itemInfo.offset < 0) {
                itemInfo.offset + itemInfo.size >= viewportSize
            } else {
                itemInfo.size - itemInfo.offset >= viewportSize
            }
        }?.month
    }
}

fun toScheduleWriteScreen(scheduleID: Int): String {
    return "scheduleWrite_screen/$scheduleID"
}

//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    RoutinerTheme {
//        MainScreen()
//    }
//}