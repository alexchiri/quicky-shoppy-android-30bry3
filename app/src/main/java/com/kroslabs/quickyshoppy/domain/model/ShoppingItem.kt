package com.kroslabs.quickyshoppy.domain.model

data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val quantity: String? = null,
    val category: Category = Category.UNCATEGORISED,
    val isCompleted: Boolean = false,
    val recipeLink: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = if (quantity.isNullOrBlank()) name else "$name ($quantity)"
}
