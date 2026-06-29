package top.yjp.my.app.model.api

import kotlinx.serialization.Serializable
import top.yjp.my.app.model.User

/** 登录/注册请求 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

/** 第三方 OAuth 登录请求（预留） */
@Serializable
data class OAuthLoginRequest(
    val provider: String,   // "wechat" or "dingtalk"
    val code: String        // OAuth authorization code
)

/** 登录/注册响应 */
@Serializable
data class LoginResponse(
    val success: Boolean,
    val token: String = "",
    val user: User? = null,
    val message: String = ""
)

/** 获取当前用户响应 */
@Serializable
data class UserResponse(
    val success: Boolean,
    val user: User? = null,
    val message: String = ""
)
