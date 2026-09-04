package com.example.domain

import com.example.domain.economy.EconomyEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class EconomyTest {

    @Test
    fun `army size generates proper maintenance costs for food and pay`() {
        val cohorts = listOf(
            Cohort(id = "c1", name = "I Когорта", soldiers = 80, maxSoldiers = 80, veteransCount = 20, morale = 100, attackPower = 20, defensePower = 20, discipline = 20, level = 1, xp = 0, maxXp = 100),
            Cohort(id = "c2", name = "II Когорта", soldiers = 70, maxSoldiers = 80, veteransCount = 10, morale = 90, attackPower = 22, defensePower = 22, discipline = 20, level = 1, xp = 0, maxXp = 100)
        )
        val commanders = listOf(
            Commander(id = "cmd1", name = "Марк", level = 1, xp = 10, maxXp = 100, rankTitle = "Центурион", expeditionsLed = 0, victoriesCount = 0, greatVictoriesCount = 0, defeatsCount = 0, isAlive = true, moodStatus = "Готов к бою")
        )
        val buildings = listOf(
            Building(type = BuildingType.PRINCIPIA, level = 1, maxLevel = 3),
            Building(type = BuildingType.HORREUM, level = 2, maxLevel = 3),
            Building(type = BuildingType.TABULARIUM, level = 2, maxLevel = 3)
        )

        val breakdown = EconomyEngine.calculateMaintenance(cohorts, commanders, buildings)

        assertEquals(150, breakdown.totalSoldiers)
        assertEquals(30, breakdown.totalVeterans)
        assertTrue("Provisions consumed must be greater than 0", breakdown.netProvisionsConsumed > 0)
        assertTrue("Denarii maintenance must be greater than 0", breakdown.netDenariiMaintenance > 0)
        assertTrue("Tabularium must provide discount", breakdown.tabulariumDiscountDenarii > 0)
    }

    @Test
    fun `replenishing cohort casualties costs denarii and provisions`() {
        val cohort = Cohort(
            id = "c1", name = "I Когорта",
            soldiers = 60, maxSoldiers = 80, veteransCount = 15,
            morale = 75, attackPower = 20, defensePower = 20, discipline = 20,
            level = 1, xp = 0, maxXp = 100
        )
        val initialResources = LegionResources(denarii = 100, provisions = 100, glory = 20, senateFavor = 50)

        val result = EconomyEngine.replenishCohort(
            cohort = cohort,
            recruitsCount = 20,
            resources = initialResources,
            campLevel = 2,
            hasPopularesDiscount = false
        )

        assertTrue("Replenishment must succeed", result.isSuccess)
        assertEquals(80, result.updatedState.soldiers)
        assertTrue("Treasury should decrease", result.updatedResources.denarii < initialResources.denarii)
        assertTrue("Provisions should decrease", result.updatedResources.provisions < initialResources.provisions)
    }

    @Test
    fun `replenishing fails when treasury or provisions are insufficient`() {
        val cohort = Cohort(
            id = "c1", name = "I Когорта",
            soldiers = 50, maxSoldiers = 80, veteransCount = 10,
            morale = 70, attackPower = 20, defensePower = 20, discipline = 20,
            level = 1, xp = 0, maxXp = 100
        )
        val bankruptResources = LegionResources(denarii = 2, provisions = 2, glory = 0, senateFavor = 50)

        val result = EconomyEngine.replenishCohort(
            cohort = cohort,
            recruitsCount = 30,
            resources = bankruptResources
        )

        assertFalse("Replenishment must fail due to lack of funds", result.isSuccess)
        assertNotNull(result.errorMessageRu)
    }

    @Test
    fun `logistics genius talent reduces expedition supply cost`() {
        val expedition = Expedition(
            id = "exp1",
            titleRu = "Поход в Самний",
            historicalContextRu = "Expeditio Samnitica",
            regionRu = "Самний",
            difficulty = 2,
            denariiCost = 40,
            provisionsCost = 50,
            rewardDenarii = 80,
            rewardProvisions = 60,
            rewardGlory = 10,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Умеренная",
                dangerLevelRu = "Средняя",
                enemyTacticRu = "Фаланга",
                recommendedTactic = Tactics.FLANK_AMBUSH
            )
        )

        val standardCmd = Commander(
            id = "cmd1", name = "Гай", level = 1, xp = 0, maxXp = 100,
            rankTitle = "Центурион", expeditionsLed = 0, victoriesCount = 0,
            greatVictoriesCount = 0, defeatsCount = 0, isAlive = true,
            moodStatus = "Готов", unlockedTalents = emptyList()
        )

        val logisticsCmd = standardCmd.copy(
            unlockedTalents = listOf(OfficerTalent.LOGISTICS_GENIUS)
        )

        val (standardDenarii, standardProvisions) = EconomyEngine.calculateExpeditionSupplyCost(expedition, standardCmd)
        val (discountDenarii, discountProvisions) = EconomyEngine.calculateExpeditionSupplyCost(expedition, logisticsCmd)

        assertEquals(expedition.denariiCost, standardDenarii)
        assertEquals(expedition.provisionsCost, standardProvisions)
        assertTrue("Logistics talent should reduce provisions cost", discountProvisions < standardProvisions)
    }

    @Test
    fun `seasonal yields account for buildings, harvest, and senate stipends`() {
        val buildings = listOf(
            Building(type = BuildingType.PRINCIPIA, level = 2, maxLevel = 3),
            Building(type = BuildingType.HORREUM, level = 3, maxLevel = 3)
        )
        val autumnYear = SeasonYear(seasonIndex = 2, seasonNumber = 3, yearBc = 310) // Autumn

        val yield = EconomyEngine.calculateSeasonalYield(
            buildings = buildings,
            investments = emptyList(),
            seasonYear = autumnYear,
            senateFavor = 80
        )

        assertTrue("Autumn harvest should yield ample provisions", yield.totalProvisionsIncome >= 80)
        assertTrue("Senate stipend should be awarded based on favor", yield.senateStipendDenarii > 0)
        assertTrue("Total denarii income must include stipend and buildings", yield.totalDenariiIncome >= yield.senateStipendDenarii)
    }
}
