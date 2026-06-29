package top.yjp.my.app.db

import top.yjp.my.app.model.User
import java.sql.Connection
import java.util.*

class UserRepository(private val connection: Connection) {

    fun createTable() {
        connection.createStatement().use { stmt ->
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    password_hash VARCHAR(200) NOT NULL DEFAULT '',
                    nickname VARCHAR(100) DEFAULT '',
                    avatar VARCHAR(500) DEFAULT '',
                    token VARCHAR(100) DEFAULT '',
                    provider VARCHAR(20) DEFAULT 'local',
                    provider_id VARCHAR(200) DEFAULT '',
                    created_at VARCHAR(50) NOT NULL
                )
            """.trimIndent())
        }
    }

    fun findByToken(token: String): User? {
        val sql = "SELECT id, username, nickname, avatar, provider, provider_id, created_at FROM users WHERE token = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, token)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    return User(
                        id = rs.getString("id"),
                        username = rs.getString("username"),
                        nickname = rs.getString("nickname") ?: "",
                        avatar = rs.getString("avatar") ?: "",
                        provider = rs.getString("provider") ?: "local",
                        providerId = rs.getString("provider_id") ?: "",
                        createdAt = rs.getString("created_at") ?: ""
                    )
                }
                return null
            }
        }
    }

    fun findByUsernameWithPassword(username: String): Pair<User?, String?> {
        val sql = "SELECT id, username, nickname, avatar, provider, provider_id, created_at, password_hash FROM users WHERE username = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, username)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    val user = User(
                        id = rs.getString("id"),
                        username = rs.getString("username"),
                        nickname = rs.getString("nickname") ?: "",
                        avatar = rs.getString("avatar") ?: "",
                        provider = rs.getString("provider") ?: "local",
                        providerId = rs.getString("provider_id") ?: "",
                        createdAt = rs.getString("created_at") ?: ""
                    )
                    return Pair(user, rs.getString("password_hash"))
                }
                return Pair(null, null)
            }
        }
    }

    fun createUser(username: String, passwordHash: String, token: String): User {
        val id = UUID.randomUUID().toString()
        val now = java.time.Instant.now().toString()
        val sql = "INSERT INTO users (id, username, password_hash, nickname, token, provider, created_at) VALUES (?, ?, ?, ?, ?, 'local', ?)"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, id)
            ps.setString(2, username)
            ps.setString(3, passwordHash)
            ps.setString(4, username) // nickname defaults to username
            ps.setString(5, token)
            ps.setString(6, now)
            ps.executeUpdate()
        }
        return User(
            id = id,
            username = username,
            nickname = username,
            avatar = "",
            provider = "local",
            createdAt = now
        )
    }

    fun updateToken(userId: String, token: String) {
        val sql = "UPDATE users SET token = ? WHERE id = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, token)
            ps.setString(2, userId)
            ps.executeUpdate()
        }
    }

    fun clearToken(token: String) {
        val sql = "UPDATE users SET token = '' WHERE token = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, token)
            ps.executeUpdate()
        }
    }

    fun updateProfile(userId: String, nickname: String, avatar: String): User? {
        val sql = "UPDATE users SET nickname = ?, avatar = ? WHERE id = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, nickname)
            ps.setString(2, avatar)
            ps.setString(3, userId)
            ps.executeUpdate()
        }
        // Fetch updated user
        val selectSql = "SELECT id, username, nickname, avatar, provider, provider_id, created_at FROM users WHERE id = ?"
        connection.prepareStatement(selectSql).use { ps ->
            ps.setString(1, userId)
            ps.executeQuery().use { rs ->
                if (rs.next()) {
                    return User(
                        id = rs.getString("id"),
                        username = rs.getString("username"),
                        nickname = rs.getString("nickname") ?: "",
                        avatar = rs.getString("avatar") ?: "",
                        provider = rs.getString("provider") ?: "local",
                        providerId = rs.getString("provider_id") ?: "",
                        createdAt = rs.getString("created_at") ?: ""
                    )
                }
            }
        }
        return null
    }
}
