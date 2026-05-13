package org.example.project.Model

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}

class NewsRepository(private val client: HttpClient = HttpClientFactory.create()) {

    private val baseUrl = "https://saurav.tech/NewsAPI/top-headlines/category/technology/us.json"

    suspend fun getTopNews(): Result<List<Article>> {
        return try {
            val response: NewsResponse = client.get(baseUrl).body()
            Result.success(response.articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}