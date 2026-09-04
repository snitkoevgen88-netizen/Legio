package com.example.domain

import com.example.domain.battle.BattleEngine
import com.example.domain.chronicle.ChronicleEngine
import com.example.domain.economy.EconomyEngine
import com.example.domain.religion.AquilaEngine
import com.example.domain.religion.ReligionEngine
import com.example.domain.senate.SenateEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class StageThreeIntegrationTest {

    @Test
    fun `full stage 3 cycle - campaign consequence cascade`() {
        // 1. Initial State
        var resources = LegionResources(denarii = 200, provisions = 200, glory = 50, senateFavor = 60)
        var aquila = LegionAquilaState(eagleUpgradeLevel = 1, totalSacredGlory = 50, isAquilaProtected = true, isAquilaLost = false)
        val cohort = Cohort(
            id = "c1", name = "I Когорта Прима",
            soldiers = 80, maxSoldiers = 80, veteransCount = 20,
            morale = 90, attackPower = 25, defensePower = 25, discipline = 30,
            level = 2, xp = 20, maxXp = 100
        )
        val commander = Commander(
            id = "cmd1", name = "Аппий Клавдий", rankTitle = "Военный трибун",
            level = 2, xp = 30, maxXp = 100,
            expeditionsLed = 2, victoriesCount = 2, greatVictoriesCount = 0,
            defeatsCount = 0, isAlive = true,
            moodStatus = "Готов", unlockedTalents = listOf(OfficerTalent.LOGISTICS_GENIUS)
        )
        val expedition = Expedition(
            id = "exp_samnium_epic",
            titleRu = "Штурм Каудийского ущелья",
            historicalContextRu = "Pugna Furculae Caudinae",
            regionRu = "Самний",
            difficulty = 3,
            denariiCost = 40,
            provisionsCost = 50,
            rewardDenarii = 120,
            rewardProvisions = 80,
            rewardGlory = 15,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Высокая",
                dangerLevelRu = "Высокая",
                enemyTacticRu = "Засада в ущелье",
                recommendedTactic = Tactics.FLANK_AMBUSH
            ),
            isSenateTrial = true
        )

        // 2. Perform Divine Ritual before battle (Mars blessing)
        val ritual = DivineRitual(
            id = "rit_mars", god = GodType.MARS, nameRu = "Жертвоприношение Марсу",
            descriptionRu = "Благословение мечей", costDenarii = 25, costProvisions = 20,
            blessingEffectRu = "+15% к атаке"
        )
        val ritualRes = ReligionEngine.performRitual(ritual, resources, listOf(cohort))
        assertTrue(ritualRes.isSuccess)
        resources = ritualRes.updatedResources
        assertEquals(175, resources.denarii)
        assertEquals(180, resources.provisions)

        // 3. Launch Expedition with Logistical Cost
        val (denariiCost, provisionsCost) = EconomyEngine.calculateExpeditionSupplyCost(expedition, commander)
        resources = resources.copy(
            denarii = resources.denarii - denariiCost,
            provisions = resources.provisions - provisionsCost
        )
        assertTrue("Logistics talent discounted provisions", provisionsCost < expedition.provisionsCost)

        // 4. Resolve Battle with BattleEngine
        val odds = BattleEngine.calculateBattleOdds(
            expedition = expedition,
            commander = commander,
            cohort = cohort,
            tactics = Tactics.FLANK_AMBUSH
        )
        val battleResult = BattleEngine.resolveBattle(
            expedition = expedition,
            commander = commander,
            cohort = cohort,
            tactics = Tactics.FLANK_AMBUSH,
            odds = odds,
            campLevel = 2,
            fabricaLevel = 2,
            valetudinariumLevel = 2
        )

        assertTrue(battleResult.outcome.isSuccess)
        val lootDenarii = battleResult.lootDenarii
        val lootProvisions = battleResult.lootProvisions
        val casualties = battleResult.casualties

        // 5. Update Resources & Glory from Battle
        resources = resources.copy(
            denarii = resources.denarii + lootDenarii,
            provisions = resources.provisions + lootProvisions,
            glory = resources.glory + battleResult.gloryDelta + 15 // +15 for Senate Trial
        )
        assertTrue("Glory should increase after victory", resources.glory > 50)

        // 6. Senate Reaction to Victory
        val senateReaction = SenateEngine.evaluateSenateReaction(
            outcome = battleResult.outcome,
            casualtiesRatio = casualties.toFloat() / cohort.soldiers.toFloat(),
            isAquilaLost = false,
            currentFavor = resources.senateFavor,
            totalGlory = resources.glory
        )
        resources = resources.copy(senateFavor = senateReaction.updatedFavor)
        assertTrue("Senate favor must increase following glorious victory", resources.senateFavor > 60)

        // 7. Competing Legions update
        val initialRivals = listOf(
            CompetingLegion(id = "leg1", name = "Legio I Adiutrix", ratingScore = 150, glory = 60, victories = 3, defeats = 1, senateReputation = 55, commanderNameRu = "Легат Аппий", commanderReputation = 60, currentActivityRu = "В походе", badgeSymbol = "🦅")
        )
        val updatedRivals = SenateEngine.updateCompetingLegions(
            currentLegions = initialRivals,
            playerGlory = resources.glory,
            playerVictories = commander.victoriesCount + 1,
            playerSenateFavor = resources.senateFavor,
            seasonNumber = 3
        )
        assertNotNull(updatedRivals)

        // 8. Reinforce Cohort Casualties
        if (casualties > 0) {
            val replenishRes = EconomyEngine.replenishCohort(
                cohort = battleResult.cohort,
                recruitsCount = casualties,
                resources = resources,
                campLevel = 2
            )
            assertTrue(replenishRes.isSuccess)
            assertEquals(80, replenishRes.updatedState.soldiers)
            resources = replenishRes.updatedResources
        }

        // 9. Consecrate Aquila Standard Upgrade
        val upgradeAquilaRes = AquilaEngine.upgradeAquila(aquila, resources)
        assertTrue(upgradeAquilaRes.isSuccess)
        aquila = upgradeAquilaRes.updatedAquila
        resources = upgradeAquilaRes.updatedResources
        assertEquals(2, aquila.eagleUpgradeLevel)

        // 10. Record Chronicle
        val shouldRecord = ChronicleEngine.shouldRecordExpedition(
            expedition = expedition,
            outcome = battleResult.outcome,
            casualties = casualties,
            commanderPromoted = battleResult.commanderPromoted,
            commanderKilled = false
        )
        assertTrue("Major trial battle should be chronicled", shouldRecord)

        val chronicleEntry = ChronicleEngine.recordBattleChronicle(
            seasonYear = SeasonYear(seasonIndex = 1, seasonNumber = 2, yearBc = 310),
            expedition = expedition,
            commander = commander,
            cohort = cohort,
            outcome = battleResult.outcome,
            narrative = "Славная победа при Каудийском ущелье!",
            casualties = casualties,
            lootDenarii = lootDenarii,
            lootProvisions = lootProvisions,
            gloryEarned = battleResult.gloryDelta
        )
        assertNotNull(chronicleEntry)
        assertEquals(battleResult.outcome, chronicleEntry.outcome)
    }
}
