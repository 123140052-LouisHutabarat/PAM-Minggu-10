package org.example.project.Navigasi

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Notes    : Screen("notes",    "Notes",    Icons.Default.List)
    object Favorites: Screen("favorites","Favorites",Icons.Default.Favorite)
    object Profile  : Screen("profile",  "Profile",  Icons.Default.Person)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    object AddNote  : Screen("addnote",  "Add Note")

    object NoteDetail : Screen("notedetail/{noteId}", "Note Detail") {
        fun createRoute(noteId: String) = "notedetail/$noteId"
    }
    object EditNote : Screen("editnote/{noteId}", "Edit Note") {
        fun createRoute(noteId: String) = "editnote/$noteId"
    }
}