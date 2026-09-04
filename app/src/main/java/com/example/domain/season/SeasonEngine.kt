package com.example.domain.season

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

        // 1. Calculate building yields
        var baseDenariiGain = 35
        var baseProvisionsGain = 40

        currentState.buildings.forEach { bld ->
            when (bld.type) {
                BuildingType.PRINCIPIA -> baseDenariiGain += bld.level * 8
                BuildingType.CAMPUS_MARTIUS -> baseDenariiGain += bld.level * 4
                BuildingType.HORREUM -> baseProvisionsGain += bld.level * 18
                BuildingType.FABRICA -> baseDenariiGain += bld.level * 5
                BuildingType.VALETUDINARIUM -> baseProvisionsGain += bld.level * 4
                BuildingType.TABULARIUM -> baseDenariiGain += bld.level * 10
                BuildingType.THERMAE_LEGIONIS -> {}
                BuildingType.AQUILA_SHRINE -> {}
                BuildingType.CASTRA_EQUITUM -> baseDenariiGain += bld.level * 4
                BuildingType.BALLISTARIUM -> {}
                BuildingType.SPECULA -> {}
            }
        }

        // Season-specific bonuses
        when (newSeasonYear.season) {
            Season.SPRING -> baseDenariiGain += 15
            Season.SUMMER -> baseDenariiGain += 25
            Season.AUTUMN -> baseProvisionsGain += 60 // Harvest
            Season.WINTER -> {
                baseProvisionsGain = (baseProvisionsGain * 0.7f).toInt()
                baseDenariiGain = (baseDenariiGain * 0.8f).toInt()
            }
        }

        // 2. Investment yields
        var investmentDenarii = 0
        var investmentProvisions = 0
        var investmentGlory = 0

        currentState.investments.forEach { inv ->
            investmentDenarii += inv.currentYieldDenarii
            investmentProvisions += inv.currentYieldProvisions
            investmentGlory += inv.currentYieldGlory
        }

        // 3. Upgrades from Seasonal Plan
        val updatedBuildings = currentState.buildings.map { bld ->
            if (currentState.seasonalPlan.upgradeBuildingType == bld.type && bld.level < bld.maxLevel) {
                bld.copy(level = bld.level + 1)
            } else {
                bld
            }
        }

        // 4. Cohort replenishment & food consumption
        var totalSoldiers = 0
        val updatedCohorts = currentState.cohorts.map { cohort ->
            totalSoldiers += cohort.soldiers
            val isTargetTraining = currentState.seasonalPlan.trainCohortId == cohort.id
            val soldiersGained = if (cohort.soldiers < cohort.maxSoldiers) {
                val recoverySpeed = if (isTargetTraining) 14 else 8
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

        val grainConsumed = (totalSoldiers * 0.35f).toInt()

        // 5. Banking interest & loan repayment
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

        // 6. Resources calculation
        val netDenarii = max(0, currentState.resources.denarii + baseDenariiGain + investmentDenarii - loanPaymentDeducted)
        val netProvisions = max(0, currentState.resources.provisions + baseProvisionsGain + investmentProvisions - grainConsumed)
        val netGlory = currentState.resources.glory + investmentGlory

        val updatedResources = currentState.resources.copy(
            denarii = netDenarii,
            provisions = netProvisions,
            glory = netGlory
        )

        // 7. Competing Legions AI update
        val updatedCompetingLegions = currentState.competingLegions.map { legion ->
            val aiWins = Random.nextBoolean()
            val scoreDelta = if (aiWins) Random.nextInt(15, 35) else Random.nextInt(-10, 10)
            legion.copy(
                ratingScore = max(100, legion.ratingScore + scoreDelta),
                victories = if (aiWins) legion.victories + 1 else legion.victories,
                defeats = if (!aiWins) legion.defeats + 1 else legion.defeats
            )
        }.sortedByDescending { it.ratingScore }

        // 8. Active Blessing duration
        val updatedBlessing = currentState.activeBlessing?.let { blessing ->
            val remaining = blessing.seasonsRemaining - 1
            if (remaining > 0) blessing.copy(seasonsRemaining = remaining) else null
        }

        // 9. Random Seasonal Event
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

        val summary = "Наступил ${newSeasonYear.formatted}. Доход: +${baseDenariiGain + investmentDenarii} 🪙, +${baseProvisionsGain + investmentProvisions} 🌾. Расход зерна: -$grainConsumed 🌾."

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
