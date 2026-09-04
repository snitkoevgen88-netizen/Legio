package com.example.domain

import com.example.domain.battle.BattleEngine
import com.example.domain.economy.EconomyEngine
import com.example.domain.equipment.EquipmentEngine
import com.example.domain.expeditions.ExpeditionEngine
import com.example.domain.religion.ReligionEngine
import com.example.domain.season.SeasonEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StageFourIntegrationTest {

    private lateinit var state: GameUiState

    @Before
    fun setUp() {
        state = GameUiState(
            seasonYear = SeasonYear(seasonNumber = 1, yearBc = 310),
            resources = LegionResources(
                denarii = 150,
                provisions = 100,
                glory = 25,
                senateFavor = 60
            ),
            cohorts = listOf(
                Cohort(
                    id = "coh_1",
                    name = "I Когорта Прима",
                    soldiers = 100,
                    maxSoldiers = 100,
                    attackPower = 35,
                    defensePower = 40,
                    morale = 90,
                    discipline = 88,
                    veteransCount = 20,
                    level = 2
                ),
                Cohort(
                    id = "coh_2",
                    name = "II Когорта «Громовержцы»",
                    soldiers = 95,
                    maxSoldiers = 100,
                    attackPower = 30,
                    defensePower = 35,
                    morale = 85,
                    discipline = 80,
                    veteransCount = 10,
                    level = 1
                ),
                Cohort(
                    id = "coh_3",
                    name = "III Когорта «Копейщики»",
                    soldiers = 90,
                    maxSoldiers = 100,
                    attackPower = 28,
                    defensePower = 38,
                    morale = 80,
                    discipline = 82,
                    veteransCount = 8,
                    level = 1
                )
            ),
            commanders = listOf(
                Commander(
                    id = "cmd_1",
                    name = "Марк Валерий Корв",
                    rankTitle = "Легат Легиона",
                    level = 2,
                    xp = 20,
                    maxXp = 100,
                    trait = CommanderTrait.BRAVE,
                    isAlive = true
                )
            ),
            buildings = listOf(
                Building(BuildingType.PRINCIPIA, level = 2, maxLevel = 4),
                Building(BuildingType.FABRICA, level = 1, maxLevel = 4),
                Building(BuildingType.HORREUM, level = 1, maxLevel = 4),
                Building(BuildingType.VALETUDINARIUM, level = 1, maxLevel = 3)
            ),
            availableExpeditions = listOf(
                Expedition(
                    id = "exp_samnite_ridge",
                    titleRu = "Штурм Самнитийского хребта",
                    historicalContextRu = "Pugna Furculae Caudinae",
                    regionRu = "Самний",
                    difficulty = 2,
                    denariiCost = 25,
                    provisionsCost = 20,
                    rewardDenarii = 60,
                    rewardProvisions = 40,
                    rewardGlory = 15,
                    scoutIntel = ScoutIntel(
                        estimatedEnemyStrengthRu = "Средняя",
                        dangerLevelRu = "Умеренная",
                        enemyTacticRu = "Фаланга",
                        recommendedTactic = Tactics.AGGRESSIVE
                    )
                )
            ),
            senateQuests = listOf(
                SenateQuest(
                    id = "quest_samnite_war",
                    titleRu = "Покорение Самния",
                    issuerRu = "Консул SPQR",
                    descriptionRu = "Разгромите врага на Апеннинах",
                    rewardDenarii = 80,
                    rewardSenateFavor = 15,
                    rewardGlory = 20,
                    targetType = "EXPEDITION_WIN",
                    targetCount = 1,
                    currentProgress = 0,
                    status = QuestStatus.ACTIVE,
                    isClaimed = false
                )
            )
        )
    }

    @Test
    fun testFullGameplayLoopCampaignBattleAndConsequences() {
        // 1. Initial Vitals Verification
        assertEquals(285, state.totalSoldiers)
        assertEquals(300, state.maxSoldiers)
        assertEquals(95, state.strengthPercentage)
        assertEquals(85, state.averageMorale)
        assertEquals(83, state.averageDiscipline)

        // 2. Perform Sacred Altar Ritual
        val ritual = DivineRitual(
            id = "rit_mars_blessing",
            god = GodType.MARS,
            nameRu = "Жертвоприношение Марсу",
            descriptionRu = "Благословение мечей",
            costDenarii = 20,
            costProvisions = 15,
            blessingEffectRu = "+15% к атаке"
        )
        val ritualRes = ReligionEngine.performRitual(ritual, state.resources, state.cohorts)
        assertTrue(ritualRes.isSuccess)
        val updatedResources = ritualRes.updatedResources

        // 3. Launch Planned Expedition
        val expedition = state.availableExpeditions.first()
        val commander = state.commanders.first()
        val cohort = state.cohorts.first()

        val afterDeployRes = ExpeditionEngine.deductDeploymentCosts(
            expedition = expedition,
            resources = updatedResources
        )
        assertTrue(afterDeployRes.denarii < updatedResources.denarii)
        assertTrue(afterDeployRes.provisions < updatedResources.provisions)

        // 4. Resolve Battle through BattleEngine
        val odds = BattleEngine.calculateBattleOdds(
            expedition = expedition,
            commander = commander,
            cohort = cohort,
            tactics = Tactics.AGGRESSIVE
        )
        val battleResult = BattleEngine.resolveBattle(
            expedition = expedition,
            commander = commander,
            cohort = cohort,
            tactics = Tactics.AGGRESSIVE,
            odds = odds,
            campLevel = state.campLevel,
            fabricaLevel = 1,
            valetudinariumLevel = 1
        )
        assertNotNull(battleResult)
        assertTrue(battleResult.outcome.isSuccess)

        // 5. Advance Season & Economy
        val seasonRes = SeasonEngine.advanceSeason(
            currentState = state.copy(
                resources = afterDeployRes
            )
        )
        assertNotNull(seasonRes)
        assertNotNull(seasonRes.newSeasonYear)
        assertEquals(2, seasonRes.newSeasonYear.seasonNumber)
        assertNotNull(seasonRes.updatedResources)
        assertTrue(seasonRes.seasonalSummaryRu.isNotEmpty())
    }

    @Test
    fun testAutoPlanAllPriorities() {
        for (priority in AutoPlanPriority.entries) {
            val plan = ExpeditionEngine.generateSeasonalPlan(
                priority = priority,
                buildings = state.buildings,
                cohorts = state.cohorts,
                commanders = state.commanders,
                expeditions = state.availableExpeditions,
                resources = state.resources,
                season = Season.SPRING
            )
            assertNotNull(plan)
            assertEquals(priority, plan.priority)
            assertNotNull(plan.selectedCommanderId)
            assertNotNull(plan.selectedCohortId)
            assertNotNull(plan.selectedTactics)
        }
    }

    @Test
    fun testLegionAlertsGeneration() {
        // 1. Initial State Alerts
        val initialAlerts = state.generateLegionAlerts()
        assertTrue(initialAlerts.any { it.id == "alert_no_blessing" })

        // 2. Critical Casualties Alert
        val woundedCohorts = state.cohorts.map { it.copy(soldiers = 40) } // 40/100
        val lowRes = state.resources.copy(denarii = 15, provisions = 10)
        val readyQuest = state.senateQuests.map { it.copy(currentProgress = 1, targetCount = 1, isClaimed = false) }

        val criticalState = state.copy(
            cohorts = woundedCohorts,
            resources = lowRes,
            senateQuests = readyQuest
        )
        val alerts = criticalState.generateLegionAlerts()

        assertTrue(alerts.any { it.id == "alert_casualties" && it.severity == AlertSeverity.CRITICAL })
        assertTrue(alerts.any { it.id == "alert_low_denarii" && it.severity == AlertSeverity.CRITICAL })
        assertTrue(alerts.any { it.id == "alert_low_provisions" && it.severity == AlertSeverity.WARNING })
        assertTrue(alerts.any { it.id == "alert_claimable_quests" && it.severity == AlertSeverity.OPPORTUNITY })
    }

    @Test
    fun testRoutineReplenishAndAutoEquip() {
        // Damaged cohorts
        val damagedCohorts = listOf(
            Cohort(id = "c1", name = "I", soldiers = 60, maxSoldiers = 100),
            Cohort(id = "c2", name = "II", soldiers = 70, maxSoldiers = 100)
        )
        var res = state.resources.copy(denarii = 100, provisions = 100)

        // Replenish All
        val replenishedCohorts = damagedCohorts.map { coh ->
            val needed = coh.maxSoldiers - coh.soldiers
            val cost = (needed * 0.3f).toInt()
            res = res.copy(denarii = res.denarii - cost)
            coh.copy(soldiers = coh.maxSoldiers)
        }

        assertTrue(replenishedCohorts.all { it.soldiers == 100 })
        assertTrue(res.denarii < 100)

        // Auto-Equip All
        val testEquipment = listOf(
            EquipmentItem(id = "eq1", nameRu = "Гладиус Майнц", type = EquipmentType.WEAPON, isCrafted = true, attackBonus = 8, descRu = "Оружие"),
            EquipmentItem(id = "eq2", nameRu = "Скутум Легиона", type = EquipmentType.ARMOR, isCrafted = true, defenseBonus = 12, descRu = "Броня")
        )
        val updatedEquipment = EquipmentEngine.autoEquipAll(testEquipment, replenishedCohorts)
        assertTrue(updatedEquipment.any { it.equippedCohortId != null })
    }
}
