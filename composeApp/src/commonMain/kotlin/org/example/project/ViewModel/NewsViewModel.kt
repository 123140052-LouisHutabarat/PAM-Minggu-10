package org.example.project.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.Model.Article
import org.example.project.Model.NewsRepository

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val articles: List<Article>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}

class NewsViewModel(
    private val repository: NewsRepository = NewsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchNews()
    }

    fun fetchNews() {
        viewModelScope.launch {
            _uiState.value = NewsUiState.Loading
            repository.getTopNews()
                .onSuccess { articles ->
                    val validArticles = articles.filter { it.title != "[Removed]" }
                    _uiState.value = NewsUiState.Success(validArticles)
                }
                .onFailure { error ->
                    _uiState.value = NewsUiState.Error(
                        "Tidak ada koneksi internet.\nPastikan perangkat terhubung ke jaringan dan coba lagi."
                    )
                }
        }
    }

    fun refreshNews() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.getTopNews()
                .onSuccess { articles ->
                    val validArticles = articles.filter { it.title != "[Removed]" }
                    _uiState.value = NewsUiState.Success(validArticles)
                    _isRefreshing.value = false
                }
                .onFailure { error ->
                    _uiState.value = NewsUiState.Error(
                         "Gagal memperbarui berita"
                    )
                    _isRefreshing.value = false
                }
        }
    }
}