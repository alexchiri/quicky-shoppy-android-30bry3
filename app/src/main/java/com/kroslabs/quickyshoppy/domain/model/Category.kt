package com.kroslabs.quickyshoppy.domain.model

enum class Category(val displayName: String, val emoji: String) {
    UNCATEGORISED("Uncategorised", "❓"),
    VEGETABLES_AND_FRUITS("Vegetables and Fruits", "🥬"),
    VEGETARIAN("Vegetarian", "🥗"),
    GLUCOSE_FREE("Glucose-Free", "🚫"),
    LACTOSE_FREE("Lactose-Free", "🥥"),
    BREAD_PRODUCTS("Bread Products", "🍞"),
    SWEETS("Sweets", "🍬"),
    PANTRY("Pantry", "🏺"),
    MILK_PRODUCTS("Milk Products", "🥛"),
    MEAT_AND_SEAFOOD("Meat and Seafood", "🥩"),
    EGGS("Eggs", "🥚"),
    COFFEE_AND_TEA("Coffee & Tea", "☕"),
    HOUSEHOLD_SUPPLIES("Household Supplies", "🧹"),
    BEVERAGES("Beverages", "🥤"),
    REFRIGERATED_ITEMS("Refrigerated Items", "🧊"),
    ELECTRONICS("Electronics", "📱"),
    OTHER("Other", "🔷");

    companion object {
        fun fromDisplayName(name: String): Category {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: UNCATEGORISED
        }
    }
}
