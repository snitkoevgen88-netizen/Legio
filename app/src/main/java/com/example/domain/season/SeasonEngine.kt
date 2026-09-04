package com.example.domain.season

import com.example.domain.economy.EconomyEngine
import com.example.domain.senate.SenateEngine
import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object SeasonEngine {

    data class SeasonAdvanceResult(
        val newSeasonYear: SeasonYear,
        val updatedResources: LegionResources,
        val updatedCohorts: List<Cohort>,
        val updatedBuildings: List<Building>,
        val updatedInvestments: List<ProvincialInvestment>,
        val updatedBanking: RomanBankingState,
        val updatedMarket: MarketState,
        val updatedCompetingLegions: List<CompetingLegion>,
        val updatedActiveBlessing: ActiveBlessing?,
        val generatedEvent: CampEvent?,
        val seasonalSummaryRu: String
    )

    /**
     * Advances the game state by one season (Spring -> Summer -> Autumn -> Winter -> Spring).
     * Integrates army maintenance, infrastructure yield, banking interest, competing legions and event logic.
     */
    fun advanceSeason(
        currentState: GameUiState
    ): SeasonAdvanceResult {
        val nextSeasonIndex = (currentState.seasonYear.seasonIndex + 1) % 4
        val nextSeasonNumber = currentState.seasonYear.seasonNumber + 1
        val nextYearBc = if (nextSeasonIndex == 0) currentState.seasonYear.yearBc - 1 else currentState.seasonYear.yearBc

        val newSeasonYear = SeasonYear(
            seasonIndex = nextSeasonIndex,
            seasonNumber = nextSeasonNumber,
            yearBc = nextYearBc
        )

        // 1. Upgrades from Seasonal Plan
        val updatedBuildings = currentState.buildings.map { bld ->
            if (currentState.seasonalPlan.upgradeBuildingType == bld.type && bld.level < bld.maxLevel) {
                bld.copy(level = bld.level + 1)
            } else {
                bld
            }
        }

        // 2. Calculate Gross Yields & Maintenance via EconomyEngine
        val yield = EconomyEngine.calculateSeasonalYield(
            buildings = updatedBuildings,
            investments = currentState.investments,
            seasonYear = newSeasonYear,
            senateFavor = currentState.resources.senateFavor,
            doctrines = currentState.doctrines
        )

        val maintenance = EconomyEngine.calculateMaintenance(
            cohorts = currentState.cohorts,
            commanders = currentState.commanders,
            buildings = updatedBuildings,
            doctrines = currentState.doctrines
        )

        // 3. Cohort replenishment & experience
        val updatedCohorts = currentState.cohorts.map { cohort ->
            val isTargetTraining = currentState.seasonalPlan.trainCohortId == cohort.id
            val soldiersGained = if (cohort.soldiers < cohort.maxSoldiers) {
                val recoverySpeed = if (isTargetTraining) 14 else 6
                min(cohort.maxSoldiers - cohort.soldiers, recoverySpeed)
            } else 0

            val moraleRecovery = if (cohort.morale < 85) 5 else 0
            val xpGain = if (isTargetTraining) 25 else 5

            cohort.copy(
                soldiers = cohort.soldiers + soldiersGained,
                morale = min(100, cohort.morale + moraleRecovery),
                xp = cohort.xp + xpGain
            )
        }

        // 4. Banking interest & loan repayment
        var bankingState = currentState.bankingState
        var loanPaymentDeducted = 0
        var interestEarned = 0

        if (bankingState.depositDenarii > 0) {
            interestEarned = (bankingState.depositDenarii * 0.05f).toInt()
            bankingState = bankingState.copy(
                depositDenarii = bankingState.depositDenarii + interestEarned,
                totalInterestEarned = bankingState.totalInterestEarned + interestEarned
            )
        }

        if (bankingState.hasActiveLoan && bankingState.loanSeasonsRemaining > 0) {
            val payment = bankingState.seasonalLoanPayment
            loanPaymentDeducted = payment
            val remainingLoan = max(0, bankingState.activeLoanDenarii - payment)
            val remainingSeasons = max(0, bankingState.loanSeasonsRemaining - 1)
            bankingState = bankingState.copy(
                activeLoanDenarii = remainingLoan,
                loanSeasonsRemaining = remainingSeasons
            )
        }

        // 5. Net Resources calculation (Income - Maintenance - Loans)
        val grossDenarii = currentState.resources.denarii + yield.totalDenariiIncome
        val netDenarii = max(0, grossDenarii - maintenance.netDenariiMaintenance - loanPaymentDeducted)

        val grossProvisions = currentState.resources.provisions + yield.totalProvisionsIncome
        val netProvisions = max(0, grossProvisions - maintenance.netProvisionsConsumed)

        val netGlory = currentState.resources.glory + yield.totalGloryIncome

        val updatedResources = currentState.resources.copy(
            denarii = netDenarii,
            provisions = netProvisions,
            glory = netGlory
        )

        // 6. Competing Legions AI update via SenateEngine
        val updatedCompetingLegions = SenateEngine.updateCompetingLegions(
            currentLegions = currentState.competingLegions,
            playerGlory = updatedResources.glory,
            playerVictories = currentState.commanders.sumOf { it.victoriesCount },
            playerSenateFavor = updatedResources.senateFavor,
            seasonNumber = nextSeasonNumber
        )

        // 7. Active Blessing duration countdown
        val updatedBlessing = currentState.activeBlessing?.let { blessing ->
            val remaining = blessing.seasonsRemaining - 1
            if (remaining > 0) blessing.copy(seasonsRemaining = remaining) else null
        }

        // 8. Random Seasonal Event
        val eventList = listOf(
            CampEvent(
                id = "evt_harvest_bounty",
                titleRu = "Обильный урожай в Кампании",
                descRu = "Торговцы доставили в лагерь свежие обозы зерна и вина. Сенат доволен положением дел на юге.",
                icon = "🌾",
                choices = listOf(
                    CampEventChoice(
                        textRu = "Принять дар и распределить по складам",
                        effectDescRu = "+50 🌾, +5 к морали",
                        provisionsDelta = 50,
                        moraleDelta = 5,
                        resultLogRu = "Склады Horreum заполнены первосортным кампанским зерном."
                    ),
                    CampEventChoice(
                        textRu = "Продать излишки на форуме",
                        effectDescRu = "+40 🪙",
                        denariiDelta = 40,
                        resultLogRu = "Казна легиона пополнилась звонкой монетой."
                    )
                )
            ),
            CampEvent(
                id = "evt_senate_delegation",
                titleRu = "Посольство Сената из Рима",
                descRu = "Патриции прибыли с инспекцией легионного каструма. Они требуют отчета о расходовании денариев.",
                icon = "🏛️",
                choices = listOf(
                    CampEventChoice(
                        textRu = "Устроить пышный пир для сенаторов",
                        effectDescRu = "-30 🪙, +12 Одобрение Сената",
                        denariiDelta = -30,
                        senateFavorDelta = 12,
                        resultLogRu = "Сенаторы впечатлены гостеприимством и похвалили порядок в лагере."
                    ),
                    CampEventChoice(
                        textRu = "Показать суровую дисциплину когорт",
                        effectDescRu = "+15 к морали, +5 Слава",
                        gloryDelta = 5,
                        moraleDelta = 15,
                        resultLogRu = "Легионеры продемонстрировали идеальные маневры на Campus Martius."
                    )
                )
            ),
            CampEvent(
                id = "evt_veteran_petition",
                titleRu = "Прошение ветеранов триариев",
                descRu = "Старейшие бойцы легиона просят о выделении земельных наделов в плодородных долинах Лация.",
                icon = "📜",
                choices = listOf(
                    CampEventChoice(
                        textRu = "Пообещать участки земли после триумфа",
                        effectDescRu = "+20 к морали",
                        moraleDelta = 20,
                        resultLogRu = "Ветераны воодушевлены и дали клятву стоять насмерть."
                    ),
                    CampEventChoice(
                        textRu = "Выдать денежную награду из казны",
                        effectDescRu = "-40 🪙, +10 к опыту",
                        denariiDelta = -40,
                        cohortXpDelta = 10,
                        resultLogRu = "Щедрое жалование укрепило авторитет командования."
                    )
                )
            )
        )

        val generatedEvent = if (Random.nextInt(100) < 65) eventList.random() else null

        val summary = "Наступил ${newSeasonYear.formatted}. Доход: +${yield.totalDenariiIncome} 🪙, +${yield.totalProvisionsIncome} 🌾. Содержание армии: -${maintenance.netDenariiMaintenance} 🪙, -${maintenance.netProvisionsConsumed} 🌾."

        return SeasonAdvanceResult(
            newSeasonYear = newSeasonYear,
            updatedResources = updatedResources,
            updatedCohorts = updatedCohorts,
            updatedBuildings = updatedBuildings,
            updatedInvestments = currentState.investments,
            updatedBanking = bankingState,
            updatedMarket = currentState.marketState,
            updatedCompetingLegions = updatedCompetingLegions,
            updatedActiveBlessing = updatedBlessing,
            generatedEvent = generatedEvent,
            seasonalSummaryRu = summary
        )
    }
}
