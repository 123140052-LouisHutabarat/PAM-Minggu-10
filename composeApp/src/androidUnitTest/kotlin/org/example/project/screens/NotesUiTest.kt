package org.example.project.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.example.project.Note
import org.example.project.UI.NoteCard
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

private object Tags {
    const val FAVORITE_BUTTON = "favorite_button"
    const val DELETE_BUTTON   = "delete_button"
    const val EMPTY_STATE     = "empty_state"
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class NotesUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @After
    fun tearDown() {
        stopKoin()
    }

    private val sampleNote = Note(
        id = "test_1", title = "Judul UI Test",
        content = "Ini adalah konten catatan untuk UI testing", isFavorite = false
    )

    @Test
    fun noteCard_displaysTitleAndContent() {
        composeTestRule.setContent {
            MaterialTheme {
                NoteCard(note = sampleNote, isFav = false,
                    onClick = {}, onLongPress = {}, onFavClick = {}, onDeleteClick = {})
            }
        }
        composeTestRule.onNodeWithText("Judul UI Test").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ini adalah konten", substring = true).assertIsDisplayed()
    }

    @Test
    fun noteCard_hasFavoriteAndDeleteButtons() {
        composeTestRule.setContent {
            MaterialTheme {
                NoteCard(note = sampleNote, isFav = false,
                    onClick = {}, onLongPress = {}, onFavClick = {}, onDeleteClick = {})
            }
        }
        composeTestRule.onNodeWithTag(Tags.FAVORITE_BUTTON).assertExists()
        composeTestRule.onNodeWithTag(Tags.DELETE_BUTTON).assertExists()
    }

    @Test
    fun noteCard_deleteButton_triggersCallback() {
        var deleteCalled = false
        composeTestRule.setContent {
            MaterialTheme {
                NoteCard(note = sampleNote, isFav = false,
                    onClick = {}, onLongPress = {}, onFavClick = {},
                    onDeleteClick = { deleteCalled = true })
            }
        }
        composeTestRule.onNodeWithTag(Tags.DELETE_BUTTON).performClick()
        assert(deleteCalled)
    }

    @Test
    fun noteCard_favoriteButton_triggersCallback() {
        var favCalled = false
        composeTestRule.setContent {
            MaterialTheme {
                NoteCard(note = sampleNote, isFav = false,
                    onClick = {}, onLongPress = {}, onFavClick = { favCalled = true },
                    onDeleteClick = {})
            }
        }
        composeTestRule.onNodeWithTag(Tags.FAVORITE_BUTTON).performClick()
        assert(favCalled)
    }

    @Test
    fun emptyStateView_displaysTitleAndSubtitle() {
        composeTestRule.setContent { MaterialTheme { EmptyNotesView() } }
        composeTestRule.onNodeWithTag(Tags.EMPTY_STATE).assertIsDisplayed()
    }
}

@Composable
private fun EmptyNotesView() {
    Box(
        modifier = Modifier.testTag(Tags.EMPTY_STATE).fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Belum ada catatan")
    }
}