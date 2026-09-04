package com.example.domain.cohorts

import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object CohortEngine {

    data class CohortBattleCasualtyResult(
        val updatedCohort: Cohort,
        val actualCasualties: Int,
        val veteransLost: Int,
        val veteransSaved: Int,
        val moraleDelta: Int,
        val newTradition: String?
    )

    /**
     * Applies battle casualties with cascading consequences on soldiers, veterans, morale, and traditions.
     */
    fun applyBattleCasualties(
        cohort: Cohort,
        rawCasualties: Int,
        isSuccess: Boolean,
        isGreatVictory: Boolean,
        expeditionRegion: String,
        veteranPreservationPct: Int = 15,
        casualtyMitigationPct: Int = 0
    ): CohortBattleCasualtyResult {
        val mitigatedCasualties = max(0, (rawCasualties * (1f - casualtyMitigationPct / 100f)).toInt())
        val actualCasualties = min(cohort.soldiers, mitigatedCasualties)

        // Calculate proportional veteran loss
        val vetRatio = if (cohort.soldiers > 0) cohort.veteransCount.toFloat() / cohort.soldiers.toFloat() else 0f
        val rawVetLost = (actualCasualties * vetRatio).toInt()
        val vetSaved = (rawVetLost * (veteranPreservationPct / 100f)).toInt()
        val actualVetLost = max(0, min(cohort.veteransCount, rawVetLost - vetSaved))

        val newSoldiers = max(0, cohort.soldiers - actualCasualties)
        val newVeterans = max(0, cohort.veteransCount - actualVetLost)

        // Morale impact: victories inspire troops; heavy defeats shock morale (cushioned by discipline and veterans)
        val disciplineCushion = (cohort.discipline / 3) + (cohort.veteransCount / 2)
        val moraleDelta = if (isGreatVictory) {
            15
        } else if (isSuccess) {
            8
        } else {
            val lossFraction = if (cohort.soldiers > 0) (actualCasualties.toFloat() / cohort.soldiers.toFloat()) else 0.5f
            val baseShock = -(lossFraction * 50).toInt()
            (baseShock + disciplineCushion).coerceAtMost(-5)
        }

        val newMorale = (cohort.morale + moraleDelta).coerceIn(10, 100)

        // Tradition progression
        var newTradition: String? = null
        val tradList = cohort.traditions.toMutableList()
        val victories = if (isSuccess) cohort.victoriesCount + 1 else cohort.victoriesCount
        val greatVictories = if (isGreatVictory) cohort.greatVictoriesCount + 1 else cohort.greatVictoriesCount
        val defeats = if (!isSuccess) cohort.defeatsCount + 1 else cohort.defeatsCount

        if (isGreatVictory && !tradList.contains("Победители $expeditionRegion")) {
            newTradition = "Победители $expeditionRegion"
            tradList.add(newTradition)
        } else if (victories >= 5 && !tradList.contains("Железный строй")) {
            newTradition = "Железный строй"
            tradList.add(newTradition)
        } else if (victories >= 10 && !tradList.contains("Несокрушимый орёл")) {
            newTradition = "Несокрушимый орёл"
            tradList.add(newTradition)
        }

        val updated = cohort.copy(
            soldiers = newSoldiers,
            veteransCount = newVeterans,
            morale = newMorale,
            expeditionsCount = cohort.expeditionsCount + 1,
            victoriesCount = victories,
            greatVictoriesCount = greatVictories,
            defeatsCount = defeats,
            casualtiesSuffered = cohort.casualtiesSuffered + actualCasualties,
            traditions = tradList
        )

        return CohortBattleCasualtyResult(
            updatedCohort = updated,
            actualCasualties = actualCasualties,
            veteransLost = actualVetLost,
            veteransSaved = vetSaved,
            moraleDelta = moraleDelta,
            newTradition = newTradition
        )
    }

    /**
     * Awards battle experience to a cohort, promoting veterancy and ranks.
     */
    fun awardBattleExperience(
        cohort: Cohort,
        xpEarned: Int,
        isSuccess: Boolean
    ): Cohort {
        val totalXp = cohort.xp + xpEarned
        val levelGain = totalXp / cohort.maxXp
        val remainingXp = totalXp % cohort.maxXp
        val newLevel = min(10, cohort.level + levelGain)

        val newVeterans = if (isSuccess) {
            min(cohort.soldiers, cohort.veteransCount + (if (cohort.level >= 3) 2 else 1))
        } else {
            cohort.veteransCount
        }

        return cohort.copy(
            level = newLevel,
            xp = remainingXp,
            veteransCount = newVeterans,
            attackPower = cohort.attackPower + (levelGain * 2),
            defensePower = cohort.defensePower + (levelGain * 2),
            discipline = min(100, cohort.discipline + (levelGain * 3))
        )
    }

    /**
     * Calculates replenishment costs and fully restores a cohort's rank and file.
     */
    fun replenishCohort(
        cohort: Cohort,
        resources: LegionResources
    ): Pair<Cohort, LegionResources>? {
        val missingSoldiers = cohort.maxSoldiers - cohort.soldiers
        if (missingSoldiers <= 0) return null

        val denariiCost = missingSoldiers * 1
        val provCost = missingSoldiers * 2

        if (resources.denarii < denariiCost || resources.provisions < provCost) return null

        val newRes = resources.copy(
            denarii = resources.denarii - denariiCost,
            provisions = resources.provisions - provCost
        )

        val restored = cohort.copy(
            soldiers = cohort.maxSoldiers,
            morale = min(100, max(cohort.morale, 90))
        )

        return Pair(restored, newRes)
    }

    /**
     * Replenishes all wounded/depleted cohorts at once.
     */
    fun replenishAll(
        cohorts: List<Cohort>,
        resources: LegionResources
    ): Pair<List<Cohort>, LegionResources> {
        var curDenarii = resources.denarii
        var curProvisions = resources.provisions

        val updated = cohorts.map { cohort ->
            val missing = cohort.maxSoldiers - cohort.soldiers
            val denariiCost = missing * 1
            val provCost = missing * 2

            if (missing > 0 && curDenarii >= denariiCost && curProvisions >= provCost) {
                curDenarii -= denariiCost
                curProvisions -= provCost
                cohort.copy(
                    soldiers = cohort.maxSoldiers,
                    morale = min(100, max(cohort.morale, 90))
                )
            } else {
                cohort
            }
        }

        return Pair(updated, resources.copy(denarii = curDenarii, provisions = curProvisions))
    }
}
