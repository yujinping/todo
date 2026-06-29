package top.yjp.my.app

import kotlin.test.Test
import kotlin.test.assertEquals
import top.yjp.my.app.model.Priority
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.TaskStatus

class SharedCommonTest {

    @Test
    fun testTaskModel() {
        val task = Task(
            id = "1",
            title = "测试任务",
            description = "描述",
            status = TaskStatus.TODO,
            priority = Priority.HIGH
        )
        assertEquals("1", task.id)
        assertEquals("测试任务", task.title)
        assertEquals(TaskStatus.TODO, task.status)
        assertEquals(Priority.HIGH, task.priority)
    }
}
