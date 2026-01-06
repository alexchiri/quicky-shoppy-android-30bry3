package com.kroslabs.quickyshoppy.data.remote

import android.util.Base64
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

            val request = ClaudeRequest(
                max_tokens = 50,
                messages = listOf(
                    Message(role = "user", content = prompt)
                )
            )

            val response = api.sendMessage(apiKey, request)
            val categoryText = response.content.firstOrNull()?.text?.trim() ?: "Other"
            val category = Category.fromDisplayName(categoryText)
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeRecipePhoto(apiKey: String, imageBytes: ByteArray): Result<List<Ingredient>> {
        return try {
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

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

            val request = ClaudeRequest(
                max_tokens = 1024,
                messages = listOf(
                    Message(role = "user", content = contentBlocks)
                )
            )

            val response = api.sendMessage(apiKey, request)
            val responseText = response.content.firstOrNull()?.text ?: ""

            val ingredients = responseText.lines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split("|")
                    if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                        Ingredient(
                            name = parts[0].trim(),
                            quantity = if (parts.size > 1 && parts[1].isNotBlank()) parts[1].trim() else null
                        )
                    } else null
                }

            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
