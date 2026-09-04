package com.example.domain

import com.example.data.GameDefaults
import com.example.domain.battle.BattleEngine
import com.example.domain.expeditions.ExpeditionEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class DoctrineTest {

    private val sampleExpedition = Expedition(
        id = "exp_test",
        titleRu = "Поход в Кампанию",
        historicalContextRu = "Контекст",
        regionRu = "Кампания",
        difficulty = 3,
        denariiCost = 30,
        provisionsCost = 40,
        rewardDenarii = 100,
        rewardProvisions = 100,
        rewardGlory = 8,
        scoutIntel = ScoutIntel(
            estimatedEnemyStrengthRu = "Высокая",
            dangerLevelRu = "Опасная",
            enemyTacticRu = "Стрелки",
            recommendedTactic = Tactics.TESTUDO
        )
    )

    private val sampleCommander = Commander(
        id = "cmd_1",
        name = "Марк Клавдий",
        level = 2,
        xp = 0,
        maxXp = 100,
        rankTitle = "Центурион",
        trait = CommanderTrait.CAUTIOUS
    )

    private val sampleCohort = Cohort(
        id = "coh_1",
        name = "I Когорта",
        level = 2,
        xp = 0,
        maxXp = 100,
        soldiers = 120,
        maxSoldiers = 120,
        veteransCount = 25,
        morale = 85,
        attackPower = 16,
        defensePower = 15,
        discipline = 80
    )

    @Test
    fun `default doctrines catalog provides rich tactical specializations`() {
        val doctrines = GameDefaults.createInitialDoctrines()
        assertTrue(doctrines.size >= 8)
        assertTrue(doctrines.any { it.id == "doc_disciplina_ferrea" || it.id == "doc_disciplina" })
        assertTrue(doctrines.any { it.id == "doc_testudo" })
        assertTrue(doctrines.any { it.id == "doc_pilum_volley" || it.id == "doc_pila_barrage" })
    }

    @Test
    fun `testudo doctrine with testudo tactics eliminates disaster risk`() {
        val testudoDoctrine = MilitaryDoctrine(
            id = "doc_testudo",
            titleRu = "Стена щитов «Черепаха»",
            latinNameRu = "Testudo Formatio",
            icon = "🛡️",
            costGlory = 10,
            descRu = "Оборонительное построение",
            effectRu = "0% шанс разгрома",
            isUnlocked = true
        )

        val odds = BattleEngine.calculateBattleOdds(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.TESTUDO,
            doctrines = listOf(testudoDoctrine)
        )

        assertEquals(0, odds.disasterPct)
        assertEquals(100, odds.greatVictoryPct + odds.victoryPct + odds.partialPct + odds.defeatPct + odds.disasterPct)
    }

    @Test
    fun `cursus belli doctrine reduces expedition provision logistics cost`() {
        val cursusDoctrine = MilitaryDoctrine(
            id = "doc_cursus_belli",
            titleRu = "Форсированный марш",
            latinNameRu = "Cursus Bellicus",
            icon = "👢",
            costGlory = 12,
            descRu = "Эффективная логистика",
            effectRu = "-25% затрат провианта",
            isUnlocked = true
        )

        val baseCost = ExpeditionEngine.getActualProvisionsCost(sampleExpedition, emptyList())
        val discountedCost = ExpeditionEngine.getActualProvisionsCost(sampleExpedition, listOf(cursusDoctrine))

        assertEquals(40, baseCost)
        assertEquals(30, discountedCost)
    }

    @Test
    fun `pila barrage doctrine significantly boosts great victory breakthrough chance`() {
        val pilaDoctrine = MilitaryDoctrine(
            id = "doc_pila_barrage",
            titleRu = "Залп пилумами",
            latinNameRu = "Volatus Pilorum",
            icon = "🗡️",
            costGlory = 12,
            descRu = "Шоковая атака",
            effectRu = "+12% шанс триумфа",
            isUnlocked = true
        )

        val baseOdds = BattleEngine.calculateBattleOdds(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.AGGRESSIVE,
            doctrines = emptyList()
        )

        val boostedOdds = BattleEngine.calculateBattleOdds(
            expedition = sampleExpedition,
            commander = sampleCommander,
            cohort = sampleCohort,
            tactics = Tactics.AGGRESSIVE,
            doctrines = listOf(pilaDoctrine)
        )

        assertTrue(boostedOdds.greatVictoryPct >= baseOdds.greatVictoryPct)
    }
}
