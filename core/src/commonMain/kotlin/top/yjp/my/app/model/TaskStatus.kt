package top.yjp.my.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus(val label: String) {
    TODO("待办"),
    DONE("已完成")
}
