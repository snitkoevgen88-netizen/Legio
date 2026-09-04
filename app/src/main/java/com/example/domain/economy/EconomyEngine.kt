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

    data class LegionMaintenanceBreakdown(
        val totalSoldiers: Int,
        val totalVeterans: Int,
        val baseGrainConsumed: Int,
        val soldierStipendDenarii: Int,
        val officerStipendsDenarii: Int,
        val buildingUpkeepDenarii: Int,
        val tabulariumDiscountDenarii: Int,
        val netDenariiMaintenance: Int,
        val netProvisionsConsumed: Int
    )

    data class SeasonalYieldBreakdown(
        val baseDenarii: Int,
        val baseProvisions: Int,
        val buildingDenarii: Int,
        val buildingProvisions: Int,
        val investmentDenarii: Int,
        val investmentProvisions: Int,
        val investmentGlory: Int,
        val senateStipendDenarii: Int,
        val seasonModifierDescRu: String,
        val totalDenariiIncome: Int,
        val totalProvisionsIncome: Int,
        val totalGloryIncome: Int
    )

    /**
     * Calculates itemized seasonal maintenance for the entire legion army and infrastructure.
     * «Армия должна стоить денег и хлеба.»
     */
    fun calculateMaintenance(
        cohorts: List<Cohort>,
        commanders: List<Commander>,
        buildings: List<Building>,
        doctrines: List<MilitaryDoctrine> = emptyList()
    ): LegionMaintenanceBreakdown {
        val totalSoldiers = cohorts.sumOf { it.soldiers }
        val totalVeterans = cohorts.sumOf { it.veteransCount }

        // Grain consumption: ~0.35 provisions per soldier
        val horreumLevel = buildings.find { it.type == BuildingType.HORREUM }?.level ?: 1
        val horreumEfficiency = 1.0f - (horreumLevel - 1) * 0.08f // Up to 16% grain conservation
        val baseGrainConsumed = max(10, ((totalSoldiers * 0.35f) * horreumEfficiency).toInt())

        // Soldier base stipend & veteran bonus
        val soldierStipend = max(10, (totalSoldiers * 0.20f).toInt() + (totalVeterans * 1))

        // Officer stipends based on rank
        val officerStipends = commanders.filter { it.isAlive }.sumOf { cmd ->
            when (cmd.rankIndex) {
                3 -> 18 // Legatus
                2 -> 10 // Tribunus
                else -> 5 // Centurion
            }
        }

        // Building infrastructure upkeep (1..3 denarii for developed facilities)
        val buildingUpkeep = buildings.filter { it.level > 1 }.sumOf { (it.level - 1) * 3 }

        // Tabularium tax accounting discount
        val tabulariumLevel = buildings.find { it.type == BuildingType.TABULARIUM }?.level ?: 1
        val unlockedDocs = doctrines.filter { it.isUnlocked }.map { it.id }.toSet()
        val doctrineDiscountPct = if (unlockedDocs.contains("doc_disciplina") || unlockedDocs.contains("doc_disciplina_ferrea")) 0.10f else 0.0f
        val discountRate = min(0.40f, (tabulariumLevel * 0.08f) + doctrineDiscountPct)

        val grossDenarii = soldierStipend + officerStipends + buildingUpkeep
        val discountDenarii = (grossDenarii * discountRate).toInt()
        val netDenarii = max(5, grossDenarii - discountDenarii)

        return LegionMaintenanceBreakdown(
            totalSoldiers = totalSoldiers,
            totalVeterans = totalVeterans,
            baseGrainConsumed = baseGrainConsumed,
            soldierStipendDenarii = soldierStipend,
            officerStipendsDenarii = officerStipends,
            buildingUpkeepDenarii = buildingUpkeep,
            tabulariumDiscountDenarii = discountDenarii,
            netDenariiMaintenance = netDenarii,
            netProvisionsConsumed = baseGrainConsumed
        )
    }

    /**
     * Calculates full seasonal gross yields from camp facilities, provincial investments, and the Senate.
     */
    fun calculateSeasonalYield(
        buildings: List<Building>,
        investments: List<ProvincialInvestment>,
        seasonYear: SeasonYear,
        senateFavor: Int,
        doctrines: List<MilitaryDoctrine> = emptyList()
    ): SeasonalYieldBreakdown {
        var bldDenarii = 0
        var bldProvisions = 0

        buildings.forEach { bld ->
            when (bld.type) {
                BuildingType.PRINCIPIA -> bldDenarii += bld.level * 8
                BuildingType.CAMPUS_MARTIUS -> bldDenarii += bld.level * 4
                BuildingType.HORREUM -> bldProvisions += bld.level * 18
                BuildingType.FABRICA -> bldDenarii += bld.level * 6
                BuildingType.VALETUDINARIUM -> bldProvisions += bld.level * 5
                BuildingType.TABULARIUM -> bldDenarii += bld.level * 12
                BuildingType.CASTRA_EQUITUM -> bldDenarii += bld.level * 5
                else -> {}
            }
        }

        var baseDenarii = 30
        var baseProvisions = 35
        var seasonDesc = "Умеренный сезон"

        when (seasonYear.season) {
            Season.SPRING -> {
                baseDenarii += 15
                seasonDesc = "🌱 Весенний набор и торговые караваны (+15 🪙)"
            }
            Season.SUMMER -> {
                baseDenarii += 25
                seasonDesc = "☀️ Летний сезон походов и сбора дани (+25 🪙)"
            }
            Season.AUTUMN -> {
                baseProvisions += 65
                seasonDesc = "🍂 Осенний урожай в Кампании (+65 🌾)"
            }
            Season.WINTER -> {
                baseProvisions = (baseProvisions * 0.70f).toInt()
                baseDenarii = (baseDenarii * 0.80f).toInt()
                seasonDesc = "❄️ Зимние квартиры (Снижен сбор припасов на 25%)"
            }
        }

        var invDenarii = 0
        var invProvisions = 0
        var invGlory = 0

        investments.forEach { inv ->
            invDenarii += inv.currentYieldDenarii
            invProvisions += inv.currentYieldProvisions
            invGlory += inv.currentYieldGlory
        }

        val principiaLevel = buildings.find { it.type == BuildingType.PRINCIPIA }?.level ?: 1
        val unlockedDocs = doctrines.filter { it.isUnlocked }.map { it.id }.toSet()
        var senateStipend = (senateFavor * 1.5f + 30 + (principiaLevel * 10)).toInt()
        if (unlockedDocs.contains("doc_imperium")) {
            senateStipend += 40
        }

        val totalDenarii = baseDenarii + bldDenarii + invDenarii + senateStipend
        val totalProvisions = baseProvisions + bldProvisions + invProvisions

        return SeasonalYieldBreakdown(
            baseDenarii = baseDenarii,
            baseProvisions = baseProvisions,
            buildingDenarii = bldDenarii,
            buildingProvisions = bldProvisions,
            investmentDenarii = invDenarii,
            investmentProvisions = invProvisions,
            investmentGlory = invGlory,
            senateStipendDenarii = senateStipend,
            seasonModifierDescRu = seasonDesc,
            totalDenariiIncome = totalDenarii,
            totalProvisionsIncome = totalProvisions,
            totalGloryIncome = invGlory
        )
    }

    /**
     * Replenishes cohort casualties with fresh recruits, preserving veteran ratio.
     * «Потери требуют денариев и зерна для восстановления.»
     */
    fun replenishCohort(
        cohort: Cohort,
        recruitsCount: Int,
        resources: LegionResources,
        campLevel: Int = 1,
        hasPopularesDiscount: Boolean = false
    ): TransactionResult<Cohort> {
        val needed = cohort.maxSoldiers - cohort.soldiers
        if (needed <= 0) {
            return TransactionResult(
                isSuccess = false,
                updatedState = cohort,
                updatedResources = resources,
                errorMessageRu = "Когорта уже укомплектована до предела (${cohort.maxSoldiers} легионеров)"
            )
        }

        val countToAdd = min(needed, max(1, recruitsCount))
        var costPerRecruitDenarii = 3
        var costPerRecruitProvisions = 2

        if (hasPopularesDiscount) {
            costPerRecruitDenarii = max(2, (costPerRecruitDenarii * 0.75f).toInt())
        }
        if (campLevel >= 6) {
            costPerRecruitProvisions = max(1, (costPerRecruitProvisions * 0.8f).toInt())
        }

        val totalDenariiCost = countToAdd * costPerRecruitDenarii
        val totalProvisionsCost = countToAdd * costPerRecruitProvisions

        if (resources.denarii < totalDenariiCost || resources.provisions < totalProvisionsCost) {
            return TransactionResult(
                isSuccess = false,
                updatedState = cohort,
                updatedResources = resources,
                errorMessageRu = "Недостаточно ресурсов для набора ($totalDenariiCost 🪙, $totalProvisionsCost 🌾 необходимо)"
            )
        }

        val newResources = resources.copy(
            denarii = resources.denarii - totalDenariiCost,
            provisions = resources.provisions - totalProvisionsCost
        )

        val updatedCohort = cohort.copy(
            soldiers = cohort.soldiers + countToAdd,
            morale = min(100, cohort.morale + 3)
        )

        return TransactionResult(
            isSuccess = true,
            updatedState = updatedCohort,
            updatedResources = newResources
        )
    }

    /**
     * Calculates logistics and supply cost for launching an expedition.
     */
    fun calculateExpeditionSupplyCost(
        expedition: Expedition,
        commander: Commander?
    ): Pair<Int, Int> {
        var denariiCost = expedition.denariiCost
        var provisionsCost = expedition.provisionsCost

        if (commander?.unlockedTalents?.contains(OfficerTalent.LOGISTICS_GENIUS) == true) {
            provisionsCost = max(5, (provisionsCost * 0.75f).toInt())
        }

        return Pair(denariiCost, provisionsCost)
    }

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
