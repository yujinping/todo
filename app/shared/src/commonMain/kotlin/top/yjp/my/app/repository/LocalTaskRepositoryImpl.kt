package top.yjp.my.app.repository

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.TaskStatus

class LocalTaskRepositoryImpl : ITaskRepository {
    private val tasks = mutableListOf<Task>()

    override suspend fun getAllTasks(): List<Task> {
        return tasks.toList()
    }

    override suspend fun getTasksByDate(date: String): List<Task> {
        return tasks.filter { it.dueDate == date }
    }

    override suspend fun getTasksByDateRange(startDate: String, endDate: String): List<Task> {
        return tasks.filter { it.dueDate in startDate..endDate }
    }

    override suspend fun getTaskById(id: String): Task? {
        return tasks.find { it.id == id }
    }

    override suspend fun createTask(task: Task): Task {
        val newId = if (task.id.isBlank()) {
            "${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}"
        } else task.id
        val newTask = task.copy(id = newId)
        tasks.add(newTask)
        return newTask
    }

    override suspend fun updateTask(task: Task): Task {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
        }
        return task
    }

    override suspend fun deleteTask(id: String) {
        tasks.removeAll { it.id == id }
    }

    override suspend fun toggleTaskStatus(id: String): Task? {
        val task = tasks.find { it.id == id } ?: return null
        val newStatus = if (task.status == TaskStatus.TODO) TaskStatus.DONE else TaskStatus.TODO
        val completedAt = if (newStatus == TaskStatus.DONE) {
            kotlinx.datetime.Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        } else null
        val updated = task.copy(status = newStatus, completedAt = completedAt)
        val index = tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            tasks[index] = updated
        }
        return updated
    }
}
