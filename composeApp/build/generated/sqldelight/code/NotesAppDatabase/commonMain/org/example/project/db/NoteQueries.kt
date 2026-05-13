package org.example.project.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class NoteQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAll(mapper: (
    id: String,
    title: String,
    content: String,
    is_favorite: Long,
    created_at: Long,
    updated_at: Long,
  ) -> T): Query<T> = Query(-1_832_652_852, arrayOf("Note"), driver, "Note.sq", "selectAll",
      "SELECT Note.id, Note.title, Note.content, Note.is_favorite, Note.created_at, Note.updated_at FROM Note ORDER BY updated_at DESC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!
    )
  }

  public fun selectAll(): Query<Note> = selectAll { id, title, content, is_favorite, created_at,
      updated_at ->
    Note(
      id,
      title,
      content,
      is_favorite,
      created_at,
      updated_at
    )
  }

  public fun <T : Any> selectFavorites(mapper: (
    id: String,
    title: String,
    content: String,
    is_favorite: Long,
    created_at: Long,
    updated_at: Long,
  ) -> T): Query<T> = Query(-1_865_067_454, arrayOf("Note"), driver, "Note.sq", "selectFavorites",
      "SELECT Note.id, Note.title, Note.content, Note.is_favorite, Note.created_at, Note.updated_at FROM Note WHERE is_favorite = 1 ORDER BY updated_at DESC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!
    )
  }

  public fun selectFavorites(): Query<Note> = selectFavorites { id, title, content, is_favorite,
      created_at, updated_at ->
    Note(
      id,
      title,
      content,
      is_favorite,
      created_at,
      updated_at
    )
  }

  public fun <T : Any> search(query: String, mapper: (
    id: String,
    title: String,
    content: String,
    is_favorite: Long,
    created_at: Long,
    updated_at: Long,
  ) -> T): Query<T> = SearchQuery(query) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getLong(4)!!,
      cursor.getLong(5)!!
    )
  }

  public fun search(query: String): Query<Note> = search(query) { id, title, content, is_favorite,
      created_at, updated_at ->
    Note(
      id,
      title,
      content,
      is_favorite,
      created_at,
      updated_at
    )
  }

  public fun insert(
    id: String,
    title: String,
    content: String,
    created_at: Long,
    updated_at: Long,
  ) {
    driver.execute(-1_317_586_094, """
        |INSERT INTO Note(id, title, content, is_favorite, created_at, updated_at)
        |VALUES (?, ?, ?, 0, ?, ?)
        """.trimMargin(), 5) {
          bindString(0, id)
          bindString(1, title)
          bindString(2, content)
          bindLong(3, created_at)
          bindLong(4, updated_at)
        }
    notifyQueries(-1_317_586_094) { emit ->
      emit("Note")
    }
  }

  public fun update(
    title: String,
    content: String,
    updated_at: Long,
    id: String,
  ) {
    driver.execute(-972_639_902,
        """UPDATE Note SET title = ?, content = ?, updated_at = ? WHERE id = ?""", 4) {
          bindString(0, title)
          bindString(1, content)
          bindLong(2, updated_at)
          bindString(3, id)
        }
    notifyQueries(-972_639_902) { emit ->
      emit("Note")
    }
  }

  public fun updateFavorite(is_favorite: Long, id: String) {
    driver.execute(-1_250_665_186, """UPDATE Note SET is_favorite = ? WHERE id = ?""", 2) {
          bindLong(0, is_favorite)
          bindString(1, id)
        }
    notifyQueries(-1_250_665_186) { emit ->
      emit("Note")
    }
  }

  public fun delete(id: String) {
    driver.execute(-1_469_252_028, """DELETE FROM Note WHERE id = ?""", 1) {
          bindString(0, id)
        }
    notifyQueries(-1_469_252_028) { emit ->
      emit("Note")
    }
  }

  private inner class SearchQuery<out T : Any>(
    public val query: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("Note", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("Note", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_040_130_495, """
    |SELECT Note.id, Note.title, Note.content, Note.is_favorite, Note.created_at, Note.updated_at FROM Note
    |WHERE title LIKE '%' || ? || '%'
    |   OR content LIKE '%' || ? || '%'
    |ORDER BY updated_at DESC
    """.trimMargin(), mapper, 2) {
      bindString(0, query)
      bindString(1, query)
    }

    override fun toString(): String = "Note.sq:search"
  }
}
