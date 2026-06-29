package top.yjp.my.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String = "",
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: String = "",       // ISO format: YYYY-MM-DD
    val createdAt: String = "",
    val completedAt: String? = null,
    val tags: List<String> = emptyList()
)
