package org.example.project.viewmodel

import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.example.project.Note
import org.example.project.ViewModel.NotesViewModel
import org.example.project.data.NoteRepository
import org.example.project.data.repository.validation.NoteValidator
import org.example.project.data.repository.validation.ValidationResult
import org.example.project.testing.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * NotesViewModelFlowTest - 4 test Flow dengan Turbine
 * Memverifikasi emisi StateFlow secara berurutan.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelFlowTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: NoteRepository
    private lateinit var mockValidator: NoteValidator
    private lateinit var viewModel: NotesViewModel

    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())
    private val favFlow   = MutableStateFlow<List<Note>>(emptyList())

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
        mockValidator  = mockk()

        every { mockRepository.getAllNotes() }     returns notesFlow
        every { mockRepository.getFavoriteNotes() } returns favFlow
        every { mockRepository.searchNotes(any()) } returns flowOf(emptyList())
        every { mockValidator.validateDetailed(any()) } returns ValidationResult(isValid = true)

        viewModel = NotesViewModel(
            repository = mockRepository,
            validator  = mockValidator
        )
    }

    // ── 1 ─────────────────────────────────────────────────────────────────────
    @Test
    fun uiState_initialEmission_isCorrect() = runTest {
        // Assert - initial state should be Loading or Empty
        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(
                initial is NotesViewModel.NotesUiState.Loading ||
                initial is NotesViewModel.NotesUiState.Empty
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── 2 ─────────────────────────────────────────────────────────────────────
    @Test
    fun uiState_emitsNotesAfterRepositoryEmits() = runTest {
        // Arrange
        val sampleNotes = listOf(
            Note(id = "1", title = "Catatan A", content = "Isi A"),
            Note(id = "2", title = "Catatan B", content = "Isi B")
        )

        viewModel.uiState.test {
            awaitItem() // skip initial

            // Act - emit from repository
            notesFlow.value = sampleNotes
            advanceUntilIdle()

            // Assert
            val content = awaitItem() as NotesViewModel.NotesUiState.Content
            assertEquals(2, content.notes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── 3 ─────────────────────────────────────────────────────────────────────
    @Test
    fun uiState_emitsEmptyAfterNotesCleared() = runTest {
        // Arrange - first populate, then clear
        val sampleNotes = listOf(Note(id = "1", title = "A", content = "B"))
        notesFlow.value = sampleNotes
        advanceUntilIdle()

        viewModel.uiState.test {
            val first = awaitItem()
            assertTrue(first is NotesViewModel.NotesUiState.Content)

            // Act - clear notes
            notesFlow.value = emptyList()
            advanceUntilIdle()

            // Assert
            val empty = awaitItem()
            assertTrue(empty is NotesViewModel.NotesUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── 4 ─────────────────────────────────────────────────────────────────────
    @Test
    fun uiState_searchQuery_propagatesToFlow() = runTest {
        // Arrange
        val searchResults = listOf(Note(id = "x", title = "Kotlin", content = "Flow"))
        every { mockRepository.searchNotes("Kotlin") } returns MutableStateFlow(searchResults)

        // Act
        viewModel.updateSearchQuery("Kotlin")
        advanceUntilIdle()

        // Assert
        assertEquals("Kotlin", viewModel.searchQuery.value)
    }
}
