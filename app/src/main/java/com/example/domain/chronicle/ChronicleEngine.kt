package com.example.domain.chronicle

import com.example.model.*
import java.util.UUID

object ChronicleEngine {

    /**
     * Filters and records major historical milestones in the Legion's permanent Annals.
     * «Не записывать рутину — фиксировать историю и судьбоносные решения.»
     */
    fun shouldRecordExpedition(
        expedition: Expedition,
        outcome: ExpeditionOutcome,
        casualties: Int,
        commanderPromoted: Boolean,
        commanderKilled: Boolean
    ): Boolean {
        // Record all great victories, disasters, senate trials, or events with promotions/deaths
        if (outcome == ExpeditionOutcome.GREAT_VICTORY || outcome == ExpeditionOutcome.DISASTER) return true
        if (commanderPromoted || commanderKilled) return true
        if (expedition.isSenateTrial) return true
        if (casualties >= 30) return true
        return outcome == ExpeditionOutcome.VICTORY && expedition.difficulty >= 3
    }

    fun recordBattleChronicle(
        seasonYear: SeasonYear,
        expedition: Expedition,
        commander: Commander,
        cohort: Cohort,
        outcome: ExpeditionOutcome,
        narrative: String,
        casualties: Int,
        lootDenarii: Int,
        lootProvisions: Int,
        gloryEarned: Int,
        traditionUnlocked: String? = null
    ): ChronicleEntry {
        val headline = "${outcome.icon} ${outcome.titleRu}: ${expedition.titleRu}"
        return ChronicleEntry(
            id = "chr_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
            seasonFormatted = seasonYear.formatted,
            yearBc = seasonYear.yearBc,
            headlineRu = headline,
            textRu = narrative,
            outcome = outcome,
            commanderName = commander.name,
            cohortName = cohort.name,
            casualties = casualties,
            lootDenarii = lootDenarii,
            lootProvisions = lootProvisions,
            gloryEarned = gloryEarned,
            traditionUnlocked = traditionUnlocked
        )
    }

    fun recordAquilaEvent(
        seasonYear: SeasonYear,
        isLost: Boolean,
        headline: String,
        description: String,
        gloryDelta: Int
    ): ChronicleEntry {
        return ChronicleEntry(
            id = "chr_aquila_${System.currentTimeMillis()}",
            seasonFormatted = seasonYear.formatted,
            yearBc = seasonYear.yearBc,
            headlineRu = headline,
            textRu = description,
            outcome = if (isLost) ExpeditionOutcome.DISASTER else ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Аквилифер IV Легиона",
            cohortName = "I Когорта Прима",
            casualties = 0,
            lootDenarii = 0,
            lootProvisions = 0,
            gloryEarned = gloryDelta
        )
    }

    fun recordPoliticalElection(
        seasonYear: SeasonYear,
        rank: MagistracyRank,
        isSuccess: Boolean,
        description: String
    ): ChronicleEntry {
        val headline = if (isSuccess) {
            "🏛️ Избрание на пост: ${rank.titleRu}"
        } else {
            "🏛️ Неудача на выборах в Риме"
        }

        return ChronicleEntry(
            id = "chr_pol_${System.currentTimeMillis()}",
            seasonFormatted = seasonYear.formatted,
            yearBc = seasonYear.yearBc,
            headlineRu = headline,
            textRu = description,
            outcome = if (isSuccess) ExpeditionOutcome.GREAT_VICTORY else ExpeditionOutcome.DEFEAT,
            commanderName = "Легат Легиона",
            cohortName = "Курия Гостилия",
            casualties = 0,
            lootDenarii = 0,
            lootProvisions = 0,
            gloryEarned = if (isSuccess) 10 else 0
        )
    }

    fun recordCommanderPromotion(
        seasonYear: SeasonYear,
        commander: Commander,
        newRankTitleRu: String
    ): ChronicleEntry {
        return ChronicleEntry(
            id = "chr_prom_${System.currentTimeMillis()}",
            seasonFormatted = seasonYear.formatted,
            yearBc = seasonYear.yearBc,
            headlineRu = "🎖️ Повышение в чине: ${commander.name} -> $newRankTitleRu",
            textRu = "За выдающиеся заслуги на поле брани и образцовую доблесть ${commander.name} возведен в звание $newRankTitleRu.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = commander.name,
            cohortName = "Штаб легиона",
            casualties = 0,
            lootDenarii = 0,
            lootProvisions = 0,
            gloryEarned = 5
        )
    }
}
