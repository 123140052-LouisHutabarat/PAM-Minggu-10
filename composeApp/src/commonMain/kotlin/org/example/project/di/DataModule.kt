package org.example.project.di

import org.example.project.data.AppSettings
import org.example.project.data.repository.validation.NoteValidator
import org.koin.dsl.module

val dataModule = module {
    single { NoteValidator() }
    single { AppSettings() }
}