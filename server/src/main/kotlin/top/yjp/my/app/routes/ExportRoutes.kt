package top.yjp.my.app.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import top.yjp.my.app.repository.ITaskRepository
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.TaskStatus

fun Route.exportRoutes(repository: ITaskRepository) {
    get("/api/export/markdown") {
        val startDate = call.request.queryParameters["startDate"] ?: ""
        val endDate = call.request.queryParameters["endDate"] ?: ""
        val statusFilter = call.request.queryParameters["status"] ?: "all"

        val tasks = when {
            startDate.isNotBlank() && endDate.isNotBlank() ->
                repository.getTasksByDateRange(startDate, endDate)
            else -> repository.getAllTasks()
        }.let { list ->
            when (statusFilter) {
                "done" -> list.filter { it.status == TaskStatus.DONE }
                "todo" -> list.filter { it.status == TaskStatus.TODO }
                else -> list
            }
        }.sortedBy { it.dueDate }

        val md = buildMarkdown(tasks, startDate, endDate, statusFilter)
        call.respondText(md, io.ktor.http.ContentType.Text.Plain)
    }
}

private fun buildMarkdown(tasks: List<Task>, startDate: String, endDate: String, statusFilter: String): String {
    val sb = StringBuilder()

    // Title
    sb.appendLine("# 📋 任务导出报告")
    sb.appendLine()

    // Period
    if (startDate.isNotBlank() && endDate.isNotBlank()) {
        sb.appendLine("**周期**: $startDate 至 $endDate")
    }
    when (statusFilter) {
        "done" -> sb.appendLine("**范围**: 已完成任务")
        "todo" -> sb.appendLine("**范围**: 待办任务")
        else -> sb.appendLine("**范围**: 全部任务")
    }
    sb.appendLine("**导出时间**: ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
    sb.appendLine("**任务总数**: ${tasks.size}")
    sb.appendLine()

    // Summary
    val doneCount = tasks.count { it.status == TaskStatus.DONE }
    val todoCount = tasks.count { it.status == TaskStatus.TODO }
    sb.appendLine("## 📊 统计")
    sb.appendLine()
    sb.appendLine("| 状态 | 数量 |")
    sb.appendLine("|------|------|")
    sb.appendLine("| ✅ 已完成 | $doneCount |")
    sb.appendLine("| 📝 待办 | $todoCount |")
    sb.appendLine()

    // Group by date
    val grouped = tasks.groupBy { it.dueDate }
    val sortedDates = grouped.keys.sorted()

    sb.appendLine("## 📅 按日期分组")
    sb.appendLine()

    sortedDates.forEach { date ->
        val dateTasks = grouped[date] ?: return@forEach
        sb.appendLine("### $date")
        sb.appendLine()
        sb.appendLine("| 状态 | 优先级 | 标题 | 描述 | 标签 |")
        sb.appendLine("|------|--------|------|------|------|")
        dateTasks.forEach { task ->
            val statusIcon = if (task.status == TaskStatus.DONE) "✅" else "📝"
            val priorityLabel = when (task.priority) {
                top.yjp.my.app.model.Priority.HIGH -> "🔴 高"
                top.yjp.my.app.model.Priority.MEDIUM -> "🟡 中"
                top.yjp.my.app.model.Priority.LOW -> "🟢 低"
            }
            val desc = task.description.take(50).replace("\n", " ").replace("|", "\\|")
            val tagsStr = task.tags.joinToString(", ")
            sb.appendLine("| $statusIcon | $priorityLabel | ${task.title} | $desc | $tagsStr |")
        }
        sb.appendLine()
    }

    // All tasks flat list
    sb.appendLine("---")
    sb.appendLine()
    sb.appendLine("## 📝 任务清单")
    sb.appendLine()
    tasks.forEachIndexed { index, task ->
        val statusIcon = if (task.status == TaskStatus.DONE) "✅" else "📝"
        val priorityLabel = when (task.priority) {
            top.yjp.my.app.model.Priority.HIGH -> "🔴 高"
            top.yjp.my.app.model.Priority.MEDIUM -> "🟡 中"
            top.yjp.my.app.model.Priority.LOW -> "🟢 低"
        }
        sb.appendLine("${index + 1}. $statusIcon [$priorityLabel] **${task.title}**")
        if (task.description.isNotBlank()) {
            sb.appendLine("   > ${task.description.replace("\n", "\n   > ")}")
        }
        if (task.tags.isNotEmpty()) {
            sb.appendLine("   🏷️ ${task.tags.joinToString(", ")}")
        }
        sb.appendLine("   📅 ${task.dueDate}")
        if (task.completedAt != null) {
            sb.appendLine("   ✅ 完成于 ${task.completedAt}")
        }
        sb.appendLine()
    }

    return sb.toString()
}
