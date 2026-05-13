package org.example.project.di

import org.example.project.ViewModel.NewsViewModel
import org.example.project.ViewModel.NotesViewModel
import org.example.project.ViewModel.SettingsViewModel
import org.koin.dsl.module

val viewModelModule = module {
    // Use factory for ViewModels (created fresh per injection)
    factory { NotesViewModel(get(), get()) }
    factory { SettingsViewModel(get()) }
    factory { NewsViewModel(get()) }
}
