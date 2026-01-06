package com.kroslabs.quickyshoppy.ui.screens.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kroslabs.quickyshoppy.domain.model.Category
import com.kroslabs.quickyshoppy.domain.model.ShoppingItem
import com.kroslabs.quickyshoppy.ui.viewmodel.DeleteDialogType
import com.kroslabs.quickyshoppy.ui.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ShoppingViewModel,
    onNavigateToSettings: () -> Unit,
    onCameraClick: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var newItemText by remember { mutableStateOf("") }
    var showDeleteMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quicky Shoppy") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = {
                        val text = viewModel.getExportText()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Shopping List", text))
                        Toast.makeText(context, "List copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy to clipboard")
                    }
                    Box {
                        IconButton(onClick = { showDeleteMenu = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        DropdownMenu(
                            expanded = showDeleteMenu,
                            onDismissRequest = { showDeleteMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Completed Items") },
                                onClick = {
                                    showDeleteMenu = false
                                    viewModel.showDeleteDialog(DeleteDialogType.COMPLETED)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete All Items") },
                                onClick = {
                                    showDeleteMenu = false
                                    viewModel.showDeleteDialog(DeleteDialogType.ALL)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add item...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newItemText.isNotBlank()) {
                                viewModel.addItem(newItemText)
                                newItemText = ""
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onCameraClick) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Scan recipe",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            viewModel.addItem(newItemText)
                            newItemText = ""
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add item",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uiState.analyzingRecipe) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analyzing recipe...")
                    }
                }
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your shopping list is empty",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                ShoppingList(
                    items = items,
                    onToggleComplete = { viewModel.toggleItemCompletion(it) },
                    onDelete = { viewModel.deleteItem(it) },
                    onMoveToCategory = { itemId, category -> viewModel.moveItemToCategory(itemId, category) },
                    onUpdateLink = { itemId, link -> viewModel.updateItemLink(itemId, link) },
                    onOpenLink = { link ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = {
                Text(
                    if (uiState.deleteDialogType == DeleteDialogType.ALL)
                        "Delete All Items?"
                    else
                        "Delete Completed Items?"
                )
            },
            text = {
                Text(
                    if (uiState.deleteDialogType == DeleteDialogType.ALL)
                        "This will remove all items from your shopping list."
                    else
                        "This will remove all completed items from your shopping list."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ShoppingList(
    items: List<ShoppingItem>,
    onToggleComplete: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onMoveToCategory: (Long, Category) -> Unit,
    onUpdateLink: (Long, String?) -> Unit,
    onOpenLink: (String) -> Unit
) {
    val groupedItems = items.groupBy { it.category }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Category.entries.forEach { category ->
            val categoryItems = groupedItems[category] ?: return@forEach
            if (categoryItems.isNotEmpty()) {
                item(key = "header_${category.name}") {
                    Text(
                        text = "${category.emoji} ${category.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(
                    items = categoryItems,
                    key = { it.id }
                ) { item ->
                    ShoppingItemCard(
                        item = item,
                        onToggleComplete = { onToggleComplete(item.id) },
                        onDelete = { onDelete(item.id) },
                        onMoveToCategory = { category -> onMoveToCategory(item.id, category) },
                        onUpdateLink = { link -> onUpdateLink(item.id, link) },
                        onOpenLink = { item.recipeLink?.let { onOpenLink(it) } }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onMoveToCategory: (Category) -> Unit,
    onUpdateLink: (String?) -> Unit,
    onOpenLink: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var linkText by remember { mutableStateOf(item.recipeLink ?: "") }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset > 100) {
                            onToggleComplete()
                        } else if (dragOffset < -100) {
                            onDelete()
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            }
            .combinedClickable(
                onClick = { },
                onLongClick = { showContextMenu = true },
                onDoubleClick = {
                    if (item.recipeLink != null) {
                        onOpenLink()
                    }
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.displayName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (item.isCompleted)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurface
            )
            if (item.recipeLink != null) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = "Has link",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Move to...") },
                onClick = {
                    showContextMenu = false
                    showCategoryMenu = true
                }
            )
            DropdownMenuItem(
                text = { Text(if (item.recipeLink == null) "Add Link" else "Edit Link") },
                onClick = {
                    showContextMenu = false
                    linkText = item.recipeLink ?: ""
                    showLinkDialog = true
                }
            )
            if (item.recipeLink != null) {
                DropdownMenuItem(
                    text = { Text("Remove Link") },
                    onClick = {
                        showContextMenu = false
                        onUpdateLink(null)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    showContextMenu = false
                    onDelete()
                }
            )
        }

        DropdownMenu(
            expanded = showCategoryMenu,
            onDismissRequest = { showCategoryMenu = false }
        ) {
            Category.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text("${category.emoji} ${category.displayName}") },
                    onClick = {
                        showCategoryMenu = false
                        onMoveToCategory(category)
                    }
                )
            }
        }
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("Add Link") },
            text = {
                OutlinedTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    placeholder = { Text("https://...") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateLink(linkText.ifBlank { null })
                        showLinkDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
