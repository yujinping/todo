package top.yjp.my.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import top.yjp.my.app.model.Priority
import top.yjp.my.app.model.Tag
import top.yjp.my.app.model.Task
import top.yjp.my.app.model.TaskStatus
import top.yjp.my.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTaskDialog(
    task: Task,
    availableTags: List<Tag> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, priority: Priority, dueDate: String, status: TaskStatus, tags: List<String>) -> Unit
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description) }
    var priority by remember(task.id) { mutableStateOf(task.priority) }
    var dueDate by remember(task.id) { mutableStateOf(task.dueDate) }
    var status by remember(task.id) { mutableStateOf(task.status) }
    var selectedTagNames by remember(task.id) { mutableStateOf(task.tags.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("编辑任务", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("任务标题") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("描述（可选）") }, maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // 优先级
                Text("优先级", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        SelectableChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = p.label
                        )
                    }
                }

                // 状态
                Text("状态", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskStatus.entries.forEach { s ->
                        SelectableChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = s.label
                        )
                    }
                }

                OutlinedTextField(
                    value = dueDate, onValueChange = { dueDate = it },
                    label = { Text("日期 (YYYY-MM-DD)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("示例: ${DateUtils.today()}") }
                )

                // 标签
                if (availableTags.isNotEmpty()) {
                    Text("标签", style = MaterialTheme.typography.bodyMedium)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableTags.forEach { tag ->
                            SelectableChip(
                                selected = tag.name in selectedTagNames,
                                onClick = {
                                    selectedTagNames = if (tag.name in selectedTagNames)
                                        selectedTagNames - tag.name
                                    else selectedTagNames + tag.name
                                },
                                label = "${tag.icon} ${tag.name}"
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(title, description, priority, dueDate, status, selectedTagNames.toList()) },
                        enabled = title.isNotBlank()
                    ) { Text("保存") }
                }
            }
        }
    }
}
