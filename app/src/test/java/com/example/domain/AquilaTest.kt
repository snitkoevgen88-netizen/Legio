package com.example.domain

import com.example.domain.religion.AquilaEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class AquilaTest {

    @Test
    fun `consecrating and upgrading aquila increases level and grants sacred glory`() {
        val initialAquila = LegionAquilaState(
            aquilaNameRu = "Золотой Орел Марса",
            eagleUpgradeLevel = 1,
            totalSacredGlory = 50,
            isAquilaProtected = true,
            isAquilaLost = false
        )
        val resources = LegionResources(denarii = 200, provisions = 100, glory = 20, senateFavor = 60)

        val result = AquilaEngine.upgradeAquila(initialAquila, resources)

        assertTrue(result.isSuccess)
        assertEquals(2, result.updatedAquila.eagleUpgradeLevel)
        assertTrue(result.updatedAquila.totalSacredGlory > 50)
        assertTrue(result.updatedResources.glory > 20)
        assertTrue(result.updatedResources.denarii < 200)
    }

    @Test
    fun `battle disaster with heavy casualties can result in lost Aquila`() {
        val aquila = LegionAquilaState(
            eagleUpgradeLevel = 1,
            isAquilaProtected = false,
            isAquilaLost = false
        )

        val disasterResult = AquilaEngine.evaluateBattleDisaster(
            aquilaState = aquila,
            outcome = ExpeditionOutcome.DISASTER,
            casualtiesRatio = 0.60f,
            hasJupiterBlessing = false,
            randomSeed = 10 // Roll will trigger loss
        )

        assertTrue("High casualties disaster can lose the Aquila", disasterResult.isLost)
        assertTrue(disasterResult.updatedAquila.isAquilaLost)
        assertTrue("Loss incurs heavy glory penalty", disasterResult.gloryLost >= 30)
        assertTrue("Loss crushes morale", disasterResult.moralePenalty >= 25)
    }

    @Test
    fun `jupiter divine blessing protects Aquila standard even in disaster`() {
        val aquila = LegionAquilaState(
            eagleUpgradeLevel = 2,
            isAquilaProtected = true,
            isAquilaLost = false
        )

        val result = AquilaEngine.evaluateBattleDisaster(
            aquilaState = aquila,
            outcome = ExpeditionOutcome.DISASTER,
            casualtiesRatio = 0.80f,
            hasJupiterBlessing = true // Divine protection
        )

        assertFalse("Jupiter blessing guarantees Aquila protection", result.isLost)
        assertFalse(result.updatedAquila.isAquilaLost)
    }

    @Test
    fun `reclaiming lost aquila in retribution campaign restores Roman honor`() {
        val lostAquila = LegionAquilaState(
            eagleUpgradeLevel = 2,
            totalSacredGlory = 30,
            isAquilaProtected = false,
            isAquilaLost = true
        )
        val resources = LegionResources(denarii = 50, provisions = 50, glory = 10, senateFavor = 30)

        val reclaimResult = AquilaEngine.reclaimLostAquila(
            aquilaState = lostAquila,
            resources = resources,
            outcome = ExpeditionOutcome.GREAT_VICTORY
        )

        assertTrue("Reclaim should succeed on victory", reclaimResult.isReclaimed)
        assertFalse("Eagle is no longer lost", reclaimResult.updatedAquila.isAquilaLost)
        assertTrue("Should award massive glory", reclaimResult.gloryGained >= 30)
        assertTrue("Should award Senate favor", reclaimResult.senateFavorGained >= 20)
    }
}
