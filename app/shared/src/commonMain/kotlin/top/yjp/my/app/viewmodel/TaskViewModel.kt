package top.yjp.my.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import top.yjp.my.app.model.Priority
import top.yjp.my.app.model.Tag
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.TaskStatus
import top.yjp.my.app.repository.ITagRepository
import top.yjp.my.app.repository.ITaskRepository
import top.yjp.my.app.util.DateUtils

enum class AppView { DAY, WEEK, MONTH, TAGS, PROFILE }

class TaskViewModel(
    private val taskRepository: ITaskRepository,
    private val tagRepository: ITagRepository
) : ViewModel() {

    private val _currentView = MutableStateFlow(AppView.DAY)
    val currentView: StateFlow<AppView> = _currentView.asStateFlow()

    private val _selectedDate = MutableStateFlow(DateUtils.today())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    val allTasks: StateFlow<List<Task>> = _allTasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    private val _exportContent = MutableStateFlow<String?>(null)
    val exportContent: StateFlow<String?> = _exportContent.asStateFlow()

    val filteredTasks: List<Task>
        get() {
            val query = _searchQuery.value.trim().lowercase()
            if (query.isEmpty()) return _tasks.value
            return _tasks.value.filter { task ->
                task.title.lowercase().contains(query) ||
                task.description.lowercase().contains(query) ||
                task.tags.any { it.lowercase().contains(query) }
            }
        }

    init {
        loadAllTasks()
        loadTasksForCurrentView()
        loadTags()
    }

    fun setView(view: AppView) {
        _currentView.value = view
        if (view == AppView.TAGS) {
            loadTags()
        } else {
            loadTasksForCurrentView()
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        loadTasksForDate(date)
    }

    fun goToToday() {
        _selectedDate.value = DateUtils.today()
        loadTasksForDate(DateUtils.today())
    }

    fun nextPeriod() {
        when (_currentView.value) {
            AppView.DAY -> {
                val next = LocalDate.parse(_selectedDate.value).plus(1, DateTimeUnit.DAY).toString()
                _selectedDate.value = next
            }
            AppView.WEEK -> {
                val next = LocalDate.parse(_selectedDate.value).plus(7, DateTimeUnit.DAY).toString()
                _selectedDate.value = next
            }
            AppView.MONTH -> {
                val ld = LocalDate.parse(_selectedDate.value)
                val nextYear = if (ld.monthNumber == 12) ld.year + 1 else ld.year
                val nextMonth = if (ld.monthNumber == 12) 1 else ld.monthNumber + 1
                _selectedDate.value = LocalDate(nextYear, nextMonth, 1).toString()
            }
            AppView.TAGS -> { /* no-op */ }
            AppView.PROFILE -> { /* no-op */ }
        }
        loadTasksForCurrentView()
    }

    fun previousPeriod() {
        when (_currentView.value) {
            AppView.DAY -> {
                val prev = LocalDate.parse(_selectedDate.value).minus(1, DateTimeUnit.DAY).toString()
                _selectedDate.value = prev
            }
            AppView.WEEK -> {
                val prev = LocalDate.parse(_selectedDate.value).minus(7, DateTimeUnit.DAY).toString()
                _selectedDate.value = prev
            }
            AppView.MONTH -> {
                val ld = LocalDate.parse(_selectedDate.value)
                val prevYear = if (ld.monthNumber == 1) ld.year - 1 else ld.year
                val prevMonth = if (ld.monthNumber == 1) 12 else ld.monthNumber - 1
                _selectedDate.value = LocalDate(prevYear, prevMonth, 1).toString()
            }
            AppView.TAGS -> { /* no-op */ }
            AppView.PROFILE -> { /* no-op */ }
        }
        loadTasksForCurrentView()
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun dismissAddDialog() {
        _showAddDialog.value = false
    }

    fun showEditDialog(task: Task) {
        _editingTask.value = task
    }

    fun dismissEditDialog() {
        _editingTask.value = null
    }

    fun addTask(title: String, description: String, priority: Priority, dueDate: String, tags: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                val now = kotlinx.datetime.Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                val task = Task(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate.ifBlank { DateUtils.today() },
                    createdAt = now,
                    tags = tags
                )
                taskRepository.createTask(task)
                _showAddDialog.value = false
                loadTasksForCurrentView()
                loadAllTasks()
            } catch (e: Exception) {
                _error.value = "添加任务失败: ${e.message}"
            }
        }
    }

    fun editTask(
        taskId: String,
        title: String,
        description: String,
        priority: Priority,
        dueDate: String,
        status: TaskStatus,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                val existing = taskRepository.getTaskById(taskId)
                if (existing != null) {
                    val updated = existing.copy(
                        title = title,
                        description = description,
                        priority = priority,
                        dueDate = dueDate.ifBlank { DateUtils.today() },
                        status = status,
                        tags = tags,
                        completedAt = if (status == TaskStatus.DONE && existing.status != TaskStatus.DONE) {
                            kotlinx.datetime.Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                        } else if (status == TaskStatus.TODO) {
                            null
                        } else {
                            existing.completedAt
                        }
                    )
                    taskRepository.updateTask(updated)
                    _editingTask.value = null
                    loadTasksForCurrentView()
                    loadAllTasks()
                }
            } catch (e: Exception) {
                _error.value = "更新任务失败: ${e.message}"
            }
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.toggleTaskStatus(task.id)
                loadTasksForCurrentView()
                loadAllTasks()
            } catch (e: Exception) {
                _error.value = "更新任务失败: ${e.message}"
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(id)
                loadTasksForCurrentView()
                loadAllTasks()
            } catch (e: Exception) {
                _error.value = "删除任务失败: ${e.message}"
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                loadTasksForCurrentView()
                loadAllTasks()
            } catch (e: Exception) {
                _error.value = "更新任务失败: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun loadTasksForCurrentView() {
        when (_currentView.value) {
            AppView.DAY -> loadTasksForDate(_selectedDate.value)
            AppView.WEEK -> loadTasksForWeek()
            AppView.MONTH -> loadTasksForMonth()
            AppView.TAGS -> { /* no-op: tags have their own loading */ }
            AppView.PROFILE -> { /* no-op */ }
        }
    }

    private fun loadTasksForDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _tasks.value = taskRepository.getTasksByDate(date)
            } catch (e: Exception) {
                _error.value = "加载任务失败: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    private fun loadTasksForWeek() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val start = DateUtils.startOfWeek(_selectedDate.value)
                val end = DateUtils.endOfWeek(_selectedDate.value)
                _tasks.value = taskRepository.getTasksByDateRange(start, end)
            } catch (e: Exception) {
                _error.value = "加载任务失败: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    private fun loadTasksForMonth() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val start = DateUtils.startOfMonth(_selectedDate.value)
                val end = DateUtils.endOfMonth(_selectedDate.value)
                _tasks.value = taskRepository.getTasksByDateRange(start, end)
            } catch (e: Exception) {
                _error.value = "加载任务失败: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    private fun loadAllTasks() {
        viewModelScope.launch {
            try {
                _allTasks.value = taskRepository.getAllTasks()
            } catch (e: Exception) {
                // silently fail for all tasks
            }
        }
    }

    fun addTag(name: String, icon: String) {
        viewModelScope.launch {
            try {
                tagRepository.createTag(Tag(name = name, icon = icon))
                loadTags()
            } catch (e: Exception) {
                _error.value = "添加标签失败: ${e.message}"
            }
        }
    }

    fun updateTag(id: String, name: String, icon: String) {
        viewModelScope.launch {
            try {
                val existing = tagRepository.getTagById(id)
                if (existing != null) {
                    tagRepository.updateTag(existing.copy(name = name, icon = icon))
                    loadTags()
                }
            } catch (e: Exception) {
                _error.value = "更新标签失败: ${e.message}"
            }
        }
    }

    fun deleteTag(id: String) {
        viewModelScope.launch {
            try {
                tagRepository.deleteTag(id)
                loadTags()
            } catch (e: Exception) {
                _error.value = "删除标签失败: ${e.message}"
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            try {
                _tags.value = tagRepository.getAllTags()
            } catch (e: Exception) {
                // silently fail
            }
        }
    }

    fun tasksForDate(date: String): List<Task> {
        return _tasks.value.filter { it.dueDate == date }
    }

    fun hasTasksOnDate(date: String): Boolean {
        return _allTasks.value.any { it.dueDate == date }
    }

    fun incompleteTasksCount(date: String): Int {
        return _allTasks.value.count { it.dueDate == date && it.status == TaskStatus.TODO }
    }

    fun isDateToday(date: String): Boolean {
        return DateUtils.isDateToday(date)
    }

    fun exportWeek(status: String = "all") {
        viewModelScope.launch {
            try {
                val start = DateUtils.startOfWeek(_selectedDate.value)
                val end = DateUtils.endOfWeek(_selectedDate.value)
                val url = "http://localhost:8080/api/export/markdown?startDate=$start&endDate=$end&status=$status"
                // 使用简单的 HTTP GET 获取 Markdown 文本
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                val response = connection.inputStream.bufferedReader().readText()
                connection.disconnect()
                _exportContent.value = response
            } catch (e: Exception) {
                _error.value = "导出失败: ${e.message}"
            }
        }
    }

    fun dismissExport() {
        _exportContent.value = null
    }

    companion object {
        // We'll use LocalTaskRepository for offline/demo mode
        // and switch to RemoteTaskRepository when server is available
    }
}
