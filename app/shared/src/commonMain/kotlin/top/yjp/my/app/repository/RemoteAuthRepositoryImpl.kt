package top.yjp.my.app.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import top.yjp.my.app.model.User
import top.yjp.my.app.model.api.*

class RemoteAuthRepositoryImpl(
    private val baseUrl: String = "http://localhost:8080"
) : IAuthRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                isLenient = true
            })
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        expectSuccess = false
    }

    override suspend fun login(username: String, password: String): Result<Pair<String, User>> {
        return try {
            val response = client.post("$baseUrl/api/auth/login") {
                setBody(LoginRequest(username, password))
            }
            if (response.status.isSuccess() || response.status == HttpStatusCode.Created) {
                val loginResp: LoginResponse = response.body()
                if (loginResp.success && loginResp.user != null) {
                    Result.success(Pair(loginResp.token, loginResp.user!!))
                } else {
                    Result.failure(Exception(loginResp.message))
                }
            } else if (response.status == HttpStatusCode.Unauthorized) {
                val errorResp: LoginResponse = response.body()
                Result.failure(Exception(errorResp.message))
            } else {
                Result.failure(Exception("登录失败: HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("连接服务器失败: ${e.message}"))
        }
    }

    override suspend fun oauthLogin(provider: String, code: String): Result<Pair<String, User>> {
        return Result.failure(Exception("OAuth 登录暂未实现"))
    }

    override suspend fun getMe(token: String): Result<User> {
        return try {
            val response = client.get("$baseUrl/api/auth/me") {
                header("Authorization", "Bearer $token")
            }
            if (response.status.isSuccess()) {
                val userResp: UserResponse = response.body()
                if (userResp.success && userResp.user != null) {
                    Result.success(userResp.user!!)
                } else {
                    Result.failure(Exception(userResp.message))
                }
            } else {
                Result.failure(Exception("未登录或登录已过期"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("连接服务器失败: ${e.message}"))
        }
    }

    override suspend fun logout(token: String) {
        try {
            client.post("$baseUrl/api/auth/logout") {
                header("Authorization", "Bearer $token")
            }
        } catch (_: Exception) { }
    }

    override suspend fun updateProfile(token: String, nickname: String, avatar: String): Result<User> {
        return Result.failure(Exception("暂未实现"))
    }
}
