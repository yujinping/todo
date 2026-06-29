package top.yjp.my.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.TaskStatus
import top.yjp.my.app.util.DateUtils
import top.yjp.my.app.util.LunarCalendar

@Composable
fun MonthView(
    selectedDate: String,
    tasks: List<Task>,
    getTasksForDate: (String) -> List<Task>,
    getHasTasks: (String) -> Boolean,
    getIncompleteCount: (String) -> Int,
    onDateSelected: (String) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (String) -> Unit,
    onEditTask: (Task) -> Unit = {},
    onAddClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ld = LocalDate.parse(selectedDate)
    val year = ld.year
    val month = ld.monthNumber
    val monthDates = remember(year, month) { DateUtils.getMonthDates(year, month) }
    val firstDay = LocalDate(year, month, 1)
    val firstDayOfWeek = firstDay.dayOfWeek.ordinal // 0=Monday
    val monthLabel = DateUtils.formatMonthYear(year, month)

    val selectedDateTasks = remember(selectedDate, tasks) { getTasksForDate(selectedDate) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    scrollState.dispatchRawDelta(dragAmount)
                }
            }
    ) {
        // Month header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousMonth) {
                        Text("◀")
                    }
                    Text(
                        monthLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(onClick = onNextMonth) {
                        Text("▶")
                    }
                }
                TextButton(
                    onClick = onTodayClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("回到今天")
                }
            }
        }

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            (0..6).forEach { dow ->
                Text(
                    DateUtils.dayOfWeekLabel(dow),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Calendar grid
        // We need to align the first day properly in the grid
        // The grid has 7 columns. We track total cells = firstDayOfWeek + daysInMonth
        val totalCells = firstDayOfWeek + monthDates.size
        val numRows = (totalCells + 6) / 7
        val allDays = mutableListOf<String?>()
        repeat(firstDayOfWeek) { allDays.add(null) }
        allDays.addAll(monthDates)

        for (row in 0 until numRows) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    if (index < allDays.size) {
                        val date = allDays[index]
                        if (date != null) {
                            val isSelected = date == selectedDate
                            val isToday = DateUtils.isDateToday(date)
                            val hasTasks = getHasTasks(date)
                            val incompleteCount = getIncompleteCount(date)
                            val dayLd = LocalDate.parse(date)

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                                            isToday -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .pointerInput(date) {
                                        detectTapGestures { onDateSelected(date) }
                                    }
                                    .padding(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Day number
                                Text(
                                    dayLd.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                // Lunar date / holiday
                                val lunarInfo = remember(date) {
                                    LunarCalendar.solarToLunar(dayLd.year, dayLd.monthNumber, dayLd.dayOfMonth)
                                }
                                val lunarText = if (lunarInfo.isHoliday) {
                                    lunarInfo.holiday ?: lunarInfo.dayName
                                } else if (lunarInfo.lunarDate.day == 1) {
                                    lunarInfo.monthName
                                } else {
                                    lunarInfo.dayName
                                }
                                Text(
                                    lunarText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = if (lunarInfo.isHoliday) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Task indicators
                                if (incompleteCount > 0) {
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Show dots for incomplete tasks (max 3)
                                        val dotsToShow = minOf(incompleteCount, 3)
                                        repeat(dotsToShow) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.error)
                                                    .offset(x = (it * -2).dp) // slight overlap
                                            )
                                            if (it < dotsToShow - 1) {
                                                Spacer(modifier = Modifier.width(2.dp))
                                            }
                                        }
                                        if (incompleteCount > 3) {
                                            Text(
                                                "+",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                } else if (hasTasks) {
                                    // All tasks done - green dot
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF69F0AE))
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(0.85f))
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(0.85f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Tasks for selected date
        HorizontalDivider()
        Text(
            DateUtils.formatDisplayDate(selectedDate),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (selectedDateTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "这天没有任务",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                selectedDateTasks.forEach { task ->
                    TaskItem(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task.id) },
                        onEdit = { onEditTask(task) }
                    )
                }
            }
        }

        // Add button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("＋ 添加任务", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
