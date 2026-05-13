package org.example.project.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Unit
import org.example.project.db.composeApp.newInstance
import org.example.project.db.composeApp.schema

public interface NotesAppDatabase : Transacter {
  public val noteQueries: NoteQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = NotesAppDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): NotesAppDatabase =
        NotesAppDatabase::class.newInstance(driver)
  }
}
