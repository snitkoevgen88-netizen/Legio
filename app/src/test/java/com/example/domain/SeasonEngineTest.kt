package com.example.domain

import com.example.data.GameDefaults
import com.example.domain.season.SeasonEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class SeasonEngineTest {

    @Test
    fun `advanceSeason advances season and year correctly`() {
        val springState = GameUiState(
            seasonYear = SeasonYear(seasonIndex = 0, seasonNumber = 1, yearBc = 280),
            resources = LegionResources(denarii = 100, provisions = 200, glory = 10, senateFavor = 50),
            buildings = GameDefaults.createInitialBuildings(),
            cohorts = GameDefaults.createInitialCohorts()
        )

        val result = SeasonEngine.advanceSeason(springState)

        assertEquals(1, result.newSeasonYear.seasonIndex) // Summer
        assertEquals(Season.SUMMER, result.newSeasonYear.season)
        assertEquals(280, result.newSeasonYear.yearBc)
        assertTrue("Provisions must be calculated after food consumption", result.updatedResources.provisions >= 0)
        assertTrue("Denarii must be calculated after building yields", result.updatedResources.denarii >= 0)
    }

    @Test
    fun `advanceSeason rolls year BC when transitioning from winter to spring`() {
        val winterState = GameUiState(
            seasonYear = SeasonYear(seasonIndex = 3, seasonNumber = 4, yearBc = 280),
            resources = LegionResources(denarii = 100, provisions = 200, glory = 10, senateFavor = 50),
            buildings = GameDefaults.createInitialBuildings(),
            cohorts = GameDefaults.createInitialCohorts()
        )

        val result = SeasonEngine.advanceSeason(winterState)

        assertEquals(0, result.newSeasonYear.seasonIndex) // Spring
        assertEquals(Season.SPRING, result.newSeasonYear.season)
        assertEquals(279, result.newSeasonYear.yearBc) // 280 BC -> 279 BC
    }
}
