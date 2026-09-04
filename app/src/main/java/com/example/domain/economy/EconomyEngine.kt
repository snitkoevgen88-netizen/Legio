package com.example.domain.economy

import com.example.model.*
import kotlin.math.max
import kotlin.math.min

object EconomyEngine {

    data class TransactionResult<T>(
        val isSuccess: Boolean,
        val updatedState: T,
        val updatedResources: LegionResources,
        val errorMessageRu: String? = null
    )

    /**
     * Buys grain/provisions from the market.
     */
    fun buyGrain(
        marketState: MarketState,
        resources: LegionResources,
        batchCount: Int = 1 // 1 batch = 40 provisions for marketState.grainPriceBuy denarii
    ): TransactionResult<MarketState> {
        val totalCost = marketState.grainPriceBuy * batchCount
        val grainGained = 40 * batchCount

        if (resources.denarii < totalCost) {
            return TransactionResult(
                isSuccess = false,
                updatedState = marketState,
                updatedResources = resources,
                errorMessageRu = "Недостаточно денариев в казне легиона ($totalCost необходимо)"
            )
        }

        val newResources = resources.copy(
            denarii = resources.denarii - totalCost,
            provisions = resources.provisions + grainGained
        )

        return TransactionResult(
            isSuccess = true,
            updatedState = marketState,
            updatedResources = newResources
        )
    }

    /**
     * Sells grain/provisions to the market.
     */
    fun sellGrain(
        marketState: MarketState,
        resources: LegionResources,
        batchCount: Int = 1 // 1 batch = 30 provisions for marketState.grainPriceSell denarii
    ): TransactionResult<MarketState> {
        val grainNeeded = 30 * batchCount
        val denariiGained = marketState.grainPriceSell * batchCount

        if (resources.provisions < grainNeeded) {
            return TransactionResult(
                isSuccess = false,
                updatedState = marketState,
                updatedResources = resources,
                errorMessageRu = "Недостаточно провианта в амбарах ($grainNeeded необходимо)"
            )
        }

        val newResources = resources.copy(
            denarii = resources.denarii + denariiGained,
            provisions = resources.provisions - grainNeeded
        )

        return TransactionResult(
            isSuccess = true,
            updatedState = marketState,
            updatedResources = newResources
        )
    }

    /**
     * Deposits denarii into the Roman bank.
     */
    fun depositDenarii(
        bankingState: RomanBankingState,
        resources: LegionResources,
        amount: Int
    ): TransactionResult<RomanBankingState> {
        if (amount <= 0 || resources.denarii < amount) {
            return TransactionResult(
                isSuccess = false,
                updatedState = bankingState,
                updatedResources = resources,
                errorMessageRu = "Недостаточно денариев для внесения депозита"
            )
        }

        val newResources = resources.copy(denarii = resources.denarii - amount)
        val newBanking = bankingState.copy(depositDenarii = bankingState.depositDenarii + amount)

        return TransactionResult(
            isSuccess = true,
            updatedState = newBanking,
            updatedResources = newResources
        )
    }

    /**
     * Withdraws denarii from the Roman bank.
     */
    fun withdrawDeposit(
        bankingState: RomanBankingState,
        resources: LegionResources,
        amount: Int
    ): TransactionResult<RomanBankingState> {
        if (amount <= 0 || bankingState.depositDenarii < amount) {
            return TransactionResult(
                isSuccess = false,
                updatedState = bankingState,
                updatedResources = resources,
                errorMessageRu = "Сумма снятия превышает размер депозита"
            )
        }

        val newResources = resources.copy(denarii = resources.denarii + amount)
        val newBanking = bankingState.copy(depositDenarii = bankingState.depositDenarii - amount)

        return TransactionResult(
            isSuccess = true,
            updatedState = newBanking,
            updatedResources = newResources
        )
    }

    /**
     * Takes a loan from Roman moneylenders.
     */
    fun takeLoan(
        bankingState: RomanBankingState,
        resources: LegionResources,
        loanAmount: Int,
        termSeasons: Int = 4
    ): TransactionResult<RomanBankingState> {
        if (bankingState.hasActiveLoan) {
            return TransactionResult(
                isSuccess = false,
                updatedState = bankingState,
                updatedResources = resources,
                errorMessageRu = "У вас уже есть непогашенный заем"
            )
        }

        val totalOwed = (loanAmount * 1.15f).toInt()
        val newResources = resources.copy(denarii = resources.denarii + loanAmount)
        val newBanking = bankingState.copy(
            activeLoanDenarii = totalOwed,
            loanSeasonsRemaining = termSeasons
        )

        return TransactionResult(
            isSuccess = true,
            updatedState = newBanking,
            updatedResources = newResources
        )
    }

    /**
     * Pays off existing loan in full.
     */
    fun payOffLoan(
        bankingState: RomanBankingState,
        resources: LegionResources
    ): TransactionResult<RomanBankingState> {
        if (!bankingState.hasActiveLoan) {
            return TransactionResult(
                isSuccess = false,
                updatedState = bankingState,
                updatedResources = resources,
                errorMessageRu = "Нет активных долговых обязательств"
            )
        }

        if (resources.denarii < bankingState.activeLoanDenarii) {
            return TransactionResult(
                isSuccess = false,
                updatedState = bankingState,
                updatedResources = resources,
                errorMessageRu = "Недостаточно денариев для полного погашения займа"
            )
        }

        val newResources = resources.copy(denarii = resources.denarii - bankingState.activeLoanDenarii)
        val newBanking = bankingState.copy(
            activeLoanDenarii = 0,
            loanSeasonsRemaining = 0
        )

        return TransactionResult(
            isSuccess = true,
            updatedState = newBanking,
            updatedResources = newResources
        )
    }

    /**
     * Distributes a donativum to boost cohort morale.
     */
    fun distributeDonativum(
        resources: LegionResources,
        cohorts: List<Cohort>,
        costPerCohort: Int = 15,
        moraleBoost: Int = 12
    ): Pair<LegionResources, List<Cohort>>? {
        val totalCost = cohorts.size * costPerCohort
        if (resources.denarii < totalCost) return null

        val newResources = resources.copy(denarii = resources.denarii - totalCost)
        val updatedCohorts = cohorts.map { cohort ->
            cohort.copy(morale = min(100, cohort.morale + moraleBoost))
        }

        return Pair(newResources, updatedCohorts)
    }

    /**
     * Upgrades a provincial investment.
     */
    fun upgradeInvestment(
        investment: ProvincialInvestment,
        resources: LegionResources
    ): TransactionResult<ProvincialInvestment> {
        if (investment.isMaxLevel) {
            return TransactionResult(
                isSuccess = false,
                updatedState = investment,
                updatedResources = resources,
                errorMessageRu = "Достигнут максимальный уровень развития инвестиции"
            )
        }

        val cost = investment.nextUpgradeCost
        if (resources.denarii < cost) {
            return TransactionResult(
                isSuccess = false,
                updatedState = investment,
                updatedResources = resources,
                errorMessageRu = "Недостаточно денариев ($cost необходимо)"
            )
        }

        val newResources = resources.copy(denarii = resources.denarii - cost)
        val updatedInvestment = investment.copy(level = investment.level + 1)

        return TransactionResult(
            isSuccess = true,
            updatedState = updatedInvestment,
            updatedResources = newResources
        )
    }

    /**
     * Paves a strategic road.
     */
    fun paveRoad(
        road: StrategicRoadUpgrade,
        resources: LegionResources
    ): TransactionResult<StrategicRoadUpgrade> {
        if (road.isPaved) {
            return TransactionResult(
                isSuccess = false,
                updatedState = road,
                updatedResources = resources,
                errorMessageRu = "Дорога уже замощена и укреплена"
            )
        }

        if (resources.denarii < road.costDenarii) {
            return TransactionResult(
                isSuccess = false,
                updatedState = road,
                updatedResources = resources,
                errorMessageRu = "Недостаточно денариев (${road.costDenarii} необходимо)"
            )
        }

        val newResources = resources.copy(denarii = resources.denarii - road.costDenarii)
        val updatedRoad = road.copy(isPaved = true)

        return TransactionResult(
            isSuccess = true,
            updatedState = updatedRoad,
            updatedResources = newResources
        )
    }

    /**
     * Crafts equipment at the legion forge.
     */
    fun craftEquipment(
        item: EquipmentItem,
        resources: LegionResources
    ): TransactionResult<EquipmentItem> {
        if (item.isCrafted) {
            return TransactionResult(
                isSuccess = false,
                updatedState = item,
                updatedResources = resources,
                errorMessageRu = "Снаряжение уже выковано"
            )
        }

        if (resources.denarii < item.costDenarii) {
            return TransactionResult(
                isSuccess = false,
                updatedState = item,
                updatedResources = resources,
                errorMessageRu = "Недостаточно денариев (${item.costDenarii} необходимо)"
            )
        }

        val newResources = resources.copy(denarii = resources.denarii - item.costDenarii)
        val craftedItem = item.copy(isCrafted = true)

        return TransactionResult(
            isSuccess = true,
            updatedState = craftedItem,
            updatedResources = newResources
        )
    }

    /**
     * Tempers/sharpens existing crafted equipment.
     */
    fun temperEquipment(
        item: EquipmentItem,
        resources: LegionResources
    ): TransactionResult<EquipmentItem> {
        if (!item.isCrafted) {
            return TransactionResult(
                isSuccess = false,
                updatedState = item,
                updatedResources = resources,
                errorMessageRu = "Сначала выкуйте базовый образец снаряжения"
            )
        }

        if (item.temperLevel >= 3) {
            return TransactionResult(
                isSuccess = false,
                updatedState = item,
                updatedResources = resources,
                errorMessageRu = "Достигнут максимальный уровень закалки оружия (+3)"
            )
        }

        val cost = (item.costDenarii * 0.6f * (item.temperLevel + 1)).toInt()
        if (resources.denarii < cost) {
            return TransactionResult(
                isSuccess = false,
                updatedState = item,
                updatedResources = resources,
                errorMessageRu = "Недостаточно денариев для закалки ($cost необходимо)"
            )
        }

        val newResources = resources.copy(denarii = resources.denarii - cost)
        val updatedItem = item.copy(temperLevel = item.temperLevel + 1)

        return TransactionResult(
            isSuccess = true,
            updatedState = updatedItem,
            updatedResources = newResources
        )
    }

    /**
     * Equips or un-equips an item for a cohort.
     */
    fun toggleEquipItem(
        allEquipment: List<EquipmentItem>,
        itemId: String,
        targetCohortId: String
    ): List<EquipmentItem> {
        val targetItem = allEquipment.find { it.id == itemId } ?: return allEquipment

        return allEquipment.map { item ->
            when {
                item.id == itemId -> {
                    if (item.equippedCohortId == targetCohortId) {
                        item.copy(equippedCohortId = null) // unequip
                    } else {
                        item.copy(equippedCohortId = targetCohortId) // equip
                    }
                }
                // If another item of the SAME type is equipped on this cohort, unequip it
                item.type == targetItem.type && item.equippedCohortId == targetCohortId -> {
                    item.copy(equippedCohortId = null)
                }
                else -> item
            }
        }
    }
}
