package top.yjp.my.app.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val username: String,
    val nickname: String = "",
    val avatar: String = "",
    val provider: String = "local",  // "local", "wechat", "dingtalk"
    val providerId: String = "",      // 第三方平台的用户 ID
    val createdAt: String = ""
)
