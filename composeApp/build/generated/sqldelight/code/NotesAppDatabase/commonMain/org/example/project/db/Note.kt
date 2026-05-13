package org.example.project.db

import kotlin.Long
import kotlin.String

public data class Note(
  public val id: String,
  public val title: String,
  public val content: String,
  public val is_favorite: Long,
  public val created_at: Long,
  public val updated_at: Long,
)
