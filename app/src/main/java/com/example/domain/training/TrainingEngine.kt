package com.example.domain.training

import com.example.model.*
import kotlin.math.max
import kotlin.math.min

object TrainingEngine {

    data class TrainingResult(
        val updatedCohorts: List<Cohort>,
        val updatedAllocations: List<UnitTrainingAllocation>,
        val updatedResources: LegionResources,
        val messageRu: String
    )

    /**
     * Updates allocation count for a unit training category.
     */
    fun updateAllocationCount(
        allocations: List<UnitTrainingAllocation>,
        unitType: UnitType,
        newCount: Int
    ): List<UnitTrainingAllocation> {
        return allocations.map { alloc ->
            if (alloc.unitType == unitType) {
                alloc.copy(allocatedCount = newCount.coerceIn(1, 100))
            } else alloc
        }
    }

    /**
     * Updates drill intensity for a unit training category.
     */
    fun updateDrillIntensity(
        allocations: List<UnitTrainingAllocation>,
        unitType: UnitType,
        intensity: DrillIntensity
    ): List<UnitTrainingAllocation> {
        return allocations.map { alloc ->
            if (alloc.unitType == unitType) {
                alloc.copy(drillIntensity = intensity)
            } else alloc
        }
    }

    /**
     * Updates target cohort for a unit training category.
     */
    fun updateTargetCohort(
        allocations: List<UnitTrainingAllocation>,
        unitType: UnitType,
        targetCohortId: String
    ): List<UnitTrainingAllocation> {
        return allocations.map { alloc ->
            if (alloc.unitType == unitType) {
                alloc.copy(targetCohortId = targetCohortId)
            } else alloc
        }
    }

    /**
     * Automatically balances unit allocations across all available unit training types.
     */
    fun autoAllocateBalanced(allocations: List<UnitTrainingAllocation>): List<UnitTrainingAllocation> {
        return allocations.map { alloc ->
            val balancedCount = when (alloc.unitType) {
                UnitType.HASTATI -> 30
                UnitType.PRINCIPES -> 25
                UnitType.TRIARII -> 15
                UnitType.VELITES -> 20
                UnitType.EQUITES -> 10
                UnitType.FUNDITORES -> 15
            }
            alloc.copy(
                allocatedCount = balancedCount,
                drillIntensity = DrillIntensity.INTENSIVE
            )
        }
    }

    /**
     * Completes unit training and integrates new recruits into target cohort.
     */
    fun completeTraining(
        allocation: UnitTrainingAllocation,
        cohorts: List<Cohort>,
        doctrines: List<MilitaryDoctrine>
    ): Pair<List<Cohort>, UnitTrainingAllocation> {
        val targetCohort = cohorts.find { it.id == allocation.targetCohortId }
            ?: cohorts.firstOrNull()
            ?: return Pair(cohorts, allocation.copy(isTrainingActive = false, currentProgress = 0f))

        val count = allocation.allocatedCount
        val drillMultiplier = allocation.drillIntensity.costMultiplier

        val spaceAvailable = targetCohort.maxSoldiers - targetCohort.soldiers
        val addedSoldiers = min(count, spaceAvailable)

        val baseAtkBonus = (allocation.unitType.attackBonus * 0.1f * drillMultiplier).toInt().coerceAtLeast(1)
        val baseDefBonus = (allocation.unitType.defenseBonus * 0.1f * drillMultiplier).toInt().coerceAtLeast(1)

        val hasSpartanDrill = doctrines.any { (it.id == "doc_disciplina" || it.id == "doc_disciplina_ferrea") && it.isUnlocked }
        val extraDiscipline = if (hasSpartanDrill) 5 else 2

        val updatedCohort = targetCohort.copy(
            soldiers = min(targetCohort.maxSoldiers, targetCohort.soldiers + addedSoldiers),
            attackPower = targetCohort.attackPower + baseAtkBonus,
            defensePower = targetCohort.defensePower + baseDefBonus,
            discipline = min(100, targetCohort.discipline + extraDiscipline),
            morale = min(100, targetCohort.morale + 5),
            veteransCount = min(targetCohort.maxSoldiers, targetCohort.veteransCount + (addedSoldiers / 5))
        )

        val updatedCohorts = cohorts.map { if (it.id == updatedCohort.id) updatedCohort else it }
        val resetAllocation = allocation.copy(
            isTrainingActive = false,
            currentProgress = 0f,
            secondsRemaining = 0,
            totalTrainedSoFar = allocation.totalTrainedSoFar + count
        )

        return Pair(updatedCohorts, resetAllocation)
    }

    /**
     * Conducts a seasonal military drill for a cohort at the Campus Martius.
     */
    fun conductSeasonalDrill(
        cohort: Cohort,
        campusLevel: Int,
        resources: LegionResources,
        season: Season,
        doctrines: List<MilitaryDoctrine> = emptyList()
    ): Pair<Cohort, LegionResources>? {
        val denariiCost = if (season == Season.SPRING) 24 else 30
        val provisionsCost = 15

        if (resources.denarii < denariiCost || resources.provisions < provisionsCost) return null

        val newResources = resources.copy(
            denarii = resources.denarii - denariiCost,
            provisions = resources.provisions - provisionsCost
        )

        val hasDisciplina = doctrines.any { (it.id == "doc_disciplina" || it.id == "doc_disciplina_ferrea") && it.isUnlocked }
        val xpGain = campusLevel * 35 + (if (hasDisciplina) 15 else 0)
        val totalXp = cohort.xp + xpGain
        val levelGain = totalXp / cohort.maxXp
        val remainingXp = totalXp % cohort.maxXp
        val newLevel = min(10, cohort.level + levelGain)

        val veteranBonus = if (cohort.level >= 3) 2 else 1
        val newVeterans = min(cohort.soldiers, cohort.veteransCount + veteranBonus)

        val drilledCohort = cohort.copy(
            level = newLevel,
            xp = remainingXp,
            veteransCount = newVeterans,
            attackPower = cohort.attackPower + 2 + (levelGain * 2),
            defensePower = cohort.defensePower + 1 + (levelGain * 2),
            discipline = min(100, cohort.discipline + 3 + (if (hasDisciplina) 3 else 0)),
            morale = min(100, cohort.morale + 4)
        )

        return Pair(drilledCohort, newResources)
    }

    /**
     * Replenishes casualties for a single cohort using resources.
     */
    fun replenishCohort(
        cohort: Cohort,
        resources: LegionResources
    ): Pair<Cohort, LegionResources>? {
        val missing = cohort.maxSoldiers - cohort.soldiers
        if (missing <= 0) return null

        val denariiCost = missing * 1
        val provCost = missing * 2

        if (resources.denarii < denariiCost || resources.provisions < provCost) return null

        val newResources = resources.copy(
            denarii = resources.denarii - denariiCost,
            provisions = resources.provisions - provCost
        )

        val replenished = cohort.copy(
            soldiers = cohort.maxSoldiers,
            morale = min(100, cohort.morale + 10)
        )

        return Pair(replenished, newResources)
    }
}
