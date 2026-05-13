package org.example.project.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.example.project.Note
import org.example.project.data.NoteRepository
import org.example.project.db.NotesAppDatabase
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoteRepositoryTest {

    private lateinit var repository: NoteRepository

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        NotesAppDatabase.Schema.create(driver)
        repository = NoteRepository(driver)   // pakai secondary constructor
    }

    @Test
    fun insertNote_savesAndReturnsInGetAll() = runTest {
        repository.insertNote("Judul Test", "Konten Test")
        repository.getAllNotes().test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("Judul Test", notes[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllNotes_emptyInitially() = runTest {
        repository.getAllNotes().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateNote_changesTitleAndContent() = runTest {
        repository.insertNote("Judul Lama", "Konten Lama")
        var noteId = ""
        repository.getAllNotes().test {
            noteId = awaitItem().first().id
            cancelAndIgnoreRemainingEvents()
        }
        repository.updateNote(noteId, "Judul Baru", "Konten Baru")
        repository.getAllNotes().test {
            val updated = awaitItem().first()
            assertEquals("Judul Baru", updated.title)
            assertEquals("Konten Baru", updated.content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteNote_removesFromDatabase() = runTest {
        repository.insertNote("Catatan Hapus", "Konten")
        var noteId = ""
        repository.getAllNotes().test {
            noteId = awaitItem().first().id
            cancelAndIgnoreRemainingEvents()
        }
        repository.deleteNote(noteId)
        repository.getAllNotes().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleFavorite_flipsFavoriteStatus() = runTest {
        repository.insertNote("Catatan Fav", "Isi")
        var note = Note("", "", "")
        repository.getAllNotes().test {
            note = awaitItem().first()
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(note.isFavorite)
        repository.toggleFavorite(note, emptyList())
        repository.getAllNotes().test {
            assertTrue(awaitItem().first().isFavorite)
            cancelAndIgnoreRemainingEvents()
        }
        repository.toggleFavorite(note, listOf(note.id))
        repository.getAllNotes().test {
            assertFalse(awaitItem().first().isFavorite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchNotes_filtersByQuery() = runTest {
        repository.insertNote("Belajar Kotlin", "Coroutines dan Flow")
        repository.insertNote("Makan Siang", "Nasi goreng")
        repository.searchNotes("Kotlin").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("Belajar Kotlin", results[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getFavoriteNotes_returnsOnlyFavorites() = runTest {
        repository.insertNote("Normal Note", "Isi normal")
        repository.insertNote("Favorit Note", "Isi favorit")
        var favNote = Note("", "", "")
        repository.getAllNotes().test {
            favNote = awaitItem().first { it.title == "Favorit Note" }
            cancelAndIgnoreRemainingEvents()
        }
        repository.toggleFavorite(favNote, emptyList())
        repository.getFavoriteNotes().test {
            val favs = awaitItem()
            assertEquals(1, favs.size)
            assertEquals("Favorit Note", favs[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllNotes_emitsUpdatesReactively() = runTest {
        repository.getAllNotes().test {
            assertTrue(awaitItem().isEmpty())
            repository.insertNote("Reaktif", "Isi")
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}