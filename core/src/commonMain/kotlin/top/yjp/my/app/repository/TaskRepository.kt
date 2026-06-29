package top.yjp.my.app.repository

import top.yjp.my.app.model.Task

interface ITaskRepository {
    suspend fun getAllTasks(): List<Task>
    suspend fun getTasksByDate(date: String): List<Task>
    suspend fun getTasksByDateRange(startDate: String, endDate: String): List<Task>
    suspend fun getTaskById(id: String): Task?
    suspend fun createTask(task: Task): Task
    suspend fun updateTask(task: Task): Task
    suspend fun deleteTask(id: String)
    suspend fun toggleTaskStatus(id: String): Task?
}
