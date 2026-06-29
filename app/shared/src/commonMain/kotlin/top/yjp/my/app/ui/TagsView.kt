package top.yjp.my.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import top.yjp.my.app.model.Tag

val EMOJI_OPTIONS = listOf(
    "🏷️", "📌", "🔖", "⭐", "💡", "🔥", "💼", "🎯",
    "📚", "🏠", "🛒", "🎮", "❤️", "💰", "✈️", "🎵",
    "🎨", "🏃", "💪", "🎉", "🌱", "🚗", "💻", "📱",
    "✏️", "🗂️", "🔔", "✅", "⚠️", "🧠", "🎓", "☕"
)

@Composable
fun TagsView(
    tags: List<Tag>,
    onAddTag: (name: String, icon: String) -> Unit,
    onUpdateTag: (id: String, name: String, icon: String) -> Unit,
    onDeleteTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingTag by remember { mutableStateOf<Tag?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "标签管理",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "创建和管理你的标签",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        if (tags.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏷️", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("还没有标签", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "点击下方按钮创建第一个标签",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(tags, key = { it.id }) { tag ->
                    TagItem(
                        tag = tag,
                        onEdit = {
                            editingTag = tag
                            showDialog = true
                        },
                        onDelete = { onDeleteTag(tag.id) }
                    )
                }
            }
        }

        // Add button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = {
                    editingTag = null
                    showDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("＋ 新建标签", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    // Add/Edit tag dialog
    if (showDialog) {
        TagDialog(
            existingTag = editingTag,
            onDismiss = { showDialog = false },
            onConfirm = { name, icon ->
                if (editingTag != null) {
                    onUpdateTag(editingTag!!.id, name, icon)
                } else {
                    onAddTag(name, icon)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun TagItem(
    tag: Tag,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tag icon
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(tag.icon.ifBlank { "🏷️" }, style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tag name
            Text(
                tag.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Edit button
            IconButton(onClick = onEdit) {
                Text("✏️")
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Text("🗑")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagDialog(
    existingTag: Tag?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String) -> Unit
) {
    var name by remember { mutableStateOf(existingTag?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(existingTag?.icon ?: "🏷️") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    if (existingTag != null) "编辑标签" else "新建标签",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("标签名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Emoji picker
                Text("选择图标", style = MaterialTheme.typography.bodyMedium)
                // Show selected icon preview
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        selectedIcon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "当前选中",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Emoji grid
                val columns = 8
                val rows = (EMOJI_OPTIONS.size + columns - 1) / columns
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        for (col in 0 until columns) {
                            val index = row * columns + col
                            if (index < EMOJI_OPTIONS.size) {
                                val emoji = EMOJI_OPTIONS[index]
                                Surface(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable { selectedIcon = emoji },
                                    shape = CircleShape,
                                    color = if (emoji == selectedIcon)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface,
                                    tonalElevation = if (emoji == selectedIcon) 2.dp else 0.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(emoji, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(name.trim(), selectedIcon) },
                        enabled = name.isNotBlank()
                    ) {
                        Text(if (existingTag != null) "保存" else "创建")
                    }
                }
            }
        }
    }
}
