package org.example.project.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.project.Note
import org.example.project.data.NoteRepository
import org.example.project.data.repository.validation.NoteValidator
import org.example.project.db.NetworkMonitor

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(
    private val repository: NoteRepository,
    private val validator: NoteValidator,
    private val networkMonitor: NetworkMonitor? = null,
    val enableBatteryPolling: Boolean = false
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _showDeleteDialog = MutableStateFlow<Note?>(null)
    val showDeleteDialog: StateFlow<Note?> = _showDeleteDialog.asStateFlow()

    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query: String ->
            if (query.isBlank()) repository.getAllNotes()
            else repository.searchNotes(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteNotes: StateFlow<List<Note>> = repository.getFavoriteNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    sealed class NotesUiState {
        object Loading : NotesUiState()
        object Empty : NotesUiState()
        data class Content(val notes: List<Note>) : NotesUiState()
    }

    val uiState: StateFlow<NotesUiState> = notes
        .map { list: List<Note> ->
            if (list.isEmpty()) NotesUiState.Empty
            else NotesUiState.Content(list)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesUiState.Loading
        )

    // Network connectivity state (optional dependency)
    val isConnected: StateFlow<Boolean> = networkMonitor
        ?.observeConnectivity()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
        ?: MutableStateFlow(true)

    fun addNote(title: String, content: String) {
        val tempNote = Note(id = "temp", title = title, content = content)
        val result = validator.validateDetailed(tempNote)
        if (!result.isValid) {
            viewModelScope.launch {
                _snackbarMessage.emit(result.errors.joinToString("; "))
            }
            return
        }
        viewModelScope.launch { repository.insertNote(title, content) }
    }

    fun updateNote(noteId: String, newTitle: String, newContent: String) {
        viewModelScope.launch { repository.updateNote(noteId, newTitle, newContent) }
    }

    fun requestDeleteNote(note: Note) {
        _showDeleteDialog.value = note
    }

    fun confirmDeleteNote() {
        val note = _showDeleteDialog.value ?: return
        _showDeleteDialog.value = null
        deleteNote(note.id)
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = null
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch { repository.deleteNote(noteId) }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            val currentFavIds: List<String> = favoriteNotes.value.map { it.id }
            repository.toggleFavorite(note, currentFavIds)
        }
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun clearSearch() { _searchQuery.value = "" }
}
