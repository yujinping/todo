package top.yjp.my.app.model.api

import kotlinx.serialization.Serializable
import top.yjp.my.app.model.Tag
import top.yjp.my.app.model.Task

@Serializable
data class TaskListResponse(val tasks: List<Task>)

@Serializable
data class TagListResponse(val tags: List<Tag>)

@Serializable
data class ApiResponse(val success: Boolean, val message: String = "")
