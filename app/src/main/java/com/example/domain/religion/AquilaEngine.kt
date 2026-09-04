package com.example.domain.religion

import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object AquilaEngine {

    data class AquilaUpgradeResult(
        val isSuccess: Boolean,
        val updatedAquila: LegionAquilaState,
        val updatedResources: LegionResources,
        val gloryGained: Int,
        val messageRu: String
    )

    data class AquilaLossResult(
        val isLost: Boolean,
        val updatedAquila: LegionAquilaState,
        val gloryLost: Int,
        val senateFavorLost: Int,
        val moralePenalty: Int,
        val alertMessageRu: String
    )

    data class AquilaReclaimResult(
        val isReclaimed: Boolean,
        val updatedAquila: LegionAquilaState,
        val updatedResources: LegionResources,
        val gloryGained: Int,
        val senateFavorGained: Int,
        val messageRu: String
    )

    /**
     * Consecrates and upgrades the sacred Legion Aquila standard.
     * «Получение и усиление Aquila: Major Glory, Senate recognition, Chronicle entry.»
     */
    fun upgradeAquila(
        aquilaState: LegionAquilaState,
        resources: LegionResources
    ): AquilaUpgradeResult {
        if (aquilaState.isAquilaLost) {
            return AquilaUpgradeResult(
                isSuccess = false,
                updatedAquila = aquilaState,
                updatedResources = resources,
                gloryGained = 0,
                messageRu = "Орёл легиона потерян в бою! Сначала верните святыню Рима."
            )
        }

        if (aquilaState.eagleUpgradeLevel >= 3) {
            return AquilaUpgradeResult(
                isSuccess = false,
                updatedAquila = aquilaState,
                updatedResources = resources,
                gloryGained = 0,
                messageRu = "Достигнут высший ранг освящения Святыни Рима (Legio Invicta)."
            )
        }

        val cost = aquilaState.upgradeCostDenarii
        if (resources.denarii < cost) {
            return AquilaUpgradeResult(
                isSuccess = false,
                updatedAquila = aquilaState,
                updatedResources = resources,
                gloryGained = 0,
                messageRu = "Недостаточно денариев для освящения Орла ($cost необходимо)"
            )
        }

        val gloryBonus = 15 + (aquilaState.eagleUpgradeLevel * 10)
        val updatedAquila = aquilaState.copy(
            eagleUpgradeLevel = aquilaState.eagleUpgradeLevel + 1,
            totalSacredGlory = aquilaState.totalSacredGlory + gloryBonus
        )

        val updatedResources = resources.copy(
            denarii = resources.denarii - cost,
            glory = resources.glory + gloryBonus,
            senateFavor = min(100, resources.senateFavor + 8)
        )

        return AquilaUpgradeResult(
            isSuccess = true,
            updatedAquila = updatedAquila,
            updatedResources = updatedResources,
            gloryGained = gloryBonus,
            messageRu = "Священный Орёл освящён перед понтификами! Слава легиона возросла на +$gloryBonus."
        )
    }

    /**
     * Evaluates battle disaster risk of losing the sacred Aquila to barbarians.
     * «Потеря Aquila: Glory loss, Morale consequences, Political consequences, Chronicle entry.»
     */
    fun evaluateBattleDisaster(
        aquilaState: LegionAquilaState,
        outcome: ExpeditionOutcome,
        casualtiesRatio: Float,
        hasJupiterBlessing: Boolean,
        randomSeed: Int = Random.nextInt(100)
    ): AquilaLossResult {
        if (aquilaState.isAquilaLost) {
            return AquilaLossResult(
                isLost = true,
                updatedAquila = aquilaState,
                gloryLost = 0,
                senateFavorLost = 0,
                moralePenalty = 0,
                alertMessageRu = "Орёл легиона уже находится в руках врагов."
            )
        }

        // Jupiter blessing or non-disaster guarantees Aquila safety
        if (hasJupiterBlessing || outcome != ExpeditionOutcome.DISASTER) {
            return AquilaLossResult(
                isLost = false,
                updatedAquila = aquilaState,
                gloryLost = 0,
                senateFavorLost = 0,
                moralePenalty = 0,
                alertMessageRu = "Аквилифер и первая когорта удержали священный штандарт."
            )
        }

        // DISASTER scenario: high risk if casualties are catastrophic
        val lossChancePct = if (casualtiesRatio > 0.40f) 45 else 20
        val isLostRoll = (randomSeed % 100) < lossChancePct

        return if (isLostRoll) {
            val updated = aquilaState.copy(isAquilaLost = true)
            AquilaLossResult(
                isLost = true,
                updatedAquila = updated,
                gloryLost = 35,
                senateFavorLost = 25,
                moralePenalty = 30,
                alertMessageRu = "💀 КАТАСТРОФА: Варвары захватили Золотого Орла легиона! Рим в трауре и позоре!"
            )
        } else {
            AquilaLossResult(
                isLost = false,
                updatedAquila = aquilaState,
                gloryLost = 0,
                senateFavorLost = 0,
                moralePenalty = 0,
                alertMessageRu = "Аквилифер ценой своей жизни вынес Орла из окружения."
            )
        }
    }

    /**
     * Reclaims a lost Aquila upon a decisive victory in a retribution campaign.
     */
    fun reclaimLostAquila(
        aquilaState: LegionAquilaState,
        resources: LegionResources,
        outcome: ExpeditionOutcome
    ): AquilaReclaimResult {
        if (!aquilaState.isAquilaLost) {
            return AquilaReclaimResult(
                isReclaimed = false,
                updatedAquila = aquilaState,
                updatedResources = resources,
                gloryGained = 0,
                senateFavorGained = 0,
                messageRu = "Орёл легиона не был утерян."
            )
        }

        if (!outcome.isSuccess) {
            return AquilaReclaimResult(
                isReclaimed = false,
                updatedAquila = aquilaState,
                updatedResources = resources,
                gloryGained = 0,
                senateFavorGained = 0,
                messageRu = "Поход возмездия не увенчался успехом. Орёл всё ещё у врагов."
            )
        }

        val gloryAward = if (outcome == ExpeditionOutcome.GREAT_VICTORY) 45 else 30
        val favorAward = 25

        val updated = aquilaState.copy(
            isAquilaLost = false,
            totalSacredGlory = aquilaState.totalSacredGlory + gloryAward
        )

        val updatedRes = resources.copy(
            glory = resources.glory + gloryAward,
            senateFavor = min(100, resources.senateFavor + favorAward)
        )

        return AquilaReclaimResult(
            isReclaimed = true,
            updatedAquila = updated,
            updatedResources = updatedRes,
            gloryGained = gloryAward,
            senateFavorGained = favorAward,
            messageRu = "🦅 ТРИУМФ ВОЗМЕЗДИЯ! Священный Орёл возвращён легиону! Позор смыт вражеской кровью!"
        )
    }

    /**
     * Customizes the Vexillum motto and standard color.
     */
    fun customizeVexillum(
        aquilaState: LegionAquilaState,
        motto: String,
        bannerColorIndex: Int
    ): LegionAquilaState {
        return aquilaState.copy(
            customVexillumMotto = motto.trim().ifEmpty { "SENATVS POPVLVSQVE ROMANVS • LEGIO IV" },
            selectedBannerColorIndex = bannerColorIndex.coerceIn(0, 3)
        )
    }
}
