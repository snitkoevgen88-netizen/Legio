package com.example.domain.expeditions

import com.example.model.*
import kotlin.math.max

object ExpeditionEngine {

    /**
     * Checks if legion can launch an expedition (has sufficient provisions and denarii).
     */
    fun canLaunchExpedition(
        expedition: Expedition,
        resources: LegionResources,
        doctrines: List<MilitaryDoctrine> = emptyList()
    ): Boolean {
        val provCost = getActualProvisionsCost(expedition, doctrines)
        return resources.denarii >= expedition.denariiCost && resources.provisions >= provCost
    }

    /**
     * Calculates actual provisions cost taking doctrines into account (e.g. Cursus Bellicus reduces provision cost).
     */
    fun getActualProvisionsCost(
        expedition: Expedition,
        doctrines: List<MilitaryDoctrine> = emptyList()
    ): Int {
        val hasCursus = doctrines.any { (it.id == "doc_cursus_belli" || it.id == "doc_logistics") && it.isUnlocked }
        return if (hasCursus) (expedition.provisionsCost * 0.75f).toInt().coerceAtLeast(5) else expedition.provisionsCost
    }

    /**
     * Deducts expedition deployment costs from legion resources.
     */
    fun deductDeploymentCosts(
        expedition: Expedition,
        resources: LegionResources,
        doctrines: List<MilitaryDoctrine> = emptyList()
    ): LegionResources {
        val provCost = getActualProvisionsCost(expedition, doctrines)
        return resources.copy(
            denarii = resources.denarii - expedition.denariiCost,
            provisions = resources.provisions - provCost
        )
    }

    /**
     * Selects optimal commander, cohort, and counter-tactics based on enemy scouting intel and priority.
     */
    fun selectOptimalSquad(
        expedition: Expedition,
        commanders: List<Commander>,
        cohorts: List<Cohort>,
        priority: AutoPlanPriority = AutoPlanPriority.BALANCED
    ): Triple<Commander?, Cohort?, Tactics> {
        val livingCommanders = commanders.filter { it.isAlive }

        val bestCommander = livingCommanders.maxByOrNull { cmd ->
            var score = cmd.level * 10
            when (priority) {
                AutoPlanPriority.MILITARY -> {
                    if (cmd.trait == CommanderTrait.BRAVE) score += 25
                    if (cmd.trait == CommanderTrait.TACTICIAN) score += 20
                }
                AutoPlanPriority.ECONOMY -> {
                    if (cmd.trait == CommanderTrait.GREEDY) score += 25
                    if (cmd.trait == CommanderTrait.CAUTIOUS) score += 15
                }
                AutoPlanPriority.RECOVERY -> {
                    if (cmd.trait == CommanderTrait.CAUTIOUS) score += 30
                    if (cmd.trait == CommanderTrait.DISCIPLINED) score += 20
                }
                AutoPlanPriority.POLITICS -> {
                    if (cmd.trait == CommanderTrait.AMBITIOUS) score += 30
                    if (cmd.trait == CommanderTrait.LOYAL) score += 20
                }
                AutoPlanPriority.BALANCED -> {
                    if (expedition.difficulty >= 3 && cmd.trait == CommanderTrait.CAUTIOUS) score += 15
                    if (expedition.difficulty <= 2 && cmd.trait == CommanderTrait.BRAVE) score += 15
                }
            }
            score
        }

        val bestCohort = cohorts
            .filter { if (priority == AutoPlanPriority.RECOVERY) it.soldiers >= (it.maxSoldiers * 0.7) else it.soldiers >= (it.maxSoldiers * 0.4) }
            .maxByOrNull { coh ->
                when (priority) {
                    AutoPlanPriority.MILITARY -> coh.attackPower * 3 + coh.veteransCount * 5 + coh.soldiers
                    AutoPlanPriority.RECOVERY -> coh.defensePower * 3 + coh.morale * 2 + coh.soldiers
                    else -> coh.soldiers * 2 + coh.discipline * 3 + coh.veteransCount * 4
                }
            } ?: cohorts.maxByOrNull { it.soldiers }

        val chosenTactics = when (priority) {
            AutoPlanPriority.MILITARY -> if (expedition.difficulty <= 2) Tactics.AGGRESSIVE else expedition.scoutIntel.recommendedTactic
            AutoPlanPriority.RECOVERY, AutoPlanPriority.ECONOMY -> if (expedition.difficulty >= 2) Tactics.TESTUDO else Tactics.CAUTIOUS
            else -> expedition.scoutIntel.recommendedTactic
        }

        return Triple(bestCommander, bestCohort, chosenTactics)
    }

    /**
     * Generates a complete SeasonalPlan according to the selected strategic priority.
     */
    fun generateSeasonalPlan(
        priority: AutoPlanPriority,
        buildings: List<Building>,
        cohorts: List<Cohort>,
        commanders: List<Commander>,
        expeditions: List<Expedition>,
        resources: LegionResources,
        season: Season,
        doctrines: List<MilitaryDoctrine> = emptyList()
    ): SeasonalPlan {
        var plannedDenarii = resources.denarii
        var plannedProvisions = resources.provisions

        // 1. Building selection based on priority
        val upgradable = buildings.filter {
            it.level < it.maxLevel && plannedDenarii >= it.upgradeCostDenarii && plannedProvisions >= it.upgradeCostProvisions
        }

        val chosenBuilding = when (priority) {
            AutoPlanPriority.MILITARY -> upgradable.find { it.type == BuildingType.FABRICA || it.type == BuildingType.CAMPUS_MARTIUS || it.type == BuildingType.BALLISTARIUM } ?: upgradable.minByOrNull { it.upgradeCostDenarii }
            AutoPlanPriority.ECONOMY -> upgradable.find { it.type == BuildingType.HORREUM || it.type == BuildingType.TABULARIUM } ?: upgradable.minByOrNull { it.upgradeCostDenarii }
            AutoPlanPriority.RECOVERY -> upgradable.find { it.type == BuildingType.VALETUDINARIUM || it.type == BuildingType.THERMAE_LEGIONIS } ?: upgradable.minByOrNull { it.upgradeCostDenarii }
            AutoPlanPriority.POLITICS -> upgradable.find { it.type == BuildingType.PRINCIPIA || it.type == BuildingType.AQUILA_SHRINE } ?: upgradable.minByOrNull { it.upgradeCostDenarii }
            AutoPlanPriority.BALANCED -> upgradable.minByOrNull { it.upgradeCostDenarii }
        }

        if (chosenBuilding != null) {
            plannedDenarii -= chosenBuilding.upgradeCostDenarii
            plannedProvisions -= chosenBuilding.upgradeCostProvisions
        }

        // 2. Training selection
        val trainCost = if (season == Season.SPRING) 24 else 30
        val canAffordTraining = plannedDenarii >= trainCost && plannedProvisions >= 15

        val targetCohort = if (canAffordTraining) {
            when (priority) {
                AutoPlanPriority.MILITARY -> cohorts.maxByOrNull { it.attackPower }
                AutoPlanPriority.RECOVERY -> cohorts.minByOrNull { it.discipline }
                else -> cohorts.filter { it.soldiers >= 40 }.minByOrNull { it.level } ?: cohorts.firstOrNull()
            }
        } else null

        val selectedTrainCohortId = if (targetCohort != null && canAffordTraining) {
            plannedDenarii -= trainCost
            plannedProvisions -= 15
            targetCohort.id
        } else null

        // 3. Expedition selection
        val affordableExpeditions = expeditions.filter {
            val pCost = getActualProvisionsCost(it, doctrines)
            plannedDenarii >= it.denariiCost && plannedProvisions >= pCost
        }

        val chosenExpedition = when (priority) {
            AutoPlanPriority.MILITARY -> affordableExpeditions.maxByOrNull { it.difficulty }
            AutoPlanPriority.POLITICS -> affordableExpeditions.find { it.isSenateTrial } ?: affordableExpeditions.maxByOrNull { it.rewardGlory }
            AutoPlanPriority.ECONOMY -> affordableExpeditions.maxByOrNull { it.rewardDenarii.toFloat() / max(1, it.denariiCost) }
            AutoPlanPriority.RECOVERY -> affordableExpeditions.minByOrNull { it.difficulty }
            AutoPlanPriority.BALANCED -> affordableExpeditions.maxByOrNull { it.rewardGlory }
        }

        val (bestCmd, bestCoh, tactics) = if (chosenExpedition != null) {
            selectOptimalSquad(chosenExpedition, commanders, cohorts, priority)
        } else {
            val cmd = commanders.filter { it.isAlive }.maxByOrNull { it.level }
            val coh = cohorts.maxByOrNull { it.soldiers }
            Triple(cmd, coh, Tactics.BALANCED)
        }

        return SeasonalPlan(
            trainCohortId = selectedTrainCohortId,
            upgradeBuildingType = chosenBuilding?.type,
            launchedExpeditionId = chosenExpedition?.id,
            selectedCommanderId = bestCmd?.id,
            selectedCohortId = bestCoh?.id,
            selectedTactics = tactics,
            priority = priority
        )
    }
}
