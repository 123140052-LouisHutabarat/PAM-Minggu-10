package org.example.project.data.repository.validation

import org.example.project.Note
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoteValidatorTest {

    private val validator = NoteValidator()

    // ── 1 ─────────────────────────────────────────────────────────────────────
    @Test
    fun `valid note returns true`() {
        // Arrange
        val note = Note(id = "1", title = "Judul Normal", content = "Isi catatan yang normal")
        // Act
        val result = validator.isValid(note)
        // Assert
        assertTrue(result)
    }

    // ── 2 ─────────────────────────────────────────────────────────────────────
    @Test
    fun `empty title returns false`() {
        // Arrange
        val note = Note(id = "1", title = "", content = "Isi catatan")
        // Act
        val result = validator.isValid(note)
        // Assert
        assertFalse(result)
    }

    // ── 3 ─────────────────────────────────────────────────────────────────────
    @Test
    fun `blank title (whitespace only) returns false`() {
        // Arrange
        val note = Note(id = "1", title = "   ", content = "Isi catatan")
        // Act
        val result = validator.isValid(note)
        // Assert
        assertFalse(result)
    }

    // ── 4 ─────────────────────────────────────────────────────────────────────
    @Test
    fun `title at max length is valid`() {
        // Arrange
        val title = "A".repeat(NoteValidator.MAX_TITLE_LENGTH) // 100 chars
        val note = Note(id = "1", title = title, content = "Isi catatan")
        // Act
        val result = validator.isValid(note)
        // Assert
        assertTrue(result)
    }

    // ── 5 ─────────────────────────────────────────────────────────────────────
    @Test
    fun `title over max length returns false`() {
        // Arrange
        val title = "A".repeat(NoteValidator.MAX_TITLE_LENGTH + 1) // 101 chars
        val note = Note(id = "1", title = title, content = "Isi catatan")
        // Act
        val result = validator.isValid(note)
        // Assert
        assertFalse(result)
    }

    // ── 6 ─────────────────────────────────────────────────────────────────────
    @Test
    fun `validate throws ValidationException for too-long title`() {
        // Arrange
        val title = "X".repeat(NoteValidator.MAX_TITLE_LENGTH + 1)
        val note = Note(id = "1", title = title, content = "Isi")
        // Act & Assert
        val ex = assertFailsWith<ValidationException> { validator.validate(note) }
        assertTrue(ex.message!!.contains("100"))
    }

    // ── 7 ─────────────────────────────────────────────────────────────────────
    @Test
    fun `validate throws ValidationException for blank title`() {
        // Arrange
        val note = Note(id = "1", title = "   ", content = "Isi catatan")
        // Act & Assert
        val ex = assertFailsWith<ValidationException> { validator.validate(note) }
        assertTrue(ex.message!!.isNotBlank())
    }

    // ── 8 ─────────────────────────────────────────────────────────────────────
    @Test
    fun `content over max length is invalid`() {
        // Arrange
        val content = "C".repeat(NoteValidator.MAX_CONTENT_LENGTH + 1) // > 5000
        val note = Note(id = "1", title = "Judul Valid", content = content)
        // Act
        val result = validator.isValid(note)
        // Assert
        assertFalse(result)
    }

    // ── 9 ─────────────────────────────────────────────────────────────────────
    @Test
    fun `validateDetailed returns multiple errors for multiple violations`() {
        // Arrange
        val title   = "T".repeat(NoteValidator.MAX_TITLE_LENGTH + 1)
        val content = "C".repeat(NoteValidator.MAX_CONTENT_LENGTH + 1)
        val note = Note(id = "1", title = title, content = content)
        // Act
        val result = validator.validateDetailed(note)
        // Assert
        assertFalse(result.isValid)
        assertTrue(result.errors.size >= 2)
    }

    // ── 10 ────────────────────────────────────────────────────────────────────
    @Test
    fun `isValidTitle helper - simple cases`() {
        // Arrange & Act & Assert
        assertFalse(validator.isValidTitle(""))
        assertFalse(validator.isValidTitle("  "))
        assertTrue(validator.isValidTitle("Hello"))
        assertFalse(validator.isValidTitle("A".repeat(NoteValidator.MAX_TITLE_LENGTH + 1)))
        assertTrue(validator.isValidTitle("A".repeat(NoteValidator.MAX_TITLE_LENGTH)))
    }
}
