package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.model.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: GameRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = GameRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `saveSnapshot and loadSnapshot preserves full game state`() = runBlocking {
        val initialUiState = GameUiState(
            seasonYear = SeasonYear(seasonIndex = 2, seasonNumber = 7, yearBc = 278),
            resources = LegionResources(denarii = 350, provisions = 420, glory = 88, senateFavor = 75),
            totalVictories = 12,
            totalGreatVictories = 4,
            totalDefeats = 2,
            longestWinStreak = 6,
            currentWinStreak = 3
        )

        val snapshot = initialUiState.toSnapshot()
        repository.saveSnapshot(snapshot)

        val loadedSnapshot = repository.loadSnapshot()
        assertNotNull(loadedSnapshot)

        val loadedUiState = loadedSnapshot!!.toUiState()
        assertEquals(initialUiState.seasonYear.seasonIndex, loadedUiState.seasonYear.seasonIndex)
        assertEquals(initialUiState.seasonYear.yearBc, loadedUiState.seasonYear.yearBc)
        assertEquals(initialUiState.resources.denarii, loadedUiState.resources.denarii)
        assertEquals(initialUiState.resources.provisions, loadedUiState.resources.provisions)
        assertEquals(initialUiState.resources.glory, loadedUiState.resources.glory)
        assertEquals(initialUiState.resources.senateFavor, loadedUiState.resources.senateFavor)
        assertEquals(initialUiState.totalVictories, loadedUiState.totalVictories)
        assertEquals(initialUiState.totalGreatVictories, loadedUiState.totalGreatVictories)
        assertEquals(initialUiState.longestWinStreak, loadedUiState.longestWinStreak)
    }

    @Test
    fun `clearAllData wipes database cleanly`() = runBlocking {
        val snapshot = GameUiState(
            resources = LegionResources(denarii = 999, provisions = 999, glory = 99, senateFavor = 100)
        ).toSnapshot()

        repository.saveSnapshot(snapshot)
        assertNotNull(repository.loadSnapshot())

        repository.clearAllData()
        val emptySnapshot = repository.loadSnapshot()
        assertNull(emptySnapshot)
    }
}
