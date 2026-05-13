package org.example.project.UI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.example.project.Model.Article
import org.example.project.Model.ProfileUiState
import org.example.project.Navigasi.Screen
import org.example.project.ViewModel.NewsViewModel
import org.example.project.ViewModel.NotesViewModel
import org.example.project.ViewModel.ProfileViewModel
import org.example.project.ViewModel.SettingsViewModel
import org.example.project.db.NetworkMonitor
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {

    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val notesViewModel    = koinViewModel<NotesViewModel>()
    val profileViewModel  = remember { ProfileViewModel() }
    val newsViewModel     = remember { NewsViewModel() }

    val profileUiState by profileViewModel.uiState.collectAsState()
    val currentNotes   by notesViewModel.notes.collectAsState()
    val favoriteNotes  by notesViewModel.favoriteNotes.collectAsState()
    val settingsState  by settingsViewModel.uiState.collectAsState()

    val isDarkMode = settingsState.isDarkMode

    val myColorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = Color(0xFF90CAF9), onPrimary = Color(0xFF003258),
            surface = Color(0xFF1E1E1E), background = Color(0xFF121212)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF1976D2), onPrimary = Color.White,
            surface = Color.White, background = Color(0xFFF5F5F5)
        )
    }

    MaterialTheme(colorScheme = myColorScheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = currentRoute in listOf(
            Screen.Notes.route, Screen.Favorites.route, Screen.Profile.route,
            Screen.Settings.route, "news"
        )

        var selectedArticle by remember { mutableStateOf<Article?>(null) }

        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(when (currentRoute) {
                                "news"                 -> "Berita Teknologi"
                                "newsDetail"           -> selectedArticle?.source?.name ?: "Detail Berita"
                                Screen.Notes.route     -> "Notes App"
                                Screen.Favorites.route -> "Catatan Favorit"
                                Screen.Profile.route   -> "Profil"
                                Screen.Settings.route  -> "Pengaturan"
                                Screen.AddNote.route   -> "Tambah Catatan"
                                else                   -> "Aplikasi"
                            })
                        },
                        navigationIcon = {
                            if (!showBottomBar) {
                                IconButton(onClick = { navController.navigateUp() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    NetworkStatusIndicator()
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Article, contentDescription = "News") },
                            label = { Text("News") }, selected = currentRoute == "news",
                            onClick = { navController.navigate("news") { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
                        )
                        NavigationBarItem(
                            icon = { Icon(Screen.Notes.icon ?: Icons.Default.Home, contentDescription = "Notes") },
                            label = { Text("Notes") }, selected = currentRoute == Screen.Notes.route,
                            onClick = { navController.navigate(Screen.Notes.route) { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
                        )
                        NavigationBarItem(
                            icon = { Icon(Screen.Favorites.icon ?: Icons.Default.Favorite, contentDescription = "Favorites") },
                            label = { Text("Favs") }, selected = currentRoute == Screen.Favorites.route,
                            onClick = { navController.navigate(Screen.Favorites.route) { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
                        )
                        NavigationBarItem(
                            icon = { Icon(Screen.Settings.icon ?: Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }, selected = currentRoute == Screen.Settings.route,
                            onClick = { navController.navigate(Screen.Settings.route) { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
                        )
                        NavigationBarItem(
                            icon = { Icon(Screen.Profile.icon ?: Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") }, selected = currentRoute == Screen.Profile.route,
                            onClick = { navController.navigate(Screen.Profile.route) { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(navController = navController, startDestination = "news", modifier = Modifier.padding(padding)) {
                composable("news") {
                    NewsListScreen(viewModel = newsViewModel, onArticleClick = { article -> selectedArticle = article; navController.navigate("newsDetail") })
                }
                composable("newsDetail") {
                    NewsDetailScreen(article = selectedArticle, onBackClick = { navController.navigateUp() })
                }
                composable(Screen.Notes.route) {
                    NotesListScreen(navController = navController, notesList = currentNotes, favList = favoriteNotes, viewModel = notesViewModel, settingsViewModel = settingsViewModel)
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(navController = navController, favList = favoriteNotes, viewModel = notesViewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = settingsViewModel)
                }
                composable(Screen.Profile.route) {
                    MyProfileApp(uiState = profileUiState, viewModel = profileViewModel)
                }
                composable(Screen.AddNote.route) {
                    AddNoteScreen(onNoteSaved = { title, content -> notesViewModel.addNote(title, content); navController.navigateUp() })
                }
                composable(route = Screen.NoteDetail.route, arguments = listOf(navArgument("noteId") { type = NavType.StringType })) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                    NoteDetailContent(noteData = currentNotes.find { it.id == noteId }, navController = navController)
                }
                composable(route = Screen.EditNote.route, arguments = listOf(navArgument("noteId") { type = NavType.StringType })) { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                    EditNoteScreen(noteData = currentNotes.find { it.id == noteId }, onNoteSaved = { newTitle, newContent -> notesViewModel.updateNote(noteId, newTitle, newContent); navController.navigateUp() })
                }
            }
        }
    }
}

@Composable
fun NetworkStatusIndicator() {
    val networkMonitor: NetworkMonitor = koinInject()
    val isConnected by networkMonitor.observeConnectivity()
        .collectAsState(initial = true)

    AnimatedVisibility(visible = !isConnected) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFB00020)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tidak ada koneksi internet",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun MyProfileApp(uiState: ProfileUiState, viewModel: ProfileViewModel) {
    val containerColor = if (uiState.isDarkMode) Color(0xFF1E1E1E) else Color.White

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        if (uiState.isEditing) {
            EditProfileForm(
                name = uiState.name, bio = uiState.bio, email = uiState.email,
                phone = uiState.phone, loc = uiState.location, isDarkMode = uiState.isDarkMode,
                onSave = { n, b, e, p, l -> viewModel.saveProfile(n, b, e, p, l) },
                onCancel = { viewModel.setEditingMode(false) }
            )
        } else {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor)
            ) {
                ProfileCard(
                    name = uiState.name, bio = uiState.bio, email = uiState.email,
                    phone = uiState.phone, loc = uiState.location,
                    isDarkMode = uiState.isDarkMode,
                    onEdit = { viewModel.setEditingMode(true) }
                )
            }
        }
    }
}

@Composable
fun EditProfileForm(name: String, bio: String, email: String, phone: String, loc: String, isDarkMode: Boolean, onSave: (String, String, String, String, String) -> Unit, onCancel: () -> Unit) {
    var n by remember { mutableStateOf(name) }
    var b by remember { mutableStateOf(bio) }
    var e by remember { mutableStateOf(email) }
    var p by remember { mutableStateOf(phone) }
    var l by remember { mutableStateOf(loc) }
    val containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(8.dp)) {
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Edit Profil", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = textColor))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth(), minLines = 2, textStyle = androidx.compose.ui.text.TextStyle(color = textColor))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = e, onValueChange = { e = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = textColor))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = p, onValueChange = { p = it }, label = { Text("Telepon") }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = textColor))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = l, onValueChange = { l = it }, label = { Text("Lokasi") }, modifier = Modifier.fillMaxWidth(), textStyle = androidx.compose.ui.text.TextStyle(color = textColor))
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Batal", color = MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onSave(n, b, e, p, l) }) { Text("Simpan") }
            }
        }
    }
}

@Composable
fun ProfileCard(
    name: String, bio: String, email: String, phone: String,
    loc: String, isDarkMode: Boolean = false,
    onEdit: () -> Unit
) {
    val textColor = if (isDarkMode) Color.White else Color.Black

    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            KamelImage(
                resource = asyncPainterResource("https://ui-avatars.com/api/?name=${name.replace(" ", "+")}&size=200&background=1976D2&color=fff&bold=true"),
                contentDescription = "Foto Profil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onLoading = { CircularProgressIndicator(Modifier.size(40.dp)) },
                onFailure = {
                    Box(Modifier.fillMaxSize().clip(CircleShape), contentAlignment = Alignment.Center) {
                        Text(
                            text = name.split(" ").take(2).joinToString("") { it.first().uppercase() },
                            color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(name, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = textColor)
        Text(bio, textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        InfoItem(Icons.Default.Email, email, textColor)
        InfoItem(Icons.Default.Phone, phone, textColor)
        InfoItem(Icons.Default.LocationOn, loc, textColor)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth(0.8f)) { Text("Edit Profile") }
    }
}

@Composable
fun InfoItem(icon: ImageVector, text: String, textColor: Color = Color.Black) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = textColor)
    }
}