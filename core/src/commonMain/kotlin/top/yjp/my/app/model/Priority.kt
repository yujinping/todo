package top.yjp.my.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority(val label: String, val weight: Int) {
    LOW("低", 0),
    MEDIUM("中", 1),
    HIGH("高", 2)
}
