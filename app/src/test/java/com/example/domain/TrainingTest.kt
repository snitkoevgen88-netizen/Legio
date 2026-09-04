package com.example.domain

import com.example.domain.training.TrainingEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class TrainingTest {

    private val sampleCohort = Cohort(
        id = "coh_1",
        name = "I Гастаты",
        level = 1,
        xp = 0,
        maxXp = 100,
        soldiers = 100,
        maxSoldiers = 120,
        veteransCount = 10,
        morale = 80,
        attackPower = 12,
        defensePower = 10,
        discipline = 75
    )

    private val sampleAllocation = UnitTrainingAllocation(
        unitType = UnitType.HASTATI,
        allocatedCount = 20,
        drillIntensity = DrillIntensity.INTENSIVE,
        targetCohortId = "coh_1"
    )

    @Test
    fun `completeTraining increases cohort strength, attack, and discipline`() {
        val (cohorts, resetAlloc) = TrainingEngine.completeTraining(
            allocation = sampleAllocation,
            cohorts = listOf(sampleCohort),
            doctrines = emptyList()
        )

        val updated = cohorts.first()
        assertEquals(120, updated.soldiers)
        assertTrue(updated.attackPower > sampleCohort.attackPower)
        assertTrue(updated.discipline > sampleCohort.discipline)
        assertFalse(resetAlloc.isTrainingActive)
        assertEquals(20, resetAlloc.totalTrainedSoFar)
    }

    @Test
    fun `autoAllocateBalanced sets appropriate quotas for all unit types`() {
        val initialAllocations = UnitType.entries.map {
            UnitTrainingAllocation(unitType = it, allocatedCount = 10)
        }

        val balanced = TrainingEngine.autoAllocateBalanced(initialAllocations)
        assertEquals(6, balanced.size)
        assertTrue(balanced.find { it.unitType == UnitType.HASTATI }?.allocatedCount == 30)
        assertTrue(balanced.find { it.unitType == UnitType.EQUITES }?.allocatedCount == 10)
    }

    @Test
    fun `conductSeasonalDrill advances cohort XP, level, and discipline with campus rank`() {
        val resources = LegionResources(denarii = 100, provisions = 100)
        val drillResult = TrainingEngine.conductSeasonalDrill(
            cohort = sampleCohort,
            campusLevel = 2,
            resources = resources,
            season = Season.SPRING
        )

        assertNotNull(drillResult)
        val (drilled, newRes) = drillResult!!
        assertTrue(drilled.xp > 0 || drilled.level > 1)
        assertTrue(drilled.discipline > sampleCohort.discipline)
        assertEquals(76, newRes.denarii) // Spring discount: 24 denarii
        assertEquals(85, newRes.provisions) // 15 prov
    }
}
