package top.yjp.my.app.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import top.yjp.my.app.repository.ITaskRepository
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.api.ApiResponse
import top.yjp.my.app.model.api.TaskListResponse

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

fun Route.taskRoutes(repository: ITaskRepository) {
    // Get all tasks
    get("/api/tasks") {
        val date = call.request.queryParameters["date"]
        val startDate = call.request.queryParameters["startDate"]
        val endDate = call.request.queryParameters["endDate"]

        val tasks = when {
            date != null -> repository.getTasksByDate(date)
            startDate != null && endDate != null -> repository.getTasksByDateRange(startDate, endDate)
            else -> repository.getAllTasks()
        }
        call.respond(TaskListResponse(tasks))
    }

    // Get task by ID
    get("/api/task/{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse(false, "Missing task ID")
        )
        val task = repository.getTaskById(id)
        if (task != null) {
            call.respond(task)
        } else {
            call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Task not found"))
        }
    }

    // Create task
    post("/api/task") {
        val task: Task
        try {
            val bodyText = call.receiveText()
            println("[DEBUG] POST /api/task body: $bodyText")
            task = json.decodeFromString(bodyText)
        } catch (e: Exception) {
            println("[ERROR] POST /api/task parse failed: ${e.javaClass.name}: ${e.message}")
            e.printStackTrace()
            return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse(false, "Invalid request body: ${e.message}")
            )
        }
        val created = repository.createTask(task)
        call.respond(HttpStatusCode.Created, created)
    }

    // Update task
    put("/api/task/{id}") {
        val id = call.parameters["id"] ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse(false, "Missing task ID")
        )
        val existing = repository.getTaskById(id)
        if (existing == null) {
            return@put call.respond(
                HttpStatusCode.NotFound,
                ApiResponse(false, "Task not found")
            )
        }
        val task: Task
        try {
            task = json.decodeFromString(call.receiveText())
        } catch (e: Exception) {
            println("[ERROR] PUT /api/task parse failed: ${e.message}")
            return@put call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse(false, "Invalid request body: ${e.message}")
            )
        }
        val updated = repository.updateTask(task.copy(id = id))
        call.respond(updated)
    }

    // Delete task
    delete("/api/task/{id}") {
        val id = call.parameters["id"] ?: return@delete call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse(false, "Missing task ID")
        )
        repository.deleteTask(id)
        call.respond(ApiResponse(true, "Task deleted"))
    }

    // Toggle task status
    put("/api/task/{id}/toggle") {
        val id = call.parameters["id"] ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse(false, "Missing task ID")
        )
        val toggled = repository.toggleTaskStatus(id)
        if (toggled != null) {
            call.respond(toggled)
        } else {
            call.respond(HttpStatusCode.NotFound, ApiResponse(false, "Task not found"))
        }
    }
}
