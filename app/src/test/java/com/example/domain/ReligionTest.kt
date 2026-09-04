package com.example.domain

import com.example.domain.religion.ReligionEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ReligionTest {

    @Test
    fun `performing divine ritual confers blessing and boosts morale`() {
        val ritual = DivineRitual(
            id = "rit_mars_ferrum", god = GodType.MARS, nameRu = "Жертвоприношение Марсу Мстителю",
            descriptionRu = "Освящение гладиусов и пилумов", costDenarii = 30, costProvisions = 25,
            blessingEffectRu = "+15% к атаке в бою, +10 к морали"
        )
        val resources = LegionResources(denarii = 100, provisions = 100, glory = 20, senateFavor = 60)
        val cohorts = listOf(
            Cohort(id = "c1", name = "I Когорта", soldiers = 80, maxSoldiers = 80, veteransCount = 20, morale = 70, attackPower = 20, defensePower = 20, discipline = 20, level = 1, xp = 0, maxXp = 100)
        )

        val result = ReligionEngine.performRitual(ritual, resources, cohorts)

        assertTrue(result.isSuccess)
        assertNotNull(result.updatedBlessing)
        assertEquals(GodType.MARS, result.updatedBlessing?.god)
        assertTrue(result.updatedCohorts.first().morale > 70)
        assertEquals(70, result.updatedResources.denarii)
        assertEquals(75, result.updatedResources.provisions)
    }

    @Test
    fun `lustratio purifies standards and increases discipline and morale`() {
        val resources = LegionResources(denarii = 50, provisions = 50, glory = 10, senateFavor = 50)
        val cohorts = listOf(
            Cohort(id = "c1", name = "I Когорта", soldiers = 80, maxSoldiers = 80, veteransCount = 20, morale = 60, attackPower = 20, defensePower = 20, discipline = 50, level = 1, xp = 0, maxXp = 100)
        )

        val result = ReligionEngine.performLustratio(resources, cohorts, costDenarii = 20, costProvisions = 20)
        assertNotNull(result)
        val (newRes, newCohorts) = result!!

        assertEquals(30, newRes.denarii)
        assertEquals(30, newRes.provisions)
        assertTrue(newCohorts.first().discipline > 50)
        assertTrue(newCohorts.first().morale > 60)
    }

    @Test
    fun `ritual fails when resources are insufficient`() {
        val ritual = DivineRitual(
            id = "rit_jupiter_capitolium", god = GodType.JUPITER, nameRu = "Великая суоветаврилия Юпитеру",
            descriptionRu = "Жертва быка, барана и кабана", costDenarii = 50, costProvisions = 50,
            blessingEffectRu = "Защита Орла и благословение Сената"
        )
        val poorResources = LegionResources(denarii = 10, provisions = 10, glory = 0, senateFavor = 50)
        val cohorts = emptyList<Cohort>()

        val result = ReligionEngine.performRitual(ritual, poorResources, cohorts)

        assertFalse("Should fail when resources are below cost", result.isSuccess)
        assertNull(result.updatedBlessing)
    }
}
