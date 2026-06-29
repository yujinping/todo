package top.yjp.my.app

import kotlin.test.Test
import kotlin.test.assertEquals
import top.yjp.my.app.model.Priority
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.TaskStatus
import top.yjp.my.app.util.DateUtils

class ModelTest {

    @Test
    fun testTaskCreation() {
        val task = Task(
            id = "1",
            title = "Buy groceries",
            status = TaskStatus.TODO,
            priority = Priority.HIGH
        )
        assertEquals(TaskStatus.TODO, task.status)
        assertEquals(Priority.HIGH, task.priority)
    }

    @Test
    fun testToday() {
        val today = DateUtils.today()
        assertEquals(true, today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun testDaysInMonth() {
        assertEquals(31, DateUtils.daysInMonth(2024, 1))
        assertEquals(29, DateUtils.daysInMonth(2024, 2))
        assertEquals(30, DateUtils.daysInMonth(2024, 4))
    }
}
