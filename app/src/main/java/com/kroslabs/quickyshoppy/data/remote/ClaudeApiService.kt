package com.kroslabs.quickyshoppy.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ClaudeApiService {
    @Headers("Content-Type: application/json", "anthropic-version: 2023-06-01")
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Body request: ClaudeRequest
    ): ClaudeResponse
}

data class ClaudeRequest(
    val model: String = "claude-sonnet-4-5-20250929",
    val max_tokens: Int,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: Any // Can be String or List<ContentBlock>
)

data class ContentBlock(
    val type: String,
    val text: String? = null,
    val source: ImageSource? = null
)

data class ImageSource(
    val type: String = "base64",
    val media_type: String,
    val data: String
)

data class ClaudeResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ContentBlockResponse>,
    val model: String,
    @SerializedName("stop_reason")
    val stopReason: String?,
    val usage: Usage?
)

data class ContentBlockResponse(
    val type: String,
    val text: String?
)

data class Usage(
    @SerializedName("input_tokens")
    val inputTokens: Int,
    @SerializedName("output_tokens")
    val outputTokens: Int
)
