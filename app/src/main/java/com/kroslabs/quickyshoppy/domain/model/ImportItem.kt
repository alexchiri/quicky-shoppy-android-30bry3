package com.kroslabs.quickyshoppy.domain.model

data class ImportItem(
    val name: String,
    val quantity: String? = null,
    val category: String? = null,
    val recipeLink: String? = null,
    val isSelected: Boolean = true
)

data class ImportData(
    val items: List<ImportItem>
)
