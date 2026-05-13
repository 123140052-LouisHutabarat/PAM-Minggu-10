# PAM Notes - Pertemuan 10: Testing & Dependency Injection

Tugas Praktikum Pertemuan 10 — Mata Kuliah Pengembangan Aplikasi Mobile  
Program Studi Teknik Informatika, Institut Teknologi Sumatera  
Tahun Akademik Genap 2025/2026

---

## Data Mahasiswa

| Field | Keterangan |
|-------|------------|
| Nama | Louis Hutabarat |
| NIM | 123140052 |
| Kelas | IF25-22017 |
| Branch | `week-10` |

---

## Tentang Tugas Ini

Tugas ini merupakan kelanjutan dari project PAM Notes yang sebelumnya sudah terintegrasi dengan AI Gemini. Pada pertemuan 10, fokus pengerjaan adalah meningkatkan kualitas kode melalui dua hal utama:

- **Dependency Injection** menggunakan Koin — modul yang sebelumnya tergabung dalam satu file `AppModule` dipecah menjadi 4 modul terpisah berdasarkan tanggung jawabnya masing-masing
- **Testing** — mencakup unit test, flow test, UI test, serta pengukuran code coverage menggunakan JaCoCo

---

## Struktur Modul Koin

Modul DI dipecah menjadi 4 file agar lebih terorganisir:

```
di/
├── AppModule.kt       → menggabungkan semua modul (aggregator)
├── DataModule.kt      → database, repository, settings, validator
├── NetworkModule.kt   → HTTP client, API service
├── PlatformModule.kt  → implementasi platform (device info, network monitor)
└── ViewModelModule.kt → semua ViewModel
```

Inisialisasi di `MyApplication.kt`:

```kotlin
startKoin {
    androidContext(this@MyApplication)
    modules(
        androidPlatformModule(),
        *allModules.toTypedArray()
    )
}
```

---

## Hasil Testing

Total: **39 test — BUILD SUCCESSFUL**

```
./gradlew :composeApp:testDebugUnitTest
BUILD SUCCESSFUL
37 actionable tasks: 20 executed, 17 from cache
```

### Rincian per File Test

| File | Lokasi | Jumlah |
|------|--------|--------|
| `NoteValidatorTest.kt` | `commonTest` | 10 |
| `NoteRepositoryTest.kt` | `androidUnitTest` | 8 |
| `NotesViewModelTest.kt` | `androidUnitTest` | 8 |
| `NotesViewModelFlowTest.kt` | `androidUnitTest` | 4 |
| `NotesUiTest.kt` | `androidUnitTest` | 5 |
| `KoinModulesTest.kt` | `androidUnitTest` | 3 |
| **Total** | | **39** |

### Detail Tiap Test

**NoteValidatorTest** — validasi business logic murni (pure Kotlin, tanpa dependency)

| No | Nama Test |
|----|-----------|
| 1 | `valid note returns true` |
| 2 | `empty title returns false` |
| 3 | `blank title (whitespace only) returns false` |
| 4 | `title at max length is valid` |
| 5 | `title over max length returns false` |
| 6 | `validate throws ValidationException for too-long title` |
| 7 | `validate throws ValidationException for blank title` |
| 8 | `content over max length is invalid` |
| 9 | `validateDetailed returns multiple errors for multiple violations` |
| 10 | `isValidTitle helper - simple cases` |

**NoteRepositoryTest** — menggunakan in-memory SQLite, tanpa emulator

| No | Nama Test |
|----|-----------|
| 1 | `insertNote_savesAndReturnsInGetAll` |
| 2 | `getAllNotes_emptyInitially` |
| 3 | `updateNote_changesTitleAndContent` |
| 4 | `deleteNote_removesFromDatabase` |
| 5 | `toggleFavorite_flipsFavoriteStatus` |
| 6 | `searchNotes_filtersByQuery` |
| 7 | `getFavoriteNotes_returnsOnlyFavorites` |
| 8 | `getAllNotes_emitsUpdatesReactively` |

**NotesViewModelTest** — mocking dengan MockK

| No | Nama Test |
|----|-----------|
| 1 | `addNote_validInput_callsRepository` |
| 2 | `addNote_blankTitle_doesNotCallRepository` |
| 3 | `addNote_titleTooLong_emitsSnackbarError` |
| 4 | `confirmDeleteNote_callsRepositoryDelete` |
| 5 | `updateSearchQuery_updatesUiState` |
| 6 | `toggleFavorite_callsRepository` |
| 7 | `requestDeleteNote_setsDialogState` |
| 8 | `dismissDeleteDialog_clearsDialogState` |

**NotesViewModelFlowTest** — verifikasi emisi StateFlow dengan Turbine

| No | Nama Test |
|----|-----------|
| 1 | `uiState_initialEmission_isCorrect` |
| 2 | `uiState_emitsNotesAfterRepositoryEmits` |
| 3 | `uiState_emitsEmptyAfterNotesCleared` |
| 4 | `uiState_searchQuery_propagatesToFlow` |

**NotesUiTest** — UI testing dengan Compose + Robolectric (berjalan di JVM)

| No | Nama Test |
|----|-----------|
| 1 | `noteCard_displaysTitleAndContent` |
| 2 | `noteCard_hasFavoriteAndDeleteButtons` |
| 3 | `noteCard_deleteButton_triggersCallback` |
| 4 | `noteCard_favoriteButton_triggersCallback` |
| 5 | `emptyStateView_displaysTitleAndSubtitle` |

**KoinModulesTest** — verifikasi struktur DI graph

| No | Nama Test |
|----|-----------|
| 1 | `noteValidator_canBeResolvedFromKoin` |
| 2 | `allModules_aggregatorHasAtLeastFourModules` |
| 3 | `dataModule_isPartOfAllModules` |

---

## Cara Menjalankan

**Prasyarat:** JDK 11+, Android SDK, koneksi internet (untuk download dependency pertama kali)

Buat file `local.properties` di root project:
```properties
sdk.dir=C:\\Users\\<USERNAME>\\AppData\\Local\\Android\\Sdk
GEMINI_API_KEY=isi_bebas_karena_test_tidak_memanggil_API
```

Jalankan semua test:
```bash
./gradlew :composeApp:testDebugUnitTest
```

Generate laporan coverage:
```bash
./gradlew :composeApp:testDebugUnitTest :composeApp:jacocoTestReport
```

Laporan HTML test: `composeApp/build/reports/tests/testDebugUnitTest/index.html`  
Laporan coverage: `composeApp/build/reports/jacoco/jacocoTestReport/html/index.html`

---

## Dependencies yang Ditambahkan

```kotlin
// commonTest
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
implementation("app.cash.turbine:turbine:1.1.0")
implementation("io.insert-koin:koin-test:4.1.0")

// androidUnitTest
implementation("junit:junit:4.13.2")
implementation("io.mockk:mockk:1.13.13")
implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
implementation("io.insert-koin:koin-test-junit4:4.1.0")
implementation("org.robolectric:robolectric:4.11.1")
implementation("androidx.compose.ui:ui-test-junit4-android:1.7.5")
implementation("androidx.compose.ui:ui-test-manifest:1.7.5")
implementation("androidx.test.ext:junit:1.1.5")
```

---

## Pemenuhan Rubrik

| Komponen | Target | Capaian |
|----------|--------|---------|
| Koin DI (min. 2 modul) | 2+ modul | 4 modul |
| Repository Test | 5+ kasus | 8 kasus |
| ViewModel Test + MockK | 4+ test | 8 test |
| Flow Test + Turbine | 2+ test | 4 test |
| UI Test + Compose | 3+ test | 5 test |
| Code Quality (AAA pattern) | clean | terpenuhi |

---

## Screenshots

### Hasil Test Report


![Test Report](https://github.com/user-attachments/assets/fca6666b-84ac-4201-b5a5-9336c6b9ed2f)

### Code Coverage


![Coverage Report](https://github.com/user-attachments/assets/327b1f58-cf6e-4907-bbc5-093629578c46)

---

## Video Demo

Link: _https://drive.google.com/file/d/1Qp60Yy_pdI-3g9l117NaHSRIqOebBqUh/view?usp=drive_link_

---

## Referensi

- [Koin](https://insert-koin.io/docs)
- [MockK](https://mockk.io)
- [Turbine](https://github.com/cashapp/turbine)
- [Robolectric](https://robolectric.org)
- [Compose UI Testing](https://developer.android.com/jetpack/compose/testing)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
