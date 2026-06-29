package top.yjp.my.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun WeekView(
    selectedDate: String,
    tasks: List<Task>,
    getTasksForDate: (String) -> List<Task>,
    getIncompleteCount: (String) -> Int,
    onDateSelected: (String) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (String) -> Unit,
    onEditTask: (Task) -> Unit = {},
    onAddClick: () -> Unit,
    onExportClick: () -> Unit = {},
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDates = remember(selectedDate) { DateUtils.getWeekDates(selectedDate) }
    val selectedDateTasks = remember(selectedDate, tasks) { getTasksForDate(selectedDate) }

    Column(modifier = modifier.fillMaxSize()) {
        // Week header with navigation
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
                    IconButton(onClick = onPreviousWeek) {
                        Text("◀")
                    }
                    val weekStart = LocalDate.parse(weekDates.first())
                    val weekEnd = LocalDate.parse(weekDates.last())
                    Text(
                        "${weekStart.monthNumber}月${weekStart.dayOfMonth}日 — ${weekEnd.monthNumber}月${weekEnd.dayOfMonth}日",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(onClick = onNextWeek) {
                        Text("▶")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!DateUtils.isDateToday(selectedDate) || weekDates.first() != DateUtils.today()) {
                        TextButton(onClick = onTodayClick) { Text("回到今天") }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = onExportClick) { Text("📤 导出本周") }
                }
            }
        }

        // 7-day strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            weekDates.forEachIndexed { index, date ->
                val isSelected = date == selectedDate
                val isToday = DateUtils.isDateToday(date)
                val incompleteCount = getIncompleteCount(date)
                val ld = LocalDate.parse(date)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else if (isToday) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                        .pointerInput(date) {
                            detectTapGestures { onDateSelected(date) }
                        }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        DateUtils.dayOfWeekLabel(index),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else if (isToday) MaterialTheme.colorScheme.tertiary
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            ld.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected || isToday)
                                MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Lunar date / holiday
                    val lunarInfo = remember(date) {
                        LunarCalendar.solarToLunar(ld.year, ld.monthNumber, ld.dayOfMonth)
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

                    if (incompleteCount > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${incompleteCount}项",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }
        }

        HorizontalDivider()

        // Tasks for selected date
        val displayDate = DateUtils.formatDisplayDate(selectedDate)
        Text(
            displayDate,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (selectedDateTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "这天没有任务",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(selectedDateTasks, key = { it.id }) { task ->
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
