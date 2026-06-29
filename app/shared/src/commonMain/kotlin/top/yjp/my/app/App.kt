package top.yjp.my.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import top.yjp.my.app.repository.IAuthRepository
import top.yjp.my.app.repository.ITagRepository
import top.yjp.my.app.repository.ITaskRepository
import top.yjp.my.app.ui.*
import top.yjp.my.app.viewmodel.AppView
import top.yjp.my.app.viewmodel.AuthViewModel
import top.yjp.my.app.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val taskRepository: ITaskRepository = koinInject()
    val tagRepository: ITagRepository = koinInject()
    val authRepository: IAuthRepository = koinInject()

    val authViewModel = remember { AuthViewModel(authRepository) }
    val taskViewModel = remember { TaskViewModel(taskRepository, tagRepository) }

    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val authError by authViewModel.error.collectAsStateWithLifecycle()
    val authLoading by authViewModel.isLoading.collectAsStateWithLifecycle()

    MaterialTheme {
        if (!isLoggedIn) {
            LoginPage(
                isLoading = authLoading,
                error = authError,
                onLogin = { username, password -> authViewModel.login(username, password) },
                onClearError = { authViewModel.clearError() }
            )
        } else {
            val currentView by taskViewModel.currentView.collectAsStateWithLifecycle()
            val selectedDate by taskViewModel.selectedDate.collectAsStateWithLifecycle()
            val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
            val allTasks by taskViewModel.allTasks.collectAsStateWithLifecycle()
            val showAddDialog by taskViewModel.showAddDialog.collectAsStateWithLifecycle()
            val editingTask by taskViewModel.editingTask.collectAsStateWithLifecycle()
            val tags by taskViewModel.tags.collectAsStateWithLifecycle()
            val error by taskViewModel.error.collectAsStateWithLifecycle()
            val exportContent by taskViewModel.exportContent.collectAsStateWithLifecycle()

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        AppView.entries.forEach { view ->
                            NavigationBarItem(
                                selected = currentView == view,
                                onClick = { taskViewModel.setView(view) },
                                icon = {
                                    Text(
                                        when (view) {
                                            AppView.DAY -> "📋"
                                            AppView.WEEK -> "📅"
                                            AppView.MONTH -> "🗓"
                                            AppView.TAGS -> "🏷️"
                                            AppView.PROFILE -> "👤"
                                        }
                                    )
                                },
                                label = {
                                    Text(
                                        when (view) {
                                            AppView.DAY -> "日"
                                            AppView.WEEK -> "周"
                                            AppView.MONTH -> "月"
                                            AppView.TAGS -> "标签"
                                            AppView.PROFILE -> "我的"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (currentView) {
                        AppView.DAY -> DayView(
                            date = selectedDate,
                            tasks = taskViewModel.filteredTasks,
                            onToggleTask = { taskViewModel.toggleTask(it) },
                            onDeleteTask = { taskViewModel.deleteTask(it) },
                            onEditTask = { taskViewModel.showEditDialog(it) },
                            onAddClick = { taskViewModel.showAddDialog() },
                            onPreviousDay = { taskViewModel.previousPeriod() },
                            onNextDay = { taskViewModel.nextPeriod() },
                            onTodayClick = { taskViewModel.goToToday() },
                            searchQuery = taskViewModel.searchQuery.collectAsStateWithLifecycle().value,
                            onSearchQueryChange = { taskViewModel.setSearchQuery(it) }
                        )
                        AppView.WEEK -> WeekView(
                            selectedDate = selectedDate,
                            tasks = tasks,
                            getTasksForDate = { taskViewModel.tasksForDate(it) },
                            getIncompleteCount = { taskViewModel.incompleteTasksCount(it) },
                            onDateSelected = { taskViewModel.selectDate(it) },
                            onToggleTask = { taskViewModel.toggleTask(it) },
                            onDeleteTask = { taskViewModel.deleteTask(it) },
                            onAddClick = { taskViewModel.showAddDialog() },
                            onExportClick = { taskViewModel.exportWeek() },
                            onPreviousWeek = { taskViewModel.previousPeriod() },
                            onNextWeek = { taskViewModel.nextPeriod() },
                            onTodayClick = { taskViewModel.goToToday() }
                        )
                        AppView.MONTH -> MonthView(
                            selectedDate = selectedDate,
                            tasks = tasks,
                            getTasksForDate = { taskViewModel.tasksForDate(it) },
                            getHasTasks = { taskViewModel.hasTasksOnDate(it) },
                            getIncompleteCount = { taskViewModel.incompleteTasksCount(it) },
                            onDateSelected = { taskViewModel.selectDate(it) },
                            onToggleTask = { taskViewModel.toggleTask(it) },
                            onDeleteTask = { taskViewModel.deleteTask(it) },
                            onEditTask = { taskViewModel.showEditDialog(it) },
                            onAddClick = { taskViewModel.showAddDialog() },
                            onPreviousMonth = { taskViewModel.previousPeriod() },
                            onNextMonth = { taskViewModel.nextPeriod() },
                            onTodayClick = { taskViewModel.goToToday() }
                        )
                        AppView.TAGS -> TagsView(
                            tags = tags,
                            onAddTag = { name, icon -> taskViewModel.addTag(name, icon) },
                            onUpdateTag = { id, name, icon -> taskViewModel.updateTag(id, name, icon) },
                            onDeleteTag = { taskViewModel.deleteTag(it) }
                        )
                        AppView.PROFILE -> ProfilePage(
                            user = currentUser,
                            onLogout = {
                                authViewModel.logout()
                                taskViewModel.setView(AppView.DAY)
                            }
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddTaskDialog(
                    initialDate = selectedDate,
                    availableTags = tags,
                    onDismiss = { taskViewModel.dismissAddDialog() },
                    onConfirm = { title, description, priority, dueDate, selectedTags ->
                        taskViewModel.addTask(title, description, priority, dueDate, selectedTags)
                    }
                )
            }

            editingTask?.let { task ->
                EditTaskDialog(
                    task = task,
                    availableTags = tags,
                    onDismiss = { taskViewModel.dismissEditDialog() },
                    onConfirm = { title, description, priority, dueDate, status, selectedTags ->
                        taskViewModel.editTask(task.id, title, description, priority, dueDate, status, selectedTags)
                    }
                )
            }

            exportContent?.let { content ->
                ExportDialog(
                    content = content,
                    onDismiss = { taskViewModel.dismissExport() }
                )
            }

            if (error != null) {
                AlertDialog(
                    onDismissRequest = { taskViewModel.clearError() },
                    title = { Text("提示") },
                    text = { Text(error ?: "") },
                    confirmButton = {
                        TextButton(onClick = { taskViewModel.clearError() }) {
                            Text("确定")
                        }
                    }
                )
            }
        }
    }
}
