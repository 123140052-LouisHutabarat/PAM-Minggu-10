package org.example.project.UI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import org.example.project.Note
import org.example.project.Navigasi.Screen
import org.example.project.ViewModel.NotesViewModel
import org.example.project.ViewModel.SettingsViewModel
import org.example.project.db.BatteryInfo
import org.example.project.db.DeviceInfo
import org.example.project.ui.testing.TestTags
import org.koin.compose.koinInject

@Composable
fun NoteCard(
    note: Note,
    isFav: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onFavClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { testTag = TestTags.NOTE_CARD }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() }, onLongPress = { onLongPress() })
            },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    note.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { testTag = TestTags.NOTE_TITLE }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    note.content,
                    maxLines = 2,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier.semantics { testTag = TestTags.NOTE_CONTENT }
                )
            }
            IconButton(
                onClick = onFavClick,
                modifier = Modifier.semantics { testTag = TestTags.FAVORITE_BUTTON }
            ) {
                Icon(
                    if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorit",
                    tint = if (isFav) Color.Red else Color.Gray
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.semantics { testTag = TestTags.DELETE_BUTTON }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    navController: NavController,
    notesList: List<Note>,
    favList: List<Note>,
    viewModel: NotesViewModel,
    settingsViewModel: SettingsViewModel? = null
) {
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val settingsState = settingsViewModel?.uiState?.collectAsState()?.value

    val sortedList = remember(notesList, settingsState) {
        when (settingsState?.sortOrder) {
            "oldest" -> notesList.sortedBy { it.id }
            "title"  -> notesList.sortedBy { it.title }
            else     -> notesList
        }.let { list ->
            if (settingsState?.showFavoritesFirst == true)
                list.sortedByDescending { note -> favList.any { it.id == note.id } }
            else list
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.onSearchQueryChange(it) },
            onClear = { viewModel.clearSearch() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            when (uiState) {
                is NotesViewModel.NotesUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is NotesViewModel.NotesUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.NoteAlt, null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (searchQuery.isBlank()) "Belum ada catatan.\nTambah catatan baru!"
                                else "Catatan '${searchQuery}' tidak ditemukan.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 15.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                is NotesViewModel.NotesUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp, top = 4.dp)
                    ) {
                        items(sortedList, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                isFav = favList.any { it.id == note.id },
                                onClick = { navController.navigate(Screen.NoteDetail.createRoute(note.id)) },
                                onLongPress = { noteToDelete = note },
                                onFavClick = { viewModel.toggleFavorite(note) }
                            )
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddNote.route) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Catatan")
            }
        }
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Hapus Catatan?") },
            text = { Text("Hapus '${noteToDelete!!.title}' secara permanen?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteNote(noteToDelete!!.id); noteToDelete = null }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Batal", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query, onValueChange = onQueryChange, modifier = modifier,
        placeholder = { Text("Cari catatan...") },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotEmpty()) {
                IconButton(onClick = onClear) { Icon(Icons.Default.Clear, null) }
            }
        },
        singleLine = true, shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun FavoritesScreen(navController: NavController, favList: List<Note>, viewModel: NotesViewModel) {
    if (favList.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FavoriteBorder, null, Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))
                Text("Belum ada catatan favorit.\nTap ❤ pada catatan untuk menambahkan.",
                    color = Color.Gray, fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)) {
            items(favList, key = { it.id }) { note ->
                NoteCard(note = note, isFav = true,
                    onClick = { navController.navigate(Screen.NoteDetail.createRoute(note.id)) },
                    onLongPress = {}, onFavClick = { viewModel.toggleFavorite(note) })
            }
        }
    }
}

@Composable
fun AddNoteScreen(onNoteSaved: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it },
            label = { Text("Judul Catatan") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = content, onValueChange = { content = it },
            label = { Text("Isi Catatan") },
            modifier = Modifier.fillMaxWidth().weight(1f),
            minLines = 5, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onNoteSaved(title, content) },
            enabled = title.isNotBlank() && content.isNotBlank(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Save, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp)); Text("Simpan Catatan")
        }
    }
}

@Composable
fun NoteDetailContent(noteData: Note?, navController: NavController) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (noteData == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Data tidak ditemukan", color = Color.Gray)
            }
        } else {
            Text(noteData.title, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            Text(noteData.content, fontSize = 16.sp, modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground, lineHeight = 24.sp)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.navigate(Screen.EditNote.createRoute(noteData.id)) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text("Edit Catatan")
            }
        }
    }
}

@Composable
fun EditNoteScreen(noteData: Note?, onNoteSaved: (String, String) -> Unit) {
    var title by remember { mutableStateOf(noteData?.title ?: "") }
    var content by remember { mutableStateOf(noteData?.content ?: "") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it },
            label = { Text("Judul Catatan") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = content, onValueChange = { content = it },
            label = { Text("Isi Catatan") },
            modifier = Modifier.fillMaxWidth().weight(1f),
            minLines = 5, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onNoteSaved(title, content) },
            enabled = title.isNotBlank() && content.isNotBlank(),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Save, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp)); Text("Update Catatan")
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state      by viewModel.uiState.collectAsState()
    val deviceInfo: DeviceInfo = koinInject()
    val batteryInfo: BatteryInfo = koinInject()

    val batteryLevel = remember { batteryInfo.getBatteryLevel() }
    val isCharging   = remember { batteryInfo.isCharging() }

    val batteryColor = when {
        batteryLevel >= 60 -> Color(0xFF2E7D32)
        batteryLevel >= 30 -> Color(0xFFF57F17)
        else               -> Color(0xFFC62828)
    }

    val cardBg   = MaterialTheme.colorScheme.surface
    val onCardBg = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Pengaturan Aplikasi", fontWeight = FontWeight.Bold, fontSize = 20.sp,
            color = onCardBg)
        Spacer(Modifier.height(20.dp))

        SettingsSectionHeader("Informasi Perangkat")
        Spacer(Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(16.dp)) {

                DeviceInfoRow(Icons.Default.PhoneAndroid, "Perangkat",
                    deviceInfo.getDeviceName(), onCardBg)
                RowDivider()

                DeviceInfoRow(Icons.Default.Android, "Sistem Operasi",
                    deviceInfo.getOsVersion(), onCardBg)
                RowDivider()

                DeviceInfoRow(Icons.Default.AppSettingsAlt, "Versi Aplikasi",
                    deviceInfo.getAppVersion(), onCardBg)
                RowDivider()

                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = if (isCharging) Icons.Default.BatteryChargingFull
                        else Icons.Default.Battery5Bar,
                        contentDescription = null, tint = batteryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Baterai", fontSize = 12.sp, color = onCardBg.copy(alpha = 0.6f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$batteryLevel%", fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold, color = batteryColor)
                            if (isCharging) {
                                Spacer(Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF2E7D32).copy(alpha = 0.15f)) {
                                    Text("Mengisi daya", fontSize = 11.sp,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { batteryLevel / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = batteryColor,
                    trackColor = batteryColor.copy(alpha = 0.2f)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SettingsSectionHeader("Tampilan")
        Spacer(Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(4.dp)) {
            SettingsToggleItem(
                icon = Icons.Default.DarkMode, title = "Mode Gelap",
                subtitle = "Aktifkan tema gelap", checked = state.isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode(it) }
            )
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        SettingsSectionHeader("Catatan")
        Spacer(Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(4.dp)) {
            Column {
                SettingsToggleItem(
                    icon = Icons.Default.Favorite, title = "Favorit di Atas",
                    subtitle = "Tampilkan catatan favorit di urutan pertama",
                    checked = state.showFavoritesFirst,
                    onCheckedChange = { viewModel.toggleShowFavoritesFirst(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("Urutan Catatan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        Triple("newest", Icons.Default.AccessTime,  "Terbaru"),
                        Triple("oldest", Icons.Default.History,     "Terlama"),
                        Triple("title",  Icons.Default.SortByAlpha, "Abjad (A-Z)")
                    ).forEach { (value, icon, label) ->
                        SortOrderItem(
                            icon = { Icon(icon, null, Modifier.size(20.dp), tint = onCardBg) },
                            label = label, selected = state.sortOrder == value,
                            onClick = { viewModel.setSortOrder(value) }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RowDivider() = HorizontalDivider(
    modifier = Modifier.padding(vertical = 10.dp),
    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
)

@Composable
private fun DeviceInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, value: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = contentColor.copy(alpha = 0.6f))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = contentColor)
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f,
            androidx.compose.ui.unit.TextUnitType.Sp))
}

@Composable
private fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String, subtitle: String, checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ))
    }
}

@Composable
private fun SortOrderItem(icon: @Composable () -> Unit, label: String,
                          selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(8.dp)); icon()
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}