package com.example.domain

import com.example.domain.events.EventEngine
import com.example.domain.expeditions.ExpeditionEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExpeditionAndEventEngineTest {

    @Test
    fun `ExpeditionEngine checks launch requirements and deducts costs`() {
        val expedition = Expedition(
            id = "exp_etru",
            titleRu = "Поход в Этрурию",
            historicalContextRu = "Поход",
            regionRu = "Этрурия",
            difficulty = 1,
            denariiCost = 25,
            provisionsCost = 35,
            rewardDenarii = 60,
            rewardProvisions = 70,
            rewardGlory = 4,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Низкая",
                dangerLevelRu = "Минимальная",
                enemyTacticRu = "Натиск",
                recommendedTactic = Tactics.BALANCED
            )
        )

        val poorResources = LegionResources(denarii = 10, provisions = 10)
        assertFalse(ExpeditionEngine.canLaunchExpedition(expedition, poorResources))

        val richResources = LegionResources(denarii = 100, provisions = 100)
        assertTrue(ExpeditionEngine.canLaunchExpedition(expedition, richResources))

        val deducted = ExpeditionEngine.deductDeploymentCosts(expedition, richResources)
        assertEquals(75, deducted.denarii)
        assertEquals(65, deducted.provisions)
    }

    @Test
    fun `EventEngine resolves event choices with resource effects`() {
        val choice = CampEventChoice(
            textRu = "Одарить легионеров зерном",
            effectDescRu = "-20 провианта, +5 славы",
            provisionsDelta = -20,
            gloryDelta = 5,
            moraleDelta = 10,
            resultLogRu = "Легионеры довольны щедростью полководца!"
        )

        val initialRes = LegionResources(denarii = 100, provisions = 100, glory = 10, senateFavor = 50)
        val initialCohorts = listOf(Cohort(id = "c1", name = "I Гастаты", morale = 60))

        val result = EventEngine.resolveChoice(choice, initialRes, initialCohorts)
        assertEquals(80, result.updatedResources.provisions)
        assertEquals(15, result.updatedResources.glory)
        assertEquals(70, result.updatedCohorts[0].morale)
    }
}
