package com.example.usageinsight.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DeepSeekApi {
    @POST("v1/chat/completions")
    suspend fun generateAnalysis(
        @Header("Authorization") apiKey: String,
        @Body request: DeepSeekRequest
    ): DeepSeekResponse
}

data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class DeepSeekResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
