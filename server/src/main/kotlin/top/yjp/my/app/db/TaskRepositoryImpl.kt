package top.yjp.my.app.db

import top.yjp.my.app.model.Priority
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.TaskStatus
import top.yjp.my.app.repository.ITaskRepository
import java.sql.ResultSet
import java.util.*

class TaskRepositoryImpl : ITaskRepository {

    private val ds = Database.getDataSource()

    private fun ResultSet.toTask(): Task = Task(
        id = getString("id"),
        title = getString("title"),
        description = getString("description") ?: "",
        status = TaskStatus.valueOf(getString("status")),
        priority = Priority.valueOf(getString("priority")),
        dueDate = getString("due_date") ?: "",
        createdAt = getString("created_at") ?: "",
        completedAt = getString("completed_at"),
        tags = (getString("tags") ?: "")
            .split(",")
            .filter { it.isNotBlank() }
    )

    override suspend fun getAllTasks(): List<Task> {
        val sql = "SELECT * FROM tasks ORDER BY priority DESC, created_at DESC"
        ds.connection.use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(sql)
                val tasks = mutableListOf<Task>()
                while (rs.next()) tasks.add(rs.toTask())
                return tasks
            }
        }
    }

    override suspend fun getTasksByDate(date: String): List<Task> {
        val sql = "SELECT * FROM tasks WHERE due_date = ? ORDER BY priority DESC, created_at DESC"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, date)
                val rs = stmt.executeQuery()
                val tasks = mutableListOf<Task>()
                while (rs.next()) tasks.add(rs.toTask())
                return tasks
            }
        }
    }

    override suspend fun getTasksByDateRange(startDate: String, endDate: String): List<Task> {
        val sql = "SELECT * FROM tasks WHERE due_date >= ? AND due_date <= ? ORDER BY due_date, priority DESC"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, startDate)
                stmt.setString(2, endDate)
                val rs = stmt.executeQuery()
                val tasks = mutableListOf<Task>()
                while (rs.next()) tasks.add(rs.toTask())
                return tasks
            }
        }
    }

    override suspend fun getTaskById(id: String): Task? {
        val sql = "SELECT * FROM tasks WHERE id = ?"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, id)
                val rs = stmt.executeQuery()
                return if (rs.next()) rs.toTask() else null
            }
        }
    }

    override suspend fun createTask(task: Task): Task {
        val id = if (task.id.isBlank()) UUID.randomUUID().toString() else task.id
        val sql = """
            INSERT INTO tasks (id, title, description, status, priority, due_date, created_at, completed_at, tags)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, id)
                stmt.setString(2, task.title)
                stmt.setString(3, task.description)
                stmt.setString(4, task.status.name)
                stmt.setString(5, task.priority.name)
                stmt.setString(6, task.dueDate)
                stmt.setString(7, task.createdAt)
                stmt.setString(8, task.completedAt)
                stmt.setString(9, task.tags.joinToString(","))
                stmt.executeUpdate()
            }
        }
        return task.copy(id = id)
    }

    override suspend fun updateTask(task: Task): Task {
        val sql = """
            UPDATE tasks SET title=?, description=?, status=?, priority=?, due_date=?, completed_at=?, tags=?
            WHERE id=?
        """.trimIndent()
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, task.title)
                stmt.setString(2, task.description)
                stmt.setString(3, task.status.name)
                stmt.setString(4, task.priority.name)
                stmt.setString(5, task.dueDate)
                stmt.setString(6, task.completedAt)
                stmt.setString(7, task.tags.joinToString(","))
                stmt.setString(8, task.id)
                stmt.executeUpdate()
            }
        }
        return task
    }

    override suspend fun deleteTask(id: String) {
        val sql = "DELETE FROM tasks WHERE id = ?"
        ds.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setString(1, id)
                stmt.executeUpdate()
            }
        }
    }

    override suspend fun toggleTaskStatus(id: String): Task? {
        val task = getTaskById(id) ?: return null
        val newStatus = if (task.status == TaskStatus.TODO) TaskStatus.DONE else TaskStatus.TODO
        val completedAt = if (newStatus == TaskStatus.DONE) {
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
        } else null
        val updated = task.copy(status = newStatus, completedAt = completedAt)
        updateTask(updated)
        return updated
    }
}
