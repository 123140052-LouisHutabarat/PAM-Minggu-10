package org.example.project.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.example.project.Note
import org.example.project.db.DatabaseDriverFactory
import org.example.project.db.NotesAppDatabase
import org.example.project.db.Note as NoteEntity
import kotlin.random.Random

class NoteRepository {
    private val database: NotesAppDatabase
    private val queries get() = database.noteQueries

    constructor(driverFactory: DatabaseDriverFactory) {
        database = NotesAppDatabase(driverFactory.createDriver())
    }

    constructor(driver: SqlDriver) {
        database = NotesAppDatabase(driver)
    }

    fun getAllNotes(): Flow<List<Note>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    fun getFavoriteNotes(): Flow<List<Note>> =
        queries.selectFavorites()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    fun searchNotes(query: String): Flow<List<Note>> =
        queries.search(query)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    suspend fun insertNote(title: String, content: String) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            val newId = "id_${Random.nextLong(0, Long.MAX_VALUE)}"
            queries.insert(
                id = newId,
                title = title,
                content = content,
                created_at = now,
                updated_at = now
            )
        }
    }

    suspend fun updateNote(noteId: String, newTitle: String, newContent: String) {
        withContext(Dispatchers.Default) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.update(
                title = newTitle,
                content = newContent,
                updated_at = now,
                id = noteId
            )
        }
    }

    suspend fun toggleFavorite(note: Note, currentFavIds: List<String>) {
        withContext(Dispatchers.Default) {
            val newValue = if (currentFavIds.contains(note.id)) 0L else 1L
            queries.updateFavorite(is_favorite = newValue, id = note.id)
        }
    }

    suspend fun deleteNote(noteId: String) {
        withContext(Dispatchers.Default) {
            queries.delete(noteId)
        }
    }

    private fun NoteEntity.toDomain(): Note = Note(
        id = id,
        title = title,
        content = content,
        isFavorite = is_favorite == 1L
    )
}