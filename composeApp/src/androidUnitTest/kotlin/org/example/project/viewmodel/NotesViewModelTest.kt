package org.example.project.viewmodel

import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockRepository: NoteRepository
    private lateinit var mockValidator: NoteValidator
    private lateinit var viewModel: NotesViewModel

    private val sampleNote = Note(id = "id_1", title = "Catatan Test", content = "Isi Test")
    private val notesFlow  = MutableStateFlow<List<Note>>(emptyList())
    private val favFlow    = MutableStateFlow<List<Note>>(emptyList())

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
        mockValidator  = mockk()

        every { mockRepository.getAllNotes() }      returns notesFlow
        every { mockRepository.getFavoriteNotes() } returns favFlow
        every { mockRepository.searchNotes(any()) } returns flowOf(emptyList())

        viewModel = NotesViewModel(
            repository = mockRepository,
            validator  = mockValidator
        )
    }

    @Test
    fun addNote_validInput_callsRepository() = runTest {
        every { mockValidator.validateDetailed(any()) } returns
                ValidationResult(isValid = true)

        viewModel.addNote("Judul Valid", "Konten Valid")
        advanceUntilIdle()

        coVerify { mockRepository.insertNote("Judul Valid", "Konten Valid") }
    }

    @Test
    fun addNote_blankTitle_doesNotCallRepository() = runTest {
        every { mockValidator.validateDetailed(any()) } returns
                ValidationResult(isValid = false, errors = listOf("Judul tidak boleh kosong"))

        viewModel.addNote("", "Konten")
        advanceUntilIdle()

        coVerify(exactly = 0) { mockRepository.insertNote(any(), any()) }
    }

    @Test
    fun addNote_titleTooLong_emitsSnackbarError() = runTest {
        val errorMsg = "Judul tidak boleh lebih dari 100 karakter"
        every { mockValidator.validateDetailed(any()) } returns
                ValidationResult(isValid = false, errors = listOf(errorMsg))

        val messages = mutableListOf<String>()
        // backgroundScope agar collector aktif sebelum emit
        backgroundScope.launch(mainDispatcherRule.testDispatcher) {
            viewModel.snackbarMessage.collect { messages.add(it) }
        }

        viewModel.addNote("A".repeat(101), "Konten")
        advanceUntilIdle()

        assertEquals(1, messages.size)
        assertEquals(errorMsg, messages[0])
    }

    @Test
    fun confirmDeleteNote_callsRepositoryDelete() = runTest {
        viewModel.requestDeleteNote(sampleNote)
        viewModel.confirmDeleteNote()
        advanceUntilIdle()

        coVerify { mockRepository.deleteNote(sampleNote.id) }
    }

    @Test
    fun updateSearchQuery_updatesUiState() = runTest {
        viewModel.updateSearchQuery("Kotlin")
        advanceUntilIdle()

        assertEquals("Kotlin", viewModel.searchQuery.value)
    }

    @Test
    fun toggleFavorite_callsRepository() = runTest {
        viewModel.toggleFavorite(sampleNote)
        advanceUntilIdle()

        coVerify { mockRepository.toggleFavorite(sampleNote, any()) }
    }

    @Test
    fun requestDeleteNote_setsDialogState() = runTest {
        assertNull(viewModel.showDeleteDialog.value)

        viewModel.requestDeleteNote(sampleNote)

        assertNotNull(viewModel.showDeleteDialog.value)
        assertEquals(sampleNote.id, viewModel.showDeleteDialog.value?.id)
    }

    @Test
    fun dismissDeleteDialog_clearsDialogState() = runTest {
        viewModel.requestDeleteNote(sampleNote)
        assertNotNull(viewModel.showDeleteDialog.value)

        viewModel.dismissDeleteDialog()

        assertNull(viewModel.showDeleteDialog.value)
    }
}