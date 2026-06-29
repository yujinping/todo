package top.yjp.my.app.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.api.TaskListResponse

class RemoteTaskRepositoryImpl(
    private val baseUrl: String = "http://localhost:8080",
    private val tokenProvider: (() -> String)? = null
) : ITaskRepository {

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
            // Add auth token if available
            tokenProvider?.invoke()?.let { token ->
                if (token.isNotEmpty()) {
                    header("Authorization", "Bearer $token")
                }
            }
        }
        expectSuccess = false
    }

    override suspend fun getAllTasks(): List<Task> {
        val response: TaskListResponse = client.get("$baseUrl/api/tasks").body()
        return response.tasks
    }

    override suspend fun getTasksByDate(date: String): List<Task> {
        val response: TaskListResponse = client.get("$baseUrl/api/tasks") {
            parameter("date", date)
        }.body()
        return response.tasks
    }

    override suspend fun getTasksByDateRange(startDate: String, endDate: String): List<Task> {
        val response: TaskListResponse = client.get("$baseUrl/api/tasks") {
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()
        return response.tasks
    }

    override suspend fun getTaskById(id: String): Task? {
        return try {
            val response = client.get("$baseUrl/api/task/$id")
            if (response.status.isSuccess()) {
                response.body<Task>()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createTask(task: Task): Task {
        val response = client.post("$baseUrl/api/task") {
            setBody(task)
        }
        return response.body()
    }

    override suspend fun updateTask(task: Task): Task {
        val response = client.put("$baseUrl/api/task/${task.id}") {
            setBody(task)
        }
        return response.body()
    }

    override suspend fun deleteTask(id: String) {
        client.delete("$baseUrl/api/task/$id")
    }

    override suspend fun toggleTaskStatus(id: String): Task? {
        val response = client.put("$baseUrl/api/task/$id/toggle")
        return if (response.status.isSuccess()) {
            response.body<Task>()
        } else null
    }
}
