package com.example.domain

import com.example.domain.senate.SenateEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class SenateTest {

    @Test
    fun `accepting senate quest activates the mandate`() {
        val quest = SenateQuest(
            id = "q1", titleRu = "Покорение Лация", issuerRu = "Консул Фабий",
            descriptionRu = "Разбейте восставших", rewardDenarii = 100,
            rewardSenateFavor = 10, rewardGlory = 5, targetType = "VICTORIES",
            targetCount = 2, status = QuestStatus.AVAILABLE
        )
        val initialResources = LegionResources(denarii = 100, provisions = 100, glory = 20, senateFavor = 50)

        val (updatedQuest, updatedResources) = SenateEngine.handleQuestDecision(
            quest, SenateQuestDecision.ACCEPT, initialResources
        )

        assertEquals(QuestStatus.ACTIVE, updatedQuest.status)
        assertEquals(50, updatedResources.senateFavor)
    }

    @Test
    fun `declining senate quest imposes political favor penalty`() {
        val quest = SenateQuest(
            id = "q1", titleRu = "Покорение Лация", issuerRu = "Консул Фабий",
            descriptionRu = "Разбейте восставших", rewardDenarii = 100,
            rewardSenateFavor = 10, rewardGlory = 5, targetType = "VICTORIES",
            targetCount = 2, status = QuestStatus.AVAILABLE
        )
        val initialResources = LegionResources(denarii = 100, provisions = 100, glory = 20, senateFavor = 50)

        val (updatedQuest, updatedResources) = SenateEngine.handleQuestDecision(
            quest, SenateQuestDecision.DECLINE, initialResources
        )

        assertEquals(QuestStatus.DECLINED, updatedQuest.status)
        assertTrue("Favor should decrease after refusing mandate", updatedResources.senateFavor < initialResources.senateFavor)
    }

    @Test
    fun `negotiating senate quest grants advance treasury and increases rewards and targets`() {
        val quest = SenateQuest(
            id = "q1", titleRu = "Покорение Лация", issuerRu = "Консул Фабий",
            descriptionRu = "Разбейте восставших", rewardDenarii = 100,
            rewardSenateFavor = 10, rewardGlory = 5, targetType = "VICTORIES",
            targetCount = 5, status = QuestStatus.AVAILABLE
        )
        val initialResources = LegionResources(denarii = 50, provisions = 50, glory = 20, senateFavor = 50)

        val (updatedQuest, updatedResources) = SenateEngine.handleQuestDecision(
            quest, SenateQuestDecision.NEGOTIATE, initialResources
        )

        assertEquals(QuestStatus.NEGOTIATED, updatedQuest.status)
        assertTrue("Target count should increase", updatedQuest.targetCount > quest.targetCount)
        assertTrue("Reward denarii should increase", updatedQuest.rewardDenarii > quest.rewardDenarii)
        assertTrue("Advance denarii must be added to treasury", updatedResources.denarii > initialResources.denarii)
        assertTrue("Advance provisions must be added", updatedResources.provisions > initialResources.provisions)
    }

    @Test
    fun `delaying senate quest extends deadline with minor diplomatic concession`() {
        val quest = SenateQuest(
            id = "q1", titleRu = "Покорение Лация", issuerRu = "Консул Фабий",
            descriptionRu = "Разбейте восставших", rewardDenarii = 100,
            rewardSenateFavor = 10, rewardGlory = 5, targetType = "VICTORIES",
            targetCount = 2, status = QuestStatus.AVAILABLE, deadlineSeasonsRemaining = 2
        )
        val initialResources = LegionResources(denarii = 50, provisions = 50, glory = 20, senateFavor = 50)

        val (updatedQuest, updatedResources) = SenateEngine.handleQuestDecision(
            quest, SenateQuestDecision.DELAY, initialResources
        )

        assertEquals(QuestStatus.DELAYED, updatedQuest.status)
        assertEquals(4, updatedQuest.deadlineSeasonsRemaining)
        assertEquals(48, updatedResources.senateFavor)
    }

    @Test
    fun `senate reaction evaluates great victory positively and disaster with severe penalty`() {
        val victoryReaction = SenateEngine.evaluateSenateReaction(
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            casualtiesRatio = 0.05f,
            isAquilaLost = false,
            currentFavor = 50,
            totalGlory = 100
        )
        assertTrue("Great victory must increase Senate favor", victoryReaction.favorDelta > 0)
        assertTrue("Updated favor must be higher", victoryReaction.updatedFavor > 50)

        val disasterReaction = SenateEngine.evaluateSenateReaction(
            outcome = ExpeditionOutcome.DISASTER,
            casualtiesRatio = 0.50f,
            isAquilaLost = true,
            currentFavor = 50,
            totalGlory = 100
        )
        assertTrue("Disaster and lost Eagle must crush Senate favor", disasterReaction.favorDelta <= -25)
        assertTrue("Senate must condemn lost Eagle", disasterReaction.senateCommentRu.contains("Орла"))
    }

    @Test
    fun `senate petition grants requested supplies or reinforcements`() {
        val petition = SenatePetition(
            id = "pet_emergency_grain", titleRu = "Хлебный обоз", latinNameRu = "Frumentum Annonae",
            icon = "🌾", descriptionRu = "Экстренная поставка зерна", favorCost = 15,
            minFavorRequired = 30, rewardSummaryRu = "+100 🌾"
        )
        val resources = LegionResources(denarii = 50, provisions = 20, glory = 10, senateFavor = 60)
        val cohorts = listOf(
            Cohort(id = "c1", name = "I Когорта", soldiers = 80, maxSoldiers = 80, veteransCount = 10, morale = 70, attackPower = 20, defensePower = 20, discipline = 20, level = 1, xp = 0, maxXp = 100)
        )

        val result = SenateEngine.resolveSenatePetition(petition, resources, cohorts, campLevel = 2)
        assertNotNull(result)
        val (updatedRes, _) = result!!
        assertTrue("Provisions should increase from petition", updatedRes.provisions > resources.provisions)
        assertEquals(45, updatedRes.senateFavor)
    }

    @Test
    fun `competing legions update dynamic standing and activities`() {
        val rivals = listOf(
            CompetingLegion(id = "leg1", name = "Legio I Adiutrix", ratingScore = 150, glory = 60, victories = 3, defeats = 1, senateReputation = 55, commanderNameRu = "Легат Аппий", commanderReputation = 60, currentActivityRu = "В походе", badgeSymbol = "🦅"),
            CompetingLegion(id = "leg2", name = "Legio II Augusta", ratingScore = 130, glory = 50, victories = 2, defeats = 2, senateReputation = 50, commanderNameRu = "Легат Сервий", commanderReputation = 50, currentActivityRu = "В лагере", badgeSymbol = "⚡")
        )

        val updatedRivals = SenateEngine.updateCompetingLegions(
            currentLegions = rivals,
            playerGlory = 80,
            playerVictories = 5,
            playerSenateFavor = 70,
            seasonNumber = 4
        )

        assertEquals(2, updatedRivals.size)
        assertTrue("Rivals must have updated activity", updatedRivals.first().currentActivityRu.isNotEmpty())
    }
}
