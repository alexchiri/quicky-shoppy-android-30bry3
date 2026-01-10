package com.kroslabs.quickyshoppy.data.remote

import android.util.Base64
import com.kroslabs.quickyshoppy.data.local.DebugLogManager
import com.kroslabs.quickyshoppy.domain.model.Category
import com.kroslabs.quickyshoppy.domain.model.Ingredient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ClaudeRepository {

    private val api: ClaudeApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ClaudeApiService::class.java)
    }

    suspend fun categorizeItem(apiKey: String, itemName: String): Result<Category> {
        DebugLogManager.info(TAG, "Starting AI categorization for item: '$itemName'")
        return try {
            val prompt = """You are a shopping list categorizer. Given the item name, respond with ONLY the category name from this list:
- Vegetables and Fruits
- Vegetarian
- Glucose-Free
- Lactose-Free
- Bread Products
- Sweets
- Pantry
- Milk Products
- Meat and Seafood
- Eggs
- Coffee & Tea
- Household Supplies
- Beverages
- Refrigerated Items
- Electronics
- Other

Item: $itemName

Category:"""

            DebugLogManager.debug(TAG, "Sending categorization request to Claude API")
            val request = ClaudeRequest(
                max_tokens = 50,
                messages = listOf(
                    Message(role = "user", content = prompt)
                )
            )

            val response = api.sendMessage(apiKey, request)
            val categoryText = response.content.firstOrNull()?.text?.trim() ?: "Other"
            DebugLogManager.debug(TAG, "Claude API response for '$itemName': '$categoryText'")

            val category = Category.fromDisplayName(categoryText)
            if (category == Category.OTHER && categoryText != "Other") {
                DebugLogManager.warning(TAG, "Item '$itemName' - AI returned '$categoryText' which was NOT RECOGNIZED, defaulting to 'Other'")
            } else if (category == Category.UNCATEGORISED) {
                DebugLogManager.warning(TAG, "Item '$itemName' - Could not be categorized, marked as 'Uncategorised'")
            } else {
                DebugLogManager.success(TAG, "Item '$itemName' successfully categorized as '${category.displayName}' (${category.emoji})")
            }
            Result.success(category)
        } catch (e: Exception) {
            DebugLogManager.error(TAG, "Failed to categorize item '$itemName': ${e.message}")
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "AI-Categorization"
        private const val TAG_RECIPE = "AI-Recipe"
    }

    suspend fun analyzeRecipePhoto(apiKey: String, imageBytes: ByteArray): Result<List<Ingredient>> {
        val imageSizeKb = imageBytes.size / 1024
        DebugLogManager.info(TAG_RECIPE, "Starting recipe photo analysis (image size: ${imageSizeKb}KB)")
        return try {
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            DebugLogManager.debug(TAG_RECIPE, "Image encoded to base64 (${base64Image.length} characters)")

            val prompt = """Analyze this recipe image and extract all ingredients with their quantities.
For each ingredient, provide the name and quantity (if visible).
Convert imperial units to metric:
- cups to ml (1 cup = 237ml)
- ounces to grams (1 oz = 28g)
- tablespoons to ml (1 tbsp = 15ml)
- teaspoons to ml (1 tsp = 5ml)
- pounds to grams (1 lb = 454g)

Respond in this exact format, one ingredient per line:
ingredient_name|quantity

Examples:
flour|473ml
eggs|3
butter|227g
salt|5ml

If no quantity is visible, just put the ingredient name without |.
Only list ingredients, nothing else."""

            val contentBlocks = listOf(
                ContentBlock(
                    type = "image",
                    source = ImageSource(
                        type = "base64",
                        media_type = "image/jpeg",
                        data = base64Image
                    )
                ),
                ContentBlock(
                    type = "text",
                    text = prompt
                )
            )

            DebugLogManager.debug(TAG_RECIPE, "Sending recipe analysis request to Claude API")
            val request = ClaudeRequest(
                max_tokens = 1024,
                messages = listOf(
                    Message(role = "user", content = contentBlocks)
                )
            )

            val response = api.sendMessage(apiKey, request)
            val responseText = response.content.firstOrNull()?.text ?: ""
            DebugLogManager.debug(TAG_RECIPE, "Claude API raw response:\n$responseText")

            val ingredients = responseText.lines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split("|")
                    if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                        val ingredient = Ingredient(
                            name = parts[0].trim(),
                            quantity = if (parts.size > 1 && parts[1].isNotBlank()) parts[1].trim() else null
                        )
                        DebugLogManager.debug(TAG_RECIPE, "Parsed ingredient: '${ingredient.name}' ${ingredient.quantity?.let { "($it)" } ?: "(no quantity)"}")
                        ingredient
                    } else {
                        DebugLogManager.warning(TAG_RECIPE, "Failed to parse line: '$line'")
                        null
                    }
                }

            if (ingredients.isEmpty()) {
                DebugLogManager.warning(TAG_RECIPE, "No ingredients recognized from recipe photo")
            } else {
                DebugLogManager.success(TAG_RECIPE, "Successfully extracted ${ingredients.size} ingredients from recipe photo")
            }

            Result.success(ingredients)
        } catch (e: Exception) {
            DebugLogManager.error(TAG_RECIPE, "Failed to analyze recipe photo: ${e.message}")
            Result.failure(e)
        }
    }
}
