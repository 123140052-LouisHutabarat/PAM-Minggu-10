package org.example.project.data.repository.validation

import org.example.project.Note

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)

class ValidationException(message: String) : Exception(message)

class NoteValidator {

    companion object {
        const val MAX_TITLE_LENGTH = 100
        const val MAX_CONTENT_LENGTH = 5000
    }

    fun isValid(note: Note): Boolean {
        return isValidTitle(note.title) && note.content.length <= MAX_CONTENT_LENGTH
    }

    fun isValidTitle(title: String): Boolean {
        if (title.isBlank()) return false
        if (title.length > MAX_TITLE_LENGTH) return false
        return true
    }

    fun validateDetailed(note: Note): ValidationResult {
        val errors = mutableListOf<String>()

        if (note.title.isBlank()) {
            errors.add("Judul tidak boleh kosong")
        } else if (note.title.length > MAX_TITLE_LENGTH) {
            errors.add("Judul tidak boleh lebih dari $MAX_TITLE_LENGTH karakter")
        }

        if (note.content.length > MAX_CONTENT_LENGTH) {
            errors.add("Konten tidak boleh lebih dari $MAX_CONTENT_LENGTH karakter")
        }

        return ValidationResult(isValid = errors.isEmpty(), errors = errors)
    }

    fun validate(note: Note): Note {
        if (note.title.isBlank()) {
            throw ValidationException("Judul tidak boleh kosong")
        }
        if (note.title.length > MAX_TITLE_LENGTH) {
            throw ValidationException("Judul tidak boleh lebih dari $MAX_TITLE_LENGTH karakter")
        }
        if (note.content.length > MAX_CONTENT_LENGTH) {
            throw ValidationException("Konten tidak boleh lebih dari $MAX_CONTENT_LENGTH karakter")
        }
        return note
    }
}
