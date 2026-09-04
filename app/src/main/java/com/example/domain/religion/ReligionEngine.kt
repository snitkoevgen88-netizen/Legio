package com.example.domain.religion

import com.example.model.*
import kotlin.math.min

object ReligionEngine {

    data class RitualResult(
        val isSuccess: Boolean,
        val updatedBlessing: ActiveBlessing?,
        val updatedCohorts: List<Cohort>,
        val updatedResources: LegionResources,
        val messageRu: String
    )

    /**
     * Performs a divine ritual/sacrifice at the Legion Altar.
     */
    fun performRitual(
        ritual: DivineRitual,
        currentResources: LegionResources,
        cohorts: List<Cohort>
    ): RitualResult {
        if (currentResources.denarii < ritual.costDenarii || currentResources.provisions < ritual.costProvisions) {
            return RitualResult(
                isSuccess = false,
                updatedBlessing = null,
                updatedCohorts = cohorts,
                updatedResources = currentResources,
                messageRu = "Недостаточно ресурсов для совершения ритуала (${ritual.costDenarii} ден., ${ritual.costProvisions} пров.)"
            )
        }

        val newResources = currentResources.copy(
            denarii = currentResources.denarii - ritual.costDenarii,
            provisions = currentResources.provisions - ritual.costProvisions,
            senateFavor = min(100, currentResources.senateFavor + 5)
        )

        val newBlessing = ActiveBlessing(
            god = ritual.god,
            ritualNameRu = ritual.nameRu,
            effectRu = ritual.blessingEffectRu,
            seasonsRemaining = 2
        )

        val updatedCohorts = cohorts.map { cohort ->
            cohort.copy(morale = min(100, cohort.morale + 10))
        }

        return RitualResult(
            isSuccess = true,
            updatedBlessing = newBlessing,
            updatedCohorts = updatedCohorts,
            updatedResources = newResources,
            messageRu = "Ритуал «${ritual.nameRu}» совершен благоговейно. Боги благосклонны к легиону!"
        )
    }

    /**
     * Performs Lustratio (purification of legion standards and cohorts).
     */
    fun performLustratio(
        currentResources: LegionResources,
        cohorts: List<Cohort>,
        costDenarii: Int = 30,
        costProvisions: Int = 40
    ): Pair<LegionResources, List<Cohort>>? {
        if (currentResources.denarii < costDenarii || currentResources.provisions < costProvisions) return null

        val newResources = currentResources.copy(
            denarii = currentResources.denarii - costDenarii,
            provisions = currentResources.provisions - costProvisions
        )

        val updatedCohorts = cohorts.map { cohort ->
            cohort.copy(
                morale = min(100, cohort.morale + 15),
                discipline = min(100, cohort.discipline + 10)
            )
        }

        return Pair(newResources, updatedCohorts)
    }

    /**
     * Upgrades the Aquila Shrine relic.
     */
    fun upgradeAquilaShrine(
        currentLevel: Int,
        resources: LegionResources
    ): Pair<Int, LegionResources>? {
        val cost = (currentLevel + 1) * 75
        if (resources.denarii < cost) return null

        val newResources = resources.copy(denarii = resources.denarii - cost)
        return Pair(currentLevel + 1, newResources)
    }
}
