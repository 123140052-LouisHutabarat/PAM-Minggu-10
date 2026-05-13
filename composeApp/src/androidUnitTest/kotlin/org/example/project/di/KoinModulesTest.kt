package org.example.project.di

import org.example.project.data.repository.validation.NoteValidator
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KoinModulesTest : KoinTest {

    @Before
    fun setUp() {
        startKoin { modules(dataModule) }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun noteValidator_canBeResolvedFromKoin() {
        val validator: NoteValidator by inject()
        assertNotNull(validator)
    }

    @Test
    fun allModules_aggregatorHasAtLeastFourModules() {
        assertTrue(allModules.size >= 4, "Expected at least 4 modules, got ${allModules.size}")
    }

    @Test
    fun dataModule_isPartOfAllModules() {
        assertTrue(allModules.contains(dataModule), "dataModule harus ada dalam allModules")
    }
}