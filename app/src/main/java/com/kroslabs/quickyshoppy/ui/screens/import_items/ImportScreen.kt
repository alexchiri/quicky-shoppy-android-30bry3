package com.kroslabs.quickyshoppy.ui.screens.import_items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kroslabs.quickyshoppy.domain.model.Category
import com.kroslabs.quickyshoppy.domain.model.ImportItem
import com.kroslabs.quickyshoppy.ui.viewmodel.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    viewModel: ShoppingViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val items = uiState.importItems ?: return

    val allSelected = items.all { it.isSelected }
    val noneSelected = items.none { it.isSelected }

    // Group items by category
    val groupedItems = items.mapIndexed { index, item -> index to item }
        .groupBy { it.second.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Items") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearImportItems()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.selectAllImportItems(!allSelected) }
                    ) {
                        Text(
                            if (allSelected) "Deselect All" else "Select All",
                            color = Color.White
                        )
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
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedItems.forEach { (categoryName, indexedItems) ->
                    val category = categoryName?.let { Category.fromDisplayName(it) }

                    item {
                        Text(
                            text = if (category != null) {
                                "${category.emoji} ${category.displayName}"
                            } else {
                                "Uncategorised"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    itemsIndexed(indexedItems) { _, (originalIndex, item) ->
                        ImportItemCard(
                            item = item,
                            onToggle = { viewModel.toggleImportItemSelection(originalIndex) }
                        )
                    }
                }
            }

            // Bottom button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.addSelectedImportItems()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !noneSelected
                ) {
                    val count = items.count { it.isSelected }
                    Text("Add Selected ($count)")
                }
            }
        }
    }
}

@Composable
fun ImportItemCard(
    item: ImportItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggle() }
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                item.quantity?.let { quantity ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = quantity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            if (item.recipeLink != null) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = "Has link",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
