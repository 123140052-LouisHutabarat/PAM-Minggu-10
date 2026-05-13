package org.example.project.Model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class NewsResponse(
    val status: String,
    val totalResults: Int? = null,
    val articles: List<Article>
)

@Serializable
data class Article(
    val title: String = "",
    val description: String? = null,
    val url: String = "",
    @SerialName("urlToImage")
    val urlToImage: String? = null,
    val publishedAt: String = "",
    val source: Source? = null,
    val author: String? = null,
    val content: String? = null
)

@Serializable
data class Source(
    val id: String? = null,
    val name: String = ""
)