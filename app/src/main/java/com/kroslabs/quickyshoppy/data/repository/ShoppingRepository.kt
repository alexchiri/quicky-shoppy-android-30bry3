package com.kroslabs.quickyshoppy.data.repository

import com.kroslabs.quickyshoppy.data.local.ShoppingItemDao
import com.kroslabs.quickyshoppy.data.local.ShoppingItemEntity
import com.kroslabs.quickyshoppy.domain.model.Category
import com.kroslabs.quickyshoppy.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShoppingRepository(private val dao: ShoppingItemDao) {

    fun getAllItems(): Flow<List<ShoppingItem>> {
        return dao.getAllItems().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun addItem(name: String, quantity: String? = null): Long {
        val entity = ShoppingItemEntity(
            name = name,
            quantity = quantity,
            category = Category.UNCATEGORISED.displayName,
            isCompleted = false,
            recipeLink = null,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertItem(entity)
    }

    suspend fun addItemWithDetails(
        name: String,
        quantity: String? = null,
        category: Category = Category.UNCATEGORISED,
        recipeLink: String? = null
    ): Long {
        val entity = ShoppingItemEntity(
            name = name,
            quantity = quantity,
            category = category.displayName,
            isCompleted = false,
            recipeLink = recipeLink,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertItem(entity)
    }

    suspend fun updateItem(item: ShoppingItem) {
        dao.updateItem(ShoppingItemEntity.fromDomainModel(item))
    }

    suspend fun updateItemCategory(itemId: Long, category: Category) {
        val entity = dao.getItemById(itemId) ?: return
        dao.updateItem(entity.copy(category = category.displayName))
    }

    suspend fun toggleItemCompletion(itemId: Long) {
        val entity = dao.getItemById(itemId) ?: return
        dao.updateItem(entity.copy(isCompleted = !entity.isCompleted))
    }

    suspend fun updateItemLink(itemId: Long, link: String?) {
        val entity = dao.getItemById(itemId) ?: return
        dao.updateItem(entity.copy(recipeLink = link))
    }

    suspend fun deleteItem(itemId: Long) {
        dao.deleteItemById(itemId)
    }

    suspend fun deleteCompletedItems() {
        dao.deleteCompletedItems()
    }

    suspend fun deleteAllItems() {
        dao.deleteAllItems()
    }

    suspend fun getItemById(id: Long): ShoppingItem? {
        return dao.getItemById(id)?.toDomainModel()
    }
}
