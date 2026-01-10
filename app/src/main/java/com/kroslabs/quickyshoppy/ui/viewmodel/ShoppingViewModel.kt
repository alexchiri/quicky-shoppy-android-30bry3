package com.kroslabs.quickyshoppy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kroslabs.quickyshoppy.data.local.AppDatabase
import com.kroslabs.quickyshoppy.data.local.DebugLogManager
import com.kroslabs.quickyshoppy.data.local.SettingsDataStore
import com.kroslabs.quickyshoppy.data.remote.ClaudeRepository
import com.kroslabs.quickyshoppy.data.repository.ShoppingRepository
import com.kroslabs.quickyshoppy.domain.model.Category
import com.kroslabs.quickyshoppy.domain.model.ImportItem
import com.kroslabs.quickyshoppy.domain.model.Ingredient
import com.kroslabs.quickyshoppy.domain.model.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShoppingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val analyzingRecipe: Boolean = false,
    val ingredients: List<Ingredient>? = null,
    val importItems: List<ImportItem>? = null,
    val showDeleteDialog: Boolean = false,
    val deleteDialogType: DeleteDialogType = DeleteDialogType.ALL
)

enum class DeleteDialogType {
    ALL, COMPLETED
}

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ShoppingRepository(database.shoppingItemDao())
    private val claudeRepository = ClaudeRepository()
    val settingsDataStore = SettingsDataStore(application)

    val items: StateFlow<List<ShoppingItem>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val apiKey: StateFlow<String?> = settingsDataStore.apiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(ShoppingUiState())
    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    fun addItem(name: String, quantity: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            DebugLogManager.info(TAG, "Adding new item: '$name' ${quantity?.let { "(qty: $it)" } ?: ""}")
            val itemId = repository.addItem(name.trim(), quantity?.trim())
            DebugLogManager.debug(TAG, "Item added to database with ID: $itemId")
            categorizeItem(itemId, name)
        }
    }

    private fun categorizeItem(itemId: Long, itemName: String) {
        viewModelScope.launch {
            val currentApiKey = apiKey.value
            if (currentApiKey.isNullOrBlank()) {
                DebugLogManager.warning(TAG, "Skipping AI categorization for '$itemName' - No API key configured")
                return@launch
            }

            DebugLogManager.info(TAG, "Requesting AI categorization for item ID $itemId: '$itemName'")
            val result = claudeRepository.categorizeItem(currentApiKey, itemName)
            result.onSuccess { category ->
                repository.updateItemCategory(itemId, category)
                DebugLogManager.info(TAG, "Item '$itemName' moved to category: ${category.displayName}")
            }.onFailure { e ->
                DebugLogManager.error(TAG, "Categorization failed for '$itemName': ${e.message}")
            }
        }
    }

    fun toggleItemCompletion(itemId: Long) {
        viewModelScope.launch {
            DebugLogManager.debug(TAG, "Toggling completion for item ID: $itemId")
            repository.toggleItemCompletion(itemId)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            DebugLogManager.info(TAG, "Deleting item ID: $itemId")
            repository.deleteItem(itemId)
        }
    }

    fun moveItemToCategory(itemId: Long, category: Category) {
        viewModelScope.launch {
            DebugLogManager.info(TAG, "Manually moving item ID $itemId to category: ${category.displayName}")
            repository.updateItemCategory(itemId, category)
        }
    }

    fun updateItemLink(itemId: Long, link: String?) {
        viewModelScope.launch {
            repository.updateItemLink(itemId, link?.ifBlank { null })
        }
    }

    fun showDeleteDialog(type: DeleteDialogType) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true, deleteDialogType = type)
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    fun confirmDelete() {
        viewModelScope.launch {
            when (_uiState.value.deleteDialogType) {
                DeleteDialogType.ALL -> repository.deleteAllItems()
                DeleteDialogType.COMPLETED -> repository.deleteCompletedItems()
            }
            hideDeleteDialog()
        }
    }

    fun analyzeRecipePhoto(imageBytes: ByteArray) {
        viewModelScope.launch {
            val currentApiKey = apiKey.value
            if (currentApiKey.isNullOrBlank()) {
                DebugLogManager.error(TAG, "Recipe analysis failed - No API key configured")
                _uiState.value = _uiState.value.copy(error = "Please set your Claude API key in settings")
                return@launch
            }

            DebugLogManager.info(TAG, "Starting recipe photo analysis...")
            _uiState.value = _uiState.value.copy(analyzingRecipe = true, error = null)
            val result = claudeRepository.analyzeRecipePhoto(currentApiKey, imageBytes)
            result.onSuccess { ingredients ->
                DebugLogManager.success(TAG, "Recipe analysis complete - Found ${ingredients.size} ingredients")
                _uiState.value = _uiState.value.copy(
                    analyzingRecipe = false,
                    ingredients = ingredients
                )
            }.onFailure { e ->
                DebugLogManager.error(TAG, "Recipe analysis failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    analyzingRecipe = false,
                    error = "Failed to analyze recipe: ${e.message}"
                )
            }
        }
    }

    fun toggleIngredientSelection(index: Int) {
        val currentIngredients = _uiState.value.ingredients ?: return
        val updated = currentIngredients.toMutableList()
        updated[index] = updated[index].copy(isSelected = !updated[index].isSelected)
        _uiState.value = _uiState.value.copy(ingredients = updated)
    }

    fun selectAllIngredients(select: Boolean) {
        val currentIngredients = _uiState.value.ingredients ?: return
        val updated = currentIngredients.map { it.copy(isSelected = select) }
        _uiState.value = _uiState.value.copy(ingredients = updated)
    }

    fun addSelectedIngredients() {
        viewModelScope.launch {
            val ingredients = _uiState.value.ingredients ?: return@launch
            val selectedIngredients = ingredients.filter { it.isSelected }
            DebugLogManager.info(TAG, "Adding ${selectedIngredients.size} selected ingredients from recipe")
            selectedIngredients.forEach { ingredient ->
                DebugLogManager.debug(TAG, "Adding ingredient: '${ingredient.name}' ${ingredient.quantity?.let { "($it)" } ?: ""}")
                val itemId = repository.addItem(ingredient.name, ingredient.quantity)
                categorizeItem(itemId, ingredient.name)
            }
            DebugLogManager.success(TAG, "Successfully added ${selectedIngredients.size} ingredients")
            _uiState.value = _uiState.value.copy(ingredients = null)
        }
    }

    fun clearIngredients() {
        _uiState.value = _uiState.value.copy(ingredients = null)
    }

    fun setImportItems(items: List<ImportItem>) {
        DebugLogManager.info(TAG, "Import data received - ${items.size} items to import")
        items.forEachIndexed { index, item ->
            DebugLogManager.debug(TAG, "Import item $index: '${item.name}' ${item.quantity?.let { "(qty: $it)" } ?: ""} ${item.category?.let { "[cat: $it]" } ?: "[no category]"}")
        }
        _uiState.value = _uiState.value.copy(importItems = items)
    }

    fun toggleImportItemSelection(index: Int) {
        val currentItems = _uiState.value.importItems ?: return
        val updated = currentItems.toMutableList()
        updated[index] = updated[index].copy(isSelected = !updated[index].isSelected)
        _uiState.value = _uiState.value.copy(importItems = updated)
    }

    fun selectAllImportItems(select: Boolean) {
        val currentItems = _uiState.value.importItems ?: return
        val updated = currentItems.map { it.copy(isSelected = select) }
        _uiState.value = _uiState.value.copy(importItems = updated)
    }

    fun addSelectedImportItems() {
        viewModelScope.launch {
            val items = _uiState.value.importItems ?: return@launch
            val selectedItems = items.filter { it.isSelected }
            DebugLogManager.info(TAG, "Importing ${selectedItems.size} selected items")
            selectedItems.forEach { item ->
                val category = item.category?.let { Category.fromDisplayName(it) } ?: Category.UNCATEGORISED
                DebugLogManager.debug(TAG, "Importing: '${item.name}' -> category: ${category.displayName}")
                val itemId = repository.addItemWithDetails(
                    name = item.name,
                    quantity = item.quantity,
                    category = category,
                    recipeLink = item.recipeLink
                )
                // Only categorize if no category was provided
                if (item.category == null) {
                    DebugLogManager.debug(TAG, "Item '${item.name}' has no category - requesting AI categorization")
                    categorizeItem(itemId, item.name)
                } else {
                    DebugLogManager.debug(TAG, "Item '${item.name}' imported with predefined category: ${category.displayName}")
                }
            }
            DebugLogManager.success(TAG, "Successfully imported ${selectedItems.size} items")
            _uiState.value = _uiState.value.copy(importItems = null)
        }
    }

    fun clearImportItems() {
        _uiState.value = _uiState.value.copy(importItems = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            DebugLogManager.info(TAG, "API key saved (${key.take(10)}...)")
            settingsDataStore.saveApiKey(key)
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            DebugLogManager.info(TAG, "API key cleared")
            settingsDataStore.clearApiKey()
        }
    }

    companion object {
        private const val TAG = "ShoppingViewModel"
    }

    fun getExportText(): String {
        val groupedItems = items.value.groupBy { it.category }
        val sb = StringBuilder()

        Category.entries.forEach { category ->
            val categoryItems = groupedItems[category] ?: return@forEach
            if (categoryItems.isNotEmpty()) {
                sb.appendLine("${category.emoji} ${category.displayName}")
                categoryItems.forEach { item ->
                    val prefix = if (item.isCompleted) "✓ " else "• "
                    sb.appendLine("$prefix${item.displayName}")
                }
                sb.appendLine()
            }
        }

        return sb.toString().trimEnd()
    }
}
