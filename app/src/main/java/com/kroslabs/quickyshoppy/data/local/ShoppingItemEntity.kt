package com.kroslabs.quickyshoppy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kroslabs.quickyshoppy.domain.model.Category
import com.kroslabs.quickyshoppy.domain.model.ShoppingItem

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val quantity: String?,
    val category: String,
    val isCompleted: Boolean,
    val recipeLink: String?,
    val createdAt: Long
) {
    fun toDomainModel(): ShoppingItem {
        return ShoppingItem(
            id = id,
            name = name,
            quantity = quantity,
            category = Category.fromDisplayName(category),
            isCompleted = isCompleted,
            recipeLink = recipeLink,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(item: ShoppingItem): ShoppingItemEntity {
            return ShoppingItemEntity(
                id = item.id,
                name = item.name,
                quantity = item.quantity,
                category = item.category.displayName,
                isCompleted = item.isCompleted,
                recipeLink = item.recipeLink,
                createdAt = item.createdAt
            )
        }
    }
}
