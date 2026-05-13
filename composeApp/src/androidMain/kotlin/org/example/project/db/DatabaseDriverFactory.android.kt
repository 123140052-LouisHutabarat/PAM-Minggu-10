// File: androidMain/kotlin/org/example/project/db/DatabaseDriverFactory.android.kt
package org.example.project.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        // NotesAppDatabase di-generate SQLDelight dari Note.sq
        // Tidak perlu di-import karena sudah satu package: org.example.project.db
        return AndroidSqliteDriver(
            schema = NotesAppDatabase.Schema,
            context = context,
            name = "notes_app.db"
        )
    }
}