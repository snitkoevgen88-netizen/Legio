package com.example.domain

import com.example.domain.religion.ReligionEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ReligionEngineTest {

    private val sampleRitual = DivineRitual(
        id = "rit_suovetaurilia",
        god = GodType.MARS,
        nameRu = "Суоветаврилия Марсу",
        descriptionRu = "Торжественное жертвоприношение",
        blessingEffectRu = "+18% к шансу Великой Победы",
        costDenarii = 40,
        costProvisions = 35
    )

    private val sampleCohorts = listOf(
        Cohort(id = "coh_1", name = "I Гастаты", morale = 75, soldiers = 120, maxSoldiers = 120)
    )

    @Test
    fun `performRitual sets active blessing and boosts cohort morale`() {
        val resources = LegionResources(denarii = 100, provisions = 100, glory = 10, senateFavor = 50)

        val result = ReligionEngine.performRitual(sampleRitual, resources, sampleCohorts)
        assertTrue(result.isSuccess)
        assertNotNull(result.updatedBlessing)
        assertEquals(GodType.MARS, result.updatedBlessing!!.god)
        assertEquals(2, result.updatedBlessing!!.seasonsRemaining)
        assertEquals(85, result.updatedCohorts[0].morale)
        assertEquals(60, result.updatedResources.denarii)
        assertEquals(65, result.updatedResources.provisions)
    }

    @Test
    fun `performLustratio purifies cohorts boosting morale and discipline`() {
        val resources = LegionResources(denarii = 50, provisions = 50)
        val result = ReligionEngine.performLustratio(resources, sampleCohorts)
        assertNotNull(result)

        val (newRes, cohorts) = result!!
        assertEquals(20, newRes.denarii)
        assertEquals(10, newRes.provisions)
        assertEquals(90, cohorts[0].morale)
    }
}
