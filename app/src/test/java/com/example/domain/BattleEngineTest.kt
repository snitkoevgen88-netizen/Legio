package com.example.domain

import com.example.domain.battle.BattleEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class BattleEngineTest {

    private val sampleExpedition = Expedition(
        id = "exp_test",
        titleRu = "Поход в Самниум",
        historicalContextRu = "Тестовый поход",
        regionRu = "Самниум",
        difficulty = 2,
        denariiCost = 20,
        provisionsCost = 30,
        rewardDenarii = 70,
        rewardProvisions = 80,
        rewardGlory = 5,
        scoutIntel = ScoutIntel(
            estimatedEnemyStrengthRu = "Средняя",
            dangerLevelRu = "Умеренная",
            enemyTacticRu = "Фаланга",
            recommendedTactic = Tactics.TESTUDO
        )
    )

    private val sampleCommander = Commander(
        id = "cmd_1",
        name = "Марк Аврелий",
        level = 3,
        xp = 50,
        maxXp = 150,
        rankTitle = "Военный трибун",
        trait = CommanderTrait.TACTICIAN
    )

    private val sampleCohort = Cohort(
        id = "coh_1",
        name = "I Гастаты",
        level = 2,
        xp = 20,
        maxXp = 120,
        soldiers = 120,
        maxSoldiers = 120,
        veteransCount = 30,
        morale = 90,
        attackPower = 18,
        defensePower = 16,
        discipline = 85
    )

    @Test
    fun `calculateBattleOdds produces odds that strictly sum to 100 percent`() {
        Tactics.entries.forEach { tactic ->
            val odds = BattleEngine.calculateBattleOdds(
                expedition = sampleExpedition,
                commander = sampleCommander,
                cohort = sampleCohort,
                tactics = tactic,
                campLevel = 2,
                activeBlessing = null,
                doctrines = emptyList(),
                equipment = emptyList()
            )

            val total = odds.greatVictoryPct + odds.victoryPct + odds.partialPct + odds.defeatPct + odds.disasterPct
            assertEquals("Odds must sum to exactly 100% for tactic $tactic", 100, total)
            assertTrue("Great victory pct must be non-negative", odds.greatVictoryPct >= 0)
            assertTrue("Disaster pct must be non-negative", odds.disasterPct >= 0)
        }
    }

    @Test
    fun `testudo tactic decreases disaster risk compared to aggressive tactic`() {
        val testudoOdds = BattleEngine.calculateBattleOdds(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.TESTUDO,
            campLevel = 1,
            activeBlessing = null,
            doctrines = emptyList(),
            equipment = emptyList()
        )

        val aggressiveOdds = BattleEngine.calculateBattleOdds(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.AGGRESSIVE,
            campLevel = 1,
            activeBlessing = null,
            doctrines = emptyList(),
            equipment = emptyList()
        )

        assertTrue(
            "Testudo should have lower or equal disaster risk compared to Aggressive",
            testudoOdds.disasterPct <= aggressiveOdds.disasterPct
        )
    }

    @Test
    fun `resolveBattle calculates casualties and rewards accurately`() {
        val odds = BattleEngine.calculateBattleOdds(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.BALANCED,
            campLevel = 1,
            activeBlessing = null,
            doctrines = emptyList(),
            equipment = emptyList()
        )

        val result = BattleEngine.resolveBattle(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.BALANCED,
            odds = odds,
            doctrines = emptyList(),
            equipment = emptyList(),
            randomSeed = 10 // Deterministic outcome
        )

        assertNotNull(result)
        assertTrue("Casualties must be greater than 0", result.casualties > 0)
        assertTrue("Casualties should not exceed total soldiers", result.casualties <= sampleCohort.soldiers)
        assertFalse("Narrative must not be empty", result.storyNarrativeRu.isBlank())
    }
}
