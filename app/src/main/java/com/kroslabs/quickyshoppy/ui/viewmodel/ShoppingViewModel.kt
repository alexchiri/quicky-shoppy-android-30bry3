package com.kroslabs.quickyshoppy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kroslabs.quickyshoppy.data.local.AppDatabase
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
            val itemId = repository.addItem(name.trim(), quantity?.trim())
            categorizeItem(itemId, name)
        }
    }

    private fun categorizeItem(itemId: Long, itemName: String) {
        viewModelScope.launch {
            val currentApiKey = apiKey.value
            if (currentApiKey.isNullOrBlank()) return@launch

            val result = claudeRepository.categorizeItem(currentApiKey, itemName)
            result.onSuccess { category ->
                repository.updateItemCategory(itemId, category)
            }
        }
    }

    fun toggleItemCompletion(itemId: Long) {
        viewModelScope.launch {
            repository.toggleItemCompletion(itemId)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }

    fun moveItemToCategory(itemId: Long, category: Category) {
        viewModelScope.launch {
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
                _uiState.value = _uiState.value.copy(error = "Please set your Claude API key in settings")
                return@launch
            }

            _uiState.value = _uiState.value.copy(analyzingRecipe = true, error = null)
            val result = claudeRepository.analyzeRecipePhoto(currentApiKey, imageBytes)
            result.onSuccess { ingredients ->
                _uiState.value = _uiState.value.copy(
                    analyzingRecipe = false,
                    ingredients = ingredients
                )
            }.onFailure { e ->
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
            ingredients.filter { it.isSelected }.forEach { ingredient ->
                val itemId = repository.addItem(ingredient.name, ingredient.quantity)
                categorizeItem(itemId, ingredient.name)
            }
            _uiState.value = _uiState.value.copy(ingredients = null)
        }
    }

    fun clearIngredients() {
        _uiState.value = _uiState.value.copy(ingredients = null)
    }

    fun setImportItems(items: List<ImportItem>) {
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
            items.filter { it.isSelected }.forEach { item ->
                val category = item.category?.let { Category.fromDisplayName(it) } ?: Category.UNCATEGORISED
                val itemId = repository.addItemWithDetails(
                    name = item.name,
                    quantity = item.quantity,
                    category = category,
                    recipeLink = item.recipeLink
                )
                // Only categorize if no category was provided
                if (item.category == null) {
                    categorizeItem(itemId, item.name)
                }
            }
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
            settingsDataStore.saveApiKey(key)
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            settingsDataStore.clearApiKey()
        }
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
