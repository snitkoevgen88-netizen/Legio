package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.*
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = GameRepository(database)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadSavedGame()
    }

    private fun loadSavedGame() {
        viewModelScope.launch(Dispatchers.IO) {
            val snapshot = repository.loadSnapshot()
            if (snapshot != null) {
                val loadedState = snapshot.toUiState()
                _uiState.value = loadedState
            }
        }
    }

    private fun persistGameState() {
        val currentState = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSnapshot(currentState.toSnapshot())
        }
    }

    // Toggle Sound
    fun toggleSound() {
        val newValue = !_uiState.value.isSoundEnabled
        SoundManager.isSoundEnabled = newValue
        _uiState.update { it.copy(isSoundEnabled = newValue) }
    }

    // Update Seasonal Plan
    fun setPlanTrainingCohort(cohortId: String?) {
        _uiState.update {
            it.copy(seasonalPlan = it.seasonalPlan.copy(trainCohortId = cohortId))
        }
        SoundManager.playDrumBeat()
    }

    fun setPlanUpgradeBuilding(type: BuildingType?) {
        _uiState.update {
            it.copy(seasonalPlan = it.seasonalPlan.copy(upgradeBuildingType = type))
        }
        SoundManager.playDrumBeat()
    }

    fun setPlanExpedition(expeditionId: String?, commanderId: String?, cohortId: String?, tactics: Tactics = Tactics.BALANCED) {
        _uiState.update {
            it.copy(
                seasonalPlan = it.seasonalPlan.copy(
                    launchedExpeditionId = expeditionId,
                    selectedCommanderId = commanderId,
                    selectedCohortId = cohortId,
                    selectedTactics = tactics
                )
            )
        }
        SoundManager.playWarHorn()
    }

    fun setPlanTactics(tactics: Tactics) {
        _uiState.update {
            it.copy(seasonalPlan = it.seasonalPlan.copy(selectedTactics = tactics))
        }
        SoundManager.playSwordClash()
    }

    fun openSeasonPlanDialog() {
        _uiState.update { it.copy(showSeasonPlanDialog = true) }
    }

    fun dismissSeasonPlanDialog() {
        _uiState.update { it.copy(showSeasonPlanDialog = false) }
    }

    fun dismissBattleResultDialog() {
        _uiState.update { it.copy(showBattleResultDialog = false) }
        // After battle, check if there's an event or golden age
        checkPendingEvents()
    }

    fun dismissGoldenAgeDialog() {
        _uiState.update { it.copy(showGoldenAgeDialog = false) }
    }

    // Calculate dynamic battle odds for UI
    fun calculateBattleOdds(
        expedition: Expedition,
        commander: Commander,
        cohort: Cohort,
        tactics: Tactics
    ): BattleOddsPreview {
        var greatVictoryBase = 15
        var victoryBase = 45
        var partialBase = 25
        var defeatBase = 12
        var disasterBase = 3

        val state = _uiState.value
        val unlockedDoctrines = state.doctrines.filter { it.isUnlocked }.map { it.id }.toSet()

        // Equipped items on this cohort
        val equippedItems = state.equipment.filter { it.isCrafted && it.equippedCohortId == cohort.id }
        val eqAttack = equippedItems.sumOf { it.attackBonus }
        val eqDefense = equippedItems.sumOf { it.defenseBonus }
        val eqMorale = equippedItems.sumOf { it.moraleBonus }

        // Commander trait adjustments
        greatVictoryBase += commander.trait.victoryBonusChance
        disasterBase += commander.trait.disasterRiskChange
        victoryBase += (commander.level * 3)

        // Cohort experience, equipment & veterans
        victoryBase += (cohort.level * 4) + (cohort.veteransCount / 2) + (eqAttack / 2) + (eqDefense / 2)
        defeatBase -= (cohort.discipline / 5) + (eqDefense / 2)
        greatVictoryBase += (eqMorale / 10)

        // DOCTRINES SYNERGIES
        if (unlockedDoctrines.contains("doc_disciplina_ferrea")) {
            victoryBase += 6
            defeatBase -= 4
            disasterBase = max(1, disasterBase - 3)
        }
        if (unlockedDoctrines.contains("doc_pilum_volley")) {
            greatVictoryBase += 15
            victoryBase += 5
        }
        if (unlockedDoctrines.contains("doc_testudo")) {
            disasterBase = max(0, disasterBase - 8)
            defeatBase = max(2, defeatBase - 6)
            partialBase += 8
        }
        if (unlockedDoctrines.contains("doc_equites")) {
            greatVictoryBase += 10
            victoryBase += 6
        }

        // Tactics vs Enemy Intel synergy
        when (tactics) {
            Tactics.AGGRESSIVE -> {
                greatVictoryBase += 20
                disasterBase += 6
                defeatBase += 4
            }
            Tactics.CAUTIOUS -> {
                greatVictoryBase -= 10
                disasterBase -= 8
                partialBase += 15
            }
            Tactics.BALANCED -> {
                victoryBase += 6
            }
            Tactics.TESTUDO -> {
                disasterBase -= 12
                defeatBase -= 8
                partialBase += 12
            }
            Tactics.FLANK_AMBUSH -> {
                greatVictoryBase += 25
                disasterBase += 4
                victoryBase -= 4
            }
        }

        // Season effect (Summer boosts campaigns)
        if (state.seasonYear.season == Season.SUMMER) {
            victoryBase += 15
            greatVictoryBase += 5
        } else if (state.seasonYear.season == Season.WINTER) {
            disasterBase = max(1, disasterBase - 4)
        }

        // Divine Blessing modifiers
        when (state.activeBlessing?.god) {
            GodType.MARS -> {
                greatVictoryBase += 18
                victoryBase += 8
            }
            GodType.JUPITER -> {
                disasterBase = 0
                victoryBase += 6
            }
            GodType.FORTUNA -> {
                disasterBase = max(0, disasterBase - 12)
                defeatBase = max(0, defeatBase - 8)
                partialBase += 10
            }
            GodType.MINERVA -> {
                victoryBase += 8
            }
            GodType.CERES -> {
                // Ceres boosts logistics
            }
            null -> {}
        }

        // Trophies passive bonuses
        val unlockedTrophies = state.trophies.filter { it.isUnlocked }.map { it.id }.toSet()
        if (unlockedTrophies.contains("trophy_pyrrhic_phalanx_banner")) {
            greatVictoryBase += 6
        }
        if (unlockedTrophies.contains("trophy_samnite_crest")) {
            defeatBase = max(0, defeatBase - 4)
            disasterBase = max(0, disasterBase - 4)
        }

        // Difficulty scaling
        val diffPenalty = (expedition.difficulty - 1) * 8
        victoryBase -= diffPenalty
        greatVictoryBase -= (diffPenalty / 2)
        defeatBase += (diffPenalty / 2)
        disasterBase += (diffPenalty / 3)

        // Clamp
        greatVictoryBase = max(5, min(75, greatVictoryBase))
        disasterBase = max(0, min(35, disasterBase))
        val total = greatVictoryBase + victoryBase + partialBase + defeatBase + disasterBase
        val gvPct = (greatVictoryBase * 100) / total
        val vPct = (victoryBase * 100) / total
        val pPct = (partialBase * 100) / total
        val dPct = (defeatBase * 100) / total
        val disPct = max(0, 100 - (gvPct + vPct + pPct + dPct))

        val advice = when {
            unlockedDoctrines.contains("doc_pilum_volley") && tactics == Tactics.AGGRESSIVE ->
                "⚡ Залп пилумов сокрушит авангард врага при первой же атаке!"
            unlockedDoctrines.contains("doc_testudo") && tactics == Tactics.TESTUDO ->
                "🛡️ Стена щитов «Черепаха» отразит любые стрелы и сведет риск к минимуму."
            commander.trait == CommanderTrait.BRAVE && tactics == Tactics.AGGRESSIVE ->
                "⚔️ Марк Фабий пылает отвагой! Штурм принесет максимальный шанс триумфа."
            commander.trait == CommanderTrait.CAUTIOUS && tactics == Tactics.CAUTIOUS ->
                "🛡️ Гай Корнелий сомкнет щиты — риск гибели ветеранов сведен к минимуму."
            expedition.isSenateTrial ->
                "⚡ Приказ Сената чрезвычайно опасен! Требуется наивысшая дисциплина."
            else ->
                "⚖️ Манипулярный строй готов выполнить любой приказ центуриона."
        }

        return BattleOddsPreview(
            greatVictoryPct = gvPct,
            victoryPct = vPct,
            partialPct = pPct,
            defeatPct = dPct,
            disasterPct = disPct,
            adviceRu = advice
        )
    }

    // AUTO-ASSIST: Auto-select optimal squad and tactics for an expedition
    fun autoSelectExpeditionSquad(expeditionId: String) {
        val state = _uiState.value
        val expedition = state.availableExpeditions.find { it.id == expeditionId } ?: return
        val livingCommanders = state.commanders.filter { it.isAlive }
        if (livingCommanders.isEmpty()) return

        // 1. Pick best commander
        val bestCommander = livingCommanders.maxByOrNull { cmd ->
            var score = cmd.level * 10
            if (expedition.difficulty >= 4 && cmd.trait == CommanderTrait.CAUTIOUS) score += 20
            if (expedition.difficulty <= 2 && cmd.trait == CommanderTrait.BRAVE) score += 20
            if (expedition.rewardDenarii >= 120 && cmd.trait == CommanderTrait.GREEDY) score += 15
            score
        } ?: livingCommanders.first()

        // 2. Pick best ready cohort
        val bestCohort = state.cohorts.maxByOrNull { coh ->
            coh.soldiers * 2 + coh.discipline * 3 + coh.veteransCount * 5
        } ?: state.cohorts.first()

        // 3. Pick optimal counter-tactics
        val optimalTactics = when {
            expedition.difficulty >= 4 -> Tactics.TESTUDO
            expedition.scoutIntel.enemyTacticRu.contains("Засада", ignoreCase = true) -> Tactics.FLANK_AMBUSH
            expedition.scoutIntel.enemyTacticRu.contains("Конный", ignoreCase = true) -> Tactics.TESTUDO
            bestCommander.trait == CommanderTrait.BRAVE -> Tactics.AGGRESSIVE
            bestCommander.trait == CommanderTrait.CAUTIOUS -> Tactics.CAUTIOUS
            else -> Tactics.BALANCED
        }

        _uiState.update {
            it.copy(
                seasonalPlan = it.seasonalPlan.copy(
                    launchedExpeditionId = expedition.id,
                    selectedCommanderId = bestCommander.id,
                    selectedCohortId = bestCohort.id,
                    selectedTactics = optimalTactics
                )
            )
        }
        SoundManager.playWarHorn()
    }

    // AUTO-ASSIST: Auto-plan season (Building + Training + Expedition)
    fun autoPlanSeason() {
        val state = _uiState.value
        val res = state.resources
        var plannedDenarii = res.denarii
        var plannedProvisions = res.provisions

        // 1. Auto-select building upgrade
        val upgradableBuilding = state.buildings
            .filter { it.level < it.maxLevel && plannedDenarii >= it.upgradeCostDenarii && plannedProvisions >= it.upgradeCostProvisions }
            .minByOrNull { it.upgradeCostDenarii }

        if (upgradableBuilding != null) {
            plannedDenarii -= upgradableBuilding.upgradeCostDenarii
            plannedProvisions -= upgradableBuilding.upgradeCostProvisions
        }

        // 2. Auto-select training cohort
        val trainCohort = state.cohorts
            .filter { it.soldiers >= 40 }
            .minByOrNull { it.level }

        val trainCost = if (state.seasonYear.season == Season.SPRING) 24 else 30
        val hasTrainFunds = plannedDenarii >= trainCost && plannedProvisions >= 15
        val selectedTrainCohortId = if (hasTrainFunds && trainCohort != null) {
            plannedDenarii -= trainCost
            plannedProvisions -= 15
            trainCohort.id
        } else null

        // 3. Auto-select best expedition
        val affordableExpeditions = state.availableExpeditions.filter {
            plannedDenarii >= it.denariiCost && plannedProvisions >= it.provisionsCost
        }
        val bestExp = affordableExpeditions.maxByOrNull { it.rewardGlory }
        val livingCommanders = state.commanders.filter { it.isAlive }
        val bestCmd = livingCommanders.maxByOrNull { it.level }
        val bestCoh = state.cohorts.maxByOrNull { it.soldiers }

        _uiState.update {
            it.copy(
                seasonalPlan = SeasonalPlan(
                    trainCohortId = selectedTrainCohortId,
                    upgradeBuildingType = upgradableBuilding?.type,
                    launchedExpeditionId = bestExp?.id,
                    selectedCommanderId = bestCmd?.id,
                    selectedCohortId = bestCoh?.id,
                    selectedTactics = if (bestExp != null && bestExp.difficulty >= 3) Tactics.TESTUDO else Tactics.BALANCED
                )
            )
        }
        SoundManager.playWarHorn()
    }

    // AUTO-ASSIST: Auto-equip all cohorts with optimal crafted gear
    fun autoEquipAll() {
        val state = _uiState.value
        val craftedItems = state.equipment.filter { it.isCrafted }
        if (craftedItems.isEmpty() || state.cohorts.isEmpty()) return

        val cohorts = state.cohorts
        val updatedEquipment = state.equipment.map { item ->
            if (!item.isCrafted) item
            else {
                // Distribute strategically by type
                val assignedCohort = when (item.type) {
                    EquipmentType.WEAPON -> cohorts.maxByOrNull { it.attackPower }
                    EquipmentType.ARMOR, EquipmentType.HELMET, EquipmentType.SHIELD -> cohorts.minByOrNull { it.defensePower }
                    EquipmentType.STANDARD, EquipmentType.ACCESSORY -> cohorts.maxByOrNull { it.veteransCount }
                }
                item.copy(equippedCohortId = assignedCohort?.id)
            }
        }

        SoundManager.playGladiusClash()
        _uiState.update { it.copy(equipment = updatedEquipment) }
        persistGameState()
    }

    // AUTO-ASSIST: Replenish all cohorts at once
    fun replenishAllCohorts() {
        val state = _uiState.value
        var curDenarii = state.resources.denarii
        val updatedCohorts = state.cohorts.map { cohort ->
            val missing = cohort.maxSoldiers - cohort.soldiers
            if (missing > 0 && curDenarii >= missing) {
                curDenarii -= missing
                cohort.copy(soldiers = cohort.maxSoldiers, morale = 95)
            } else {
                cohort
            }
        }

        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = curDenarii),
                cohorts = updatedCohorts
            )
        }
        persistGameState()
    }

    // MAIN SEASON CONFIRMATION LOOP
    fun confirmSeasonPlan() {
        val state = _uiState.value
        val plan = state.seasonalPlan

        var curDenarii = state.resources.denarii
        var curProvisions = state.resources.provisions
        var curGlory = state.resources.glory
        var curSenate = state.resources.senateFavor

        var updatedCommanders = state.commanders.toMutableList()
        var updatedCohorts = state.cohorts.toMutableList()
        var updatedBuildings = state.buildings.toMutableList()
        var updatedChronicles = state.chronicles.toMutableList()
        var updatedAchievements = state.achievements.toMutableList()

        var battleResult: ExpeditionResult? = null

        // 1. Process Building Upgrade
        if (plan.upgradeBuildingType != null) {
            val bIndex = updatedBuildings.indexOfFirst { it.type == plan.upgradeBuildingType }
            if (bIndex != -1) {
                val b = updatedBuildings[bIndex]
                if (curDenarii >= b.upgradeCostDenarii && curProvisions >= b.upgradeCostProvisions && b.level < b.maxLevel) {
                    curDenarii -= b.upgradeCostDenarii
                    curProvisions -= b.upgradeCostProvisions
                    updatedBuildings[bIndex] = b.copy(level = b.level + 1)
                    curGlory += 3
                }
            }
        }

        // 2. Process Training
        if (plan.trainCohortId != null) {
            val cIndex = updatedCohorts.indexOfFirst { it.id == plan.trainCohortId }
            if (cIndex != -1) {
                val cohort = updatedCohorts[cIndex]
                val trainingCostDenarii = if (state.seasonYear.season == Season.SPRING) 24 else 30
                val trainingCostProvisions = 15
                if (curDenarii >= trainingCostDenarii && curProvisions >= trainingCostProvisions) {
                    curDenarii -= trainingCostDenarii
                    curProvisions -= trainingCostProvisions
                    val campusLevel = updatedBuildings.find { it.type == BuildingType.CAMPUS_MARTIUS }?.level ?: 1
                    val xpGain = campusLevel * 35
                    val newXp = cohort.xp + xpGain
                    val newLevel = if (newXp >= cohort.maxXp) cohort.level + 1 else cohort.level
                    val finalXp = if (newXp >= cohort.maxXp) newXp - cohort.maxXp else newXp
                    val newVeterans = cohort.veteransCount + (if (cohort.level >= 3) 2 else 1)
                    updatedCohorts[cIndex] = cohort.copy(
                        level = newLevel,
                        xp = finalXp,
                        veteransCount = min(cohort.maxSoldiers, newVeterans),
                        attackPower = cohort.attackPower + 2,
                        discipline = cohort.discipline + 2
                    )
                }
            }
        }

        // 3. Process Expedition / Campaign
        if (plan.launchedExpeditionId != null) {
            val expedition = state.availableExpeditions.find { it.id == plan.launchedExpeditionId }
            val commander = updatedCommanders.find { it.id == plan.selectedCommanderId } ?: updatedCommanders.first()
            val cohort = updatedCohorts.find { it.id == plan.selectedCohortId } ?: updatedCohorts.first()
            val tactics = plan.selectedTactics

            if (expedition != null && curDenarii >= expedition.denariiCost && curProvisions >= expedition.provisionsCost) {
                curDenarii -= expedition.denariiCost
                curProvisions -= expedition.provisionsCost

                val odds = calculateBattleOdds(expedition, commander, cohort, tactics)
                val roll = Random.nextInt(100)

                val outcome = when {
                    roll < odds.greatVictoryPct -> ExpeditionOutcome.GREAT_VICTORY
                    roll < (odds.greatVictoryPct + odds.victoryPct) -> ExpeditionOutcome.VICTORY
                    roll < (odds.greatVictoryPct + odds.victoryPct + odds.partialPct) -> ExpeditionOutcome.PARTIAL_SUCCESS
                    roll < (odds.greatVictoryPct + odds.victoryPct + odds.partialPct + odds.defeatPct) -> ExpeditionOutcome.DEFEAT
                    else -> ExpeditionOutcome.DISASTER
                }

                // Calculate spoils & casualties
                val fabricaLevel = updatedBuildings.find { it.type == BuildingType.FABRICA }?.level ?: 1
                val valetudinariumLevel = updatedBuildings.find { it.type == BuildingType.VALETUDINARIUM }?.level ?: 1
                val unlockedDoctrines = state.doctrines.filter { it.isUnlocked }.map { it.id }.toSet()

                // Equipped gear mitigation
                val cohortEquipped = state.equipment.filter { it.isCrafted && it.equippedCohortId == cohort.id }
                val gearCasReductionPct = cohortEquipped.sumOf { it.casualtyReductionPct }

                val baseCasualties = when (outcome) {
                    ExpeditionOutcome.GREAT_VICTORY -> Random.nextInt(0, 4)
                    ExpeditionOutcome.VICTORY -> Random.nextInt(4, 12)
                    ExpeditionOutcome.PARTIAL_SUCCESS -> Random.nextInt(10, 20)
                    ExpeditionOutcome.DEFEAT -> Random.nextInt(18, 32)
                    ExpeditionOutcome.DISASTER -> Random.nextInt(35, 55)
                }

                // Fabrica armor mitigation + Iron discipline doctrine + Gear
                var mitigatedCas = max(0, baseCasualties - (fabricaLevel * 2))
                if (unlockedDoctrines.contains("doc_disciplina_ferrea")) {
                    mitigatedCas = (mitigatedCas * 0.8f).toInt()
                }
                if (gearCasReductionPct > 0) {
                    mitigatedCas = max(0, (mitigatedCas * (1f - gearCasReductionPct / 100f)).toInt())
                }

                // Valetudinarium wounded treated + Field medics doctrine
                val healRatio = (valetudinariumLevel * 0.2f) + (if (unlockedDoctrines.contains("doc_medici_castrorum")) 0.35f else 0f)
                val woundedTreated = (mitigatedCas * healRatio).toInt()
                val netCasualties = max(0, mitigatedCas - woundedTreated)
                val veteransSaved = if (valetudinariumLevel >= 2 || unlockedDoctrines.contains("doc_medici_castrorum")) 2 else 0

                var lootMultiplier = when (outcome) {
                    ExpeditionOutcome.GREAT_VICTORY -> 1.5f + (if (commander.trait == CommanderTrait.AMBITIOUS) 0.3f else 0f)
                    ExpeditionOutcome.VICTORY -> 1.1f + (if (commander.trait == CommanderTrait.GREEDY) 0.4f else 0f)
                    ExpeditionOutcome.PARTIAL_SUCCESS -> 0.6f
                    ExpeditionOutcome.DEFEAT -> 0.15f
                    ExpeditionOutcome.DISASTER -> 0.0f
                }
                if (unlockedDoctrines.contains("doc_equites") && outcome.isSuccess) {
                    lootMultiplier += 0.2f
                }
                if (unlockedDoctrines.contains("doc_art_tormentorum") && expedition.difficulty >= 3 && outcome.isSuccess) {
                    lootMultiplier += 0.35f
                }

                val lootDenarii = (expedition.rewardDenarii * lootMultiplier).toInt()
                val lootProvisions = (expedition.rewardProvisions * lootMultiplier).toInt()
                val gloryDelta = outcome.gloryDelta + (if (expedition.isSenateTrial && outcome.isSuccess) 15 else 0)

                curDenarii += lootDenarii
                curProvisions += lootProvisions
                curGlory = max(0, curGlory + gloryDelta)
                if (outcome.isSuccess) curSenate = min(100, curSenate + 6) else curSenate = max(0, curSenate - 8)

                val xpEarned = when (outcome) {
                    ExpeditionOutcome.GREAT_VICTORY -> 60
                    ExpeditionOutcome.VICTORY -> 40
                    ExpeditionOutcome.PARTIAL_SUCCESS -> 20
                    ExpeditionOutcome.DEFEAT -> 10
                    ExpeditionOutcome.DISASTER -> 5
                }

                // Update Cohort stats
                val cIndex = updatedCohorts.indexOfFirst { it.id == cohort.id }
                if (cIndex != -1) {
                    val currentSoldiers = max(15, cohort.soldiers - netCasualties)
                    val newTotalVictories = if (outcome.isSuccess) cohort.victoriesCount + 1 else cohort.victoriesCount
                    val newGreatVictories = if (outcome == ExpeditionOutcome.GREAT_VICTORY) cohort.greatVictoriesCount + 1 else cohort.greatVictoriesCount
                    val newDefeats = if (!outcome.isSuccess) cohort.defeatsCount + 1 else cohort.defeatsCount

                    var newTradition: String? = null
                    val tradList = cohort.traditions.toMutableList()
                    if (outcome == ExpeditionOutcome.GREAT_VICTORY && !tradList.contains("Победители ${expedition.regionRu}")) {
                        newTradition = "Победители ${expedition.regionRu}"
                        tradList.add(newTradition)
                    } else if (newTotalVictories >= 5 && !tradList.contains("Железный строй")) {
                        newTradition = "Железный строй"
                        tradList.add(newTradition)
                    }

                    updatedCohorts[cIndex] = cohort.copy(
                        soldiers = currentSoldiers,
                        xp = (cohort.xp + xpEarned) % cohort.maxXp,
                        level = cohort.level + ((cohort.xp + xpEarned) / cohort.maxXp),
                        expeditionsCount = cohort.expeditionsCount + 1,
                        victoriesCount = newTotalVictories,
                        greatVictoriesCount = newGreatVictories,
                        defeatsCount = newDefeats,
                        casualtiesSuffered = cohort.casualtiesSuffered + netCasualties,
                        traditions = tradList
                    )
                }

                // Update Commander stats
                val cmdIndex = updatedCommanders.indexOfFirst { it.id == commander.id }
                var commanderPromoted = false
                val commanderKilled = outcome == ExpeditionOutcome.DISASTER && Random.nextInt(100) < 25

                if (cmdIndex != -1) {
                    val cmd = updatedCommanders[cmdIndex]
                    val newCmdXp = cmd.xp + xpEarned
                    val newCmdLevel = if (newCmdXp >= cmd.maxXp) cmd.level + 1 else cmd.level
                    if (newCmdLevel > cmd.level && (newCmdLevel == 4 || newCmdLevel == 7)) {
                        commanderPromoted = true
                    }
                    updatedCommanders[cmdIndex] = cmd.copy(
                        level = newCmdLevel,
                        xp = newCmdXp % cmd.maxXp,
                        expeditionsLed = cmd.expeditionsLed + 1,
                        victoriesCount = if (outcome.isSuccess) cmd.victoriesCount + 1 else cmd.victoriesCount,
                        greatVictoriesCount = if (outcome == ExpeditionOutcome.GREAT_VICTORY) cmd.greatVictoriesCount + 1 else cmd.greatVictoriesCount,
                        defeatsCount = if (!outcome.isSuccess) cmd.defeatsCount + 1 else cmd.defeatsCount,
                        isAlive = !commanderKilled,
                        moodStatus = if (commanderKilled) "Погиб с честью на поле боя" else if (outcome.isSuccess) "Празднует победу в претории" else "Хмуро разбирает ошибки похода"
                    )
                }

                // Sound triggers
                when (outcome) {
                    ExpeditionOutcome.GREAT_VICTORY -> {
                        SoundManager.playTriumphFanfare()
                        SoundManager.playCoins()
                    }
                    ExpeditionOutcome.VICTORY -> {
                        SoundManager.playWarHorn()
                        SoundManager.playCoins()
                    }
                    ExpeditionOutcome.PARTIAL_SUCCESS -> SoundManager.playSwordClash()
                    ExpeditionOutcome.DEFEAT, ExpeditionOutcome.DISASTER -> SoundManager.playDrumBeat()
                }

                val narrative = generateBattleNarrative(expedition, commander, cohort, tactics, outcome, netCasualties, lootDenarii)

                battleResult = ExpeditionResult(
                    expedition = expedition,
                    commander = commander,
                    cohort = cohort,
                    tactics = tactics,
                    outcome = outcome,
                    casualties = netCasualties,
                    veteransSaved = veteransSaved,
                    woundedTreated = woundedTreated,
                    lootDenarii = lootDenarii,
                    lootProvisions = lootProvisions,
                    gloryDelta = gloryDelta,
                    xpEarned = xpEarned,
                    commanderPromoted = commanderPromoted,
                    newTradition = null,
                    commanderKilled = commanderKilled,
                    storyNarrativeRu = narrative
                )

                // Add to Chronicles
                val newChr = ChronicleEntry(
                    id = "chr_${System.currentTimeMillis()}",
                    seasonFormatted = state.seasonYear.formatted,
                    yearBc = state.seasonYear.yearBc,
                    headlineRu = "${outcome.icon} ${outcome.titleRu}: ${expedition.titleRu}",
                    textRu = narrative,
                    outcome = outcome,
                    commanderName = commander.name,
                    cohortName = cohort.name,
                    casualties = netCasualties,
                    lootDenarii = lootDenarii,
                    lootProvisions = lootProvisions,
                    gloryEarned = gloryDelta
                )
                updatedChronicles.add(0, newChr)
            }
        }

        // 4. Senate Seasonal Stipend (Stipendium Senatus) & Autumn Harvest & Upkeep
        val principiaLevel = updatedBuildings.find { it.type == BuildingType.PRINCIPIA }?.level ?: 1
        val unlockedDocs = state.doctrines.filter { it.isUnlocked }.map { it.id }.toSet()

        // Formula for seasonal stipend from Rome: Base 35 + (Favor * 1.5) + (Principia rank * 10)
        var senateStipend = (curSenate * 1.5f + 35 + (principiaLevel * 10)).toInt()
        if (unlockedDocs.contains("doc_imperium")) {
            senateStipend += 45 // Dan' provintsiy tribute
        }

        curDenarii += senateStipend

        val stipendChr = ChronicleEntry(
            id = "chr_stipend_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏛️ Сезонное жалование Сената: +$senateStipend 🪙",
            textRu = "Из Казначейства Сатурна в Риме прибыл караван с жалованием. Расположение Сената (${curSenate}%) и ранг Принципии ($principiaLevel) обеспечили выплату +$senateStipend денариев легиону.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Квестор Сената (Quaestor)",
            cohortName = "Казна легиона",
            casualties = 0,
            lootDenarii = senateStipend,
            lootProvisions = 0,
            gloryEarned = 0
        )
        updatedChronicles.add(0, stipendChr)

        if (state.seasonYear.season == Season.AUTUMN) {
            val horreumLevel = updatedBuildings.find { it.type == BuildingType.HORREUM }?.level ?: 1
            var harvestBonus = 50 + (horreumLevel * 30)
            if (state.activeBlessing?.god == GodType.CERES) {
                harvestBonus = (harvestBonus * 1.4f).toInt()
            }
            curProvisions += harvestBonus
        }

        // Army Upkeep (reduced if logistics doctrine unlocked)
        val totalSoldiers = updatedCohorts.sumOf { it.soldiers }
        val upkeepRate = if (unlockedDocs.contains("doc_logistics")) 0.09f else 0.12f
        val foodUpkeep = (totalSoldiers * upkeepRate).toInt()
        curProvisions = max(0, curProvisions - foodUpkeep)

        // 4b. Provincial Investments & Enterprises Yield
        val activeInvestments = state.investments.filter { it.isOwned }
        val totalInvestDenarii = activeInvestments.sumOf { it.currentYieldDenarii }
        val totalInvestProvisions = activeInvestments.sumOf { it.currentYieldProvisions }
        val totalInvestGlory = activeInvestments.sumOf { it.currentYieldGlory }

        if (totalInvestDenarii > 0 || totalInvestProvisions > 0) {
            curDenarii += totalInvestDenarii
            curProvisions += totalInvestProvisions
            curGlory += totalInvestGlory

            val investChr = ChronicleEntry(
                id = "chr_invest_${System.currentTimeMillis()}",
                seasonFormatted = state.seasonYear.formatted,
                yearBc = state.seasonYear.yearBc,
                headlineRu = "🏛️ Доход провинциальных имений: +$totalInvestDenarii 🪙, +$totalInvestProvisions 🌾",
                textRu = "Управляющие латифундиями, винодельнями и торговыми факториями доставили сезонную прибыль: +$totalInvestDenarii денариев и +$totalInvestProvisions мер провианта" + if (totalInvestGlory > 0) ", а также +$totalInvestGlory к Славе Рима." else ".",
                outcome = ExpeditionOutcome.VICTORY,
                commanderName = "Прокуратор провинций",
                cohortName = "Казна легиона",
                casualties = 0,
                lootDenarii = totalInvestDenarii,
                lootProvisions = totalInvestProvisions,
                gloryEarned = totalInvestGlory
            )
            updatedChronicles.add(0, investChr)
        }

        // 4c. Bank Deposit Interest & Loan Installment
        var updatedBanking = state.bankingState
        if (updatedBanking.depositDenarii > 0) {
            val interestEarned = (updatedBanking.depositDenarii * 0.05f).toInt().coerceAtLeast(1)
            updatedBanking = updatedBanking.copy(
                depositDenarii = updatedBanking.depositDenarii + interestEarned,
                totalInterestEarned = updatedBanking.totalInterestEarned + interestEarned
            )
            val bankChr = ChronicleEntry(
                id = "chr_interest_${System.currentTimeMillis()}",
                seasonFormatted = state.seasonYear.formatted,
                yearBc = state.seasonYear.yearBc,
                headlineRu = "🏦 Проценты по депозиту в банке: +$interestEarned 🪙",
                textRu = "Менялы и аргентарии Римского Форума начислили +$interestEarned денариев (+5%) на вклад легиона. Баланс депозита: ${updatedBanking.depositDenarii} 🪙.",
                outcome = ExpeditionOutcome.VICTORY,
                commanderName = "Аргентарий Рима",
                cohortName = "Mensa Nummaria",
                casualties = 0,
                lootDenarii = interestEarned,
                lootProvisions = 0,
                gloryEarned = 0
            )
            updatedChronicles.add(0, bankChr)
        }

        if (updatedBanking.hasActiveLoan) {
            val payment = updatedBanking.seasonalLoanPayment
            val actualPayment = min(curDenarii, payment)
            curDenarii -= actualPayment
            val remLoan = max(0, updatedBanking.activeLoanDenarii - actualPayment)
            val remSeasons = max(0, updatedBanking.loanSeasonsRemaining - 1)
            updatedBanking = updatedBanking.copy(
                activeLoanDenarii = remLoan,
                loanSeasonsRemaining = if (remLoan == 0) 0 else remSeasons
            )
            val loanChr = ChronicleEntry(
                id = "chr_loan_pay_${System.currentTimeMillis()}",
                seasonFormatted = state.seasonYear.formatted,
                yearBc = state.seasonYear.yearBc,
                headlineRu = "📜 Погашение военного займа Сената: -$actualPayment 🪙",
                textRu = "Удержан сезонный взнос по военному займу Сената SPQR. Остаток долга: $remLoan денариев.",
                outcome = ExpeditionOutcome.PARTIAL_SUCCESS,
                commanderName = "Квестор Сената",
                cohortName = "Казначейство Рима",
                casualties = 0,
                lootDenarii = -actualPayment,
                lootProvisions = 0,
                gloryEarned = 0
            )
            updatedChronicles.add(0, loanChr)
        }

        // 4d. Dynamic Market Shift for Forum Boarium
        val marketPossibilities = listOf(
            MarketState(grainPriceBuy = 26, grainPriceSell = 20, marketConditionTitleRu = "🌾 Обильный урожай в Кампании", marketConditionDescRu = "Цены на пшеницу упали. Отличное время для оптовых закупок зерна в Хорреум.", marketTrendIcon = "🟢", priceModifier = 0.85f),
            MarketState(grainPriceBuy = 30, grainPriceSell = 22, marketConditionTitleRu = "⚖️ Стабильный рынок провианта", marketConditionDescRu = "Торговые пути Лация и Кампании открыты, поставки пшеницы регулярны.", marketTrendIcon = "⚖️", priceModifier = 1.0f),
            MarketState(grainPriceBuy = 38, grainPriceSell = 30, marketConditionTitleRu = "🔥 Засуха и неурожай в Самнии", marketConditionDescRu = "Резкий взлет цен на хлеб на Форуме. Исключительно выгодно продавать излишки из Хорреума!", marketTrendIcon = "📈", priceModifier = 1.3f),
            MarketState(grainPriceBuy = 34, grainPriceSell = 26, marketConditionTitleRu = "🚢 Прибытие торговых судов в Остию", marketConditionDescRu = "Купеческие корбиты доставили полные трюмы сицилийского зерна и оливкового масла.", marketTrendIcon = "🚢", priceModifier = 1.1f)
        )
        val newMarketState = marketPossibilities.random()

        // 5. Advance Season
        val nextSeasonIndex = (state.seasonYear.seasonIndex + 1)
        val nextSeasonNumber = state.seasonYear.seasonNumber + 1
        val nextYearBc = if (nextSeasonIndex % 4 == 0) state.seasonYear.yearBc - 1 else state.seasonYear.yearBc
        val newSeasonYear = SeasonYear(seasonIndex = nextSeasonIndex % 4, seasonNumber = nextSeasonNumber, yearBc = nextYearBc)

        // 6. Simulate Competing Legions
        val updatedLegions = simulateCompetingLegions(state.competingLegions, curGlory)

        // 7. Check Achievements & Senate Quests Progression
        val totalV = state.totalVictories + (if (battleResult?.outcome?.isSuccess == true) 1 else 0)
        val totalGV = state.totalGreatVictories + (if (battleResult?.outcome == ExpeditionOutcome.GREAT_VICTORY) 1 else 0)
        val totalD = state.totalDefeats + (if (battleResult?.outcome?.isSuccess == false) 1 else 0)
        val isFirst = updatedLegions.maxByOrNull { it.ratingScore }?.id == "legio_4_player"

        updatedAchievements = updatedAchievements.map { ach ->
            when (ach.id) {
                "ach_great_victory" -> if (totalGV >= 1) ach.copy(isUnlocked = true) else ach
                "ach_ten_victories" -> if (totalV >= 10) ach.copy(isUnlocked = true) else ach
                "ach_veteran_wall" -> if (updatedCohorts.any { it.veteransCount >= 20 }) ach.copy(isUnlocked = true) else ach
                "ach_best_legion" -> if (isFirst) ach.copy(isUnlocked = true) else ach
                "ach_pyrrhic_conqueror" -> if (battleResult?.expedition?.isSenateTrial == true && battleResult.outcome.isSuccess) ach.copy(isUnlocked = true) else ach
                "ach_grand_camp" -> if (updatedBuildings.all { it.level >= 3 }) ach.copy(isUnlocked = true) else ach
                else -> ach
            }
        }.toMutableList()

        val totalVeteransInLegion = updatedCohorts.sumOf { it.veteransCount }
        val totalSoldiersInLegion = updatedCohorts.sumOf { it.soldiers }
        val isSamniumWon = battleResult?.outcome?.isSuccess == true && (battleResult.expedition.id == "exp_samnium_pacify" || battleResult.expedition.regionRu.contains("Самний", ignoreCase = true))

        val newWinStreak = if (battleResult?.outcome?.isSuccess == true) state.currentWinStreak + 1 else 0
        val longestStreak = max(state.longestWinStreak, newWinStreak)

        var updatedQuests = evaluateSenateQuests(
            quests = state.senateQuests,
            totalV = totalV,
            totalGV = totalGV,
            totalSoldiers = totalSoldiersInLegion,
            totalVeterans = totalVeteransInLegion,
            buildings = updatedBuildings,
            equipment = state.equipment,
            doctrines = state.doctrines,
            provisions = curProvisions,
            denarii = curDenarii,
            glory = curGlory,
            senateFavor = curSenate,
            isSamniumWon = isSamniumWon,
            hasActiveBlessing = state.activeBlessing != null,
            winStreak = newWinStreak
        )

        // Replenish quests if too few active
        val existingQuestIds = updatedQuests.map { it.id }.toSet()
        val poolQuests = GameDefaults.createDynamicQuestPool().filter { it.id !in existingQuestIds }
        if (updatedQuests.count { !it.isClaimed } < 5 && poolQuests.isNotEmpty()) {
            updatedQuests = updatedQuests + poolQuests.take(2)
        }

        // Trophies dynamic unlocks
        val updatedTrophies = state.trophies.map { trophy ->
            when (trophy.id) {
                "trophy_samnite_crest" -> if (isSamniumWon) trophy.copy(isUnlocked = true) else trophy
                "trophy_etruscan_stele" -> if (updatedBuildings.any { it.level >= 3 }) trophy.copy(isUnlocked = true) else trophy
                "trophy_pyrrhic_phalanx_banner" -> if (totalGV >= 5) trophy.copy(isUnlocked = true) else trophy
                "trophy_golden_aquila" -> if (curGlory >= 160) trophy.copy(isUnlocked = true) else trophy
                else -> trophy
            }
        }

        // Blessings countdown
        val nextBlessing = state.activeBlessing?.let {
            if (it.seasonsRemaining > 1) it.copy(seasonsRemaining = it.seasonsRemaining - 1) else null
        }

        // 8. Roll Random Event
        val rolledEvent = if (Random.nextInt(100) < 65) {
            GameDefaults.getRandomEvents().random()
        } else null

        val isGoldenAgeNow = curGlory >= 150 && curSenate >= 75
        val showGoldenAge = isGoldenAgeNow && !state.resources.isGoldenAge

        _uiState.update {
            it.copy(
                seasonYear = newSeasonYear,
                resources = LegionResources(
                    denarii = curDenarii,
                    provisions = curProvisions,
                    glory = curGlory,
                    senateFavor = curSenate
                ),
                commanders = updatedCommanders,
                cohorts = updatedCohorts,
                buildings = updatedBuildings,
                competingLegions = updatedLegions,
                chronicles = updatedChronicles,
                achievements = updatedAchievements,
                senateQuests = updatedQuests,
                trophies = updatedTrophies,
                activeBlessing = nextBlessing,
                bankingState = updatedBanking,
                marketState = newMarketState,
                seasonalPlan = SeasonalPlan(), // reset for new season
                activeEvent = rolledEvent,
                lastExpeditionResult = battleResult,
                showSeasonPlanDialog = false,
                showBattleResultDialog = battleResult != null,
                showEventDialog = (battleResult == null && rolledEvent != null),
                showGoldenAgeDialog = showGoldenAge,
                totalVictories = totalV,
                totalGreatVictories = totalGV,
                totalDefeats = totalD
            )
        }

        persistGameState()
    }

    private fun checkPendingEvents() {
        if (_uiState.value.activeEvent != null) {
            _uiState.update { it.copy(showEventDialog = true) }
        }
    }

    fun resolveEventChoice(choice: CampEventChoice) {
        val state = _uiState.value
        val res = state.resources
        val newDenarii = max(0, res.denarii + choice.denariiDelta)
        val newProvisions = max(0, res.provisions + choice.provisionsDelta)
        val newGlory = max(0, res.glory + choice.gloryDelta)
        val newSenate = max(0, min(100, res.senateFavor + choice.senateFavorDelta))

        // Update cohorts if XP or Morale delta
        val updatedCohorts = state.cohorts.map { coh ->
            coh.copy(
                xp = min(coh.maxXp, coh.xp + choice.cohortXpDelta),
                morale = max(20, min(100, coh.morale + choice.moraleDelta))
            )
        }

        // Add Chronicle note for the decision
        val eventChr = ChronicleEntry(
            id = "chr_evt_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏛️ Решение легиона: ${state.activeEvent?.titleRu}",
            textRu = "${choice.textRu}. ${choice.resultLogRu}",
            outcome = null,
            commanderName = state.commanders.firstOrNull()?.name ?: "Совет легиона",
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = choice.denariiDelta,
            lootProvisions = choice.provisionsDelta,
            gloryEarned = choice.gloryDelta
        )

        SoundManager.playCoins()

        _uiState.update {
            it.copy(
                resources = LegionResources(
                    denarii = newDenarii,
                    provisions = newProvisions,
                    glory = newGlory,
                    senateFavor = newSenate
                ),
                cohorts = updatedCohorts,
                chronicles = listOf(eventChr) + it.chronicles,
                activeEvent = null,
                showEventDialog = false
            )
        }

        persistGameState()
    }

    // Replenish Cohort Soldiers
    fun replenishCohort(cohortId: String) {
        val state = _uiState.value
        val cohort = state.cohorts.find { it.id == cohortId } ?: return
        val missing = cohort.maxSoldiers - cohort.soldiers
        if (missing <= 0) return

        val cost = missing * 1 // 1 denarius per recruit
        if (state.resources.denarii >= cost) {
            val updated = state.cohorts.map {
                if (it.id == cohortId) it.copy(soldiers = it.maxSoldiers, morale = 90) else it
            }
            SoundManager.playCoins()
            _uiState.update {
                it.copy(
                    resources = it.resources.copy(denarii = it.resources.denarii - cost),
                    cohorts = updated
                )
            }
            persistGameState()
        }
    }

    // Recruit New Promising Commander
    fun recruitNewCommander() {
        val state = _uiState.value
        val cost = 90
        if (state.resources.denarii < cost) return

        val romanNames = listOf("Тит Манлий", "Аппий Клавдий", "Гней Помпей", "Публий Деций", "Луций Эмилий", "Квинт Фабий")
        val randomName = romanNames.random()
        val randomTrait = CommanderTrait.entries.random()

        val newCommander = Commander(
            id = "cmd_${System.currentTimeMillis()}",
            name = randomName,
            level = 1,
            xp = 0,
            maxXp = 100,
            trait = randomTrait,
            avatarSkinTone = Random.nextInt(4),
            hairStyle = Random.nextInt(4),
            helmetType = Random.nextInt(4),
            beardStyle = Random.nextInt(3),
            cloakColorIndex = Random.nextInt(4),
            moodStatus = "Прибыл из Рима на службу"
        )

        SoundManager.playWarHorn()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = it.resources.denarii - cost),
                commanders = it.commanders + newCommander
            )
        }
        persistGameState()
    }

    private fun simulateCompetingLegions(legions: List<CompetingLegion>, playerGlory: Int): List<CompetingLegion> {
        val activities = listOf(
            "Штурмует крепость этрусков",
            "Охраняет долину реки По",
            "Пополняет новобранцев в Риме",
            "Патрулирует Аппиеву дорогу",
            "Празднует триумф на Капитолии",
            "Отражает набег галльских конников",
            "Разбивает лагерь у мыса Мизенум"
        )

        return legions.map { leg ->
            if (leg.id == "legio_4_player") {
                leg.copy(
                    ratingScore = min(99, playerGlory / 2 + 30),
                    victories = _uiState.value.totalVictories,
                    defeats = _uiState.value.totalDefeats
                )
            } else {
                val scoreChange = Random.nextInt(-2, 4)
                val newScore = max(30, min(98, leg.ratingScore + scoreChange))
                val isWin = Random.nextBoolean()
                leg.copy(
                    ratingScore = newScore,
                    victories = if (isWin) leg.victories + 1 else leg.victories,
                    defeats = if (!isWin) leg.defeats + 1 else leg.defeats,
                    currentActivityRu = activities.random()
                )
            }
        }.sortedByDescending { it.ratingScore }
    }

    fun unlockDoctrine(doctrineId: String) {
        val state = _uiState.value
        val doctrine = state.doctrines.find { it.id == doctrineId } ?: return
        if (doctrine.isUnlocked) return
        if (state.resources.glory < doctrine.costGlory) return

        val updatedDoctrines = state.doctrines.map {
            if (it.id == doctrineId) it.copy(isUnlocked = true) else it
        }

        val updatedQuests = evaluateSenateQuests(
            quests = state.senateQuests,
            totalV = state.totalVictories,
            totalGV = state.totalGreatVictories,
            totalSoldiers = state.cohorts.sumOf { it.soldiers },
            totalVeterans = state.cohorts.sumOf { it.veteransCount },
            buildings = state.buildings,
            equipment = state.equipment,
            doctrines = updatedDoctrines,
            provisions = state.resources.provisions,
            denarii = state.resources.denarii,
            glory = state.resources.glory - doctrine.costGlory,
            senateFavor = state.resources.senateFavor
        )

        SoundManager.playVictoryFanfare()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(glory = it.resources.glory - doctrine.costGlory),
                doctrines = updatedDoctrines,
                senateQuests = updatedQuests
            )
        }
        persistGameState()
    }

    fun craftEquipment(itemId: String) {
        val state = _uiState.value
        val item = state.equipment.find { it.id == itemId } ?: return
        if (item.isCrafted) return
        if (state.resources.denarii < item.costDenarii) return

        val updatedEquipment = state.equipment.map {
            if (it.id == itemId) it.copy(isCrafted = true) else it
        }

        val updatedQuests = evaluateSenateQuests(
            quests = state.senateQuests,
            totalV = state.totalVictories,
            totalGV = state.totalGreatVictories,
            totalSoldiers = state.cohorts.sumOf { it.soldiers },
            totalVeterans = state.cohorts.sumOf { it.veteransCount },
            buildings = state.buildings,
            equipment = updatedEquipment,
            doctrines = state.doctrines,
            provisions = state.resources.provisions,
            denarii = state.resources.denarii - item.costDenarii,
            glory = state.resources.glory,
            senateFavor = state.resources.senateFavor
        )

        SoundManager.playGladiusClash()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = it.resources.denarii - item.costDenarii),
                equipment = updatedEquipment,
                senateQuests = updatedQuests
            )
        }
        persistGameState()
    }

    fun temperEquipmentItem(itemId: String) {
        val state = _uiState.value
        val item = state.equipment.find { it.id == itemId } ?: return
        if (!item.isCrafted || item.temperLevel >= 3) return
        if (state.resources.denarii < item.temperCostDenarii) return

        val cost = item.temperCostDenarii
        val updatedEquipment = state.equipment.map {
            if (it.id == itemId) it.copy(temperLevel = it.temperLevel + 1) else it
        }

        SoundManager.playGladiusClash()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = it.resources.denarii - cost),
                equipment = updatedEquipment
            )
        }
        persistGameState()
    }

    fun salvageEquipmentItem(itemId: String) {
        val state = _uiState.value
        val item = state.equipment.find { it.id == itemId } ?: return
        if (!item.isCrafted) return

        val refund = item.salvageDenarii
        val updatedEquipment = state.equipment.map {
            if (it.id == itemId) it.copy(isCrafted = false, temperLevel = 0, equippedCohortId = null) else it
        }

        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = it.resources.denarii + refund),
                equipment = updatedEquipment
            )
        }
        persistGameState()
    }

    fun equipItem(itemId: String, cohortId: String?) {
        val state = _uiState.value
        val updatedEquipment = state.equipment.map {
            if (it.id == itemId) it.copy(equippedCohortId = cohortId) else it
        }
        SoundManager.playMarchDrums()
        _uiState.update {
            it.copy(equipment = updatedEquipment)
        }
        persistGameState()
    }

    fun claimSenateQuest(questId: String) {
        val state = _uiState.value
        val quest = state.senateQuests.find { it.id == questId } ?: return
        if (!quest.isFinished || quest.isClaimed) return

        var updatedQuests = state.senateQuests.map {
            if (it.id == questId) it.copy(isClaimed = true) else it
        }

        // Replenish new quest from dynamic pool if available
        val existingIds = updatedQuests.map { it.id }.toSet()
        val nextQuest = GameDefaults.createDynamicQuestPool().firstOrNull { it.id !in existingIds }
        if (nextQuest != null && updatedQuests.count { !it.isClaimed } < 6) {
            updatedQuests = updatedQuests + nextQuest
        }

        val perkText = if (quest.bonusPerkDescRu != null) " Бонус: ${quest.bonusPerkDescRu}." else ""
        val chr = ChronicleEntry(
            id = "chr_quest_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏛️ Награда Сената: «${quest.titleRu}»",
            textRu = "Поручение от ${quest.issuerRu} с честью выполнено. В казну легиона поступило +${quest.rewardDenarii} денариев, авторитет в Курии вырос на +${quest.rewardSenateFavor}%, слава +${quest.rewardGlory}.$perkText",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = quest.issuerRu,
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = quest.rewardDenarii,
            lootProvisions = 0,
            gloryEarned = quest.rewardGlory
        )

        SoundManager.playTriumphFanfare()
        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii + quest.rewardDenarii,
                    senateFavor = min(100, it.resources.senateFavor + quest.rewardSenateFavor),
                    glory = it.resources.glory + quest.rewardGlory
                ),
                senateQuests = updatedQuests,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun claimAllSenateQuests() {
        val state = _uiState.value
        val completedQuests = state.senateQuests.filter { it.isFinished && !it.isClaimed }
        if (completedQuests.isEmpty()) return

        val completedIds = completedQuests.map { it.id }.toSet()
        var updatedQuests = state.senateQuests.map {
            if (it.id in completedIds) it.copy(isClaimed = true) else it
        }

        val totalDenarii = completedQuests.sumOf { it.rewardDenarii }
        val totalFavor = completedQuests.sumOf { it.rewardSenateFavor }
        val totalGlory = completedQuests.sumOf { it.rewardGlory }

        // Replenish new quests from dynamic pool
        val existingIds = updatedQuests.map { it.id }.toSet()
        val pool = GameDefaults.createDynamicQuestPool().filter { it.id !in existingIds }
        val slotsNeeded = (6 - updatedQuests.count { !it.isClaimed }).coerceAtLeast(0)
        val newQuestsToAdd = pool.take(slotsNeeded)
        updatedQuests = updatedQuests + newQuestsToAdd

        val chr = ChronicleEntry(
            id = "chr_quests_all_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏛️ Сенат: Получены награды за ${completedQuests.size} поручений",
            textRu = "Казна легиона разом пополнилась на +$totalDenarii денариев, авторитет в Курии вырос на +$totalFavor%, слава Республики +$totalGlory.",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Сенат и Народ Рима",
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = totalDenarii,
            lootProvisions = 0,
            gloryEarned = totalGlory
        )

        SoundManager.playTriumphFanfare()
        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii + totalDenarii,
                    senateFavor = min(100, it.resources.senateFavor + totalFavor),
                    glory = it.resources.glory + totalGlory
                ),
                senateQuests = updatedQuests,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun resolveSenatePetition(petitionId: String) {
        val state = _uiState.value
        val petition = state.senatePetitions.find { it.id == petitionId } ?: return
        if (state.resources.senateFavor < petition.minFavorRequired) return
        if (state.resources.senateFavor < petition.favorCost) return
        if (state.resources.denarii < petition.denariiCost) return

        var newDenarii = state.resources.denarii - petition.denariiCost
        var newProvisions = state.resources.provisions
        var newGlory = state.resources.glory
        var newSenateFavor = max(0, min(100, state.resources.senateFavor - petition.favorCost))
        var updatedCohorts = state.cohorts

        when (petition.id) {
            "pet_subventio" -> {
                newDenarii += 160
            }
            "pet_veteran_levy" -> {
                updatedCohorts = updatedCohorts.map { cohort ->
                    val addedSoldiers = min(cohort.maxSoldiers - cohort.soldiers, 20)
                    val newTotal = cohort.soldiers + addedSoldiers
                    val newVets = min(newTotal, cohort.veteransCount + 10)
                    cohort.copy(soldiers = newTotal, veteransCount = newVets, morale = 100)
                }
            }
            "pet_lex_agraria" -> {
                updatedCohorts = updatedCohorts.map { cohort ->
                    cohort.copy(
                        morale = 100,
                        discipline = min(100, cohort.discipline + 15),
                        attackPower = cohort.attackPower + 3,
                        defensePower = cohort.defensePower + 3
                    )
                }
                newGlory += 15
            }
            "pet_oratio_curia" -> {
                newSenateFavor = min(100, newSenateFavor + 15)
                newGlory += 5
            }
            "pet_banquet_munera" -> {
                newSenateFavor = min(100, newSenateFavor + 30)
                newGlory += 10
            }
        }

        val chr = ChronicleEntry(
            id = "chr_petition_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "📜 Senatus Consultum: ${petition.titleRu}",
            textRu = "Курия утвердила прошение легиона «${petition.latinNameRu}». ${petition.rewardSummaryRu}.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Сенат Республики",
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = if (petition.id == "pet_subventio") 160 else -petition.denariiCost,
            lootProvisions = 0,
            gloryEarned = if (petition.id == "pet_banquet_munera") 10 else 5
        )

        SoundManager.playTriumphFanfare()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = newDenarii,
                    provisions = newProvisions,
                    glory = newGlory,
                    senateFavor = newSenateFavor
                ),
                cohorts = updatedCohorts,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    private fun evaluateSenateQuests(
        quests: List<SenateQuest>,
        totalV: Int,
        totalGV: Int,
        totalSoldiers: Int,
        totalVeterans: Int,
        buildings: List<Building>,
        equipment: List<EquipmentItem>,
        doctrines: List<MilitaryDoctrine>,
        provisions: Int,
        denarii: Int,
        glory: Int,
        senateFavor: Int,
        isSamniumWon: Boolean = false,
        hasActiveBlessing: Boolean = false,
        unitsTrained: Int = 0,
        caravanDispatched: Int = 0,
        winStreak: Int = 0
    ): List<SenateQuest> {
        return quests.map { quest ->
            if (quest.isClaimed) return@map quest
            val currentProgress = when (quest.targetType) {
                "VICTORIES" -> totalV
                "GREAT_VICTORIES" -> totalGV
                "LEGION_SIZE" -> totalSoldiers
                "VETERANS_COUNT" -> totalVeterans
                "BUILDING_LEVEL" -> {
                    if (quest.id == "sq_granary_reserves") {
                        buildings.find { it.type == BuildingType.HORREUM }?.level ?: 1
                    } else {
                        buildings.maxOfOrNull { it.level } ?: 1
                    }
                }
                "FORGED_EQUIPMENT" -> equipment.count { it.isCrafted }
                "DOCTRINES_LEARNED" -> doctrines.count { it.isUnlocked }
                "PROVISIONS_RESERVE" -> provisions
                "DENARII_TREASURY" -> denarii
                "GLORY" -> glory
                "SENATE_FAVOR" -> senateFavor
                "EXPEDITION_WIN", "EXPEDITION_SAMNIUM" -> if (isSamniumWon) quest.targetCount else quest.currentProgress
                "ACTIVE_BLESSING" -> if (hasActiveBlessing) quest.targetCount else quest.currentProgress
                "UNIT_TRAINING" -> if (unitsTrained > 0) min(quest.targetCount, quest.currentProgress + unitsTrained) else quest.currentProgress
                "CARAVAN_DISPATCHED" -> if (caravanDispatched > 0) min(quest.targetCount, quest.currentProgress + caravanDispatched) else quest.currentProgress
                "WIN_STREAK" -> winStreak
                "ALL_BUILDINGS_UPGRADED" -> buildings.minOfOrNull { it.level } ?: 1
                else -> quest.currentProgress
            }
            val clamped = min(quest.targetCount, currentProgress)
            quest.copy(
                currentProgress = clamped,
                isCompleted = clamped >= quest.targetCount
            )
        }
    }

    fun performDivineRitual(ritualId: String) {
        val state = _uiState.value
        val ritual = state.rituals.find { it.id == ritualId } ?: return
        if (state.resources.denarii < ritual.costDenarii || state.resources.provisions < ritual.costProvisions) return

        val newBlessing = ActiveBlessing(
            god = ritual.god,
            ritualNameRu = ritual.nameRu,
            effectRu = ritual.blessingEffectRu,
            seasonsRemaining = 2
        )

        val chr = ChronicleEntry(
            id = "chr_bless_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🕊️ Священный обряд: ${ritual.nameRu}",
            textRu = "Авгуры испросили благословение ${ritual.god.titleRu}. Знамения благоприятны: ${ritual.blessingEffectRu}",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = state.commanders.firstOrNull()?.name ?: "Понтифик",
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = -ritual.costDenarii,
            lootProvisions = -ritual.costProvisions,
            gloryEarned = 5
        )

        val updatedQuests = evaluateSenateQuests(
            quests = state.senateQuests,
            totalV = state.totalVictories,
            totalGV = state.totalGreatVictories,
            totalSoldiers = state.cohorts.sumOf { it.soldiers },
            totalVeterans = state.cohorts.sumOf { it.veteransCount },
            buildings = state.buildings,
            equipment = state.equipment,
            doctrines = state.doctrines,
            provisions = state.resources.provisions - ritual.costProvisions,
            denarii = state.resources.denarii - ritual.costDenarii,
            glory = state.resources.glory + 5,
            senateFavor = state.resources.senateFavor,
            hasActiveBlessing = true
        )

        SoundManager.playTriumphFanfare()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii - ritual.costDenarii,
                    provisions = it.resources.provisions - ritual.costProvisions,
                    glory = it.resources.glory + 5
                ),
                activeBlessing = newBlessing,
                senateQuests = updatedQuests,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun holdCommanderSpeech() {
        val state = _uiState.value
        val cmd = state.commanders.firstOrNull { it.isAlive } ?: return

        val updatedCohorts = state.cohorts.map { coh ->
            coh.copy(
                morale = 100,
                discipline = coh.discipline + 2
            )
        }

        val chr = ChronicleEntry(
            id = "chr_speech_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "📢 Adlocutio: Речь полководца ${cmd.name}",
            textRu = "«Солдаты! Рим смотрит на вас! Мы не отступим ни на шаг!» Мораль всех когорт поднята до предела.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = cmd.name,
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = 0,
            lootProvisions = 0,
            gloryEarned = 3
        )

        SoundManager.playWarHorn()
        _uiState.update {
            it.copy(
                cohorts = updatedCohorts,
                resources = it.resources.copy(glory = it.resources.glory + 3),
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun payDonativum() {
        val state = _uiState.value
        val cost = 50
        if (state.resources.denarii < cost) return

        val updatedCohorts = state.cohorts.map { coh ->
            val addedVets = min(3, coh.soldiers - coh.veteransCount)
            coh.copy(
                veteransCount = coh.veteransCount + max(0, addedVets),
                morale = min(100, coh.morale + 10)
            )
        }

        val chr = ChronicleEntry(
            id = "chr_donativum_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🪙 Выплата Донатива легионерам",
            textRu = "Из личной казны полководца роздано $cost денариев. 3 новобранца в каждой манипуле стали ветеранами, преданность Сената возросла.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Полководец Сципион",
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = -cost,
            lootProvisions = 0,
            gloryEarned = 5
        )

        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii - cost,
                    senateFavor = min(100, it.resources.senateFavor + 10),
                    glory = it.resources.glory + 5
                ),
                cohorts = updatedCohorts,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun performLustratio() {
        val state = _uiState.value
        val cost = 30
        if (state.resources.provisions < cost) return

        val updatedCohorts = state.cohorts.map { coh ->
            coh.copy(
                discipline = coh.discipline + 3,
                morale = 100
            )
        }

        val chr = ChronicleEntry(
            id = "chr_lustratio_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🦅 Lustratio: Освящение знамён и орлов",
            textRu = "Жрецы окропили штандарты легиона. Боевой дух и дисциплина воинов непоколебимы.",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Главный понтифик",
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = 0,
            lootProvisions = -cost,
            gloryEarned = 20
        )

        SoundManager.playTriumphFanfare()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    provisions = it.resources.provisions - cost,
                    glory = it.resources.glory + 20
                ),
                cohorts = updatedCohorts,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    // --- UNIT TRAINING & RESOURCE ALLOCATION ---
    private val activeTrainingJobs = mutableMapOf<UnitType, Job>()

    fun updateUnitAllocationCount(unitType: UnitType, newCount: Int) {
        val countClamped = newCount.coerceIn(1, 100)
        _uiState.update { state ->
            val updatedAllocations = state.unitAllocations.map { alloc ->
                if (alloc.unitType == unitType && !alloc.isTrainingActive) {
                    alloc.copy(allocatedCount = countClamped)
                } else alloc
            }
            state.copy(unitAllocations = updatedAllocations)
        }
    }

    fun updateUnitDrillIntensity(unitType: UnitType, intensity: DrillIntensity) {
        _uiState.update { state ->
            val updatedAllocations = state.unitAllocations.map { alloc ->
                if (alloc.unitType == unitType && !alloc.isTrainingActive) {
                    alloc.copy(drillIntensity = intensity)
                } else alloc
            }
            state.copy(unitAllocations = updatedAllocations)
        }
        SoundManager.playDrumBeat()
    }

    fun updateUnitTargetCohort(unitType: UnitType, targetCohortId: String) {
        _uiState.update { state ->
            val updatedAllocations = state.unitAllocations.map { alloc ->
                if (alloc.unitType == unitType && !alloc.isTrainingActive) {
                    alloc.copy(targetCohortId = targetCohortId)
                } else alloc
            }
            state.copy(unitAllocations = updatedAllocations)
        }
    }

    fun autoAllocateBalancedTraining() {
        val state = _uiState.value
        val totalDenarii = state.resources.denarii
        val totalProvisions = state.resources.provisions
        if (totalDenarii < 20 || totalProvisions < 15) return

        // Distribute proportionally across unit types (Hastati 30%, Principes 25%, Velites 20%, Triarii 15%, Equites 10%)
        val targetAllocations = state.unitAllocations.map { alloc ->
            val count = when (alloc.unitType) {
                UnitType.HASTATI -> ((totalDenarii * 0.28) / (alloc.unitType.baseCostDenarii)).toInt().coerceIn(4, 30)
                UnitType.PRINCIPES -> ((totalDenarii * 0.24) / (alloc.unitType.baseCostDenarii)).toInt().coerceIn(3, 20)
                UnitType.VELITES -> ((totalDenarii * 0.18) / (alloc.unitType.baseCostDenarii)).toInt().coerceIn(5, 35)
                UnitType.TRIARII -> ((totalDenarii * 0.14) / (alloc.unitType.baseCostDenarii)).toInt().coerceIn(2, 12)
                UnitType.EQUITES -> ((totalDenarii * 0.10) / (alloc.unitType.baseCostDenarii)).toInt().coerceIn(2, 10)
                UnitType.FUNDITORES -> ((totalDenarii * 0.06) / (alloc.unitType.baseCostDenarii)).toInt().coerceIn(4, 20)
            }
            alloc.copy(allocatedCount = count)
        }

        SoundManager.playCoins()
        _uiState.update { it.copy(unitAllocations = targetAllocations) }
    }

    fun startUnitTraining(unitType: UnitType) {
        val state = _uiState.value
        val allocation = state.unitAllocations.find { it.unitType == unitType } ?: return
        if (allocation.isTrainingActive) return

        val costDenarii = allocation.totalCostDenarii
        val costProvisions = allocation.totalCostProvisions

        if (state.resources.denarii < costDenarii || state.resources.provisions < costProvisions) {
            return
        }

        // Deduct resources and activate training
        val durationSeconds = allocation.drillIntensity.timeSeconds
        val updatedAllocations = state.unitAllocations.map {
            if (it.unitType == unitType) {
                it.copy(
                    isTrainingActive = true,
                    currentProgress = 0f,
                    secondsRemaining = durationSeconds,
                    totalSeconds = durationSeconds
                )
            } else it
        }

        SoundManager.playWarHorn()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii - costDenarii,
                    provisions = it.resources.provisions - costProvisions
                ),
                unitAllocations = updatedAllocations
            )
        }

        // Launch smooth dynamic progress ticker
        activeTrainingJobs[unitType]?.cancel()
        activeTrainingJobs[unitType] = viewModelScope.launch(Dispatchers.Main) {
            val totalSteps = durationSeconds * 10 // 100ms ticks
            for (step in 1..totalSteps) {
                delay(100L)
                val progress = step.toFloat() / totalSteps.toFloat()
                val remSeconds = max(0, durationSeconds - (step / 10))
                _uiState.update { s ->
                    val allocs = s.unitAllocations.map { a ->
                        if (a.unitType == unitType && a.isTrainingActive) {
                            a.copy(
                                currentProgress = progress,
                                secondsRemaining = remSeconds
                            )
                        } else a
                    }
                    s.copy(unitAllocations = allocs)
                }
            }

            // Training finished! Apply results to cohort
            completeTrainingUnit(unitType)
        }
    }

    fun instantCompleteUnitTraining(unitType: UnitType) {
        activeTrainingJobs[unitType]?.cancel()
        completeTrainingUnit(unitType)
    }

    fun cancelUnitTraining(unitType: UnitType) {
        val state = _uiState.value
        val allocation = state.unitAllocations.find { it.unitType == unitType } ?: return
        if (!allocation.isTrainingActive) return

        activeTrainingJobs[unitType]?.cancel()
        val refundDenarii = (allocation.totalCostDenarii * 0.75f).toInt()
        val refundProvisions = (allocation.totalCostProvisions * 0.75f).toInt()

        val updatedAllocations = state.unitAllocations.map {
            if (it.unitType == unitType) {
                it.copy(
                    isTrainingActive = false,
                    currentProgress = 0f,
                    secondsRemaining = 0,
                    totalSeconds = 0
                )
            } else it
        }

        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii + refundDenarii,
                    provisions = it.resources.provisions + refundProvisions
                ),
                unitAllocations = updatedAllocations
            )
        }
    }

    private fun completeTrainingUnit(unitType: UnitType) {
        val state = _uiState.value
        val allocation = state.unitAllocations.find { it.unitType == unitType } ?: return

        val targetCohort = state.cohorts.find { it.id == allocation.targetCohortId }
            ?: state.cohorts.firstOrNull()
            ?: return

        val xpGain = allocation.drillIntensity.xpGain
        val atkGain = allocation.projectedAttackGain
        val defGain = allocation.projectedDefenseGain
        val discGain = allocation.projectedDisciplineGain

        // Calculate soldier additions and veteran conversions
        val missingSoldiers = targetCohort.maxSoldiers - targetCohort.soldiers
        val soldiersAdded = min(missingSoldiers, allocation.allocatedCount)
        val newSoldiersTotal = min(targetCohort.maxSoldiers, targetCohort.soldiers + soldiersAdded)

        // Veterans conversion
        val veteranChance = allocation.drillIntensity.veteranChancePct
        val veteransConverted = if (Random.nextInt(100) < veteranChance) {
            max(1, (allocation.allocatedCount * 0.25f).toInt())
        } else 0

        val newVeteransCount = min(newSoldiersTotal, targetCohort.veteransCount + veteransConverted)

        // XP & Level calculations
        var newXp = targetCohort.xp + xpGain
        var newLevel = targetCohort.level
        var newMaxXp = targetCohort.maxXp
        while (newXp >= newMaxXp) {
            newXp -= newMaxXp
            newLevel += 1
            newMaxXp = (newMaxXp * 1.35f).toInt()
        }

        val updatedCohort = targetCohort.copy(
            level = newLevel,
            xp = newXp,
            maxXp = newMaxXp,
            soldiers = newSoldiersTotal,
            veteransCount = newVeteransCount,
            attackPower = targetCohort.attackPower + atkGain,
            defensePower = targetCohort.defensePower + defGain,
            discipline = min(100, targetCohort.discipline + discGain),
            morale = min(100, targetCohort.morale + 10)
        )

        val updatedCohorts = state.cohorts.map {
            if (it.id == targetCohort.id) updatedCohort else it
        }

        val updatedAllocations = state.unitAllocations.map {
            if (it.unitType == unitType) {
                it.copy(
                    isTrainingActive = false,
                    currentProgress = 0f,
                    secondsRemaining = 0,
                    totalSeconds = 0,
                    totalTrainedSoFar = it.totalTrainedSoFar + it.allocatedCount
                )
            } else it
        }

        val chr = ChronicleEntry(
            id = "chr_train_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏋️ Завершены учения: ${unitType.nameRu} (${unitType.latinName})",
            textRu = "На плацу Марсова поля завершена муштра «${allocation.drillIntensity.titleRu}». Обучено ${allocation.allocatedCount} воинов для когорты ${targetCohort.name}. Показатели атаки: +$atkGain, защиты: +$defGain, опыта: +$xpGain XP.",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Главный инструктор (Campidoctor)",
            cohortName = targetCohort.name,
            casualties = 0,
            lootDenarii = -allocation.totalCostDenarii,
            lootProvisions = -allocation.totalCostProvisions,
            gloryEarned = 2
        )

        val updatedQuests = evaluateSenateQuests(
            quests = state.senateQuests,
            totalV = state.totalVictories,
            totalGV = state.totalGreatVictories,
            totalSoldiers = updatedCohorts.sumOf { it.soldiers },
            totalVeterans = updatedCohorts.sumOf { it.veteransCount },
            buildings = state.buildings,
            equipment = state.equipment,
            doctrines = state.doctrines,
            provisions = state.resources.provisions,
            denarii = state.resources.denarii,
            glory = state.resources.glory + 2,
            senateFavor = state.resources.senateFavor,
            unitsTrained = 1
        )

        SoundManager.playSwordClash()
        SoundManager.playTriumphFanfare()

        _uiState.update {
            it.copy(
                cohorts = updatedCohorts,
                unitAllocations = updatedAllocations,
                senateQuests = updatedQuests,
                chronicles = listOf(chr) + it.chronicles,
                resources = it.resources.copy(glory = it.resources.glory + 2)
            )
        }

        persistGameState()
    }

    fun tradeProvisions(provisionsDelta: Int, denariiDelta: Int) {
        val state = _uiState.value
        if (state.resources.provisions + provisionsDelta < 0) return
        if (state.resources.denarii + denariiDelta < 0) return

        val newProvisions = state.resources.provisions + provisionsDelta
        val newDenarii = state.resources.denarii + denariiDelta

        val actionDesc = if (provisionsDelta < 0) {
            "Продажа ${-provisionsDelta} мер зерна купцам за +$denariiDelta денариев"
        } else {
            "Закупка $provisionsDelta мер провианта на форуме за ${-denariiDelta} денариев"
        }

        val chr = ChronicleEntry(
            id = "chr_trade_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "⚖️ Торговая сделка на Форуме (Forum Boarium)",
            textRu = "$actionDesc. Казна легиона пересчитана.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Префект лагеря",
            cohortName = "Обоз легиона",
            casualties = 0,
            lootDenarii = denariiDelta,
            lootProvisions = provisionsDelta,
            gloryEarned = 0
        )

        val updatedQuests = evaluateSenateQuests(
            quests = state.senateQuests,
            totalV = state.totalVictories,
            totalGV = state.totalGreatVictories,
            totalSoldiers = state.cohorts.sumOf { it.soldiers },
            totalVeterans = state.cohorts.sumOf { it.veteransCount },
            buildings = state.buildings,
            equipment = state.equipment,
            doctrines = state.doctrines,
            provisions = newProvisions,
            denarii = newDenarii,
            glory = state.resources.glory,
            senateFavor = state.resources.senateFavor
        )

        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    provisions = newProvisions,
                    denarii = newDenarii
                ),
                senateQuests = updatedQuests,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun dispatchTradeCaravan() {
        val state = _uiState.value
        val cost = 40
        if (state.resources.denarii < cost) return

        val gainDenarii = Random.nextInt(65, 90)
        val gainProvisions = Random.nextInt(15, 30)

        val chr = ChronicleEntry(
            id = "chr_caravan_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🚢 Торговый караван в Остию вернулся с прибылью",
            textRu = "Обоз легиона успешно распродал трофейные изделия на рынках Тибра. Доход: +$gainDenarii денариев (чистая прибыль +${gainDenarii - cost}), доставлено +$gainProvisions мер отборного зерна.",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Торговый эдил (Aedilis)",
            cohortName = "Обоз легиона",
            casualties = 0,
            lootDenarii = gainDenarii - cost,
            lootProvisions = gainProvisions,
            gloryEarned = 1
        )

        val newDen = state.resources.denarii - cost + gainDenarii
        val newProv = state.resources.provisions + gainProvisions
        val updatedQuests = evaluateSenateQuests(
            quests = state.senateQuests,
            totalV = state.totalVictories,
            totalGV = state.totalGreatVictories,
            totalSoldiers = state.cohorts.sumOf { it.soldiers },
            totalVeterans = state.cohorts.sumOf { it.veteransCount },
            buildings = state.buildings,
            equipment = state.equipment,
            doctrines = state.doctrines,
            provisions = newProv,
            denarii = newDen,
            glory = state.resources.glory + 1,
            senateFavor = state.resources.senateFavor,
            caravanDispatched = 1
        )

        SoundManager.playCoins()
        SoundManager.playTriumphFanfare()

        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = newDen,
                    provisions = newProv,
                    glory = it.resources.glory + 1
                ),
                senateQuests = updatedQuests,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun collectProvincialVectigal() {
        val state = _uiState.value
        if (state.resources.senateFavor < 15) return

        val collected = 55
        val favorLoss = 6

        val chr = ChronicleEntry(
            id = "chr_tribute_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "📜 Сбор чрезвычайного налога (Vectigal Bellicum)",
            textRu = "Квестор легиона собрал военный сбор с окрестных общин. Казна пополнена на +$collected денариев. Сенаторы недовольны самоуправством (-$favorLoss% расположения).",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Военный трибун",
            cohortName = "Квестура легиона",
            casualties = 0,
            lootDenarii = collected,
            lootProvisions = 0,
            gloryEarned = 0
        )

        SoundManager.playCoins()

        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii + collected,
                    senateFavor = max(0, it.resources.senateFavor - favorLoss)
                ),
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun upgradeInvestment(investmentId: String) {
        val state = _uiState.value
        val inv = state.investments.find { it.id == investmentId } ?: return
        if (inv.isMaxLevel) return
        val cost = inv.nextUpgradeCost
        if (state.resources.denarii < cost) return

        val newLevel = inv.level + 1
        val updatedInvestments = state.investments.map {
            if (it.id == investmentId) it.copy(level = newLevel) else it
        }

        val newDenarii = state.resources.denarii - cost
        val chr = ChronicleEntry(
            id = "chr_inv_upg_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏛️ Инвестиции расширены: ${inv.titleRu} (Ур. $newLevel)",
            textRu = "Квестура легиона вложила $cost денариев в развитие «${inv.latinNameRu}» в регионе ${inv.regionRu}. Сезонная отдача увеличена до +${inv.nextYieldDenarii} 🪙 и +${inv.nextYieldProvisions} 🌾.",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Квестор легиона",
            cohortName = "Инвестиционный совет",
            casualties = 0,
            lootDenarii = -cost,
            lootProvisions = 0,
            gloryEarned = 1
        )

        SoundManager.playCoins()
        SoundManager.playTriumphFanfare()

        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = newDenarii, glory = it.resources.glory + 1),
                investments = updatedInvestments,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun depositDenariiToBank(amount: Int) {
        val state = _uiState.value
        val validAmount = min(state.resources.denarii, max(10, amount))
        if (state.resources.denarii < validAmount) return

        val newDenarii = state.resources.denarii - validAmount
        val newDeposit = state.bankingState.depositDenarii + validAmount
        val updatedBanking = state.bankingState.copy(depositDenarii = newDeposit)

        val chr = ChronicleEntry(
            id = "chr_dep_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏦 Вклад в Mensa Nummaria: +$validAmount 🪙",
            textRu = "В римский банк под надзор аргентариев помещено $validAmount денариев. Общий депозитный вклад: $newDeposit 🪙. Приносит +5% сезонного дохода.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Аргентарий Форума",
            cohortName = "Казна легиона",
            casualties = 0,
            lootDenarii = -validAmount,
            lootProvisions = 0,
            gloryEarned = 0
        )

        SoundManager.playCoins()

        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = newDenarii),
                bankingState = updatedBanking,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun withdrawDenariiFromBank(amount: Int) {
        val state = _uiState.value
        val validAmount = min(state.bankingState.depositDenarii, max(1, amount))
        if (state.bankingState.depositDenarii < validAmount) return

        val newDenarii = state.resources.denarii + validAmount
        val newDeposit = state.bankingState.depositDenarii - validAmount
        val updatedBanking = state.bankingState.copy(depositDenarii = newDeposit)

        val chr = ChronicleEntry(
            id = "chr_with_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏦 Изъятие вклада из банка: +$validAmount 🪙",
            textRu = "Из банковской ячейки Mensa Nummaria востребовано $validAmount денариев на текущие нужды легиона. Остаток вклада: $newDeposit 🪙.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Квестор легиона",
            cohortName = "Казна легиона",
            casualties = 0,
            lootDenarii = validAmount,
            lootProvisions = 0,
            gloryEarned = 0
        )

        SoundManager.playCoins()

        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = newDenarii),
                bankingState = updatedBanking,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun takeSenateWarLoan(amount: Int) {
        val state = _uiState.value
        if (state.bankingState.hasActiveLoan) return
        if (state.resources.senateFavor < 25) return

        val loanAmount = if (amount >= 200) 250 else 100
        val seasons = 4
        val updatedBanking = state.bankingState.copy(
            activeLoanDenarii = loanAmount,
            loanSeasonsRemaining = seasons
        )

        val chr = ChronicleEntry(
            id = "chr_loan_take_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "📜 Одобрен чрезвычайный военный заем Сената: +$loanAmount 🪙",
            textRu = "Постановлением Сената легиону выделена государственная ссуда $loanAmount денариев на 4 сезона для экстренного перевооружения. Погашение: по ${loanAmount / seasons} 🪙 в сезон.",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Консул Республики",
            cohortName = "Сенат и Народ Рима",
            casualties = 0,
            lootDenarii = loanAmount,
            lootProvisions = 0,
            gloryEarned = 0
        )

        SoundManager.playCoins()
        SoundManager.playTriumphFanfare()

        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii + loanAmount,
                    senateFavor = max(0, it.resources.senateFavor - 4)
                ),
                bankingState = updatedBanking,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun repaySenateWarLoanFull() {
        val state = _uiState.value
        val loan = state.bankingState.activeLoanDenarii
        if (loan <= 0) return
        if (state.resources.denarii < loan) return

        val newDenarii = state.resources.denarii - loan
        val updatedBanking = state.bankingState.copy(
            activeLoanDenarii = 0,
            loanSeasonsRemaining = 0
        )

        val chr = ChronicleEntry(
            id = "chr_loan_repay_full_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏛️ Досрочное погашение займа Сената: -$loan 🪙",
            textRu = "Квестор легиона полностью рассчитался с государственной казной Сатурна. Сенаторы высоко оценили финансовую честность и надежность командования (+10% расположения).",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Квестор легиона",
            cohortName = "Казначейство Рима",
            casualties = 0,
            lootDenarii = -loan,
            lootProvisions = 0,
            gloryEarned = 2
        )

        SoundManager.playCoins()
        SoundManager.playTriumphFanfare()

        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = newDenarii,
                    senateFavor = min(100, it.resources.senateFavor + 10),
                    glory = it.resources.glory + 2
                ),
                bankingState = updatedBanking,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun mintLegionCoins() {
        val state = _uiState.value
        val provCost = 45
        if (state.resources.provisions < provCost) return
        if (state.campLevel < 4) return

        val coinsGained = 90
        val gloryGained = 2
        val updatedBanking = state.bankingState.copy(
            totalCoinsMinted = state.bankingState.totalCoinsMinted + 1
        )

        val chr = ChronicleEntry(
            id = "chr_mint_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🪙 Чеканка серебряной монеты (Emissio Nummi): +$coinsGained 🪙",
            textRu = "Лагерные кузнецы и серебряных дел мастера отчеканили партию полновесных легионных серебряников с профилем командиров. Доход: +$coinsGained денариев, слава: +$gloryGained.",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Монетный трибун",
            cohortName = "Officina Monetae",
            casualties = 0,
            lootDenarii = coinsGained,
            lootProvisions = -provCost,
            gloryEarned = gloryGained
        )

        SoundManager.playCoins()
        SoundManager.playSwordClash()

        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii + coinsGained,
                    provisions = it.resources.provisions - provCost,
                    glory = it.resources.glory + gloryGained
                ),
                bankingState = updatedBanking,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun tradeProvisionsDynamic(provisionsDelta: Int, denariiDelta: Int, isBuying: Boolean) {
        val state = _uiState.value
        if (isBuying) {
            val cost = -denariiDelta
            val grain = provisionsDelta
            if (state.resources.denarii < cost) return
            val newDenarii = state.resources.denarii - cost
            val newProvisions = state.resources.provisions + grain

            val chr = ChronicleEntry(
                id = "chr_trade_buy_${System.currentTimeMillis()}",
                seasonFormatted = state.seasonYear.formatted,
                yearBc = state.seasonYear.yearBc,
                headlineRu = "🌾 Закупка пшеницы на Форуме: +$grain 🌾 (-$cost 🪙)",
                textRu = "Квестор легиона приобрел $grain мер высокосортного зерна у кампанских купцов на Бычьем Форуме Рима.",
                outcome = ExpeditionOutcome.VICTORY,
                commanderName = "Интендант легиона",
                cohortName = "Хорреум",
                casualties = 0,
                lootDenarii = -cost,
                lootProvisions = grain,
                gloryEarned = 0
            )

            SoundManager.playCoins()

            _uiState.update {
                it.copy(
                    resources = it.resources.copy(
                        denarii = newDenarii,
                        provisions = newProvisions
                    ),
                    chronicles = listOf(chr) + it.chronicles
                )
            }
        } else {
            val grainSold = -provisionsDelta
            val denariiGained = denariiDelta
            if (state.resources.provisions < grainSold) return
            val newProvisions = state.resources.provisions - grainSold
            val newDenarii = state.resources.denarii + denariiGained

            val chr = ChronicleEntry(
                id = "chr_trade_sell_${System.currentTimeMillis()}",
                seasonFormatted = state.seasonYear.formatted,
                yearBc = state.seasonYear.yearBc,
                headlineRu = "🪙 Продажа излишков зерна: +$denariiGained 🪙 (-$grainSold 🌾)",
                textRu = "Амбары легиона реализовали $grainSold мер пшеницы городским пекарям Рима по выгодному курсу. Получено +$denariiGained денариев.",
                outcome = ExpeditionOutcome.VICTORY,
                commanderName = "Торговый эдил",
                cohortName = "Forum Boarium",
                casualties = 0,
                lootDenarii = denariiGained,
                lootProvisions = -grainSold,
                gloryEarned = 0
            )

            SoundManager.playCoins()

            _uiState.update {
                it.copy(
                    resources = it.resources.copy(
                        denarii = newDenarii,
                        provisions = newProvisions
                    ),
                    chronicles = listOf(chr) + it.chronicles
                )
            }
        }
        persistGameState()
    }

    fun sellWarSpoils() {
        val state = _uiState.value
        val provCost = 20
        if (state.resources.provisions < provCost) return

        val denariiGain = 48
        val chr = ChronicleEntry(
            id = "chr_spoils_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏺 Сбыт военных трофеев и амфор: +$denariiGain 🪙",
            textRu = "Обозные служители распродали захваченное этрусское и самнитское вооружение оружейникам Рима. Выручка: +$denariiGain денариев.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Торговый префект",
            cohortName = "Лагерный рынок",
            casualties = 0,
            lootDenarii = denariiGain,
            lootProvisions = -provCost,
            gloryEarned = 0
        )

        SoundManager.playCoins()

        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii + denariiGain,
                    provisions = it.resources.provisions - provCost
                ),
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    private fun generateBattleNarrative(
        expedition: Expedition,
        commander: Commander,
        cohort: Cohort,
        tactics: Tactics,
        outcome: ExpeditionOutcome,
        casualties: Int,
        loot: Int
    ): String {
        return when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY ->
                "${commander.name} применил тактику «${tactics.titleRu}». Когорта ${cohort.name} сомкнула ряды и опрокинула врага мощным ударом пилумов! Вражеский лагерь захвачен. Триумф Рима! Потери: $casualties, добыча: +$loot золота."
            ExpeditionOutcome.VICTORY ->
                "В тяжелом полевом бою в регионе ${expedition.regionRu} ${commander.name} сломил сопротивление врага. ${cohort.name} держала строй до конца. Победа за нами! Потери: $casualties, трофеи: +$loot монет."
            ExpeditionOutcome.PARTIAL_SUCCESS ->
                "Столкновение завершилось упорным отходом противника на укрепленные высоты. Часть обоза захвачена, но основные силы врага избежали окружения. Потери: $casualties."
            ExpeditionOutcome.DEFEAT ->
                "Разведка оказалась неполной: превосходящие силы противника зажали манипулы ${cohort.name} в теснине. ${commander.name} сумел сохранить знамя и организованно отойти в лагерь. Потери: $casualties легионеров."
            ExpeditionOutcome.DISASTER ->
                "Катастрофа в горах! Внезапный конный фланговый налет врага разорвал когорту. Оставшиеся ветераны пробились сквозь кольцо окружения. Черный день для легиона! Потери: $casualties легионеров."
        }
    }

    // ==========================================
    // CURSUS HONORUM (ELECTIONS & PROMOTIONS)
    // ==========================================

    fun fundPlebeianGames() {
        val state = _uiState.value
        val cost = 40
        if (state.resources.denarii < cost) return

        val newPlebs = min(100, state.electionCampaign.plebeianSupportPct + 15)
        val newGames = state.electionCampaign.gamesOrganizedCount + 1

        SoundManager.playTriumphFanfare()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii - cost,
                    glory = it.resources.glory + 2
                ),
                electionCampaign = it.electionCampaign.copy(
                    plebeianSupportPct = newPlebs,
                    gamesOrganizedCount = newGames
                ),
                cohorts = it.cohorts.map { coh -> coh.copy(morale = min(100, coh.morale + 10)) }
            )
        }
        persistGameState()
    }

    fun bribePatricianSenators() {
        val state = _uiState.value
        val cost = 60
        if (state.resources.denarii < cost) return

        val newPatricians = min(100, state.electionCampaign.patricianSupportPct + 20)
        val newBudget = state.electionCampaign.briberyBudgetSpent + cost

        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii - cost,
                    senateFavor = min(100, it.resources.senateFavor + 8)
                ),
                electionCampaign = it.electionCampaign.copy(
                    patricianSupportPct = newPatricians,
                    briberyBudgetSpent = newBudget
                )
            )
        }
        persistGameState()
    }

    fun deliverForumSpeech() {
        val state = _uiState.value
        val newPlebs = min(100, state.electionCampaign.plebeianSupportPct + 8)
        val newPatricians = min(100, state.electionCampaign.patricianSupportPct + 5)
        val speeches = state.electionCampaign.speechesDelivered + 1

        SoundManager.playWarHorn()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(glory = it.resources.glory + 1),
                electionCampaign = it.electionCampaign.copy(
                    plebeianSupportPct = newPlebs,
                    patricianSupportPct = newPatricians,
                    speechesDelivered = speeches
                )
            )
        }
        persistGameState()
    }

    fun holdCenturiateElection() {
        val state = _uiState.value
        val campaign = state.electionCampaign
        val target = campaign.targetRank

        if (!campaign.isReadyForVote) return
        if (state.resources.glory < target.minGlory || state.resources.senateFavor < target.minSenateFavor) return

        val currentRankIndex = target.ordinal
        val nextRank = if (currentRankIndex + 1 < MagistracyRank.entries.size) {
            MagistracyRank.entries[currentRankIndex + 1]
        } else target

        val chr = ChronicleEntry(
            id = "chr_election_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏛️ Триумфальные Выборы в Комициях: ${target.titleRu} (${target.latinNameRu})!",
            textRu = "Граждане и Сенат Рима отдали свои голоса! Полководец Legio IV облачается в ${target.togaTitleRu}. Привилегия: ${target.bonusSummaryRu}",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Новый Магистрат Рима",
            cohortName = "Comitia Centuriata",
            casualties = 0,
            lootDenarii = 0,
            lootProvisions = 0,
            gloryEarned = 15
        )

        SoundManager.playTriumphFanfare()
        _uiState.update {
            it.copy(
                magistracyRank = target,
                resources = it.resources.copy(glory = it.resources.glory + 15),
                electionCampaign = RomanElectionCampaign(
                    targetRank = nextRank,
                    plebeianSupportPct = 50,
                    patricianSupportPct = 50
                ),
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    // ==========================================
    // OFFICER TALENTS & MILITARY CORONAS
    // ==========================================

    fun learnOfficerTalent(commanderId: String, talent: OfficerTalent) {
        val state = _uiState.value
        val cmd = state.commanders.find { it.id == commanderId } ?: return
        if (cmd.unlockedTalents.contains(talent)) return
        if (cmd.level < talent.levelReq) return

        val costGlory = 8
        if (state.resources.glory < costGlory) return

        val updatedCmd = cmd.copy(
            unlockedTalents = cmd.unlockedTalents + talent
        )

        val chr = ChronicleEntry(
            id = "chr_talent_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🎖️ Освоен военный талант: ${cmd.name} ➔ ${talent.titleRu}",
            textRu = "Офицер ${cmd.name} постиг искусство «${talent.titleRu}» (${talent.branchRu}). Эффект: ${talent.perkRu}",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = cmd.name,
            cohortName = "Офицерский корпус",
            casualties = 0,
            lootDenarii = 0,
            lootProvisions = 0,
            gloryEarned = 1
        )

        SoundManager.playSwordClash()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(glory = it.resources.glory - costGlory),
                commanders = it.commanders.map { c -> if (c.id == commanderId) updatedCmd else c },
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun awardMilitaryCorona(commanderId: String, corona: MilitaryCorona) {
        val state = _uiState.value
        val cmd = state.commanders.find { it.id == commanderId } ?: return
        if (cmd.awardedCoronas.contains(corona)) return

        val updatedCmd = cmd.copy(
            awardedCoronas = cmd.awardedCoronas + corona
        )

        val chr = ChronicleEntry(
            id = "chr_corona_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "👑 Торжественное награждение: ${corona.titleRu} (${corona.latinNameRu})!",
            textRu = "Перед строем легиона центуриону ${cmd.name} вручен ${corona.titleRu}. Аура: ${corona.auraPerkRu}",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = cmd.name,
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = 0,
            lootProvisions = 0,
            gloryEarned = 6
        )

        SoundManager.playTriumphFanfare()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(glory = it.resources.glory + 6),
                commanders = it.commanders.map { c -> if (c.id == commanderId) updatedCmd else c },
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    // ==========================================
    // LEGION AQUILA & VEXILLUM CUSTOMIZATION
    // ==========================================

    fun upgradeAquilaShrineRelic() {
        val state = _uiState.value
        val aquila = state.aquilaState
        val cost = aquila.upgradeCostDenarii
        if (cost <= 0 || state.resources.denarii < cost) return

        val newLevel = aquila.eagleUpgradeLevel + 1
        val updatedAquila = aquila.copy(eagleUpgradeLevel = newLevel)

        val chr = ChronicleEntry(
            id = "chr_aquila_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🦅 Освящение Орла Легиона (Aquila Martia Tier $newLevel)",
            textRu = "Жрецы Марса освятили золотой штандарт легиона. Боевой дух всех манипул несокрушим!",
            outcome = ExpeditionOutcome.GREAT_VICTORY,
            commanderName = "Аквилифер (Aquilifer)",
            cohortName = "Aedes Signorum",
            casualties = 0,
            lootDenarii = -cost,
            lootProvisions = 0,
            gloryEarned = 10
        )

        SoundManager.playTriumphFanfare()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii - cost,
                    glory = it.resources.glory + 10
                ),
                aquilaState = updatedAquila,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    fun setVexillumColor(colorIndex: Int) {
        _uiState.update {
            it.copy(aquilaState = it.aquilaState.copy(selectedBannerColorIndex = colorIndex))
        }
    }

    // ==========================================
    // STRATEGIC MAP & VIA ROADS
    // ==========================================

    fun selectStrategicProvince(province: StrategicProvince) {
        _uiState.update { it.copy(selectedProvince = province) }
    }

    fun paveStrategicRoad(roadId: String) {
        val state = _uiState.value
        val road = state.strategicRoads.find { it.id == roadId } ?: return
        if (road.isPaved) return
        if (state.resources.denarii < road.costDenarii) return

        val updatedRoads = state.strategicRoads.map {
            if (it.id == roadId) it.copy(isPaved = true) else it
        }

        val chr = ChronicleEntry(
            id = "chr_road_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🛣️ Проложен каменный тракт: ${road.nameRu}",
            textRu = "Военные инженеры легиона замостили вулканическим базальтом тракт ${road.connectingProvincesRu}. Эффект: ${road.speedAndSupplyBonusRu}",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Префект лагеря (Praefectus Castrorum)",
            cohortName = "Инженерный корпус",
            casualties = 0,
            lootDenarii = -road.costDenarii,
            lootProvisions = 0,
            gloryEarned = 4
        )

        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii - road.costDenarii,
                    glory = it.resources.glory + 4
                ),
                strategicRoads = updatedRoads,
                chronicles = listOf(chr) + it.chronicles
            )
        }
        persistGameState()
    }

    /**
     * Resets current campaign and initializes a fresh game session.
     */
    fun startNewGame() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            val freshState = GameUiState()
            _uiState.value = freshState
            repository.saveSnapshot(freshState.toSnapshot())
        }
    }

    /**
     * Manually forces an atomic persistence of current state.
     */
    fun saveGameManually() {
        persistGameState()
    }
}


