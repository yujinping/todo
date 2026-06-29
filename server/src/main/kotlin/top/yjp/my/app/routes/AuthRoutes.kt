package top.yjp.my.app.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import top.yjp.my.app.db.Database
import top.yjp.my.app.db.UserRepository
import top.yjp.my.app.model.api.*
import java.security.MessageDigest
import java.util.*

fun Route.authRoutes() {
    val ds = Database.getDataSource()

    /** 简单的密码哈希 */
    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** 生成随机 token */
    fun generateToken(): String = UUID.randomUUID().toString().replace("-", "")

    // ── 登录 / 注册（登录即注册）──
    post("/api/auth/login") {
        val request: LoginRequest
        try {
            request = call.receive()
        } catch (e: Exception) {
            return@post call.respond(
                HttpStatusCode.BadRequest,
                LoginResponse(false, message = "请求格式错误: ${e.message}")
            )
        }

        if (request.username.isBlank() || request.password.isBlank()) {
            return@post call.respond(
                HttpStatusCode.BadRequest,
                LoginResponse(false, message = "用户名和密码不能为空")
            )
        }

        if (request.password.length < 4) {
            return@post call.respond(
                HttpStatusCode.BadRequest,
                LoginResponse(false, message = "密码长度不能少于4位")
            )
        }

        ds.connection.use { conn ->
            val repo = UserRepository(conn)
            val (user, existingHash) = repo.findByUsernameWithPassword(request.username)
            val token = generateToken()
            val passwordHash = hashPassword(request.password)

            if (user != null) {
                // 已有用户：验证密码
                if (existingHash != passwordHash) {
                    return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        LoginResponse(false, message = "密码错误")
                    )
                }
                repo.updateToken(user.id, token)
                return@post call.respond(
                    LoginResponse(true, token = token, user = user)
                )
            } else {
                // 新用户：自动注册
                val newUser = repo.createUser(request.username, passwordHash, token)
                return@post call.respond(
                    HttpStatusCode.Created,
                    LoginResponse(true, token = token, user = newUser, message = "注册成功")
                )
            }
        }
    }

    // ── 获取当前用户信息 ──
    get("/api/auth/me") {
        val authHeader = call.request.header("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return@get call.respond(
                HttpStatusCode.Unauthorized,
                UserResponse(false, message = "未登录")
            )
        }
        val token = authHeader.removePrefix("Bearer ")

        ds.connection.use { conn ->
            val repo = UserRepository(conn)
            val user = repo.findByToken(token)
            if (user != null) {
                call.respond(UserResponse(true, user = user))
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    UserResponse(false, message = "登录已过期，请重新登录")
                )
            }
        }
    }

    // ── 登出 ──
    post("/api/auth/logout") {
        val authHeader = call.request.header("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.removePrefix("Bearer ")
            ds.connection.use { conn ->
                UserRepository(conn).clearToken(token)
            }
        }
        call.respond(ApiResponse(true, "已登出"))
    }

    // ── OAuth 登录（预留）──
    post("/api/auth/oauth") {
        val request: OAuthLoginRequest
        try {
            request = call.receive()
        } catch (e: Exception) {
            return@post call.respond(
                HttpStatusCode.BadRequest,
                LoginResponse(false, message = "请求格式错误")
            )
        }
        // TODO: 实现微信/钉钉 OAuth
        call.respond(
            HttpStatusCode.NotImplemented,
            LoginResponse(false, message = "OAuth 登录暂未实现，请使用用户名密码登录")
        )
    }
}
