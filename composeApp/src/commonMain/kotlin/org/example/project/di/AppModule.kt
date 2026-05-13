package org.example.project.di

// Aggregator: kumpulkan semua module common ke dalam satu list
// Sesuai README: 4 modules modular (DataModule, NetworkModule, PlatformModule, ViewModelModule)
val allModules = listOf(
    dataModule,
    networkModule,
    platformModule,
    viewModelModule
)
