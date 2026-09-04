package com.example.domain

import com.example.domain.expeditions.ExpeditionEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExpeditionTest {

    private val sampleExpedition = Expedition(
        id = "exp_etruscan",
        titleRu = "Подавление этрусков",
        historicalContextRu = "Поход на север",
        regionRu = "Этрурия",
        difficulty = 2,
        denariiCost = 25,
        provisionsCost = 35,
        rewardDenarii = 80,
        rewardProvisions = 70,
        rewardGlory = 6,
        scoutIntel = ScoutIntel(
            estimatedEnemyStrengthRu = "Умеренная",
            dangerLevelRu = "Низкая",
            enemyTacticRu = "Засада в холмах",
            recommendedTactic = Tactics.FLANK_AMBUSH
        )
    )

    private val commanders = listOf(
        Commander(id = "cmd_brave", name = "Марк", trait = CommanderTrait.BRAVE, level = 2),
        Commander(id = "cmd_cautious", name = "Гай", trait = CommanderTrait.CAUTIOUS, level = 3),
        Commander(id = "cmd_greedy", name = "Луций", trait = CommanderTrait.GREEDY, level = 2)
    )

    private val cohorts = listOf(
        Cohort(id = "coh_strong", name = "I Когорта", attackPower = 25, defensePower = 20, soldiers = 120, veteransCount = 30),
        Cohort(id = "coh_weak", name = "II Когорта", attackPower = 10, defensePower = 10, soldiers = 60, veteransCount = 5)
    )

    @Test
    fun `canLaunchExpedition and deductDeploymentCosts enforce resource requirements`() {
        val poorRes = LegionResources(denarii = 10, provisions = 10)
        assertFalse(ExpeditionEngine.canLaunchExpedition(sampleExpedition, poorRes))

        val richRes = LegionResources(denarii = 100, provisions = 100)
        assertTrue(ExpeditionEngine.canLaunchExpedition(sampleExpedition, richRes))

        val afterLaunch = ExpeditionEngine.deductDeploymentCosts(sampleExpedition, richRes)
        assertEquals(75, afterLaunch.denarii)
        assertEquals(65, afterLaunch.provisions)
    }

    @Test
    fun `selectOptimalSquad respects priority profiles`() {
        val (milCmd, milCoh, milTac) = ExpeditionEngine.selectOptimalSquad(sampleExpedition, commanders, cohorts, AutoPlanPriority.MILITARY)
        assertEquals("cmd_brave", milCmd?.id)
        assertEquals("coh_strong", milCoh?.id)

        val (recCmd, _, recTac) = ExpeditionEngine.selectOptimalSquad(sampleExpedition, commanders, cohorts, AutoPlanPriority.RECOVERY)
        assertEquals("cmd_cautious", recCmd?.id)
        assertTrue(recTac == Tactics.TESTUDO || recTac == Tactics.CAUTIOUS)
    }

    @Test
    fun `generateSeasonalPlan automatically drafts cohesive seasonal deployment`() {
        val buildings = listOf(
            Building(type = BuildingType.CAMPUS_MARTIUS, level = 1, maxLevel = 3)
        )
        val resources = LegionResources(denarii = 150, provisions = 150)

        val plan = ExpeditionEngine.generateSeasonalPlan(
            priority = AutoPlanPriority.BALANCED,
            buildings = buildings,
            cohorts = cohorts,
            commanders = commanders,
            expeditions = listOf(sampleExpedition),
            resources = resources,
            season = Season.SPRING
        )

        assertNotNull(plan.launchedExpeditionId)
        assertNotNull(plan.selectedCommanderId)
        assertNotNull(plan.selectedCohortId)
        assertEquals(AutoPlanPriority.BALANCED, plan.priority)
    }
}
