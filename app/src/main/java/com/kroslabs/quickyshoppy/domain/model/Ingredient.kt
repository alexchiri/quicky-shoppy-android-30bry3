package com.kroslabs.quickyshoppy.domain.model

data class Ingredient(
    val name: String,
    val quantity: String? = null,
    val isSelected: Boolean = true
)
