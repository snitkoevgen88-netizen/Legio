package com.example.domain.events

import com.example.model.*
import kotlin.math.max
import kotlin.math.min

object EventEngine {

    data class EventResolutionResult(
        val updatedResources: LegionResources,
        val updatedCohorts: List<Cohort>,
        val updatedSenateApproval: Int,
        val messageRu: String
    )

    /**
     * Resolves a camp event choice effect on legion state.
     */
    fun resolveChoice(
        choice: CampEventChoice,
        resources: LegionResources,
        cohorts: List<Cohort>
    ): EventResolutionResult {
        val newDenarii = max(0, resources.denarii + choice.denariiDelta)
        val newProvisions = max(0, resources.provisions + choice.provisionsDelta)
        val newGlory = max(0, resources.glory + choice.gloryDelta)
        val newSenateFavor = (resources.senateFavor + choice.senateFavorDelta).coerceIn(0, 100)

        val updatedCohorts = if (choice.moraleDelta != 0) {
            cohorts.map { cohort ->
                cohort.copy(morale = (cohort.morale + choice.moraleDelta).coerceIn(10, 100))
            }
        } else {
            cohorts
        }

        val updatedResources = resources.copy(
            denarii = newDenarii,
            provisions = newProvisions,
            glory = newGlory,
            senateFavor = newSenateFavor
        )

        return EventResolutionResult(
            updatedResources = updatedResources,
            updatedCohorts = updatedCohorts,
            updatedSenateApproval = newSenateFavor,
            messageRu = choice.resultLogRu
        )
    }
}
