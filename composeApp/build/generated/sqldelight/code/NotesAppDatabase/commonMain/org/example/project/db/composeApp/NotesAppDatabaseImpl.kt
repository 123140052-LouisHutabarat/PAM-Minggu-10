package org.example.project.db.composeApp

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass
import org.example.project.db.NoteQueries
import org.example.project.db.NotesAppDatabase

internal val KClass<NotesAppDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = NotesAppDatabaseImpl.Schema

internal fun KClass<NotesAppDatabase>.newInstance(driver: SqlDriver): NotesAppDatabase =
    NotesAppDatabaseImpl(driver)

private class NotesAppDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), NotesAppDatabase {
  override val noteQueries: NoteQueries = NoteQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE Note (
          |    id          TEXT    PRIMARY KEY NOT NULL,
          |    title       TEXT    NOT NULL,
          |    content     TEXT    NOT NULL,
          |    is_favorite INTEGER NOT NULL DEFAULT 0,
          |    created_at  INTEGER NOT NULL,
          |    updated_at  INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null,
          "SELECT Note.id, Note.title, Note.content, Note.is_favorite, Note.created_at, Note.updated_at FROM Note WHERE id = ?",
          0)
      driver.execute(null, "DELETE FROM Note", 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
