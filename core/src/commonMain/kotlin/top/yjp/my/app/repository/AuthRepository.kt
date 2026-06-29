package top.yjp.my.app.repository

import top.yjp.my.app.model.User

interface IAuthRepository {
    /** 登录（如用户不存在则自动注册），返回 token */
    suspend fun login(username: String, password: String): Result<Pair<String, User>>

    /** OAuth 登录（预留） */
    suspend fun oauthLogin(provider: String, code: String): Result<Pair<String, User>>

    /** 获取当前登录用户信息 */
    suspend fun getMe(token: String): Result<User>

    /** 登出 */
    suspend fun logout(token: String)

    /** 更新用户信息 */
    suspend fun updateProfile(token: String, nickname: String, avatar: String): Result<User>
}
