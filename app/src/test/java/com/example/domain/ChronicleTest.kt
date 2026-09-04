package com.example.domain

import com.example.domain.chronicle.ChronicleEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ChronicleTest {

    @Test
    fun `chronicle records major victories, disasters, promotions and trials`() {
        val expedition = Expedition(
            id = "exp_major",
            titleRu = "Битва при Сентине",
            historicalContextRu = "Pugna Sentinas",
            regionRu = "Умбрия",
            difficulty = 4,
            denariiCost = 50,
            provisionsCost = 60,
            rewardDenarii = 150,
            rewardProvisions = 100,
            rewardGlory = 25,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Громадная",
                dangerLevelRu = "Смертельная",
                enemyTacticRu = "Яростный навал",
                recommendedTactic = Tactics.TESTUDO
            ),
            isSenateTrial = true
        )

        // Case 1: Great victory
        val shouldRecordGreat = ChronicleEngine.shouldRecordExpedition(
            expedition = expedition, outcome = ExpeditionOutcome.GREAT_VICTORY,
            casualties = 10, commanderPromoted = false, commanderKilled = false
        )
        assertTrue("Great victory must be recorded", shouldRecordGreat)

        // Case 2: Disaster
        val shouldRecordDisaster = ChronicleEngine.shouldRecordExpedition(
            expedition = expedition, outcome = ExpeditionOutcome.DISASTER,
            casualties = 50, commanderPromoted = false, commanderKilled = false
        )
        assertTrue("Disaster must be recorded", shouldRecordDisaster)

        // Case 3: Commander promoted
        val shouldRecordPromo = ChronicleEngine.shouldRecordExpedition(
            expedition = expedition, outcome = ExpeditionOutcome.VICTORY,
            casualties = 5, commanderPromoted = true, commanderKilled = false
        )
        assertTrue("Commander promotion must be recorded", shouldRecordPromo)
    }

    @Test
    fun `chronicle filters out minor routine skirmishes without significance`() {
        val minorExpedition = Expedition(
            id = "exp_minor",
            titleRu = "Мелкая стычка с пастухами",
            historicalContextRu = "Velitatio Minor",
            regionRu = "Лаций",
            difficulty = 1,
            denariiCost = 5,
            provisionsCost = 5,
            rewardDenarii = 10,
            rewardProvisions = 10,
            rewardGlory = 1,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Слабая",
                dangerLevelRu = "Низкая",
                enemyTacticRu = "Беспорядочный набег",
                recommendedTactic = Tactics.BALANCED
            ),
            isSenateTrial = false
        )

        val shouldRecordRoutine = ChronicleEngine.shouldRecordExpedition(
            expedition = minorExpedition, outcome = ExpeditionOutcome.PARTIAL_SUCCESS,
            casualties = 2, commanderPromoted = false, commanderKilled = false
        )

        assertFalse("Routine minor skirmishes should not spam the annals", shouldRecordRoutine)
    }

    @Test
    fun `recording aquila loss creates impactful chronicle entry`() {
        val seasonYear = SeasonYear(seasonIndex = 1, seasonNumber = 2, yearBc = 310)
        val entry = ChronicleEngine.recordAquilaEvent(
            seasonYear = seasonYear,
            isLost = true,
            headline = "💀 Орел легиона захвачен врагами",
            description = "В кровопролитной сече аквилифер пал, и штандарт попал к галлам.",
            gloryDelta = -35
        )

        assertEquals(ExpeditionOutcome.DISASTER, entry.outcome)
        assertEquals(-35, entry.gloryEarned)
        assertTrue(entry.headlineRu.contains("захвачен"))
    }
}
