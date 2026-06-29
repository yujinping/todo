package top.yjp.my.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String = "",
    val name: String,
    val icon: String = "🏷️"
)
