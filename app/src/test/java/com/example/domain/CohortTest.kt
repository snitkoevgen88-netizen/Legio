package com.example.domain

import com.example.domain.cohorts.CohortEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class CohortTest {

    private val sampleCohort = Cohort(
        id = "coh_1",
        name = "I Гастаты Августа",
        level = 2,
        xp = 20,
        maxXp = 100,
        soldiers = 120,
        maxSoldiers = 120,
        veteransCount = 30,
        morale = 90,
        attackPower = 15,
        defensePower = 14,
        discipline = 80
    )

    @Test
    fun `applyBattleCasualties reduces soldiers and veterans proportionally`() {
        val result = CohortEngine.applyBattleCasualties(
            cohort = sampleCohort,
            rawCasualties = 24,
            isSuccess = true,
            isGreatVictory = false,
            expeditionRegion = "Самниум",
            veteranPreservationPct = 0,
            casualtyMitigationPct = 0
        )

        assertEquals(96, result.updatedCohort.soldiers)
        assertEquals(24, result.actualCasualties)
        // Veteran ratio was 30/120 = 25%. 24 * 0.25 = 6 veterans lost
        assertEquals(6, result.veteransLost)
        assertEquals(24, result.updatedCohort.veteransCount)
        assertTrue(result.updatedCohort.morale >= 90)
    }

    @Test
    fun `casualty mitigation and veteran preservation protect cohort core`() {
        val result = CohortEngine.applyBattleCasualties(
            cohort = sampleCohort,
            rawCasualties = 40,
            isSuccess = false,
            isGreatVictory = false,
            expeditionRegion = "Этрурия",
            veteranPreservationPct = 50,
            casualtyMitigationPct = 25
        )

        // 40 * (1 - 0.25) = 30 casualties
        assertEquals(30, result.actualCasualties)
        assertEquals(90, result.updatedCohort.soldiers)
        assertTrue("Veterans saved should be positive", result.veteransSaved > 0)
    }

    @Test
    fun `great victories bestow regional traditions`() {
        val result = CohortEngine.applyBattleCasualties(
            cohort = sampleCohort,
            rawCasualties = 2,
            isSuccess = true,
            isGreatVictory = true,
            expeditionRegion = "Этрурия"
        )

        assertEquals("Победители Этрурия", result.newTradition)
        assertTrue(result.updatedCohort.traditions.contains("Победители Этрурия"))
    }

    @Test
    fun `awardBattleExperience increases veterancy and combat stats on victory`() {
        val victorious = CohortEngine.awardBattleExperience(sampleCohort, 150, isSuccess = true)
        assertEquals(3, victorious.level)
        assertTrue(victorious.veteransCount > sampleCohort.veteransCount)
        assertTrue(victorious.attackPower > sampleCohort.attackPower)
        assertTrue(victorious.discipline > sampleCohort.discipline)
    }

    @Test
    fun `replenishCohort restores soldier count when funds exist`() {
        val depleted = sampleCohort.copy(soldiers = 80)
        val resources = LegionResources(denarii = 100, provisions = 150)

        val outcome = CohortEngine.replenishCohort(depleted, resources)
        assertNotNull(outcome)
        val (restored, newRes) = outcome!!
        assertEquals(120, restored.soldiers)
        // 40 missing soldiers * 1 denarii = 40 denarii spent
        assertEquals(60, newRes.denarii)
        // 40 missing soldiers * 2 prov = 80 prov spent
        assertEquals(70, newRes.provisions)
    }

    @Test
    fun `replenishAll restores all understrength cohorts simultaneously`() {
        val cohorts = listOf(
            sampleCohort.copy(id = "c1", soldiers = 70),
            sampleCohort.copy(id = "c2", soldiers = 90),
            sampleCohort.copy(id = "c3", soldiers = 120)
        )
        val resources = LegionResources(denarii = 200, provisions = 300)

        val (restoredList, newRes) = CohortEngine.replenishAll(cohorts, resources)
        assertEquals(120, restoredList[0].soldiers)
        assertEquals(120, restoredList[1].soldiers)
        assertEquals(120, restoredList[2].soldiers)
        assertTrue(newRes.denarii < 200)
    }
}
