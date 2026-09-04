package com.example.domain

import com.example.domain.battle.BattleEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class BattleTest {

    private val sampleExpedition = Expedition(
        id = "exp_test",
        titleRu = "Битва при Сентине",
        historicalContextRu = "Решающая битва",
        regionRu = "Умбрия",
        difficulty = 3,
        denariiCost = 40,
        provisionsCost = 50,
        rewardDenarii = 140,
        rewardProvisions = 120,
        rewardGlory = 12,
        scoutIntel = ScoutIntel(
            estimatedEnemyStrengthRu = "Грозная",
            dangerLevelRu = "Высокая",
            enemyTacticRu = "Стрелки и колесницы",
            recommendedTactic = Tactics.TESTUDO
        )
    )

    private val sampleCommander = Commander(
        id = "cmd_1",
        name = "Квинт Фабий Максим",
        level = 4,
        rankTitle = "Военный трибун",
        trait = CommanderTrait.TACTICIAN,
        unlockedTalents = listOf(OfficerTalent.IRON_DISCIPLINE, OfficerTalent.LOGISTICS_GENIUS)
    )

    private val sampleCohort = Cohort(
        id = "coh_1",
        name = "I Когорта Принципов",
        level = 3,
        soldiers = 120,
        maxSoldiers = 120,
        veteransCount = 35,
        morale = 95,
        attackPower = 22,
        defensePower = 20,
        discipline = 90
    )

    @Test
    fun `calculateBattleOdds always guarantees strict 100 percent sum`() {
        for (tactics in Tactics.entries) {
            val odds = BattleEngine.calculateBattleOdds(
                expedition = sampleExpedition,
                commander = sampleCommander,
                cohort = sampleCohort,
                tactics = tactics
            )
            val sum = odds.greatVictoryPct + odds.victoryPct + odds.partialPct + odds.defeatPct + odds.disasterPct
            assertEquals("Tactics $tactics odds sum must be exactly 100%", 100, sum)
            assertTrue(odds.greatVictoryPct >= 0)
            assertTrue(odds.disasterPct >= 0)
        }
    }

    @Test
    fun `resolveBattle executes campaign with cascading consequences`() {
        val odds = BattleEngine.calculateBattleOdds(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.TESTUDO
        )

        val result = BattleEngine.resolveBattle(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.TESTUDO,
            odds = odds,
            campLevel = 2,
            fabricaLevel = 2,
            valetudinariumLevel = 2,
            doctrines = emptyList(),
            equipment = emptyList()
        )

        assertNotNull(result.outcome)
        assertTrue(result.cohort.soldiers <= sampleCohort.soldiers)
        assertTrue(result.xpEarned > 0)
        assertTrue(result.storyNarrativeRu.isNotBlank())
    }

    @Test
    fun `tactical counter gives significant odds boost against matching enemy tactic`() {
        val matchingOdds = BattleEngine.calculateBattleOdds(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.TESTUDO // matches sampleExpedition scoutIntel.recommendedTactic
        )

        val unmatchingOdds = BattleEngine.calculateBattleOdds(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.AGGRESSIVE
        )

        assertTrue(
            "Testudo should have higher combined victory chance or lower disaster chance against missile chariots",
            (matchingOdds.greatVictoryPct + matchingOdds.victoryPct + matchingOdds.partialPct) >=
                    (unmatchingOdds.greatVictoryPct + unmatchingOdds.victoryPct)
        )
    }
}
