package com.example.domain

import com.example.domain.training.TrainingEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class TrainingEngineTest {

    @Test
    fun `updateAllocationCount updates target unit count within bounds`() {
        val trainings = listOf(
            UnitTrainingAllocation(unitType = UnitType.HASTATI, allocatedCount = 20)
        )
        val updated = TrainingEngine.updateAllocationCount(trainings, UnitType.HASTATI, 45)
        assertEquals(45, updated[0].allocatedCount)
    }

    @Test
    fun `autoAllocateBalanced sets standard army recruitment quotas`() {
        val initial = UnitType.entries.map { UnitTrainingAllocation(unitType = it, allocatedCount = 5) }
        val balanced = TrainingEngine.autoAllocateBalanced(initial)

        val hastati = balanced.find { it.unitType == UnitType.HASTATI }
        assertNotNull(hastati)
        assertEquals(30, hastati!!.allocatedCount)
        assertEquals(DrillIntensity.INTENSIVE, hastati.drillIntensity)
    }

    @Test
    fun `replenishCohort restores soldier count when resources are sufficient`() {
        val damagedCohort = Cohort(
            id = "coh_test",
            name = "I Гастаты",
            soldiers = 80,
            maxSoldiers = 120,
            morale = 70
        )
        val resources = LegionResources(denarii = 100, provisions = 150)

        val result = TrainingEngine.replenishCohort(damagedCohort, resources)
        assertNotNull(result)

        val (replenished, newRes) = result!!
        assertEquals(120, replenished.soldiers)
        assertEquals(100 - (40 * 1), newRes.denarii)
        assertEquals(150 - (40 * 2), newRes.provisions)
    }
}
